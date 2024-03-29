package cz.salmelu.discord.implementation.net.rest;

import cz.salmelu.discord.DiscordRequestException;
import cz.salmelu.discord.RequestResponse;
import org.apache.http.HttpResponse;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.concurrent.FutureCallback;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.impl.nio.client.CloseableHttpAsyncClient;
import org.apache.http.impl.nio.client.HttpAsyncClientBuilder;
import org.apache.http.impl.nio.conn.PoolingNHttpClientConnectionManager;
import org.apache.http.impl.nio.reactor.DefaultConnectingIOReactor;
import org.apache.http.nio.reactor.IOReactorException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * Takes care of establishing HTTP connections, sending REST requests and receiving responses to them.
 */
class RestRequestSender {

    private final CloseableHttpClient restClient;
    private final CloseableHttpAsyncClient asyncRestClient;
    private final Thread monitor;
    private final Thread asyncMonitor;
    private final Logger logger = LoggerFactory.getLogger("RestRequestSender");

    RestRequestSender() {
        final RequestConfig config = RequestConfig.custom()
                .setConnectTimeout(10000)
                .setConnectionRequestTimeout(60000)
                .setSocketTimeout(60000)
                .build();

        final PoolingHttpClientConnectionManager syncConnectionManager = new PoolingHttpClientConnectionManager();
        syncConnectionManager.setMaxTotal(200);
        syncConnectionManager.setDefaultMaxPerRoute(20);

        // Runs in loop and closes dead connections
        monitor = new Thread() {
            @Override
            public void run() {
                try {
                    while (!Thread.currentThread().isInterrupted()) {
                        synchronized (this) {
                            wait(10000);
                            syncConnectionManager.closeExpiredConnections();
                            syncConnectionManager.closeIdleConnections(60, TimeUnit.SECONDS);
                        }
                    }
                }
                catch (InterruptedException e) {
                    // We are done;
                }
            }
        };

        // Initialize synchronous client
        restClient = HttpClientBuilder.create()
                .setDefaultRequestConfig(config)
                .setConnectionManager(syncConnectionManager)
                .build();
        monitor.start();

        // Now for the asynchronous
        DefaultConnectingIOReactor ioReactor;
        PoolingNHttpClientConnectionManager asyncConnectionManager;
        try {
            ioReactor = new DefaultConnectingIOReactor();
            asyncConnectionManager = new PoolingNHttpClientConnectionManager(ioReactor);
            asyncConnectionManager.setMaxTotal(200);
            asyncConnectionManager.setDefaultMaxPerRoute(20);
        }
        catch (IOReactorException e) {
            throw new RuntimeException(e);
        }

        asyncMonitor = new Thread() {
            @Override
            public void run() {
                try {
                    while (!Thread.currentThread().isInterrupted()) {
                        synchronized (this) {
                            wait(10000);
                            // Close expired connections
                            asyncConnectionManager.closeExpiredConnections();
                            // Optionally, close connections
                            // that have been idle longer than 30 sec
                            asyncConnectionManager.closeIdleConnections(60, TimeUnit.SECONDS);
                        }
                    }
                } catch (InterruptedException ex) {
                    // terminate
                }
            }
        };

		asyncRestClient = HttpAsyncClientBuilder.create().setDefaultRequestConfig(config)
                .setConnectionManager(asyncConnectionManager).build();
    }

    private void startAsyncClient() {
        asyncRestClient.start();
        asyncMonitor.start();
    }

    void shutdown() throws IOException {
        restClient.close();
        monitor.interrupt();
        asyncRestClient.close();
        asyncMonitor.interrupt();
    }

    RestResponse sendRequest(RestRequest request) {
        HttpResponse response;
        final HttpRequestBase finalRequest = request.getFinalRequest();

        try {
            response = restClient.execute(finalRequest);
            final RestResponse restResponse = new RestResponse(response);
            finalRequest.releaseConnection();
            return restResponse;
        }
        catch (IOException e) {
            throw new DiscordRequestException("Unable to connect to Discord servers: " + e.getMessage(), 502);
        }
        finally {
            finalRequest.releaseConnection();
        }
    }

    private FutureCallback<HttpResponse> createCallback(Endpoint endpoint, DiscordRequester requester) {
        return new FutureCallback<HttpResponse>() {
            @Override
            public void completed(HttpResponse httpResponse) {
                final RestResponse restResponse = new RestResponse(httpResponse);
                logger.debug("Received asynchronous response: " + restResponse.getStatusCode()
                        + " (" + restResponse.getStatusText() + ")");
                requester.updateLimit(endpoint, restResponse);
            }

            @Override
            public void failed(Exception e) {}

            @Override
            public void cancelled() {}
        };
    }

    Future<RequestResponse> sendAsyncRequest(RestRequest request, Endpoint endpoint,
                                                    DiscordRequester requester) {
        final HttpRequestBase finalRequest = request.getFinalRequestAsync();
        if(!asyncRestClient.isRunning()) startAsyncClient();
        final Future<HttpResponse> future =
                asyncRestClient.execute(finalRequest, createCallback(endpoint, requester));

		return new Future<RequestResponse>() {

		    private RequestResponse converted = null;

			public boolean cancel(boolean mayInterruptIfRunning) {
				return future.cancel(mayInterruptIfRunning);
			}

			public boolean isCancelled() {
				return future.isCancelled();
			}

			public boolean isDone() {
				return future.isDone();
			}

			public RequestResponse get() throws InterruptedException, ExecutionException {
			    if(converted != null) return converted;
			    try {
                    final HttpResponse httpResponse = future.get();
                    final RestResponse restResponse = new RestResponse(httpResponse);
                    converted = restResponse.toRequestResponse();
                }
                catch(ExecutionException e) {
			        if(e.getCause() instanceof DiscordRequestException) {
                        converted = new RequestResponseImpl((DiscordRequestException) e.getCause());
                    }
                    else {
			            throw e;
                    }
                }
				return converted;
			}

			public RequestResponse get(long timeout, TimeUnit unit) throws InterruptedException, ExecutionException, TimeoutException {
                if(converted != null) return converted;
				try {
                    final HttpResponse httpResponse = future.get(timeout, unit);
                    final RestResponse restResponse = new RestResponse(httpResponse);
                    converted = restResponse.toRequestResponse();
                }
                catch(ExecutionException e) {
                    if(e.getCause() instanceof DiscordRequestException) {
                        converted = new RequestResponseImpl((DiscordRequestException) e.getCause());
                    }
                    else {
                        throw e;
                    }
                }
                return converted;
			}
		};
    }
}