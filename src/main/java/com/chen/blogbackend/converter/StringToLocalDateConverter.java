package com.chen.blogbackend.converter;

import org.jetbrains.annotations.NotNull;
import org.springframework.core.convert.converter.Converter;

import java.time.Instant;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;

public class StringToLocalDateConverter implements Converter<String, LocalDate> {
    @Override
    public LocalDate convert(@NotNull String source) {
        try {
            return LocalDate.parse(source);
        } catch (DateTimeParseException e) {
            // 可以根据需要自定义异常处理
            throw new IllegalArgumentException("错误格式", e);
        }

    }
}
