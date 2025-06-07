package com.chen.blogbackend.entities;


public class SendingReceipt {
    private boolean result;
    private long messageId;
    private long timestamp;
    private long sessionMessageId;
    private boolean delete;

    public SendingReceipt(boolean result, long messageId, long timestamp,long sessionMessageId, boolean delete) {
        this.result = result;
        this.messageId = messageId;
        this.timestamp = timestamp;
        this.sessionMessageId = sessionMessageId;
        this.delete = delete;
    }

    public long getSessionMessageId() {
        return sessionMessageId;
    }

    public void setSessionMessageId(long sessionMessageId) {
        this.sessionMessageId = sessionMessageId;
    }

    public boolean isDelete() {
        return delete;
    }

    public void setDelete(boolean delete) {
        this.delete = delete;
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
