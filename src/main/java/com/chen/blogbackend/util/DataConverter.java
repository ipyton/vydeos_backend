package com.chen.blogbackend.util;

import com.chen.blogbackend.entities.App;
import com.datastax.oss.driver.api.core.cql.ResultSet;
import com.datastax.oss.driver.api.core.cql.Row;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

// Why using this instead of mappers provided by Datax?
// Nosql database principles require redundant.
// If using mapper, it will break the integrity of the modeling theory.
public class DataConverter {
    public static List<App> convertToApp(ResultSet set) {
        List<Row> all = set.all();
        List<App> apps = new ArrayList<>();
        for (Row row: all) {
            System.out.println(row.getString("applicationId") + row.getString("name") + row.getFloat("ratings") + row.getList("pictures",String.class) + row.getString("author"));
            apps.add(new App(row.getString("applicationId"), row.getString("name"),
                    row.getFloat("ratings"), row.getList("pictures",String.class),
                    row.getString("author")));
        }
        return apps;
    }

    public static List<App> convertToAppDetail(ResultSet set) {
        List<Row> all = set.all();
        List<App> apps = new ArrayList<>();
        for (Row row: all) {
            Map<String, String> systemRequirements = row.getMap("systemRequirements", String.class, String.class);
            List<String> pictures = row.getList("pictures", String.class);
            List<String> hotComments = row.getList("hotComments", String.class);
            apps.add(new App(
                    row.getString("applicationId"), row.getString("name"), row.getString("version"),
                    row.getInstant("lastModified"), row.getFloat("ratings"), row.getString("type"),
                    systemRequirements,row.getList("historyVersions", String.class), pictures,
                    hotComments, row.getString("author"), row.getString("introduction")));
        }
        return apps;
    }




}
