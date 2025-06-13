package com.chen.notification.entities;

import java.time.Instant;
import java.util.List;

public class GroupMessage {
    private String userId;                     // 对应 user_id (text)
    private String receiverId;
    private long groupId;                      // 对应 group_id (bigint)
    private long messageId;                    // 对应 message_id (bigint)
    private String content;                    // 对应 content (text)
    private String messageType;                // 对应 messagetype (text)
    private Instant sendTime;                  // 对应 send_time (timestamp)
    private String type;                       // 对应 type (text)
    private long referMessageId;               // 对应 refer_message_id (bigint)
    private List<String> referUserId;          // 对应 refer_user_id (list<text>)
    private boolean del;                       // 对应 del (boolean)
    private long sessionMessageId;             // 对应 session_message_id (bigint)

    public GroupMessage() {}

    public String getReceiverId() {
        return receiverId;
    }

    public void setReceiverId(String receiverId) {
        this.receiverId = receiverId;
    }

    public GroupMessage(String userId, long groupId, long messageId, String content, String messageType,
                        Instant sendTime, String type, long referMessageId, List<String> referUserId,
                        boolean del, long sessionMessageId) {
        this.userId = userId;
        this.groupId = groupId;
        this.messageId = messageId;
        this.content = content;
        this.messageType = messageType;
        this.sendTime = sendTime;
        this.type = type;
        this.referMessageId = referMessageId;
        this.referUserId = referUserId;
        this.del = del;
        this.sessionMessageId = sessionMessageId;
    }

    // Getters and setters...

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public Long getGroupId() {
        return groupId;
    }

    public void setGroupId(long groupId) {
        this.groupId = groupId;
    }

    public Long getMessageId() {
        return messageId;
    }

    public void setMessageId(long messageId) {
        this.messageId = messageId;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getMessageType() {
        return messageType;
    }

    public void setMessageType(String messageType) {
        this.messageType = messageType;
    }

    public Instant getSendTime() {
        return sendTime;
    }

    public void setSendTime(Instant sendTime) {
        this.sendTime = sendTime;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public long getReferMessageId() {
        return referMessageId;
    }

    public void setReferMessageId(long referMessageId) {
        this.referMessageId = referMessageId;
    }

    public List<String> getReferUserId() {
        return referUserId;
    }

    public void setReferUserId(List<String> referUserId) {
        this.referUserId = referUserId;
    }

    public boolean isDel() {
        return del;
    }

    public void setDel(boolean del) {
        this.del = del;
    }

    public long getSessionMessageId() {
        return sessionMessageId;
    }

    public void setSessionMessageId(long sessionMessageId) {
        this.sessionMessageId = sessionMessageId;
    }

    @Override
    public String toString() {
        return "GroupMessage{" +
                "userId='" + userId + '\'' +
                ", receiverId='" + receiverId + '\'' +
                ", groupId=" + groupId +
                ", messageId=" + messageId +
                ", content='" + content + '\'' +
                ", messageType='" + messageType + '\'' +
                ", sendTime=" + sendTime +
                ", type='" + type + '\'' +
                ", referMessageId=" + referMessageId +
                ", referUserId=" + referUserId +
                ", del=" + del +
                ", sessionMessageId=" + sessionMessageId +
                '}';
    }
}
