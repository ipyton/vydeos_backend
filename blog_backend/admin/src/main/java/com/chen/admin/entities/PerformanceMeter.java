package com.chen.admin.entities;

public class PerformanceMeter {
    String host;
    long cpuUsage;
    long memoryUsage;
    long bandwidthUsage;

    public PerformanceMeter(String host, long cpuUsage, long memoryUsage, long bandwidthUsage) {
        this.host = host;
        this.cpuUsage = cpuUsage;
        this.memoryUsage = memoryUsage;
        this.bandwidthUsage = bandwidthUsage;
    }

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public long getCpuUsage() {
        return cpuUsage;
    }

    public void setCpuUsage(long cpuUsage) {
        this.cpuUsage = cpuUsage;
    }

    public long getMemoryUsage() {
        return memoryUsage;
    }

    public void setMemoryUsage(long memoryUsage) {
        this.memoryUsage = memoryUsage;
    }

    public long getBandwidthUsage() {
        return bandwidthUsage;
    }

    public void setBandwidthUsage(long bandwidthUsage) {
        this.bandwidthUsage = bandwidthUsage;
    }
}
