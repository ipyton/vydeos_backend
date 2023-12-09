package com.chen.blogbackend.filters;

import com.datastax.oss.driver.shaded.guava.common.base.Charsets;
import com.datastax.oss.driver.shaded.guava.common.hash.BloomFilter;

import com.datastax.oss.driver.shaded.guava.common.hash.Funnels;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class PostRecognizer {

    class DecoratedBloomFilter<T> {
        private Long from, to;
        BloomFilter<T> content;

        public DecoratedBloomFilter(Long from, Long to, BloomFilter<T> content) {
            this.from = from;
            this.to = to;
            this.content = content;
        }

        public Long getFrom() {
            return from;
        }

        public void setFrom(Long from) {
            this.from = from;
        }

        public Long getTo() {
            return to;
        }

        public void setTo(Long to) {
            this.to = to;
        }

        public BloomFilter<T> getContent() {
            return content;
        }

        public void setContent(BloomFilter<T> content) {
            this.content = content;
        }
    }


    ArrayList<DecoratedBloomFilter<String>> list;
    DecoratedBloomFilter<String> current;
    long previousTime = 0;
    long timeSlice = 5000;

    public PostRecognizer() {

    }

    public PostRecognizer(String path) {
        list = deSerialize("");
    }


    private void createSlice() {
        long start = System.currentTimeMillis();

        DecoratedBloomFilter<String> decoratedBloomFilter = new DecoratedBloomFilter<>(start, start + timeSlice ,BloomFilter.create(Funnels.stringFunnel(Charsets.UTF_8), 100000, 0.01));
        list.add(decoratedBloomFilter);
        current = decoratedBloomFilter;
        previousTime = start;
    }

    private boolean outOfBoundary() {
        return System.currentTimeMillis() <= timeSlice + previousTime;
    }

    public void set(ArrayList<String> userIds) {
        if (outOfBoundary()) {
            createSlice();
        }
        userIds.forEach(x->current.getContent().put(x));
    }

    public void set(String userId) {
        if (outOfBoundary()) {
            createSlice();
        }
        current.getContent().put(userId);
    }

    public ArrayList<String> get(ArrayList<String> userIds,Long start, Long timeSlices){
        Set<String> set = new HashSet<>();
        if(outOfBoundary()) {
            createSlice();
        }
        for (int i = 1; i <= timeSlices; i ++) {
            if (list.size() - i >= 0) {
                DecoratedBloomFilter<String> filter = list.get(list.size() - i);
                for (String userId : userIds) {
                    if (filter.getContent().mightContain(userId)) {
                        set.add(userId);
                    }
                }
            }
        }
        return new ArrayList<>(set);
    }

    public void serialize(String path) {


    }

    public ArrayList<DecoratedBloomFilter<String>> deSerialize(String path){
        return null;
    }



}
