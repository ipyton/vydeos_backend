package com.chen.notification.configs;


import com.datastax.oss.driver.api.core.CqlSession;
import nl.martijndwars.webpush.PushService;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

import java.net.InetSocketAddress;
import java.net.http.HttpClient;
import java.security.Security;
import java.util.Properties;

@Configuration
@Profile("endpoint")
public class EndpointConfiguration {

    private static final String ipAddress = "127.0.0.1";
    private static final String VAPID_PRIVATE_KEY = "hSMqgJnMk1W2eByorB3c1OG4TCZ_Bwf_VVuCtp6T9-s"; // 从 private.pem 获取
    private static final String VAPID_PUBLIC_KEY = "BKN0Wn4eIWJNOkZ58-G2HZ1rMuSfi8i4XfTFsti6yaF2G25Fv8dh0K_XZmklXDZ1vp0ozTpb6ZlXdnvYg3PV01w"; // 从 public.pem 获取
    private static final String VAPID_EMAIL = "mailto: czhdawang@163.com"; // 你自己的邮箱

    @Bean
    public PushService createPushService() throws Exception {
        Security.addProvider(new BouncyCastleProvider());
        PushService pushService = new PushService();
        pushService.setPublicKey(VAPID_PUBLIC_KEY);
        pushService.setPrivateKey(VAPID_PRIVATE_KEY);
        return pushService;
    }

//    @Bean
//    public JedisPool configRedis() {
//        System.out.println("configRedis2");
//        JedisPoolConfig jedisPoolConfig = new JedisPoolConfig();
//        JedisPool jedisPool = new JedisPool(jedisPoolConfig, ipAddress, 6379);
//        return jedisPool;
//    }

    @Bean
    public KafkaConsumer<String, String> configKafkaConsumer() {
        Properties props = new Properties();
        props.put("bootstrap.servers", ipAddress + ":9092");
        props.setProperty("enable.auto.commit", "false");
        props.setProperty("group.id", "endpoint");

        return new KafkaConsumer<>(props, new StringDeserializer(), new StringDeserializer());
    }

    @Bean
    public CqlSession setScyllaSession(){

        return CqlSession.builder()
                .addContactPoint(new InetSocketAddress(ipAddress,9042))
                .withAuthCredentials("cassandra", "cassandra")
                .withLocalDatacenter("datacenter1").build();

    }
    @Bean
    public HttpClient setClient() {
        return HttpClient.newHttpClient();
    }



}
