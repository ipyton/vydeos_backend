package com.chen.blogbackend.mappers;

import com.chen.blogbackend.entities.Trend;
import redis.clients.jedis.resps.Tuple;

import java.util.ArrayList;
import java.util.List;

public class TrendsMapper {
    public static List<Trend> parseTrends(List<Tuple> range) {
        List<Trend> trends = new ArrayList<>();
        for (Tuple tuple : range) {
            double score = tuple.getScore();
            String element = tuple.getElement();
            String[] split = element.split(":");
            Trend trend = new Trend(split[0], split[1], split[2], split[3], score);
            trends.add(trend);
        }
        return trends;
    }
}
