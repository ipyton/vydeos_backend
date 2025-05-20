package com.chen.blogbackend.entities;


import java.time.Instant;

//this is a receipt which is returned to the message sender.
public class MessageSendingReceipt {
    String messageId;
    Instant timestamp;

    public String getMessageId() {
        return messageId;
    }

    public void setMessageId(String messageId) {
        this.messageId = messageId;
    }

    public Instant getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Instant timestamp) {
        this.timestamp = timestamp;
    }

    public MessageSendingReceipt(String messageId, Instant timestamp) {
        this.messageId = messageId;
        this.timestamp = timestamp;
    }
}
