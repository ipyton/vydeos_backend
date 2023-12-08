package com.chen.blogbackend;


import com.datastax.driver.core.Session;
import com.datastax.driver.mapping.MappingManager;
import com.datastax.oss.driver.api.core.CqlSession;
import com.mongodb.MongoClient;
import io.minio.MinioClient;
import org.apache.ibatis.io.Resources;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.web.servlet.ServletComponentScan;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.IOException;
import java.net.InetSocketAddress;

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

    @Bean
    public static MinioClient setMinioClient(){
        return MinioClient.builder()
                // api地址
                .endpoint("http://180.164.75.24:8090")
                .credentials("minioadmin", "minioadmin")
                .build();
    }

//    @Bean
//    public static MongoClient setMongoClient(){
//        return new MongoClient("127.0.0.1", 27017);
//    }


    @Bean
    public static CqlSession setScyllaSession(){

        return CqlSession.builder()
                .addContactPoint(new InetSocketAddress("101.132.222.131", 9042))
                .withLocalDatacenter("datacenter1")
                .build();

    }
}
