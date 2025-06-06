package com.chen.notification;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.chen.notification.entities.GroupMessage;
import com.chen.notification.entities.SingleMessage;
import com.chen.notification.entities.UnreadMessage;
import com.chen.notification.mappers.UnreadMessageParser;
import com.chen.notification.service.DistributedLockService;
import com.datastax.oss.driver.api.core.CqlSession;
import com.datastax.oss.driver.api.core.cql.PreparedStatement;
import com.datastax.oss.driver.api.core.cql.ResultSet;
import com.datastax.oss.driver.api.core.cql.Row;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.errors.WakeupException;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;

import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;

@Component
@Profile("dispatcher")
public class DispatcherAutoRunner {

    private static final Logger logger = LoggerFactory.getLogger(DispatcherAutoRunner.class);
    private static final int MAX_RETRY_ATTEMPTS = 3;
    private static final long RETRY_DELAY_MS = 1000;
    private static final long LOCK_TIMEOUT_MS = 5000;

    @Autowired
    private KafkaProducer<String, String> producer;

    @Autowired
    private CqlSession cqlSession;

    @Autowired
    private DistributedLockService distributedLockService;

    @Value("${kafka.bootstrap.servers:127.0.0.1:9092}")
    private String bootstrapServers;

    @Value("${kafka.consumer.group.id:dispatcher}")
    private String consumerGroupId;

    @Value("${dispatcher.thread.pool.size:4}")
    private int threadPoolSize;

    @Value("${kafka.consumer.poll.timeout:1000}")
    private long pollTimeoutMs;

    private ExecutorService executorService;
    private final AtomicBoolean running = new AtomicBoolean(false);
    private CompletableFuture<Void> consumerTask;

    // Prepared statements
    private PreparedStatement getMembers;
    private PreparedStatement groupMessage;
    private PreparedStatement insertMessage;
    private PreparedStatement getCount;
    private PreparedStatement setCount;
    private KafkaConsumer<String, String> consumer;

    @PostConstruct
    private void initialize() {
        try {
            logger.info("Initializing DispatcherAutoRunner...");

            initializePreparedStatements();
            initializeExecutorService();
            initializeKafkaConsumer();

            // Start consumer in a separate thread
            consumerTask = CompletableFuture.runAsync(this::consumeMessages);

            logger.info("DispatcherAutoRunner initialized successfully");
        } catch (Exception e) {
            logger.error("Failed to initialize DispatcherAutoRunner", e);
            throw new RuntimeException("Initialization failed", e);
        }
    }

    private void initializePreparedStatements() {
        try {

            getMembers = cqlSession.prepare(
                    "SELECT * FROM group_chat.chat_group_members WHERE group_id = ?"
            );

            getCount = cqlSession.prepare(
                    "SELECT count FROM chat.unread_messages WHERE user_id = ? AND type = ? AND sender_id = ?"
            );

            setCount = cqlSession.prepare(
                    "INSERT INTO chat.unread_messages " +
                            "(user_id, sender_id, type, messageType, content, send_time, message_id, count, session_message_id,group_id,direction) " +
                            "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)"
            );

            logger.info("Prepared statements initialized successfully");
        } catch (Exception e) {
            logger.error("Failed to initialize prepared statements", e);
            throw new RuntimeException("Failed to prepare CQL statements", e);
        }
    }

    private void initializeExecutorService() {
        executorService = Executors.newFixedThreadPool(threadPoolSize, r -> {
            Thread t = new Thread(r, "dispatcher-worker");
            t.setDaemon(true);
            return t;
        });
        logger.info("Executor service initialized with {} threads", threadPoolSize);
    }

    private void initializeKafkaConsumer() {
        Properties props = new Properties();
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        props.put(ConsumerConfig.GROUP_ID_CONFIG, consumerGroupId);
        props.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, false);
        props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
        props.put(ConsumerConfig.MAX_POLL_INTERVAL_MS_CONFIG, 300000); // 5 minutes
        props.put(ConsumerConfig.SESSION_TIMEOUT_MS_CONFIG, 30000); // 30 seconds
        props.put(ConsumerConfig.HEARTBEAT_INTERVAL_MS_CONFIG, 10000); // 10 seconds

        consumer = new KafkaConsumer<>(props, new StringDeserializer(), new StringDeserializer());
        consumer.subscribe(Arrays.asList("dispatch"));

