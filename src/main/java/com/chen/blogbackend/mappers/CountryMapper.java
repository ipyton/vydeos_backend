package com.chen.blogbackend.mappers;

import com.chen.blogbackend.entities.Country;
import com.datastax.oss.driver.api.core.cql.ColumnDefinitions;
import com.datastax.oss.driver.api.core.cql.ResultSet;
import com.datastax.oss.driver.api.core.cql.Row;

import java.util.LinkedList;
import java.util.List;

public  class CountryMapper {
    public static List<Country> countriesMapper(ResultSet resultSet) {
        LinkedList<Country> countries = new LinkedList<>();
        for (Row row : resultSet) {
            countries.add(countryMapper(row));
        }
        return countries;
    }

    public static Country countryMapper(Row countryRow) {
        ColumnDefinitions columnDefinitions = countryRow.getColumnDefinitions();
        return new Country(
                !columnDefinitions.contains("country") ? null : countryRow.getString("country"),
                !columnDefinitions.contains("language") ? null : countryRow.getString("language"),
                !columnDefinitions.contains("country_en") ? null : countryRow.getString("country_en")
        );
    }
}
