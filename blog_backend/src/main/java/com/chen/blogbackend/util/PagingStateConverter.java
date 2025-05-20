package com.chen.blogbackend.util;

import com.datastax.oss.driver.api.core.cql.PagingState;

public class PagingStateConverter {
    public static PagingState stringToConverter(String state) {
        if (state == null || state.length() == 0) {
            return null;
        }
        return PagingState.fromString(state);
    }
}
