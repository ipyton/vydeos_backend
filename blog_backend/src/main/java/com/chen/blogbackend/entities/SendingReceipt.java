package com.chen.blogbackend.entities;


public class SendingReceipt {
    public boolean result;
    public long messageId;
    public long timestamp;
    public long sessionMessageId;


    public SendingReceipt(boolean result, long messageId, long timestamp) {
        this.result = result;
        this.messageId = messageId;
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

    public long getMessageId() {
        return messageId;
    }

    public void setMessageId(long messageId) {
        this.messageId = messageId;
    }

    public SendingReceipt() {
    }
}
