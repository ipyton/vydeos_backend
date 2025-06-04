package com.chen.notification;

import com.alibaba.fastjson.JSON;
import com.chen.notification.entities.NotificationMessage;
import com.datastax.oss.driver.api.core.CqlSession;
import com.datastax.oss.driver.api.core.cql.PreparedStatement;
import com.datastax.oss.driver.api.core.cql.ResultSet;
import com.datastax.oss.driver.api.core.cql.Row;
import jakarta.annotation.PostConstruct;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Component
@Profile("dispatcher")
public class DispatcherAutoRunner {

    KafkaConsumer<String, String> consumer;

    @Autowired
    KafkaProducer<String, String> producer;

    @Autowired
    CqlSession cqlSession;

    private ExecutorService executorService;

    @PostConstruct
    private void run(){
        executorService = Executors.newFixedThreadPool(4); // 根据需要调整线程池大小
        Properties props = new Properties();
        props.put("bootstrap.servers", "127.0.0.1" + ":9092");
        props.setProperty("group.id", "dispatcher");
        props.setProperty("enable.auto.commit", "false");

        consumer=  new KafkaConsumer<>(props, new StringDeserializer(), new StringDeserializer());

        // 使用新线程异步启动 Kafka 消费
        new Thread(this::consumeMessage).start();
    }


    private void consumeMessage() {
        //System.out.println("This is a dispatcher service");
        PreparedStatement insertMessage = cqlSession.prepare("insert into chat.chat_records (user_id," +
                " message_id, content, del, messagetype, receiver_id, refer_message_id,refer_user_id, send_time, type)" +
                "values(?,?,?,?,?,?,?,?,?,?)");
        PreparedStatement groupMessage = cqlSession.prepare("insert into chat.group_chat_records (user_id,"+
                " message_id, content, del, messagetype, group_id,refer_message_id,refer_user_id, send_time, type)" +
                "values(?,?,?,?,?,?,?,?,?,?)");

        PreparedStatement updateCount = cqlSession.prepare("insert into chat.unread_messages " +
                "(user_id text, receiver_id text, type text, messageType text, content text," +
                " send_time int, message_id int) values (?, ?, ?, ?, ?, ?, ?);" );

        PreparedStatement getCount = cqlSession.prepare("select count from chat.unread_messages where user_id = ? and type=? and receiver_id = ?");
        PreparedStatement setCount = cqlSession.prepare("insert into chat.unread_messages (user_id, receiver_id, type, messageType, content, send_time , message_id ) values(?,?,?,?,?,?,?)");



        PreparedStatement getMembers = cqlSession.prepare("select * from group_chat.chat_group_members where group_id = ?");

        consumer.subscribe(List.of("dispatch"));

        while (true) {
            ConsumerRecords<String, String> records = consumer.poll(Duration. ofMillis(100));
            for (ConsumerRecord<String, String> record : records) {
                String topic = record.topic();
                String key = record.key();
                String value = record.value();

                System.out.println(value);
                NotificationMessage notificationMessage = JSON.parseObject(value, NotificationMessage.class);
                if (notificationMessage.getType().equals("single")) {
                    System.out.println("single message");
                    cqlSession.execute(insertMessage.bind(notificationMessage.getSenderId(),notificationMessage.getMessageId(),
                            notificationMessage.getContent(),false, "text", notificationMessage.getReceiverId(), 0l,new ArrayList<String>(),
                            notificationMessage.getTime(), "single"));
                    producer.send(new ProducerRecord<String, String>("single",notificationMessage.getSenderId(), JSON.toJSONString(notificationMessage)));

                    System.out.println(notificationMessage);
                }
                else if (notificationMessage.getType().equals("group")) {
                    System.out.println("group message");
                    ResultSet execute = cqlSession.execute(getMembers.bind(notificationMessage.getGroupId()));
                    List<Row> all = execute.all();
                    cqlSession.execute(groupMessage.bind(notificationMessage.getSenderId(),notificationMessage.getMessageId(),
                            notificationMessage.getContent(),false, "text", notificationMessage.getGroupId(),0l,new ArrayList<String>(),
                            notificationMessage.getTime(), "group"));

                    for (Row row : all) {
                        String user_id = row.getString("user_id");
                        if (user_id.equals(notificationMessage.getSenderId())) {
                            continue;
                        }

                        System.out.println(user_id);
                        notificationMessage.setReceiverId(user_id);
                        producer.send(new ProducerRecord<String, String>("single",user_id, JSON.toJSONString(notificationMessage)));
                    }

                    System.out.println(notificationMessage);
                }
            }
            consumer.commitSync();
        }

    }

}
