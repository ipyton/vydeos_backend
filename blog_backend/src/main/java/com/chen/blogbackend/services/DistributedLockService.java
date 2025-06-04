package com.chen.blogbackend.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.params.SetParams;

import java.util.Collections;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Supplier;

@Service
public class DistributedLockService {

    @Autowired
    private JedisPool jedisPool;

    // Thread-safe local lock management for same-JVM synchronization
    private static final ConcurrentHashMap<String, ReentrantLock> localLocks = new ConcurrentHashMap<>();
    private static final ConcurrentHashMap<String, LockToken> activeLocks = new ConcurrentHashMap<>();

    // Lua script for atomic lock release (only release if we own the lock)
    private static final String RELEASE_SCRIPT =
            "if redis.call('get', KEYS[1]) == ARGV[1] then " +
                    "    return redis.call('del', KEYS[1]) " +
                    "else " +
                    "    return 0 " +
                    "end";

    // Lua script for atomic lock renewal (only renew if we own the lock)
    private static final String RENEW_SCRIPT =
            "if redis.call('get', KEYS[1]) == ARGV[1] then " +
                    "    return redis.call('expire', KEYS[1], ARGV[2]) " +
                    "else " +
                    "    return 0 " +
                    "end";

    // Default values
    private static final int DEFAULT_TIMEOUT_SECONDS = 30;
    private static final int DEFAULT_RETRY_DELAY_MS = 100;
    private static final int DEFAULT_MAX_RETRIES = 100; // 10 seconds with 100ms delay

    /**
     * Get or create a local lock for the given key (thread-safe)
     */
    private static ReentrantLock getLocalLock(String lockKey) {
        return localLocks.computeIfAbsent(lockKey, k -> new ReentrantLock(true)); // fair lock
    }

    /**
     * Clean up unused local locks to prevent memory leaks
     */
    private static void cleanupLocalLock(String lockKey) {
        ReentrantLock lock = localLocks.get(lockKey);
        if (lock != null && !lock.hasQueuedThreads() && !lock.isLocked()) {
            localLocks.remove(lockKey, lock);
        }
    }

    /**
     * Acquire a distributed lock with default timeout (30 seconds)
     * Thread-safe: Multiple threads can call this concurrently
     *
     * @param lockKey Unique key for the lock
     * @return LockToken if successful, null if failed
     */
    public LockToken acquireLock(String lockKey) {
        return acquireLock(lockKey, DEFAULT_TIMEOUT_SECONDS, DEFAULT_MAX_RETRIES);
    }

    /**
     * Acquire a distributed lock with custom timeout
     * Thread-safe: Handles both local thread synchronization and distributed synchronization
     *
     * @param lockKey Unique key for the lock
     * @param timeoutSeconds Lock expiration time in seconds
     * @param maxRetries Maximum number of acquisition attempts
     * @return LockToken if successful, null if failed
     */
    public LockToken acquireLock(String lockKey, int timeoutSeconds, int maxRetries) {
        // Step 1: Acquire local lock first (handles same-JVM thread safety)
        ReentrantLock localLock = getLocalLock(lockKey);

        try {
            // Try to acquire local lock with timeout
            boolean localLockAcquired = localLock.tryLock(maxRetries * DEFAULT_RETRY_DELAY_MS, TimeUnit.MILLISECONDS);
            if (!localLockAcquired) {
                return null; // Another thread in same JVM is holding this lock
            }

            try {
                // Step 2: Check if we already hold this distributed lock in current JVM
                LockToken existingLock = activeLocks.get(lockKey);
                if (existingLock != null && !existingLock.isLikelyExpired()) {
                    // We already hold this lock, return a reference to it
                    return existingLock;
                }

                // Step 3: Try to acquire distributed lock from Redis
                String lockValue = UUID.randomUUID().toString();
                int attempts = 0;

                try (Jedis jedis = jedisPool.getResource()) {
                    while (attempts < maxRetries) {
                        // Atomic SET with NX (only if not exists) and EX (expiration)
                        SetParams params = SetParams.setParams().nx().ex(timeoutSeconds);
                        String result = jedis.set(lockKey, lockValue, params);

                        if ("OK".equals(result)) {
                            // Successfully acquired distributed lock
                            LockToken token = new LockToken(lockKey, lockValue, timeoutSeconds);
                            activeLocks.put(lockKey, token);
                            return token;
                        }

                        attempts++;
                        if (attempts < maxRetries) {
                            try {
                                Thread.sleep(DEFAULT_RETRY_DELAY_MS);
                            } catch (InterruptedException e) {
                                Thread.currentThread().interrupt();
                                return null;
                            }
                        }
                    }
                } catch (Exception e) {
                    throw new RuntimeException("Failed to acquire distributed lock: " + lockKey, e);
                }

                return null; // Failed to acquire distributed lock

            } finally {
                // Always release local lock
                if (localLock.isHeldByCurrentThread()) {
                    localLock.unlock();
                }
            }

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return null;
        } finally {
            // Clean up unused local locks
            cleanupLocalLock(lockKey);
        }
    }

