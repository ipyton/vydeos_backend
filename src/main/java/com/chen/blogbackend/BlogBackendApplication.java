package com.chen.blogbackend;

import com.datastax.driver.core.Cluster;
import com.datastax.driver.core.Session;
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

//
//    @Bean
//    public static Session setScyllaSession(){
//        String ip = "192.168.1.1";
//        Cluster cluster = Cluster.builder().addContactPoints(ip).withPort(8848).build();
//        System.out.printf("connected to cluster : %s%n", cluster.getMetadata().getClusterName());
//        Session session = cluster.connect();
//        return session;
//
//    }
}
