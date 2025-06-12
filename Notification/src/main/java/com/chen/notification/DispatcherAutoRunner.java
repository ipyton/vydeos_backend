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
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;

@Component
@Profile("dispatcher")
public class DispatcherAutoRunner {

    private static final Logger logger = LoggerFactory.getLogger(DispatcherAutoRunner.class);
    private static final Logger performanceLogger = LoggerFactory.getLogger("PERFORMANCE." + DispatcherAutoRunner.class.getName());
    private static final Logger kafkaLogger = LoggerFactory.getLogger("KAFKA." + DispatcherAutoRunner.class.getName());
    private static final Logger lockLogger = LoggerFactory.getLogger("LOCK." + DispatcherAutoRunner.class.getName());

    private static final int MAX_RETRY_ATTEMPTS = 3;
    private static final long RETRY_DELAY_MS = 1000;
    private static final long LOCK_TIMEOUT_MS = 5000;

    // Performance metrics
    private final AtomicLong totalMessagesProcessed = new AtomicLong(0);
    private final AtomicLong totalSingleMessages = new AtomicLong(0);
    private final AtomicLong totalGroupMessages = new AtomicLong(0);
    private final AtomicLong totalErrors = new AtomicLong(0);
    private volatile long lastMetricsLogTime = System.currentTimeMillis();

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
        long startTime = System.currentTimeMillis();
        logger.info("=== Starting DispatcherAutoRunner initialization ===");
        logger.info("Configuration: threadPoolSize={}, consumerGroupId={}, bootstrapServers={}, pollTimeoutMs={}",
                threadPoolSize, consumerGroupId, bootstrapServers, pollTimeoutMs);