    /**
     * Try to acquire lock without blocking (non-blocking)
     * Thread-safe version
     *
     * @param lockKey Unique key for the lock
     * @param timeoutSeconds Lock expiration time in seconds
     * @return LockToken if successful, null if failed
     */
    public LockToken tryLock(String lockKey, int timeoutSeconds) {
        // For non-blocking, we only try once
        ReentrantLock localLock = getLocalLock(lockKey);

        // Try to acquire local lock without waiting
        if (!localLock.tryLock()) {
            return null; // Another thread in same JVM has the lock
        }

        try {
            // Check if we already hold this distributed lock
            LockToken existingLock = activeLocks.get(lockKey);
            if (existingLock != null && !existingLock.isLikelyExpired()) {
                return existingLock;
            }

            // Try to acquire distributed lock (single attempt)
            String lockValue = UUID.randomUUID().toString();

            try (Jedis jedis = jedisPool.getResource()) {
                SetParams params = SetParams.setParams().nx().ex(timeoutSeconds);
                String result = jedis.set(lockKey, lockValue, params);

                if ("OK".equals(result)) {
                    LockToken token = new LockToken(lockKey, lockValue, timeoutSeconds);
                    activeLocks.put(lockKey, token);
                    return token;
                }
            } catch (Exception e) {
                throw new RuntimeException("Failed to try lock: " + lockKey, e);
            }

            return null;

        } finally {
            if (localLock.isHeldByCurrentThread()) {
                localLock.unlock();
            }
            cleanupLocalLock(lockKey);
        }
    }

    /**
     * Try to acquire lock with default timeout, non-blocking
     * Thread-safe version
     */
    public LockToken tryLock(String lockKey) {
        return tryLock(lockKey, DEFAULT_TIMEOUT_SECONDS);
    }

    /**
     * Release a distributed lock
     * Thread-safe: Only releases if the current process owns the lock
     *
     * @param lockToken Token representing the acquired lock
     * @return true if successfully released, false otherwise
     */
    public boolean releaseLock(LockToken lockToken) {
        if (lockToken == null) {
            return false;
        }

        String lockKey = lockToken.getKey();
        ReentrantLock localLock = getLocalLock(lockKey);

        // Acquire local lock to ensure thread safety
        localLock.lock();
        try {
            // Remove from active locks first
            LockToken activeToken = activeLocks.get(lockKey);
            if (activeToken == null || !activeToken.getValue().equals(lockToken.getValue())) {
                return false; // We don't own this lock
            }

            // Release distributed lock
            try (Jedis jedis = jedisPool.getResource()) {
                Long result = (Long) jedis.eval(RELEASE_SCRIPT,
                        Collections.singletonList(lockToken.getKey()),
                        Collections.singletonList(lockToken.getValue()));

                boolean released = result != null && result == 1;
                if (released) {
                    activeLocks.remove(lockKey);
                }
                return released;
            } catch (Exception e) {
                throw new RuntimeException("Failed to release distributed lock: " + lockKey, e);
            }

        } finally {
            localLock.unlock();
            cleanupLocalLock(lockKey);
        }
    }

    /**
     * Renew/extend the expiration time of an existing lock
     * Thread-safe version
     */
    public boolean renewLock(LockToken lockToken, int newTimeoutSeconds) {
        if (lockToken == null) {
            return false;
        }

        String lockKey = lockToken.getKey();
        ReentrantLock localLock = getLocalLock(lockKey);

        localLock.lock();
        try {
            // Verify we still own the lock locally
            LockToken activeToken = activeLocks.get(lockKey);
            if (activeToken == null || !activeToken.getValue().equals(lockToken.getValue())) {
                return false;
            }

            try (Jedis jedis = jedisPool.getResource()) {
                Long result = (Long) jedis.eval(RENEW_SCRIPT,
                        Collections.singletonList(lockToken.getKey()),
                        java.util.Arrays.asList(lockToken.getValue(), String.valueOf(newTimeoutSeconds)));

                return result != null && result == 1;
            } catch (Exception e) {
                throw new RuntimeException("Failed to renew distributed lock: " + lockKey, e);
            }

        } finally {
            localLock.unlock();
            cleanupLocalLock(lockKey);
        }
    }