        logger.info("Kafka consumer initialized and subscribed to 'dispatch' topic");
    }

    private void consumeMessages() {
        running.set(true);
        logger.info("Starting message consumption...");

        try {
            while (running.get()) {
                try {
                    ConsumerRecords<String, String> records = consumer.poll(Duration.ofMillis(pollTimeoutMs));

                    if (records.isEmpty()) {
                        continue;
                    }

                    logger.debug("Received {} records", records.count());

                    List<CompletableFuture<Void>> futures = new ArrayList<>();

                    for (ConsumerRecord<String, String> record : records) {
                        CompletableFuture<Void> future = CompletableFuture.runAsync(
                                () -> processRecord(record), executorService
                        );
                        futures.add(future);
                    }

                    // Wait for all tasks to complete
                    CompletableFuture<Void> allTasks = CompletableFuture.allOf(
                            futures.toArray(new CompletableFuture[0])
                    );

                    try {
                        allTasks.get(30, TimeUnit.SECONDS); // Timeout after 30 seconds
                        consumer.commitSync();
                        logger.debug("Successfully processed and committed {} records", records.count());
                    } catch (TimeoutException e) {
                        logger.error("Timeout waiting for record processing to complete", e);
                        // Don't commit if processing timed out
                    }

                } catch (WakeupException e) {
                    logger.info("Consumer wakeup called, shutting down...");
                    break;
                } catch (Exception e) {
                    logger.error("Error during message consumption", e);
                    // Continue processing other messages
                }
            }
        } finally {
            try {
                consumer.close(Duration.ofSeconds(5));
                logger.info("Kafka consumer closed");
            } catch (Exception e) {
                logger.error("Error closing Kafka consumer", e);
            }
        }
    }

    private void processRecord(ConsumerRecord<String, String> record) {
        try {
            logger.debug("Processing record: key={}, value={}", record.key(), record.value());

            if (record.value() == null || record.value().trim().isEmpty()) {
                logger.warn("Received empty message, skipping");
                return;
            }
            JSONObject jsonObject = JSON.parseObject(record.value());
            if (jsonObject == null || jsonObject.isEmpty() || jsonObject.get("type") == null) {
                throw new RuntimeException("Received empty message, skipping");
            }
            if (jsonObject.get("type").equals("single")) {
                processSingleMessage(jsonObject.toJavaObject(SingleMessage.class));
            }
            else if (jsonObject.get("type").equals("group")) {
                processGroupMessage(jsonObject.toJavaObject(GroupMessage.class));
            }






        } catch (Exception e) {
            logger.error("Error processing record: {}", record.value(), e);
            // Could implement dead letter queue here
        }
    }




    private void processSingleMessage(SingleMessage message) {
        logger.debug("Processing single message {} {}", message.getUserId1(), message.getUserId2());

        try {
            addUnreadMessage(message);

            sendToKafka("single", message.getUserId1() + "_" + message.getUserId2(), message);

            logger.debug("Successfully processed single message: {}", message.getMessageId());
        } catch (Exception e) {
            logger.error("Failed to process single message: {}", message.getMessageId(), e);
            throw e;
        }
    }

    private void processGroupMessage(GroupMessage message) throws ExecutionException, InterruptedException, TimeoutException {
        logger.debug("Processing group message from {} to group {}", message.getUserId(), message.getGroupId());

        try {
            // Insert group message record

            // Get group members and send individual messages
            ResultSet memberResult = cqlSession.execute(getMembers.bind(message.getGroupId()));
            List<Row> members = memberResult.all();

            if (members.isEmpty()) {
                logger.warn("No members found for group: {}", message.getGroupId());
                return;
            }

            List<CompletableFuture<Void>> memberTasks = new ArrayList<>();

            for (Row member : members) {
                String userId = member.getString("user_id");
                if (userId == null || userId.equals(message.getUserId())) {
                    continue;
                }

                CompletableFuture<Void> memberTask = CompletableFuture.runAsync(() -> {
                    try {
                        message.setUserId(userId);


                        addUnreadMessage(message);
                        sendToKafka("single", userId, message);

                        logger.debug("Sent group message to member: {}", userId);
                    } catch (Exception e) {
                        logger.error("Failed to send message to group member: {}", userId, e);
                    }
                }, executorService);

                memberTasks.add(memberTask);
            }

            // Wait for all member notifications to complete
            CompletableFuture.allOf(memberTasks.toArray(new CompletableFuture[0]))
                    .get(30, TimeUnit.SECONDS);

            logger.debug("Successfully processed group message: {}", message.getMessageId());
        } catch (Exception e) {
            logger.error("Failed to process group message: {}", message.getMessageId(), e);
            throw e;
        }
    }

    private void addUnreadMessage(GroupMessage message) {
        DistributedLockService.LockToken lockToken = null;

        try {
            lockToken = acquireLockWithTimeout(message.getReceiverId(), LOCK_TIMEOUT_MS);

            ResultSet result = cqlSession.execute(getCount.bind(
                    message.getReceiverId(), "group", message.getUserId()
            ));

            List<UnreadMessage> unreadMessages = UnreadMessageParser.parseToUnreadMessage(result);
            UnreadMessage unreadMessage = unreadMessages.isEmpty() ?
                    new UnreadMessage() : unreadMessages.get(0);

            long count = 0;
            if (!unreadMessages.isEmpty()) {
                count = unreadMessages.get(0).getCount();

            }

            cqlSession.execute(setCount.bind(
                    message.getReceiverId(),
                    message.getUserId(),
                    "single",
                    message.getMessageType() != null ? message.getMessageType() : "text",
                    message.getContent(),
                    message.getSendTime(),
                    message,
                    count + 1,
                    message.getSessionMessageId(),
                    message.getGroupId(),
                    false
            ));



            logger.debug("Updated unread count for user {} from sender {}: {}",
                    message.getReceiverId(), message.getUserId(), unreadMessage.getCount());

        } catch (Exception e) {
            logger.error("Failed to add unread message for user: {}", message.getReceiverId(), e);
            throw e;
        } finally {
            if (lockToken != null) {
                try {
                    distributedLockService.releaseLock(lockToken);
                } catch (Exception e) {
                    logger.error("Failed to release lock for user: {}", message.getReceiverId(), e);
                }
            }
        }
    }

    private void addUnreadMessage(SingleMessage message) {
        DistributedLockService.LockToken lockToken = null;

        Boolean flag = message.getDirection();
        String sender = flag ? message.getUserId1() : message.getUserId2();
        String receiver = flag ? message.getUserId2() : message.getUserId1();

        try {
            lockToken = acquireLockWithTimeout(receiver, LOCK_TIMEOUT_MS);

            ResultSet result = cqlSession.execute(getCount.bind(
                    receiver, "single", sender
            ));

            List<UnreadMessage> unreadMessages = UnreadMessageParser.parseToUnreadMessage(result);

            long count = 0;
            if (!unreadMessages.isEmpty()) {
                count = unreadMessages.get(0).getCount();

            }

            cqlSession.execute(setCount.bind(
                    receiver,
                    sender,
                    "single",
                    message.getMessageType() != null ? message.getMessageType() : "text",
                    message.getContent(),
                    message.getTime(),
                    message,
                    count + 1,
                    message.getSessionMessageId(),
                    0l,
                    message.getDirection()
            ));


        } catch (Exception e) {
            logger.error("Failed to add unread message for user: {}", receiver, e);
            throw e;
        } finally {
            if (lockToken != null) {
                try {
                    distributedLockService.releaseLock(lockToken);
                } catch (Exception e) {
                    logger.error("Failed to release lock for user: {}", receiver, e);
                }
            }
        }
    }

    private DistributedLockService.LockToken acquireLockWithTimeout(String userId, long timeoutMs) {
        long startTime = System.currentTimeMillis();

        while (System.currentTimeMillis() - startTime < timeoutMs) {
            DistributedLockService.LockToken lockToken = distributedLockService.acquireLock(userId);
            if (lockToken != null) {
                return lockToken;
            }

            try {
                Thread.sleep(100); // Wait 100ms before retry
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new RuntimeException("Interrupted while waiting for lock", e);
            }
        }

        throw new RuntimeException("Failed to acquire lock for user: " + userId + " within timeout: " + timeoutMs + "ms");
    }

    private void sendToKafka(String topic, String key, SingleMessage message) {
        try {
            String messageJson = JSON.toJSONString(message);
            ProducerRecord<String, String> record = new ProducerRecord<>(topic, key, messageJson);

            producer.send(record, (metadata, exception) -> {
                if (exception != null) {
                    logger.error("Failed to send message to Kafka topic {}: {}", topic, exception.getMessage());
                } else {
                    logger.debug("Message sent to Kafka topic {} partition {} offset {}",
                            metadata.topic(), metadata.partition(), metadata.offset());
                }
            });
        } catch (Exception e) {
            logger.error("Error sending message to Kafka topic {}: {}", topic, e.getMessage(), e);
            throw e;
        }
    }

    private void sendToKafka(String topic, String key, GroupMessage message) {
        try {
            String messageJson = JSON.toJSONString(message);
            ProducerRecord<String, String> record = new ProducerRecord<>(topic, key, messageJson);

            producer.send(record, (metadata, exception) -> {
                if (exception != null) {
                    logger.error("Failed to send message to Kafka topic {}: {}", topic, exception.getMessage());
                } else {
                    logger.debug("Message sent to Kafka topic {} partition {} offset {}",
                            metadata.topic(), metadata.partition(), metadata.offset());
                }
            });
        } catch (Exception e) {
            logger.error("Error sending message to Kafka topic {}: {}", topic, e.getMessage(), e);
            throw e;
        }
    }

    @PreDestroy
    public void shutdown() {
        logger.info("Shutting down DispatcherAutoRunner...");

        running.set(false);

        if (consumer != null) {
            consumer.wakeup();
        }

        if (consumerTask != null) {
            try {
                consumerTask.get(10, TimeUnit.SECONDS);
            } catch (Exception e) {
                logger.error("Error waiting for consumer task to complete", e);
            }
        }

        if (executorService != null) {
            executorService.shutdown();
            try {
                if (!executorService.awaitTermination(10, TimeUnit.SECONDS)) {
                    executorService.shutdownNow();
                }
            } catch (InterruptedException e) {
                executorService.shutdownNow();
                Thread.currentThread().interrupt();
            }
        }

        logger.info("DispatcherAutoRunner shutdown complete");
    }
}