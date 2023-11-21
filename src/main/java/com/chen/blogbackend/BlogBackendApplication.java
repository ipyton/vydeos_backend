package com.chen.blogbackend;

import jakarta.annotation.Resource;
import org.apache.ibatis.io.Resources;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;
import org.mybatis.spring.SqlSessionFactoryBean;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.web.servlet.ServletComponentScan;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.IOException;

@SpringBootApplication(exclude = {DataSourceAutoConfiguration.class})
@ServletComponentScan("com.chen.blogbackend.filters")
@Configuration
public class BlogBackendApplication {

    public static void main(String[] args) throws IOException {
        SpringApplication.run(BlogBackendApplication.class, args);
    }

    @Bean
    public static void setFilters(){

    }

    @Bean
    public static SqlSessionFactory sessionFactoryBean() throws IOException {
        System.out.println("sdfgsdgsdgfsd");
        return new SqlSessionFactoryBuilder().build(
                Resources.getResourceAsStream("mybatis-config.xml")
        );
    }

}
