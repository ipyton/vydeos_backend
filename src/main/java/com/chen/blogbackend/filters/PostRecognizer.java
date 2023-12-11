package com.chen.blogbackend.filters;

import com.datastax.oss.driver.shaded.guava.common.base.Charsets;
import com.datastax.oss.driver.shaded.guava.common.hash.BloomFilter;

import com.datastax.oss.driver.shaded.guava.common.hash.Funnel;
import com.datastax.oss.driver.shaded.guava.common.hash.Funnels;
import org.springframework.stereotype.Service;

import java.io.*;
import java.util.*;

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

    public PostRecognizer(String path) throws IOException {
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
        if(outOfBoundary()) {
            createSlice();
        }

        Set<String> set = new HashSet<>();
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

    public boolean serialize(String path) throws IOException {
        File file = new File(path);
        if (!file.isFile()) {
                 return false;
        }

        OutputStream outputStream = new FileOutputStream(path);
        FileWriter fileWriter = new FileWriter(path + "_meta");

        fileWriter.write(previousTime + "_" + timeSlice + "\n");
        for (DecoratedBloomFilter<String> filter : list) {
            filter.getContent().writeTo(outputStream);
            fileWriter.write(filter.from + "_" + filter.to + "\n");
        }

        outputStream.close();
        fileWriter.close();
        return true;
    }

    public ArrayList<DecoratedBloomFilter<String>> deSerialize(String path) throws IOException {
        Scanner sc = new Scanner(new FileInputStream(path+"_meta"));
        InputStream inputStream = new FileInputStream("path");
        list = new ArrayList<>();

        if(sc.hasNext()) {
            String[] s = sc.nextLine().split("_");
            previousTime = Long.getLong(s[0]);
            timeSlice = Long.getLong(s[1]);
        }
        while(0 != inputStream.available() && sc.hasNext()) {
            BloomFilter<String> bloomFilter = BloomFilter.readFrom(inputStream, Funnels.stringFunnel(Charsets.UTF_8));
            String[] metaLine = sc.nextLine().split("_");
            list.add(new DecoratedBloomFilter<String>(Long.getLong(metaLine[0]), Long.getLong(metaLine[1]), bloomFilter));
        }

        return list;
    }



}
