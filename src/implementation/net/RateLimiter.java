package cz.salmelu.discord.implementation.net;

import cz.salmelu.discord.implementation.net.rest.Endpoint;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.HashMap;
import java.util.Map;

/**
 * <p>A special helper class to track sent requests and stall them.</p>
 * <p>Discord implements rate limits to prevent abusing bot applications and prevent spam.
 * The framework tracks user's application's requests and limits the rate before the request is even sent.</p>
 *
 * @see <a href="https://discordapp.com/developers/docs/topics/rate-limits">https://discordapp.com/developers/docs/topics/rate-limits</a></p>
 */
public class RateLimiter {

    /**
     * A holding class for value pair.
     */
    private class ResetRemainPair {
        /** timestamp when the limit is reset */
        long reset;
        /** how many more requests can we send before we hit the limit */
        int remaining;

        public ResetRemainPair(long reset, int remaining) {
            this.reset = reset;
            this.remaining = remaining;
        }
    }

    private static final long REQUESTS_PER_INTERVAL = 90; // actually 120, but to be safe
    private static final long REQUEST_INTERVAL = 60 * 1000; // a minute
    private static final long GAME_UPDATES_PER_INTERVAL = 4; // again 5, but better be safe
    private static final long REQUEST_DEFAULT_WAIT = 500; // how long do we limit for unknown endpoints

    /** We remember last X messages for gateway (timestamps) */
    private final Deque<Long> gatewayGuard;
    /** Same for presence updates */
    private final Deque<Long> gameGuard;
    /** A simple lock for endpoint maps */
    private final Object restGuard = new Object();

    /** Limits for channel endpoints */
    private final Map<String, ResetRemainPair> channelLimits;
    /** Limits for server endpoints */
    private final Map<String, ResetRemainPair> serverLimits;
    /** Limits for generic endpoints */
    private final Map<String, ResetRemainPair> endpointLimits;

    /** Discord also has a global limit, this is it; -1 means we are not limited */
    private volatile long globalLimit;

    public RateLimiter() {
        gatewayGuard = new ArrayDeque<>();
        gameGuard = new ArrayDeque<>();

        globalLimit = -1;

        channelLimits = new HashMap<>();
        serverLimits = new HashMap<>();
        endpointLimits = new HashMap<>();
    }

    /**
     * Throws away old game update request timestamps.
     */
    private void cleanGameUpdateLimit() {
        // Clean the queue
        synchronized (gameGuard) {
            while (gameGuard.peek() != null && gameGuard.peek() < System.currentTimeMillis() - REQUEST_INTERVAL) {
                gameGuard.removeFirst();
            }
        }
    }

    // Return 0 = post another, otherwise time to wait before sending

    /**
     * <p>Checks if the client can request another game update.</p>
     * <p>If denied, the client shall call this method later again, before sending the request.</p>
     * @return 0 if the request can be sent, or the minimum amount of time to wait before next try
     */
    public long checkGameUpdateLimit() {
        synchronized (gameGuard) {
            cleanGameUpdateLimit();
            // Check if possible
            if (gameGuard.size() >= GAME_UPDATES_PER_INTERVAL) {
                final long waitTime = gameGuard.peekFirst() - System.currentTimeMillis() + REQUEST_INTERVAL;
                return waitTime > 0 ? waitTime : 0;
            }
            // Approved, add new timestamp
            gameGuard.add(System.currentTimeMillis());
            return 0;
        }
    }

    /**
     * Throws away old gateway request timestamps.
     */
    private void cleanGatewayLimit() {
        // Clean the queue
        synchronized (gatewayGuard) {
            while (gatewayGuard.peek() != null && gatewayGuard.peek() < System.currentTimeMillis() - REQUEST_INTERVAL) {
                gatewayGuard.removeFirst();
            }
        }
    }

    /**
     * <p>Checks if the client can send another gateway request.</p>
     * <p>If denied, the client shall call this method later again, before sending the request.</p>
     * @return 0 if the request can be sent, or the minimum amount of time to wait before next try
     */
    public long checkGatewayLimit() {
        synchronized (gatewayGuard) {
            cleanGatewayLimit();
            // Check if possible to send a request
            if (gatewayGuard.size() >= REQUESTS_PER_INTERVAL) {
                final long waitTime = gatewayGuard.peekFirst() - System.currentTimeMillis() + REQUEST_INTERVAL;
                return waitTime > 0 ? waitTime : 0;
            }
            // Approved, add new timestamp
            gatewayGuard.add(System.currentTimeMillis());
            return 0;
        }
    }

