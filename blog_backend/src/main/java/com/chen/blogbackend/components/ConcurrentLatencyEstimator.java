package com.chen.blogbackend.components;

import org.springframework.stereotype.Component;

import java.util.ArrayDeque;
import java.util.Deque;

@Component
public class ConcurrentLatencyEstimator {
    static class StatusInASecond {
        int count;
        long latencySum;
        long startTime;
        StatusInASecond(int count, long latencySum, long startTime) {
            this.count = count;
            this.latencySum = latencySum;
            this.startTime = startTime;
        }

        void addLatency(long latency) {
            latencySum += latency;
            count ++;
        }

    }

    Deque<StatusInASecond> queue = new ArrayDeque<>();
    int count = 0;
    long latencySum = 0;
    int estimate_duration = 0;

    public ConcurrentLatencyEstimator() {
        this.estimate_duration = 10;
    }

    synchronized public void addLatency(long latency) {
        long current_time = System.currentTimeMillis();

        if (queue.peekLast() != null && queue.peekLast().startTime < System.currentTimeMillis() - 1000L || queue.isEmpty()) {
            queue.push(new StatusInASecond(0, latencySum, current_time));
            estimate_duration += latency;
            count ++;
        }
        else {
            queue.peekLast().addLatency(latency);
        }
        while (!queue.isEmpty() && (queue.peek().startTime < System.currentTimeMillis() - 1000L * estimate_duration ||
                queue.size() > estimate_duration)) {
            StatusInASecond poll = queue.poll();
            estimate_duration -= poll.latencySum;
            count -= poll.count;
        }

    }

    public long getLatency(){
        return latencySum / (count+1) ;
    }

}
