package cz.salmelu.discord.implementation.json.request;

import cz.salmelu.discord.implementation.json.reflector.MappedObject;

public class HeartbeatRequest implements MappedObject {
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