        try {
            initializePreparedStatements();
            initializeExecutorService();
            initializeKafkaConsumer();

            // Start consumer in a separate thread
            consumerTask = CompletableFuture.runAsync(this::consumeMessages);

            long initTime = System.currentTimeMillis() - startTime;
            logger.info("=== DispatcherAutoRunner initialized successfully in {}ms ===", initTime);
            performanceLogger.info("Initialization completed in {}ms", initTime);

            // Schedule periodic metrics logging
            scheduleMetricsLogging();

        } catch (Exception e) {
            logger.error("=== CRITICAL: Failed to initialize DispatcherAutoRunner after {}ms ===",
                    System.currentTimeMillis() - startTime, e);
            throw new RuntimeException("Initialization failed", e);
        }
    }

    private void initializePreparedStatements() {
        long startTime = System.currentTimeMillis();
        logger.debug("Initializing prepared statements...");

        try {
            getMembers = cqlSession.prepare(
                    "SELECT * FROM group_chat.chat_group_members WHERE group_id = ?"
            );
            logger.debug("Prepared statement 'getMembers' initialized");

            getCount = cqlSession.prepare(
                    "SELECT count FROM chat.unread_messages WHERE user_id = ? AND type = ? AND sender_id = ?"
            );
            logger.debug("Prepared statement 'getCount' initialized");

            setCount = cqlSession.prepare(
                    "INSERT INTO chat.unread_messages " +
                            "(user_id, sender_id, type, messageType, content, send_time, message_id, count, session_message_id,group_id) " +
                            "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)"
            );
            logger.debug("Prepared statement 'setCount' initialized");

            long duration = System.currentTimeMillis() - startTime;
            logger.info("All prepared statements initialized successfully in {}ms", duration);

        } catch (Exception e) {
            logger.error("CRITICAL: Failed to initialize prepared statements after {}ms",
                    System.currentTimeMillis() - startTime, e);
            throw new RuntimeException("Failed to prepare CQL statements", e);
        }
    }

    private void initializeExecutorService() {
        logger.debug("Initializing executor service with {} threads", threadPoolSize);

        executorService = Executors.newFixedThreadPool(threadPoolSize, r -> {
            Thread t = new Thread(r, "dispatcher-worker");
            t.setDaemon(true);
            t.setUncaughtExceptionHandler((thread, ex) -> {
                logger.error("Uncaught exception in dispatcher worker thread: {}", thread.getName(), ex);
            });
            return t;
        });

        logger.info("Executor service initialized with {} threads", threadPoolSize);
    }

    private void initializeKafkaConsumer() {
        logger.debug("Initializing Kafka consumer...");

        Properties props = new Properties();
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        props.put(ConsumerConfig.GROUP_ID_CONFIG, consumerGroupId);
        props.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, false);
        props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
        props.put(ConsumerConfig.MAX_POLL_INTERVAL_MS_CONFIG, 300000); // 5 minutes
        props.put(ConsumerConfig.SESSION_TIMEOUT_MS_CONFIG, 30000); // 30 seconds
        props.put(ConsumerConfig.HEARTBEAT_INTERVAL_MS_CONFIG, 10000); // 10 seconds

        logger.debug("Kafka consumer properties: {}", props);

        try {
            consumer = new KafkaConsumer<>(props, new StringDeserializer(), new StringDeserializer());
            consumer.subscribe(Arrays.asList("dispatch"));

            kafkaLogger.info("Kafka consumer initialized and subscribed to 'dispatch' topic with group '{}'", consumerGroupId);
            logger.info("Kafka consumer initialization completed");

        } catch (Exception e) {
            logger.error("CRITICAL: Failed to initialize Kafka consumer", e);
            throw new RuntimeException("Kafka consumer initialization failed", e);
        }
    }

    private void consumeMessages() {
        running.set(true);
        logger.info("=== Starting message consumption loop ===");
        kafkaLogger.info("Message consumption started for topic 'dispatch'");

        try {
            while (running.get()) {
                long pollStartTime = System.currentTimeMillis();

                try {
                    ConsumerRecords<String, String> records = consumer.poll(Duration.ofMillis(pollTimeoutMs));
                    long pollDuration = System.currentTimeMillis() - pollStartTime;

                    if (records.isEmpty()) {
                        logger.trace("No records received in poll ({}ms)", pollDuration);
                        continue;
                    }

                    int recordCount = records.count();
                    kafkaLogger.info("Received {} records from Kafka in {}ms", recordCount, pollDuration);
                    logger.debug("Processing batch of {} records", recordCount);

                    long processingStartTime = System.currentTimeMillis();
                    List<CompletableFuture<Void>> futures = new ArrayList<>();

                    for (ConsumerRecord<String, String> record : records) {
                        CompletableFuture<Void> future = CompletableFuture.runAsync(
                                () -> {
                                    // Set MDC for this record processing
                                    MDC.put("recordKey", record.key());
                                    MDC.put("recordOffset", String.valueOf(record.offset()));
                                    MDC.put("recordPartition", String.valueOf(record.partition()));

                                    try {
                                        processRecord(record);
                                    } catch (ExecutionException e) {
                                        throw new RuntimeException(e);
                                    } catch (InterruptedException e) {
                                        throw new RuntimeException(e);
                                    } catch (TimeoutException e) {
                                        throw new RuntimeException(e);
                                    } finally {
                                        MDC.clear();
                                    }
                                }, executorService
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

                        long batchProcessingTime = System.currentTimeMillis() - processingStartTime;
                        totalMessagesProcessed.addAndGet(recordCount);

                        kafkaLogger.info("Successfully processed and committed {} records in {}ms (avg: {}ms/record)",
                                recordCount, batchProcessingTime, batchProcessingTime / recordCount);
                        performanceLogger.debug("Batch processing metrics: records={}, totalTime={}ms, avgPerRecord={}ms",
                                recordCount, batchProcessingTime, batchProcessingTime / recordCount);

                    } catch (TimeoutException e) {
                        logger.error("TIMEOUT: Processing {} records took longer than 30 seconds, skipping commit", recordCount);
                        kafkaLogger.error("Batch processing timeout for {} records", recordCount, e);
                        // Don't commit if processing timed out
                    }

                } catch (WakeupException e) {
                    logger.info("Consumer wakeup called, shutting down gracefully...");
                    kafkaLogger.info("Kafka consumer received wakeup signal");
                    break;
                } catch (Exception e) {
                    totalErrors.incrementAndGet();
                    logger.error("ERROR: Exception during message consumption batch", e);
                    kafkaLogger.error("Kafka consumption error", e);
                    // Continue processing other messages
                }
            }
        } finally {
            try {
                consumer.close(Duration.ofSeconds(5));
                kafkaLogger.info("Kafka consumer closed successfully");
                logger.info("Message consumption loop ended");
            } catch (Exception e) {
                logger.error("Error closing Kafka consumer", e);
                kafkaLogger.error("Failed to close Kafka consumer gracefully", e);
            }
        }
    }

    private void processRecord(ConsumerRecord<String, String> record) throws ExecutionException, InterruptedException, TimeoutException {
        long startTime = System.currentTimeMillis();
        String messageType = "unknown";

        try {
            logger.debug("Processing record: partition={}, offset={}, key={}",
                    record.partition(), record.offset(), record.key());

            if (record.value() == null || record.value().trim().isEmpty()) {
                logger.warn("Received empty message at partition={}, offset={}, skipping",
                        record.partition(), record.offset());
                return;
            }

            JSONObject jsonObject;
            try {
                jsonObject = JSON.parseObject(record.value());
            } catch (Exception e) {
                logger.error("Failed to parse JSON from record at partition={}, offset={}: {}",
                        record.partition(), record.offset(), record.value(), e);
                throw new RuntimeException("JSON parsing failed", e);
            }

            if (jsonObject == null || jsonObject.isEmpty() || jsonObject.get("type") == null) {
                logger.error("Invalid message structure at partition={}, offset={}: missing or null 'type' field",
                        record.partition(), record.offset());
                throw new RuntimeException("Invalid message structure: missing type field");
            }

            messageType = jsonObject.getString("type");
            MDC.put("messageType", messageType);

            if ("single".equals(messageType)) {
                SingleMessage singleMessage = jsonObject.toJavaObject(SingleMessage.class);
                MDC.put("messageId", singleMessage.getMessageId());
                MDC.put("senderId", singleMessage.getDirection() ? singleMessage.getUserId1() : singleMessage.getUserId2());

                logger.debug("Processing single message: messageId={}, users={}_{}",
                        singleMessage.getMessageId(), singleMessage.getUserId1(), singleMessage.getUserId2());

                processSingleMessage(singleMessage);
                totalSingleMessages.incrementAndGet();

            } else if ("group".equals(messageType)) {
                GroupMessage groupMessage = jsonObject.toJavaObject(GroupMessage.class);
                MDC.put("messageId", String.valueOf(groupMessage.getMessageId()));
                MDC.put("senderId", groupMessage.getUserId());
                MDC.put("groupId", String.valueOf(groupMessage.getGroupId()));

                logger.debug("Processing group message: messageId={}, senderId={}, groupId={}",
                        groupMessage.getMessageId(), groupMessage.getUserId(), groupMessage.getGroupId());

                processGroupMessage(groupMessage);
                totalGroupMessages.incrementAndGet();

            } else {
                logger.error("Unknown message type '{}' at partition={}, offset={}",
                        messageType, record.partition(), record.offset());
                throw new RuntimeException("Unknown message type: " + messageType);
            }

            long processingTime = System.currentTimeMillis() - startTime;
            performanceLogger.debug("Record processed successfully: type={}, time={}ms", messageType, processingTime);

            if (processingTime > 1000) { // Log slow processing
                logger.warn("SLOW PROCESSING: Record took {}ms to process (type={})", processingTime, messageType);
            }

        } catch (Exception e) {
            totalErrors.incrementAndGet();
            long processingTime = System.currentTimeMillis() - startTime;

            logger.error("FAILED to process record after {}ms: partition={}, offset={}, type={}, value={}",
                    processingTime, record.partition(), record.offset(), messageType,
                    record.value().length() > 200 ? record.value().substring(0, 200) + "..." : record.value(), e);

            // Could implement dead letter queue here
            throw e;
        } finally {
            MDC.remove("messageType");
            MDC.remove("messageId");
            MDC.remove("senderId");
            MDC.remove("groupId");
        }
    }

    private void processSingleMessage(SingleMessage message) {
        long startTime = System.currentTimeMillis();
        String sender = message.getDirection() ? message.getUserId1() : message.getUserId2();
        String receiver = message.getDirection() ? message.getUserId2() : message.getUserId1();

        logger.debug("Processing single message: messageId={}, sender={}, receiver={}",
                message.getMessageId(), sender, receiver);

        try {
            long unreadStartTime = System.currentTimeMillis();
            addUnreadMessage(message);
            long unreadTime = System.currentTimeMillis() - unreadStartTime;

            logger.debug("Unread message added in {}ms for receiver={}", unreadTime, receiver);

            long kafkaStartTime = System.currentTimeMillis();
            sendToKafka("single", sender + "_" + receiver, message);
            long kafkaTime = System.currentTimeMillis() - kafkaStartTime;

            logger.debug("Message sent to Kafka in {}ms", kafkaTime);

            long totalTime = System.currentTimeMillis() - startTime;
            logger.info("Single message processed successfully: messageId={}, sender={}, receiver={}, totalTime={}ms",
                    message.getMessageId(), sender, receiver, totalTime);

            performanceLogger.debug("Single message performance: messageId={}, unreadTime={}ms, kafkaTime={}ms, totalTime={}ms",
                    message.getMessageId(), unreadTime, kafkaTime, totalTime);

        } catch (Exception e) {
            long processingTime = System.currentTimeMillis() - startTime;
            logger.error("FAILED to process single message after {}ms: messageId={}, sender={}, receiver={}",
                    processingTime, message.getMessageId(), sender, receiver, e);
            throw e;
        }
    }

    private void processGroupMessage(GroupMessage message) throws ExecutionException, InterruptedException, TimeoutException {
        long startTime = System.currentTimeMillis();

        logger.info("Processing group message: messageId={}, senderId={}, groupId={}",
                message.getMessageId(), message.getUserId(), message.getGroupId());

        try {
            // Get group members
            long memberQueryStart = System.currentTimeMillis();
            ResultSet memberResult = cqlSession.execute(getMembers.bind(message.getGroupId()));
            List<Row> members = memberResult.all();
            long memberQueryTime = System.currentTimeMillis() - memberQueryStart;

            if (members.isEmpty()) {
                logger.warn("No members found for group: {}, messageId={}", message.getGroupId(), message.getMessageId());
                return;
            }

            int memberCount = members.size();
            int recipientCount = memberCount - 1; // excluding sender

            logger.info("Group message distribution: groupId={}, totalMembers={}, recipients={}, memberQueryTime={}ms",
                    message.getGroupId(), memberCount, recipientCount, memberQueryTime);

            List<CompletableFuture<Void>> memberTasks = new ArrayList<>();
            List<String> recipients = new ArrayList<>();

            for (Row member : members) {
                String userId = member.getString("user_id");
                if (userId == null || userId.equals(message.getUserId())) {
                    continue;
                }

                recipients.add(userId);
                CompletableFuture<Void> memberTask = CompletableFuture.runAsync(() -> {
                    long memberStartTime = System.currentTimeMillis();

                    // Set MDC for member processing
                    MDC.put("recipientId", userId);

                    try {
                        // Create a copy for this recipient
                        GroupMessage memberMessage = new GroupMessage();
                        // Copy all properties from original message
                        memberMessage.setMessageId(message.getMessageId());
                        memberMessage.setContent(message.getContent());
                        memberMessage.setMessageType(message.getMessageType());
                        memberMessage.setSendTime(message.getSendTime());
                        memberMessage.setGroupId(message.getGroupId());
                        memberMessage.setSessionMessageId(message.getSessionMessageId());
                        memberMessage.setUserId(message.getUserId()); // Keep original sender
                        memberMessage.setReceiverId(userId); // Set recipient

                        addUnreadMessage(memberMessage);
                        sendToKafka("single", userId, memberMessage);

                        long memberTime = System.currentTimeMillis() - memberStartTime;
                        logger.debug("Group message sent to member: userId={}, time={}ms", userId, memberTime);

                        if (memberTime > 500) {
                            logger.warn("SLOW member processing: userId={}, time={}ms", userId, memberTime);
                        }

                    } catch (Exception e) {
                        logger.error("FAILED to send group message to member: userId={}, groupId={}, messageId={}",
                                userId, message.getGroupId(), message.getMessageId(), e);
                        throw new RuntimeException("Member processing failed", e);
                    } finally {
                        MDC.remove("recipientId");
                    }
                }, executorService);

                memberTasks.add(memberTask);
            }

            // Wait for all member notifications to complete
            long distributionStartTime = System.currentTimeMillis();
            CompletableFuture.allOf(memberTasks.toArray(new CompletableFuture[0]))
                    .get(30, TimeUnit.SECONDS);
            long distributionTime = System.currentTimeMillis() - distributionStartTime;

            long totalTime = System.currentTimeMillis() - startTime;

            logger.info("Group message processed successfully: messageId={}, groupId={}, recipients={}, " +
                            "memberQueryTime={}ms, distributionTime={}ms, totalTime={}ms",
                    message.getMessageId(), message.getGroupId(), recipientCount,
                    memberQueryTime, distributionTime, totalTime);

            performanceLogger.info("Group message performance: messageId={}, groupId={}, recipients={}, " +
                            "avgPerMember={}ms, totalTime={}ms",
                    message.getMessageId(), message.getGroupId(), recipientCount,
                    recipientCount > 0 ? distributionTime / recipientCount : 0, totalTime);

        } catch (Exception e) {
            long processingTime = System.currentTimeMillis() - startTime;
            logger.error("FAILED to process group message after {}ms: messageId={}, groupId={}, senderId={}",
                    processingTime, message.getMessageId(), message.getGroupId(), message.getUserId(), e);
            throw e;
        }
    }

    private void addUnreadMessage(GroupMessage message) {
        long startTime = System.currentTimeMillis();
        DistributedLockService.LockToken lockToken = null;
        String receiverId = message.getReceiverId();
        String senderId = message.getUserId();

        lockLogger.debug("Attempting to acquire lock for user: {}", receiverId);

        try {
            long lockStartTime = System.currentTimeMillis();
            lockToken = acquireLockWithTimeout(receiverId, LOCK_TIMEOUT_MS);
            long lockAcquisitionTime = System.currentTimeMillis() - lockStartTime;

            lockLogger.debug("Lock acquired for user: {} in {}ms", receiverId, lockAcquisitionTime);

            long queryStartTime = System.currentTimeMillis();
            ResultSet result = cqlSession.execute(getCount.bind(receiverId, "group", senderId));
            List<UnreadMessage> unreadMessages = UnreadMessageParser.parseToUnreadMessage(result);
            long queryTime = System.currentTimeMillis() - queryStartTime;

            int oldCount = 0;
            if (!unreadMessages.isEmpty()) {
                oldCount = unreadMessages.get(0).getCount();
            }
            int newCount = oldCount + 1;

            long updateStartTime = System.currentTimeMillis();
            cqlSession.execute(setCount.bind(
                    receiverId,
                    senderId,
                    "single", // Note: This seems to be intentionally "single" even for group messages
                    message.getMessageType() != null ? message.getMessageType() : "text",
                    message.getContent(),
                    message.getSendTime(),
                    message.getMessageId(),
                    newCount,
                    message.getSessionMessageId(),
                    message.getGroupId()));
            long updateTime = System.currentTimeMillis() - updateStartTime;

            long totalTime = System.currentTimeMillis() - startTime;

            logger.debug("Unread count updated for group message: receiver={}, sender={}, groupId={}, " +
                            "oldCount={}, newCount={}, lockTime={}ms, queryTime={}ms, updateTime={}ms, totalTime={}ms",
                    receiverId, senderId, message.getGroupId(), oldCount, newCount,
                    lockAcquisitionTime, queryTime, updateTime, totalTime);

        } catch (Exception e) {
            long processingTime = System.currentTimeMillis() - startTime;
            logger.error("FAILED to add unread group message after {}ms: receiver={}, sender={}, groupId={}",
                    processingTime, receiverId, senderId, message.getGroupId(), e);
            throw e;
        } finally {
            if (lockToken != null) {
                try {
                    long releaseStartTime = System.currentTimeMillis();
                    distributedLockService.releaseLock(lockToken);
                    long releaseTime = System.currentTimeMillis() - releaseStartTime;

                    lockLogger.debug("Lock released for user: {} in {}ms", receiverId, releaseTime);
                } catch (Exception e) {
                    lockLogger.error("FAILED to release lock for user: {}", receiverId, e);
                }
            }
        }
    }

    private void addUnreadMessage(SingleMessage message) {
        long startTime = System.currentTimeMillis();
        DistributedLockService.LockToken lockToken = null;

        Boolean flag = message.getDirection();
        String sender = flag ? message.getUserId1() : message.getUserId2();
        String receiver = flag ? message.getUserId2() : message.getUserId1();

        lockLogger.debug("Attempting to acquire lock for user: {}", receiver);

        try {
            long lockStartTime = System.currentTimeMillis();
            lockToken = acquireLockWithTimeout(receiver, LOCK_TIMEOUT_MS);
            long lockAcquisitionTime = System.currentTimeMillis() - lockStartTime;

            lockLogger.debug("Lock acquired for user: {} in {}ms", receiver, lockAcquisitionTime);

            long queryStartTime = System.currentTimeMillis();
            ResultSet result = cqlSession.execute(getCount.bind(receiver, "single", sender));
            List<UnreadMessage> unreadMessages = UnreadMessageParser.parseToUnreadMessage(result);
            long queryTime = System.currentTimeMillis() - queryStartTime;

            int oldCount = 0;
            if (!unreadMessages.isEmpty()) {
                oldCount = unreadMessages.get(0).getCount();
            }
            int newCount = oldCount + 1;

            long updateStartTime = System.currentTimeMillis();
            cqlSession.execute(setCount.bind(
                    receiver,
                    sender,
                    "single",
                    message.getMessageType() != null ? message.getMessageType() : "text",
                    message.getContent(),
                    message.getTime(),
                    message.getMessageId(),
                    newCount,
                    message.getSessionMessageId(),
                    0L
            ));
            long updateTime = System.currentTimeMillis() - updateStartTime;

            long totalTime = System.currentTimeMillis() - startTime;

            logger.debug("Unread count updated for single message: receiver={}, sender={}, " +
                            "oldCount={}, newCount={}, lockTime={}ms, queryTime={}ms, updateTime={}ms, totalTime={}ms",
                    receiver, sender, oldCount, newCount,
                    lockAcquisitionTime, queryTime, updateTime, totalTime);

        } catch (Exception e) {
            long processingTime = System.currentTimeMillis() - startTime;
            logger.error("FAILED to add unread single message after {}ms: receiver={}, sender={}",
                    processingTime, receiver, sender, e);
            throw e;
        } finally {
            if (lockToken != null) {
                try {
                    long releaseStartTime = System.currentTimeMillis();
                    distributedLockService.releaseLock(lockToken);
                    long releaseTime = System.currentTimeMillis() - releaseStartTime;

                    lockLogger.debug("Lock released for user: {} in {}ms", receiver, releaseTime);
                } catch (Exception e) {
                    lockLogger.error("FAILED to release lock for user: {}", receiver, e);
                }
            }
        }
    }

    private DistributedLockService.LockToken acquireLockWithTimeout(String userId, long timeoutMs) {
        long startTime = System.currentTimeMillis();
        int attempts = 0;

        lockLogger.debug("Acquiring lock for user: {} with timeout: {}ms", userId, timeoutMs);

        while (System.currentTimeMillis() - startTime < timeoutMs) {
            attempts++;
            long attemptStartTime = System.currentTimeMillis();

            DistributedLockService.LockToken lockToken = distributedLockService.acquireLock(userId);
            long attemptTime = System.currentTimeMillis() - attemptStartTime;

            if (lockToken != null) {
                long totalTime = System.currentTimeMillis() - startTime;
                lockLogger.debug("Lock acquired for user: {} on attempt {} in {}ms (total: {}ms)",
                        userId, attempts, attemptTime, totalTime);
                return lockToken;
            }

            lockLogger.debug("Lock acquisition attempt {} failed for user: {} in {}ms",
                    attempts, userId, attemptTime);

            try {
                Thread.sleep(100); // Wait 100ms before retry
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                lockLogger.error("Interrupted while waiting for lock: {}", userId, e);
                throw new RuntimeException("Interrupted while waiting for lock", e);
            }
        }

        long totalTime = System.currentTimeMillis() - startTime;
        lockLogger.error("FAILED to acquire lock for user: {} after {} attempts in {}ms (timeout: {}ms)",
                userId, attempts, totalTime, timeoutMs);

        throw new RuntimeException("Failed to acquire lock for user: " + userId +
                " within timeout: " + timeoutMs + "ms after " + attempts + " attempts");
    }

    private void sendToKafka(String topic, String key, SingleMessage message) {
        long startTime = System.currentTimeMillis();

        try {
            String messageJson = JSON.toJSONString(message);
            ProducerRecord<String, String> record = new ProducerRecord<>(topic, key, messageJson);

            kafkaLogger.debug("Sending single message to Kafka: topic={}, key={}, messageId={}",
                    topic, key, message.getMessageId());

            producer.send(record, (metadata, exception) -> {
                long sendTime = System.currentTimeMillis() - startTime;

                if (exception != null) {
                    kafkaLogger.error("FAILED to send single message to Kafka: topic={}, key={}, messageId={}, time={}ms",
                            topic, key, message.getMessageId(), sendTime, exception);
                    logger.error("Kafka send failed for single message: {}", message.getMessageId(), exception);
                } else {
                    kafkaLogger.debug("Single message sent successfully: topic={}, partition={}, offset={}, key={}, messageId={}, time={}ms",
                            metadata.topic(), metadata.partition(), metadata.offset(), key, message.getMessageId(), sendTime);

                    if (sendTime > 1000) {
                        kafkaLogger.warn("SLOW Kafka send: messageId={}, time={}ms", message.getMessageId(), sendTime);
                    }
                }
            });

        } catch (Exception e) {
            long sendTime = System.currentTimeMillis() - startTime;
            kafkaLogger.error("Exception sending single message to Kafka after {}ms: topic={}, key={}, messageId={}",
                    sendTime, topic, key, message.getMessageId(), e);
            logger.error("Error sending single message to Kafka: {}", message.getMessageId(), e);
            throw e;
        }
    }

    private void sendToKafka(String topic, String key, GroupMessage message) {
        long startTime = System.currentTimeMillis();

        try {
            String messageJson = JSON.toJSONString(message);
            ProducerRecord<String, String> record = new ProducerRecord<>(topic, key, messageJson);

            kafkaLogger.debug("Sending group message to Kafka: topic={}, key={}, messageId={}, groupId={}",
                    topic, key, message.getMessageId(), message.getGroupId());

            producer.send(record, (metadata, exception) -> {
                long sendTime = System.currentTimeMillis() - startTime;

                if (exception != null) {
                    kafkaLogger.error("FAILED to send group message to Kafka: topic={}, key={}, messageId={}, groupId={}, time={}ms",
                            topic, key, message.getMessageId(), message.getGroupId(), sendTime, exception);
                    logger.error("Kafka send failed for group message: {}", message.getMessageId(), exception);
                } else {
                    kafkaLogger.debug("Group message sent successfully: topic={}, partition={}, offset={}, key={}, messageId={}, groupId={}, time={}ms",
                            metadata.topic(), metadata.partition(), metadata.offset(), key,
                            message.getMessageId(), message.getGroupId(), sendTime);

                    if (sendTime > 1000) {
                        kafkaLogger.warn("SLOW Kafka send: messageId={}, time={}ms", message.getMessageId(), sendTime);
                    }
                }
            });

        } catch (Exception e) {
            long sendTime = System.currentTimeMillis() - startTime;
            kafkaLogger.error("Exception sending group message to Kafka after {}ms: topic={}, key={}, messageId={}, groupId={}",
                    sendTime, topic, key, message.getMessageId(), message.getGroupId(), e);
            logger.error("Error sending group message to Kafka: {}", message.getMessageId(), e);
            throw e;
        }
    }

    private void scheduleMetricsLogging() {
        ScheduledExecutorService metricsExecutor = Executors.newSingleThreadScheduledExecutor(r -> {
            Thread t = new Thread(r, "metrics-logger");
            t.setDaemon(true);
            return t;
        });

        metricsExecutor.scheduleAtFixedRate(() -> {
            try {
                logPerformanceMetrics();
            } catch (Exception e) {
                logger.error("Error logging performance metrics", e);
            }
        }, 60, 60, TimeUnit.SECONDS); // Log every minute

        logger.info("Performance metrics logging scheduled every 60 seconds");
    }

    private void logPerformanceMetrics() {
        long currentTime = System.currentTimeMillis();
        long timeSinceLastLog = currentTime - lastMetricsLogTime;

        long totalProcessed = totalMessagesProcessed.get();
        long totalSingle = totalSingleMessages.get();
        long totalGroup = totalGroupMessages.get();
        long totalErr = totalErrors.get();

        double messagesPerSecond = timeSinceLastLog > 0 ? (totalProcessed * 1000.0) / timeSinceLastLog : 0;
        double errorRate = totalProcessed > 0 ? (totalErr * 100.0) / totalProcessed : 0;

        performanceLogger.info("=== PERFORMANCE METRICS ===");
        performanceLogger.info("Total messages processed: {}", totalProcessed);
        performanceLogger.info("Single messages: {} ({}%)", totalSingle,
                totalProcessed > 0 ? (totalSingle * 100 / totalProcessed) : 0);
        performanceLogger.info("Group messages: {} ({}%)", totalGroup,
                totalProcessed > 0 ? (totalGroup * 100 / totalProcessed) : 0);
        performanceLogger.info("Total errors: {} ({}%)", totalErr, String.format("%.2f", errorRate));
        performanceLogger.info("Messages per second: {}", String.format("%.2f", messagesPerSecond));
        performanceLogger.info("Thread pool active threads: {}",
                executorService instanceof ThreadPoolExecutor ?
                        ((ThreadPoolExecutor) executorService).getActiveCount() : "unknown");
        performanceLogger.info("===============================");

        // Log summary to main logger as well
        logger.info("Performance summary: processed={}, single={}, group={}, errors={}, rate={}msg/s, errorRate={}%",
                totalProcessed, totalSingle, totalGroup, totalErr,
                String.format("%.2f", messagesPerSecond), String.format("%.2f", errorRate));

        lastMetricsLogTime = currentTime;
    }

    @PreDestroy
    public void shutdown() {
        long shutdownStartTime = System.currentTimeMillis();
        logger.info("=== Starting DispatcherAutoRunner shutdown ===");

        running.set(false);

        // Wake up consumer
        if (consumer != null) {
            logger.info("Waking up Kafka consumer...");
            consumer.wakeup();
        }

        // Wait for consumer task to complete
        if (consumerTask != null) {
            try {
                logger.info("Waiting for consumer task to complete...");
                consumerTask.get(10, TimeUnit.SECONDS);
                logger.info("Consumer task completed successfully");
            } catch (TimeoutException e) {
                logger.warn("Consumer task did not complete within 10 seconds");
            } catch (Exception e) {
                logger.error("Error waiting for consumer task to complete", e);
            }
        }

        // Shutdown executor service
        if (executorService != null) {
            logger.info("Shutting down executor service...");
            executorService.shutdown();
            try {
                if (!executorService.awaitTermination(10, TimeUnit.SECONDS)) {
                    logger.warn("Executor service did not terminate within 10 seconds, forcing shutdown...");
                    executorService.shutdownNow();

                    if (!executorService.awaitTermination(5, TimeUnit.SECONDS)) {
                        logger.error("Executor service did not terminate after forced shutdown");
                    } else {
                        logger.info("Executor service terminated after forced shutdown");
                    }
                } else {
                    logger.info("Executor service terminated gracefully");
                }
            } catch (InterruptedException e) {
                logger.warn("Interrupted while waiting for executor service termination, forcing shutdown...");
                executorService.shutdownNow();
                Thread.currentThread().interrupt();
            }
        }

        // Log final metrics
        logPerformanceMetrics();

        long shutdownTime = System.currentTimeMillis() - shutdownStartTime;
        logger.info("=== DispatcherAutoRunner shutdown completed in {}ms ===", shutdownTime);

        // Final summary
        logger.info("Final statistics - Total processed: {}, Single: {}, Group: {}, Errors: {}",
                totalMessagesProcessed.get(), totalSingleMessages.get(),
                totalGroupMessages.get(), totalErrors.get());
    }
}