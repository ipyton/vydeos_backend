package com.chen.blogbackend.util;

import com.chen.blogbackend.entities.App;
import com.datastax.oss.driver.api.core.cql.ResultSet;
import com.datastax.oss.driver.api.core.cql.Row;

import java.util.ArrayList;
import java.util.List;

// Why using this instead of mappers provided by Datax?
// Nosql database principles require redundant.
// If using mapper, it will break the integrity of the modeling theory.
public class DataConverter {
    public static List<App> convertToApp(ResultSet set) {
        List<Row> all = set.all();
        List<App> apps = new ArrayList<>();
        for (Row row: all) {
            System.out.println(row.getString("applicationId") + row.getString("name") + row.getFloat("ratings") + row.getList("pictures",String.class) + row.getString("author"));
            apps.add(new App());
        }
        return apps;
    }

    public static List<App> convertToAppDetail(ResultSet set) {
        List<Row> all = set.all();
        List<App> apps = new ArrayList<>();
        for (Row row: all) {
            apps.add(new App());
        }
        return apps;
    }


}
