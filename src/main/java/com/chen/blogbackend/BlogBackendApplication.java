package com.chen.blogbackend;


import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.json.jackson.JacksonJsonpMapper;
import co.elastic.clients.transport.rest_client.RestClientTransport;
import com.datastax.driver.core.Session;
import com.datastax.driver.mapping.MappingManager;
import com.datastax.oss.driver.api.core.CqlSession;
import com.mongodb.MongoClient;
import io.minio.MinioClient;
import org.apache.http.Header;
import org.apache.http.HttpHost;
import org.apache.http.message.BasicHeader;
import org.apache.ibatis.io.Resources;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;

import org.apache.rocketmq.client.ClientConfig;
import org.apache.rocketmq.client.producer.MQProducer;
import org.elasticsearch.client.RestClient;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.SchedulerFactory;
import org.quartz.impl.StdSchedulerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.autoconfigure.kafka.KafkaProperties;
import org.springframework.boot.web.servlet.ServletComponentScan;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import redis.clients.jedis.Jedis;

import java.io.IOException;
import java.net.InetSocketAddress;

import org.apache.rocketmq.client.apis.producer.SendReceipt;

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


    @Bean
    public static Jedis configRedis() {
        Jedis jedis = new Jedis("121.x.x.x", 6379);
        // 如果设置 Redis 服务的密码，需要进行验证，若没有则可以省去
        jedis.auth("123456");
        System.out.println("链接成功！");
        //查看服务是否运行
        System.out.println("服务正在运行！"+jedis.ping());
        return jedis;
    }

    @Bean
    public static ElasticsearchClient configElasticSearch() {
        String serverUrl = "https://localhost:9200";
        String apiKey = "VnVhQ2ZHY0JDZGJrU...";
        RestClient restClient = RestClient
                .builder(HttpHost.create(serverUrl))
                .setDefaultHeaders(new Header[]{
                        new BasicHeader("Authorization", "ApiKey " + apiKey)
                })
                .build();

// Create the transport with a Jackson mapper
        RestClientTransport transport = new RestClientTransport(
                restClient, new JacksonJsonpMapper());

// And create the API client

        return new ElasticsearchClient(transport);
    }


    @Bean
    public static Scheduler configScheduler() throws SchedulerException {
        SchedulerFactory factory = new StdSchedulerFactory();
        return factory.getScheduler();
    }


    @Bean
    public static void configMessageQueue() {
        String endpoint = "localhost:8081";
        String topic = "TestTopic";
        ClientServiceProvider
    }






}
