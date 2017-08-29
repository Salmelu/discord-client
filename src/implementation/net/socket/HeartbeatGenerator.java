package cz.salmelu.discord.implementation.net.socket;

import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class HeartbeatGenerator {

    private int interval = 1000;
    private DiscordWebSocket socket;
    private volatile boolean active = false;
    private volatile boolean paused = false;
    private volatile boolean heartbeatReceived = true;
    private long nextTick = 0;
    private Integer sequenceNumber = null;

    private final Logger logger = LoggerFactory.getLogger(getClass().getSimpleName());

    public HeartbeatGenerator(DiscordWebSocket socket) {
        this.socket = socket;
    }

    public void setInterval(int interval) {
        this.interval = interval;
    }

    public void start() {
        logger.debug("Heartbeat generator started.");
        active = true;
        heartbeatReceived = true;
        nextTick = System.currentTimeMillis();
        while(active) {
            if(nextTick <= System.currentTimeMillis()) {
                if(!heartbeatReceived) {
                    logger.debug("No heartbeat received, timing out.");
                    nextTick = System.currentTimeMillis() + 5000;
                    socket.timeout();
                    continue;
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
        logger.debug("Heartbeat generator stopped.");
        active = false;
    }

    public void pause() {
        logger.debug("Heartbeat generator paused.");
        paused = true;
    }

    public void resume(boolean ack) {
        logger.debug("Heartbeat generator resumed (ack = " + ack + ").");
        heartbeatReceived = ack;
        paused = false;
    }

    public void heartbeatAck() {
        heartbeatReceived = true;
    }

    void sendHeartbeat() {
        if(paused) {
            logger.debug("Stalling heartbeat because of paused generator.");
            nextTick = System.currentTimeMillis() + 5000;
            return;
        }
        logger.debug("Sending heartbeat to websocket.");
        JSONObject beat = new JSONObject();
        beat.put("op", DiscordSocketMessage.HEARTBEAT);
        if(sequenceNumber == null) {
            beat.put("d", JSONObject.NULL);
        }
        else {
            beat.put("d", sequenceNumber);
        }
        socket.sendMessage0(beat.toString());

        heartbeatReceived = false;
        nextTick = System.currentTimeMillis() + interval;
    }

}
