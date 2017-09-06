package cz.salmelu.discord.implementation.net.socket;

import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * <p>Takes care of sending heartbeats.</p>
 * <p>Works from another thread and continuously sends heartbeat packets to Discord websocket.</p>
 */
public class HeartbeatGenerator {

    /** The amount of milliseconds to stall the heartbeat if the generator is paused. */
    private static final long PAUSED_STALL = 5000;

    private int interval = 10000;
    private DiscordWebSocket socket;
    private volatile boolean active = false;
    private volatile boolean paused = false;
    private volatile boolean heartbeatReceived = true;
    private long nextTick = 0;
    private Integer sequenceNumber = null;

    private final Logger logger = LoggerFactory.getLogger(getClass().getSimpleName());

    HeartbeatGenerator(DiscordWebSocket socket) {
        this.socket = socket;
    }

    void setInterval(int interval) {
        this.interval = interval;
    }

    void start() {
        logger.debug("Heartbeat generator started.");
        active = true;
        heartbeatReceived = true;
        nextTick = System.currentTimeMillis();
        while(active) {
            if(nextTick <= System.currentTimeMillis()) {
                if(!heartbeatReceived) {
                    logger.debug("No heartbeat received, timing out.");
                    nextTick = System.currentTimeMillis() + PAUSED_STALL;
                    socket.timeout();
                    continue;
                }
                sendHeartbeat();
                try {
                    Thread.sleep(interval);
                }
                catch (InterruptedException e) {
                    // never mind, we'll continue and see if it matters
                }
            }
            else {
                // not yet the time we need
                try {
                    Thread.sleep(nextTick - System.currentTimeMillis() + 10);
                }
                catch (InterruptedException e) {
                    // never mind, we'll continue and see if it matters
                }
            }
        }
    }

    /**
     * New sequence number for the events was given by Discord.
     * @param seq new sequence number
     */
    void updateSequence(int seq) {
        sequenceNumber = seq;
    }

    void stop() {
        logger.debug("Heartbeat generator stopped.");
        active = false;
    }

    void pause() {
        logger.debug("Heartbeat generator paused.");
        paused = true;
    }

    void resume(boolean ack) {
        logger.debug("Heartbeat generator resumed (ack = " + ack + ").");
        heartbeatReceived = ack;
        paused = false;
    }

    /**
     * Server acknowledged client's heartbeat packet.
     */
    void heartbeatAck() {
        heartbeatReceived = true;
    }

    /**
     * Sends a heartbeat to Discord server.
     */
    void sendHeartbeat() {
        if(paused) {
            logger.debug("Stalling heartbeat because of paused generator.");
            nextTick = System.currentTimeMillis() + PAUSED_STALL;
            return;
        }
        logger.debug("Sending heartbeat to websocket.");

        // Heartbeat object - it is a bit special so it's made here on purpose
        JSONObject beat = new JSONObject();
        beat.put("op", DiscordSocketMessage.HEARTBEAT);
        if(sequenceNumber == null) {
            beat.put("d", JSONObject.NULL);
        }
        else {
            beat.put("d", sequenceNumber);
        }

        // Send the message and bypass some of the checks
        socket.sendMessage0(beat.toString(), true);

        heartbeatReceived = false;
        nextTick = System.currentTimeMillis() + interval;
    }

}
