package cz.salmelu.discord.implementation.net;

import cz.salmelu.discord.implementation.net.rest.Endpoint;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.HashMap;
import java.util.Map;

public class RateLimiter {

    private class ResetRemainPair {
        long reset;
        int remaining;

        public ResetRemainPair(long reset, int remaining) {
            this.reset = reset;
            this.remaining = remaining;
        }
    }

    private static final long REQUESTS_PER_INTERVAL = 90; // actually 120, but to be safe
    private static final long REQUEST_INTERVAL = 60 * 1000; // a minute
    private static final long GAME_UPDATES_PER_INTERVAL = 4; // again 5, but to be safe

    private final Deque<Long> gatewayGuard;
    private final Deque<Long> gameGuard;
    private final Object restGuard = new Object();

    private final Map<String, ResetRemainPair> channelLimits;
    private final Map<String, ResetRemainPair> serverLimits;
    private final Map<String, ResetRemainPair> endpointLimits;

    private volatile long globalLimit;

    public RateLimiter() {
        gatewayGuard = new ArrayDeque<>();
        gameGuard = new ArrayDeque<>();

        globalLimit = -1;

        channelLimits = new HashMap<>();
        serverLimits = new HashMap<>();
        endpointLimits = new HashMap<>();
    }

    private void cleanGameUpdateLimit() {
        // Clean the queue
        synchronized (gameGuard) {
            while (gameGuard.peek() != null && gameGuard.peek() < System.currentTimeMillis() - REQUEST_INTERVAL) {
                gameGuard.removeFirst();
            }
        }
    }

    // Return 0 = post another, otherwise time to wait before sending
    public long checkGameUpdateLimit() {
        synchronized (gameGuard) {
            cleanGameUpdateLimit();
            // Check if possible
            if (gameGuard.size() >= GAME_UPDATES_PER_INTERVAL) {
                long waitTime = gameGuard.peekFirst() - System.currentTimeMillis() + REQUEST_INTERVAL;
                return waitTime > 0 ? waitTime : 0;
            }
            gameGuard.add(System.currentTimeMillis());
            return 0;
        }
    }

    private void cleanGatewayLimit() {
        // Clean the queue
        synchronized (gatewayGuard) {
            while (gatewayGuard.peek() != null && gatewayGuard.peek() < System.currentTimeMillis() - REQUEST_INTERVAL) {
                gatewayGuard.removeFirst();
            }
        }
    }

    // Return 0 = can post another, otherwise time to wait before sending
    public long checkGatewayLimit() {
        synchronized (gatewayGuard) {
            cleanGatewayLimit();
            // Check if possible
            if (gatewayGuard.size() >= REQUESTS_PER_INTERVAL) {
                long waitTime = gatewayGuard.peekFirst() - System.currentTimeMillis() + REQUEST_INTERVAL;
                return waitTime > 0 ? waitTime : 0;
            }
            gatewayGuard.add(System.currentTimeMillis());
            return 0;
        }
    }

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
                // If another request comes, wait until we get new information how long we have to wait
                pair.reset = System.currentTimeMillis() + 500;
                return 0;
            }
        }
        else {
            // Allow first, stall second until we get some info about the limit
            map.put(id, new ResetRemainPair(System.currentTimeMillis() + 500, 0));
        }
        return 0;
    }

    private void updateGlobalLimit() {
        if(globalLimit != -1) {
            if(globalLimit >= System.currentTimeMillis()) {
                globalLimit = -1;
            }
        }
    }

    public long checkLimit(Endpoint endpoint) {
        synchronized (restGuard) {
            updateGlobalLimit();
            if (globalLimit != -1) {
                long retry = globalLimit - System.currentTimeMillis(); // we are limited globally
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

    private void updateEndpointMap(Map<String, ResetRemainPair> map, String id, long reset, int remaining) {
        map.put(id, new ResetRemainPair(reset, remaining));
    }

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

    public void updateLimit(Endpoint endpoint, long reset, int remaining) {
        updateLimitInner(endpoint, reset * 1000, remaining);
    }

    public void updateLimitRetry(Endpoint endpoint, long retryAfter) {
        updateLimitInner(endpoint, System.currentTimeMillis() + retryAfter, 0);
    }

    public void globalLimitExceeded(long retry) {
        globalLimit = System.currentTimeMillis() + retry;
    }
}
