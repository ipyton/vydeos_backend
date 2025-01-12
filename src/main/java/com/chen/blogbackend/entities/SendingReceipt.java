package com.chen.blogbackend.entities;


import java.time.Instant;

public class SendingReceipt {
    public boolean result;
    public long sequenceId;
    public long timestamp;

    public SendingReceipt(boolean result, long sequenceId, long timestamp) {
        this.result = result;
        this.sequenceId = sequenceId;
        this.timestamp = timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public boolean isResult() {
        return result;
    }

    public void setResult(boolean result) {
        this.result = result;
    }

    public long getSequenceId() {
        return sequenceId;
    }

    public void setSequenceId(long sequenceId) {
        this.sequenceId = sequenceId;
    }

    public SendingReceipt() {
    }
}
