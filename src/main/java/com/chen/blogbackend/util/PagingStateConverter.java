package com.chen.blogbackend.util;

import com.datastax.oss.driver.api.core.cql.PagingState;

public class PagingStateConverter {
    public static PagingState stringToConverter(String state) {
        return PagingState.fromString(state);
    }
}
