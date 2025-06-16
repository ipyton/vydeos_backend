package com.chen.blogbackend.entities;

import java.time.Instant;
import java.util.List;

public class GroupMessage {
    private String userId;                     // 对应 user_id (text)
    private String receiverId;
    private Long groupId;                      // 对应 group_id (bigint)
    private Long messageId;                    // 对应 message_id (bigint)
    private String content;                    // 对应 content (text)
    private String messageType;                // 对应 messagetype (text)
    private Instant timestamp;                  // 对应 send_time (timestamp)
    private String type;                       // 对应 type (text)
    private Long referMessageId;               // 对应 refer_message_id (bigint)
    private List<String> referUserId;          // 对应 refer_user_id (list<text>)
    private Boolean del;                       // 对应 del (boolean)
    private Long sessionMessageId;             // 对应 session_message_id (bigint)

    public GroupMessage() {}



    public String getReceiverId() {
        return receiverId;
    }

    public void setReceiverId(String receiverId) {
        this.receiverId = receiverId;
    }

    public GroupMessage(String userId, Long groupId, Long messageId, String content, String messageType,
                        Instant timestamp, String type, Long referMessageId, List<String> referUserId,
                        Boolean del, Long sessionMessageId) {
        this.userId = userId;
        this.groupId = groupId;
        this.messageId = messageId;
        this.content = content;
        this.messageType = messageType;
        this.timestamp = timestamp;
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

    public void setGroupId(Long groupId) {
        this.groupId = groupId;
    }

    public Long getMessageId() {
        return messageId;
    }

    public void setMessageId(Long messageId) {
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

    public Instant getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Instant timestamp) {
        this.timestamp = timestamp;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public Long getReferMessageId() {
        return referMessageId;
    }

    public void setReferMessageId(Long referMessageId) {
        this.referMessageId = referMessageId;
    }

    public List<String> getReferUserId() {
        return referUserId;
    }

    public void setReferUserId(List<String> referUserId) {
        this.referUserId = referUserId;
    }

    public Boolean isDel() {
        return del;
    }

    public void setDel(Boolean del) {
        this.del = del;
    }

    public Long getSessionMessageId() {
        return sessionMessageId;
    }

    public void setSessionMessageId(Long sessionMessageId) {
        this.sessionMessageId = sessionMessageId;
    }
}
