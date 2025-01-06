package com.chen.blogbackend.Configurations;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.json.jackson.JacksonJsonpMapper;
import co.elastic.clients.transport.rest_client.RestClientTransport;
import com.datastax.oss.driver.api.core.CqlSession;
import io.minio.MinioClient;
import org.apache.http.Header;
import org.apache.http.HttpHost;
import org.apache.http.message.BasicHeader;

import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringSerializer;
import org.elasticsearch.client.RestClient;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.SchedulerFactory;
import org.quartz.impl.StdSchedulerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.Properties;

@Configuration
@Profile("dev")
public class DevConfig {
    static String ipAddress = "192.168.1.11";
     public static void setFilters(){

    }

//    @Bean
//    public static SqlSessionFactory sessionFactoryBean() throws IOException {
//        return new SqlSessionFactoryBuilder().build(
//                Resources.getResourceAsStream("mybatis-config.xml")
//        );
//    }

    @Bean
    public static MinioClient setMinioClient(){
        return MinioClient.builder()
                // api地址
                .endpoint("http://" + ipAddress + ":9000")
                .credentials("ROOTUSER", "CHANGEME123")
                .build();
    }

//    @Bean
//    public static MongoClient setMongoClient(){
//        return new MongoClient("127.0.0.1", 27017);
//    }


    @Bean
    public static CqlSession setScyllaSession(){

        return CqlSession.builder()
                .addContactPoint(new InetSocketAddress(ipAddress,9042))
                .withAuthCredentials("cassandra", "cassandra")
                .withLocalDatacenter("datacenter1").build();

    }


    @Bean
    public static Jedis configRedis() {
        Jedis jedis = new Jedis(ipAddress, 6379);
        // 如果设置 Redis 服务的密码，需要进行验证，若没有则可以省去
//        jedis.auth("123456");
        System.out.println("链接成功！");
        //查看服务是否运行
        System.out.println("服务正在运行！"+jedis.ping());
        return jedis;
    }

    @Bean
    public static JedisPool configJedisPool() {
         JedisPoolConfig jedisPoolConfig = new JedisPoolConfig();
        jedisPoolConfig.setMaxTotal(8);
        //最大空闲连接
        jedisPoolConfig.setMaxIdle(8);
        //最小空闲连接
        jedisPoolConfig.setMinIdle(4);
        //最长等待时间,ms
        jedisPoolConfig.setMaxIdle(20);
        jedisPoolConfig.setJmxEnabled(false);

        return new JedisPool(jedisPoolConfig,
                ipAddress,6379,1000);

    }


    @Bean
    public static ElasticsearchClient configElasticSearch() {
        String serverUrl = "http://" + ipAddress + ":9200";
        String apiKey = "VnVhQ2ZHY0JDZGJrU...";
        RestClient restClient = RestClient
                .builder(HttpHost.create(serverUrl))
                .setDefaultHeaders(new Header[]{
                        new BasicHeader("Authorization", "ApiKey " + apiKey)
                })
                .build();

        RestClientTransport transport = new RestClientTransport(
                restClient, new JacksonJsonpMapper());
        return new ElasticsearchClient(transport);
    }


    @Bean
    public static Scheduler configScheduler() throws SchedulerException {
        SchedulerFactory factory = new StdSchedulerFactory();
        return factory.getScheduler();
    }



    @Bean
    public static Producer<String,String> configMessageQueue()  {
// This is rocketmq configuration
        //        String endpoint = "192.168.23.129:8081";
////        String topic = "TestTopic";
//        ClientServiceProvider provider = ClientServiceProvider.loadService();
//        ClientConfigurationBuilder builder = ClientConfiguration.newBuilder().setEndpoints(endpoint);
//        //builder.enableSsl(false);
//        ClientConfiguration config = builder.build();
//        return provider.newProducerBuilder().setClientConfiguration(config).build();
        Properties props = new Properties();
        props.put("bootstrap.servers", ipAddress + ":9092");
        props.put(ProducerConfig.ACKS_CONFIG, "all");

//        props.put("transactional.id", "my-transactional-id");
        return new KafkaProducer<>(props, new StringSerializer(), new StringSerializer());

    }

}
