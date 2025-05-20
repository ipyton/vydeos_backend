package com.chen.blogbackend.mappers;

import com.chen.blogbackend.entities.MovieDownloadRequest;
import com.chen.blogbackend.entities.Relationship;
import com.datastax.oss.driver.api.core.cql.ResultSet;
import com.datastax.oss.driver.api.core.cql.Row;

import java.util.ArrayList;
import java.util.List;

public class MovieDownloadRequestParser {
    public static List<MovieDownloadRequest> parseMovieDownloadRequest(ResultSet rs) {
        ArrayList<MovieDownloadRequest> result = new ArrayList<>();
        for (Row row: rs.all()) {
            result.add(new MovieDownloadRequest(
                    rs.getColumnDefinitions().contains("resource_id") ? row.getString("resource_id"):null,
                    rs.getColumnDefinitions().contains("userId") ? row.getString("userId"):null,
                    rs.getColumnDefinitions().contains("create_time") ?row.getInstant("create_time"):null,
                    rs.getColumnDefinitions().contains("movie_name") ? row.getString("movie_name"):null,
                    rs.getColumnDefinitions().contains("actor_list") ? row.getList("actor_list", String.class):null,
                    rs.getColumnDefinitions().contains("release_year") ? row.getString("release_year"):null,
                    rs.getColumnDefinitions().contains("type") ? row.getString("type"):null));
        }
        return result;
    }
}