    /**
     * Execute code while holding a distributed lock (recommended approach)
     * Thread-safe: Automatically handles lock acquisition and release
     *
     * @param lockKey Unique key for the lock
     * @param timeoutSeconds Lock expiration time in seconds
     * @param task Code to execute while holding the lock
     * @return true if lock was acquired and task executed, false if lock acquisition failed
     */
    public boolean executeWithLock(String lockKey, int timeoutSeconds, Runnable task) {
        LockToken lock = acquireLock(lockKey, timeoutSeconds, DEFAULT_MAX_RETRIES);
        if (lock != null) {
            try {
                task.run();
                return true;
            } finally {
                releaseLock(lock);
            }
        }
        return false;
    }

    /**
     * Execute code while holding a distributed lock with default timeout
     * Thread-safe version
     */
    public boolean executeWithLock(String lockKey, Runnable task) {
        return executeWithLock(lockKey, DEFAULT_TIMEOUT_SECONDS, task);
    }

    /**
     * Execute code while holding a distributed lock and return result
     * Thread-safe: Automatically handles lock acquisition and release
     */
    public <T> T executeWithLock(String lockKey, int timeoutSeconds, Supplier<T> supplier) {
        LockToken lock = acquireLock(lockKey, timeoutSeconds, DEFAULT_MAX_RETRIES);
        if (lock != null) {
            try {
                return supplier.get();
            } finally {
                releaseLock(lock);
            }
        }
        return null;
    }

    /**
     * Execute code while holding a distributed lock with default timeout and return result
     * Thread-safe version
     */
    public <T> T executeWithLock(String lockKey, Supplier<T> supplier) {
        return executeWithLock(lockKey, DEFAULT_TIMEOUT_SECONDS, supplier);
    }

    /**
     * Check if a lock is currently held by any process
     * Thread-safe version
     */
    public boolean isLocked(String lockKey) {
        try (Jedis jedis = jedisPool.getResource()) {
            return jedis.exists(lockKey);
        } catch (Exception e) {
            throw new RuntimeException("Failed to check lock status: " + lockKey, e);
        }
    }

    /**
     * Get remaining time-to-live of a lock
     * Thread-safe version
     */
    public long getLockTTL(String lockKey) {
        try (Jedis jedis = jedisPool.getResource()) {
            return jedis.ttl(lockKey);
        } catch (Exception e) {
            throw new RuntimeException("Failed to get lock TTL: " + lockKey, e);
        }
    }

    /**
     * Force release any lock (dangerous - use only for cleanup)
     * Thread-safe version
     */
    public boolean forceReleaseLock(String lockKey) {
        ReentrantLock localLock = getLocalLock(lockKey);
        localLock.lock();
        try {
            activeLocks.remove(lockKey); // Clean up local tracking

            try (Jedis jedis = jedisPool.getResource()) {
                return jedis.del(lockKey) == 1;
            } catch (Exception e) {
                throw new RuntimeException("Failed to force release lock: " + lockKey, e);
            }
        } finally {
            localLock.unlock();
            cleanupLocalLock(lockKey);
        }
    }

    /**
     * Get statistics about active locks (useful for monitoring)
     * Thread-safe version
     */
    public int getActiveLockCount() {
        return activeLocks.size();
    }

    /**
     * Clear all expired locks from local tracking (cleanup method)
     * Thread-safe version
     */
    public void cleanupExpiredLocks() {
        activeLocks.entrySet().removeIf(entry -> entry.getValue().isLikelyExpired());
    }

    /**
     * Lock token representing an acquired distributed lock
     * Thread-safe immutable representation
     */
    public static class LockToken {
        private final String key;
        private final String value;
        private final int timeoutSeconds;
        private final long acquiredTime;
        private final String threadName;

        public LockToken(String key, String value, int timeoutSeconds) {
            this.key = key;
            this.value = value;
            this.timeoutSeconds = timeoutSeconds;
            this.acquiredTime = System.currentTimeMillis();
            this.threadName = Thread.currentThread().getName();
        }

        public String getKey() { return key; }
        public String getValue() { return value; }
        public int getTimeoutSeconds() { return timeoutSeconds; }
        public long getAcquiredTime() { return acquiredTime; }
        public String getThreadName() { return threadName; }

        /**
         * Check if lock is likely expired based on local time
         * Thread-safe method
         */
        public boolean isLikelyExpired() {
            long elapsedSeconds = (System.currentTimeMillis() - acquiredTime) / 1000;
            return elapsedSeconds >= timeoutSeconds;
        }

        @Override
        public String toString() {
            return String.format("LockToken{key='%s', timeout=%ds, acquired=%dms ago, thread='%s'}",
                    key, timeoutSeconds, System.currentTimeMillis() - acquiredTime, threadName);
        }
    }
}
