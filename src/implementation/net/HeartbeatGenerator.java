package cz.salmelu.discord.implementation.net;

import cz.salmelu.discord.implementation.json.socket.HeartbeatRequest;

public class HeartbeatGenerator {

    private int interval = 1000;
    private DiscordWebSocket socket;
    private volatile boolean active;
    private volatile boolean heartbeatReceived = true;
    private long nextTick = 0;
    private Integer sequenceNumber = null;

    public HeartbeatGenerator(DiscordWebSocket socket) {
        this.socket = socket;
    }

    public void setInterval(int interval) {
        this.interval = interval;
    }

    public void start() {
        active = true;
        heartbeatReceived = true;
        nextTick = System.currentTimeMillis();
        while(active) {
            if(nextTick <= System.currentTimeMillis()) {
                if(!heartbeatReceived) {
                    socket.timeout();
                    return;
                }
                sendHeartbeat();
                try {
                    Thread.sleep(interval);
                }
                catch (InterruptedException e) {
                    // nvm, we'll continue and see if it matters
                }
            }
            else {
                try {
                    Thread.sleep(nextTick - System.currentTimeMillis() + 10);
                }
                catch (InterruptedException e) {
                    // nvm, we'll continue and see if it matters
                }
            }
        }
    }

    public void updateSequence(int seq) {
        sequenceNumber = seq;
    }

    public void stop() {
        active = false;
    }

    public void heartbeatReceived() {
        heartbeatReceived = true;
    }

    private void sendHeartbeat() {
        final HeartbeatRequest request = new HeartbeatRequest();
        request.setSequenceNumber(sequenceNumber);
        socket.sendMessage(DiscordSocketMessage.HEARTBEAT, request);

        heartbeatReceived = false;
        nextTick = System.currentTimeMillis() + interval;
    }

}
