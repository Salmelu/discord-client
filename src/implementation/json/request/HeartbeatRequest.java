package cz.salmelu.discord.implementation.json.request;

import cz.salmelu.discord.implementation.json.JSONMappedObject;

public class HeartbeatRequest extends JSONMappedObject {
    private Integer sequenceNumber = null;

    public void setSequenceNumber(Integer sequenceNumber) {
        this.sequenceNumber = sequenceNumber;
    }

    public Integer getSequenceNumber() {
        return sequenceNumber;
    }
}
