package com.chen.blogbackend.entities;

import com.datastax.oss.driver.api.mapper.annotations.Entity;

@Entity
public class MessageCountEntity {
    long total;
    long count;
    public MessageCountEntity() {}
    public MessageCountEntity(long total, long count) {}
    public long getTotal() {
        return total;
    }

    public void setTotal(long total) {
        this.total = total;
    }

    public long getCount() {
        return count;
    }

    public void setCount(long count) {
        this.count = count;
    }
}
