package com.chen.blogbackend.converter;

import org.jetbrains.annotations.NotNull;
import org.springframework.core.convert.converter.Converter;

import java.time.Instant;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

public class StringToInstantConverter implements Converter<String, Instant> {

    @Override
    public Instant convert(@NotNull String source) {
        try {
            return Instant.ofEpochSecond(Long.parseLong(source));
        } catch (DateTimeParseException e) {
            // 可以根据需要自定义异常处理
            throw new IllegalArgumentException("Invalid date/time format", e);
        }
    }



}
