package cz.salmelu.discord.implementation.json.request;

import cz.salmelu.discord.implementation.json.JSONMappedObject;

public class HeartbeatRequest extends JSONMappedObject {
    private int op;
    private Integer d = null;

    public HeartbeatRequest() {
        this.op = 1;
    }

    public void setD(Integer sequenceNumber) {
        this.d = d;
    }

    public Integer getD() {
        return d;
    }


    public int getOp() {
        return op;
    }

    public void setOp(int op) {
        this.op = op;
    }
}
