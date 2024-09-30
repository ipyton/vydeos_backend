package com.chen.blogbackend.mappers;

import com.chen.blogbackend.entities.Cast;
import com.chen.blogbackend.entities.Post;
import com.chen.blogbackend.entities.Video;
import com.datastax.oss.driver.api.core.cql.ColumnDefinitions;
import com.datastax.oss.driver.api.core.cql.ResultSet;
import com.datastax.oss.driver.api.core.cql.Row;

import java.util.ArrayList;
import java.util.List;

public class VideoParser {
    public static List<Video> videoMetaParser(ResultSet set) {
        //String title, List<String> type, double rate, String introductions, List<String> images, List<Cast> casts
        //    private String movieId;
        //    private String poster;
        //    private String score;
        //    private String introduction;
        //    private String movie_name;
        //    private String tags;
        //    private List<String> actress_list;
        //    private String release_year ;
        //    private String level;
        //    private List<String> picture_list;
        //    private Map<String, String> maker_list;
        //    private List<String> genre_list;
        ArrayList<Video> videos = new ArrayList<>();
        for (Row row:
                set.all()) {
            ColumnDefinitions columnDefinitions = row.getColumnDefinitions();
            videos.add(new Video(!columnDefinitions.contains("movieId")?null:row.getString("movieId"),
                    !columnDefinitions.contains("poster")?null:row.getString("poster"),
                    !columnDefinitions.contains("score")?null:row.getString("score"),
                    !columnDefinitions.contains("introduction")?null:row.getString("introduction"),
                    !columnDefinitions.contains("movie_name")?null:row.getString("movie_name"),
                    !columnDefinitions.contains("tags")?null:row.getString("tags"),
                    !columnDefinitions.contains("actress_list")?null:row.getList("actress_list", String.class),
                    !columnDefinitions.contains("release_year")?null:row.getString("release_year"),
                    !columnDefinitions.contains("level")?null:row.getString("level"),
                    !columnDefinitions.contains("picture_list")?null:row.getList("picture_list",String.class),
                    !columnDefinitions.contains("maker_list")?null:row.getMap("maker_list",String.class, String.class),
                    !columnDefinitions.contains("position")?0:row.getInt("position"),
                    !columnDefinitions.contains("genre_list")?null:row.getList("genre_list", String.class)
                    ));
        }
        return videos;
    }
}
