package com.chen.blogbackend;

import com.chen.blogbackend.converter.StringToInstantConverter;
import com.chen.blogbackend.converter.StringToLocalDateConverter;
import org.springframework.context.annotation.Configuration;
import org.springframework.format.FormatterRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Override
    public void addFormatters(FormatterRegistry registry) {
        //registry.addConverter(new StringToInstantConverter());
        registry.addConverter(new StringToLocalDateConverter());
    }
}