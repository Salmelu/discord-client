package cz.salmelu.discord.implementation.net;

import cz.salmelu.discord.implementation.json.JSONMappedObject;
import cz.salmelu.discord.implementation.json.request.HeartbeatRequest;
import org.json.JSONObject;

public class HeartbeatGenerator {

    private int interval = 1000;
    private DiscordWebSocket socket;
    private volatile boolean active = false;
    private volatile boolean paused = false;
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

    public void pause() {
        paused = true;
    }

    public void resume() {
        paused = false;
    }

    public void heartbeatAck() {
        heartbeatReceived = true;
    }

    private void sendHeartbeat() {
        if(paused) {
            nextTick = System.currentTimeMillis() + 5000;
            return;
        }
        JSONObject beat = new JSONObject();
        beat.put("op", DiscordSocketMessage.HEARTBEAT);
        if(sequenceNumber == null) {
            beat.put("d", JSONObject.NULL);
        }
        else {
            beat.put("d", sequenceNumber);
        }
        socket.sendMessage(beat.toString());

        heartbeatReceived = false;
        nextTick = System.currentTimeMillis() + interval;
    }

}