    /**
     * <p>Check an endpoint map for an entry about its rate limits.</p>
     * <p>The entry contains the amount of allowed requests remaining and the timestamp denoting when the limit resets.</p>
     * <p>If the endpoint is not tracked yet, the limiter approves the requests but sets the reset to predefined constant.
     * This prevents spamming the server until we know the actual limit.</p>
     * @param map map containing the correct endpoint
     * @param id checked endpoint id
     * @return 0 if the request can be sent, or the minimum amount of time to wait before next try
     */
    private long checkEndpointMap(Map<String, ResetRemainPair> map, String id) {
        if(map.containsKey(id)) {
            final ResetRemainPair pair = map.get(id);
            if(pair.remaining > 0) {
                --pair.remaining;
                return 0;
            }
            else {
                final long reset = pair.reset - System.currentTimeMillis();
                if(reset > 0) {
                    return reset;
                }
                // We are reset but if another request comes, wait until we get information how long we have to wait
                pair.reset = System.currentTimeMillis() + REQUEST_DEFAULT_WAIT;
                return 0;
            }
        }
        else {
            // Allow first, stall second until we get some info about the limit
            map.put(id, new ResetRemainPair(System.currentTimeMillis() + REQUEST_DEFAULT_WAIT, 0));
        }
        return 0;
    }

    /**
     * Removes global limit, if we already reached that time.
     */
    private void updateGlobalLimit() {
        if(globalLimit != -1) {
            if(globalLimit >= System.currentTimeMillis()) {
                globalLimit = -1;
            }
        }
    }

    /**
     * <p>Checks if the client is allowed to send a request to given endpoint.</p>
     * <p>If the request is not approved, the client shall wait and call the method again.</p>
     * @param endpoint destination
     * @return 0 if the request is approved, amount of milliseconds to wait if it isn't
     */
    public long checkLimit(Endpoint endpoint) {
        synchronized (restGuard) {
            updateGlobalLimit();
            if (globalLimit != -1) {
                final long retry = globalLimit - System.currentTimeMillis(); // we are limited globally
                if (retry > 0) return retry;
            }

            // look what the endpoint is
            if (endpoint.isChannel()) {
                return checkEndpointMap(channelLimits, endpoint.getElement(1));
            }
            else if (endpoint.isServer()) {
                // This wont work with the createServer(), but that feature is not needed for bots
                return checkEndpointMap(serverLimits, endpoint.getElement(1));
            }
            else {
                return checkEndpointMap(endpointLimits, endpoint.getBase());
            }
        }
    }

    /**
     * Updates the endpoint map with new data.
     * @param map updated map
     * @param id endpoint id
     * @param reset new reset timestamp
     * @param remaining remaining requests allowed
     */
    private void updateEndpointMap(Map<String, ResetRemainPair> map, String id, long reset, int remaining) {
        map.put(id, new ResetRemainPair(reset, remaining));
    }

    /**
     * Updates the endpoint map with new data.
     * @param endpoint destination
     * @param reset new reset timestamp
     * @param remaining remaining requests allowed
     */
    private void updateLimitInner(Endpoint endpoint, long reset, int remaining) {
        synchronized (restGuard) {
            // look what the endpoint is
            if (endpoint.isChannel()) {
                updateEndpointMap(channelLimits, endpoint.getElement(1), reset, remaining);
            }
            else if (endpoint.isServer()) {
                // This wont work with the createServer(), but that feature is not needed for bots
                updateEndpointMap(serverLimits, endpoint.getElement(1), reset, remaining);
            }
            else {
                updateEndpointMap(endpointLimits, endpoint.getBase(), reset, remaining);
            }
        }
    }

    /**
     * Updates the endpoint map with new data.
     * @param endpoint destination
     * @param reset new reset timestamp
     * @param remaining remaining requests allowed
     */
    public void updateLimit(Endpoint endpoint, long reset, int remaining) {
        updateLimitInner(endpoint, reset * 1000, remaining);
    }

    /**
     * The server sent Retry-After header, we clear the limit to block other requests.
     * @param endpoint destination
     * @param retryAfter the amount of time to wait before next request
     */
    public void updateLimitRetry(Endpoint endpoint, long retryAfter) {
        updateLimitInner(endpoint, System.currentTimeMillis() + retryAfter, 0);
    }

    /**
     * Call when the global rate limit is exceeded. The limiter will deny every request until that time passes.
     * @param retry the amount of milliseconds to wait before next request is approved
     */
    public void globalLimitExceeded(long retry) {
        globalLimit = System.currentTimeMillis() + retry;
    }
}
