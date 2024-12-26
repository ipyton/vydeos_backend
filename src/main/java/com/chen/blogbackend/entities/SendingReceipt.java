package com.chen.blogbackend.entities;


public class SendingReceipt {
    public boolean result;
    public long sequenceId;

    public SendingReceipt(boolean result, long sequenceId) {
        this.result = result;
        this.sequenceId = sequenceId;
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
