package com.chen.notification;

import com.alibaba.fastjson.JSON;
import com.chen.notification.entities.NotificationMessage;
import com.chen.notification.entities.UnreadMessage;
import com.chen.notification.mappers.UnreadMessageParser;
import com.chen.notification.service.DistributedLockService;
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

    @Autowired
    DistributedLockService distributedLockService;

    private ExecutorService executorService;

    PreparedStatement getMembers;
    PreparedStatement groupMessage;
    PreparedStatement insertMessage;
    PreparedStatement getCount;
    PreparedStatement setCount;

    @PostConstruct
    private void run(){
        insertMessage = cqlSession.prepare("insert into chat.chat_records (user_id," +
                " message_id, content, del, messagetype, receiver_id, refer_message_id,refer_user_id, send_time, type)" +
                "values(?,?,?,?,?,?,?,?,?,?)");
        groupMessage = cqlSession.prepare("insert into chat.group_chat_records (user_id,"+
                " message_id, content, del, messagetype, group_id,refer_message_id,refer_user_id, send_time, type)" +
                "values(?,?,?,?,?,?,?,?,?,?)");


        getMembers = cqlSession.prepare("select * from group_chat.chat_group_members where group_id = ?;");

        getCount = cqlSession.prepare("select count from chat.unread_messages where user_id = ? and type= ? and sender_id = ?;");
        setCount = cqlSession.prepare("insert into chat.unread_messages (" +
                "user_id, sender_id, type, messageType, content, send_time , message_id,count, member_id) " +
                "values(?,?,?,?,?,?,?,?,?);");


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

                    addUnread(notificationMessage);


                    cqlSession.execute(insertMessage.bind(notificationMessage.getSenderId(),notificationMessage.getMessageId(),
                            notificationMessage.getContent(),false, "text", notificationMessage.getReceiverId(), 0l,new ArrayList<String>(),
                            notificationMessage.getTime(), "single"));
                    producer.send(new ProducerRecord<String, String>("single",notificationMessage.getSenderId(), JSON.toJSONString(notificationMessage)));



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
                        addUnread(notificationMessage);
                        producer.send(new ProducerRecord<String, String>("single",user_id, JSON.toJSONString(notificationMessage)));
                    }

                    System.out.println(notificationMessage);
                }
            }
            consumer.commitSync();
        }

    }

    private void addUnread(NotificationMessage notificationMessage) {


        DistributedLockService.LockToken lockToken = distributedLockService.acquireLock(notificationMessage.getReceiverId());
        if (lockToken == null) {
            throw new RuntimeException("lock acquisition failed");
        }

        ResultSet execute = cqlSession.execute(getCount.bind(notificationMessage.getReceiverId(), "single", notificationMessage.getSenderId()));
        List<UnreadMessage> unreadMessages = UnreadMessageParser.parseToUnreadMessage(execute);
        UnreadMessage unreadMessage = unreadMessages.get(0);
        unreadMessage.setCount(unreadMessage.getCount() + 1);
        unreadMessage.setMessageId(notificationMessage.getMessageId());
        unreadMessage.setContent(notificationMessage.getContent());
        unreadMessage.setSendTime(notificationMessage.getTime());
        unreadMessage.setMemberId(notificationMessage.getSenderId());
        cqlSession.execute(setCount.bind( unreadMessage.getUserId(), unreadMessage.getSenderId(), "single",
                notificationMessage.getMessageType(), unreadMessage.getContent(), unreadMessage.getSendTime(),
                unreadMessage.getMessageId(),unreadMessage.getCount(),unreadMessage.getMemberId()));
        distributedLockService.releaseLock(lockToken);
    }

}
