package com.chen.blogbackend.util;

import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;
import java.util.HashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class RandomUtilTest {

    @Test
    void testGetHash() {
        String result = RandomUtil.getHash("testString");
        assertNotNull(result);
        assertEquals(2, result.length());
        assertEquals(Integer.toString("testString".hashCode()).substring(0, 2), result);
    }

    @Test
    void testGetBase64() {
        String input = "test input";
        String result = RandomUtil.getBase64(input);
        assertNotNull(result);
        assertEquals(new String(java.util.Base64.getDecoder().decode(result), StandardCharsets.UTF_8), input);
    }

    @Test
    void testGenerateRandomString() {
        // Test with different lengths
        for (int length : new int[]{5, 10, 20}) {
            String result = RandomUtil.generateRandomString(length);
            assertNotNull(result);
            assertEquals(length, result.length());
            
            // Verify characters are from expected set
            for (char c : result.toCharArray()) {
                assertTrue(
                        (c >= 'a' && c <= 'z') || 
                        (c >= 'A' && c <= 'Z') || 
                        (c >= '0' && c <= '9')
                );
            }
        }
    }

    @Test
    void testGetMD5() {
        String input = "test input";
        String result = RandomUtil.getMD5(input);
        assertNotNull(result);
        assertEquals(32, result.length()); // MD5 is always 32 hex chars
        
        // MD5 should be consistent
        String secondResult = RandomUtil.getMD5(input);
        assertEquals(result, secondResult);
        
        // Different input should give different hash
        String differentInput = "different input";
        String differentResult = RandomUtil.getMD5(differentInput);
        assertNotEquals(result, differentResult);
    }

    @Test
    void testGenerateRandomInt() {
        // Test with different lengths
        for (int length : new int[]{5, 10, 15}) {
            String result = RandomUtil.generateRandomInt(length);
            assertNotNull(result);
            assertEquals(length, result.length());
            
            // Verify characters are digits
            for (char c : result.toCharArray()) {
                assertTrue(c >= '0' && c <= '9');
            }
        }
    }

    @Test
    void testGenerateMessageId() {
        String userId = "user123";
        String result = RandomUtil.generateMessageId(userId);
        assertNotNull(result);
        assertTrue(result.startsWith(userId + "_"));
        
        // Test uniqueness
        String secondResult = RandomUtil.generateMessageId(userId);
        assertNotEquals(result, secondResult);
    }

    @Test
    void testGenerateRandomName() {
        String result = RandomUtil.generateRandomName();
        assertNotNull(result);
        assertTrue(result.contains(" "));
    }

    @Test
    void testGenerateTimeBasedRandomLong() {
        String user1 = "user1";
        String user2 = "user2";
        
        // Test uniqueness across users at the same time
        Long id1 = RandomUtil.generateTimeBasedRandomLong(user1);
        Long id2 = RandomUtil.generateTimeBasedRandomLong(user2);
        assertNotEquals(id1, id2);
        
        // Test uniqueness for the same user over time
        try {
            Thread.sleep(1000); // wait to ensure timestamp changes
        } catch (InterruptedException e) {
            fail("Test interrupted");
        }
        
        Long id1Later = RandomUtil.generateTimeBasedRandomLong(user1);
        assertNotEquals(id1, id1Later);
        
        // Test format and size
        assertNotNull(id1);
        assertTrue(id1 > 0);
    }
    
    @Test
    void testGenerateTimeBasedRandomLongUniqueness() {
        // Generate many IDs to verify uniqueness
        int count = 1000;
        Set<Long> ids = new HashSet<>();
        String user = "testUser";
        
        for (int i = 0; i < count; i++) {
            ids.add(RandomUtil.generateTimeBasedRandomLong(user));
        }
        
        assertEquals(count, ids.size());
    }
} 