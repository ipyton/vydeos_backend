package com.chen.blogbackend;



import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.web.servlet.ServletComponentScan;

import java.io.IOException;
import java.net.InetSocketAddress;

@SpringBootApplication(exclude = {DataSourceAutoConfiguration.class})
@ServletComponentScan("com.chen.blogbackend.filters")
public class BlogBackendApplication {

    public static void main(String[] args) throws IOException {
        SpringApplication app = new SpringApplication(BlogBackendApplication.class);
        app.setAdditionalProfiles("dev"); // Activate 'dev' profile
        app.run(args);
    }




}
