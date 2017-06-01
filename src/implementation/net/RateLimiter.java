package cz.salmelu.discord.implementation.net;

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

    private final Map<String, ResetRemainPair> channelLimits;
    private final Map<String, ResetRemainPair> serverLimits;
    private final Map<String, ResetRemainPair> endpointLimits;

    private long globalLimit;

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
        while(gameGuard.peek() != null && gameGuard.peek() < System.currentTimeMillis() - REQUEST_INTERVAL) {
            gameGuard.removeFirst();
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
        while(gatewayGuard.peek() != null && gatewayGuard.peek() < System.currentTimeMillis() - REQUEST_INTERVAL) {
            gatewayGuard.removeFirst();
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
                return 0;
            }
            else {
                return pair.reset - System.currentTimeMillis();
            }
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

    public long checkLimit(String endpoint) {
        updateGlobalLimit();
        if(globalLimit != -1) {
            long retry = globalLimit - System.currentTimeMillis(); // we are limitted globally
            if (retry > 0) return retry;
        }

        // look what the endpoint is
        if(endpoint.startsWith(Endpoint.CHANNEL)) {
            return checkEndpointMap(channelLimits, endpoint.substring(Endpoint.CHANNEL.length()).split("/")[0]);
        }
        else if(endpoint.startsWith(Endpoint.SERVER)) {
            // FIXME: this wont work with the createServer(), but well...
            return checkEndpointMap(serverLimits, endpoint.substring(Endpoint.SERVER.length()).split("/")[0]);
        }
        else {
            return checkEndpointMap(endpointLimits, endpoint.substring(Endpoint.BASE.length()));
        }
    }

    private void updateEndpointMap(Map<String, ResetRemainPair> map, String id, long reset, int remaining) {
        map.put(id, new ResetRemainPair(reset, remaining));
    }

    private void updateLimitInner(String endpoint, long reset, int remaining) {
        // look what the endpoint is
        if(endpoint.startsWith(Endpoint.CHANNEL)) {
            updateEndpointMap(channelLimits,
                    endpoint.substring(Endpoint.CHANNEL.length()).split("/")[0], reset, remaining);
        }
        else if(endpoint.startsWith(Endpoint.SERVER)) {
            // FIXME: this wont work with the createServer(), but well...
            updateEndpointMap(serverLimits,
                    endpoint.substring(Endpoint.SERVER.length()).split("/")[0], reset, remaining);
        }
        else {
            updateEndpointMap(endpointLimits, endpoint.substring(Endpoint.BASE.length()), reset, remaining);
        }
    }

    public void updateLimit(String endpoint, long reset, int remaining) {
        updateLimitInner(endpoint, reset * 1000, remaining);
    }

    public void updateLimitRetry(String endpoint, long retryAfter) {
        updateLimitInner(endpoint, System.currentTimeMillis() + retryAfter, 0);
    }


    public void globalLimitExceeded(long retry) {
        globalLimit = System.currentTimeMillis() + retry;
    }
}
