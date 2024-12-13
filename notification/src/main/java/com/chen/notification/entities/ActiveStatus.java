package com.chen.notification.entities;

import com.datastax.oss.driver.api.mapper.annotations.Entity;

@Entity
public class ActiveStatus {
    public String userId;
    public String partitionId;
    public String endpointId;

    public ActiveStatus(String userId, String partitionId) {
        this.userId = userId;
        this.partitionId = partitionId;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getPartitionId() {
        return partitionId;
    }

    public void setPartitionId(String partitionId) {
        this.partitionId = partitionId;
    }
}
