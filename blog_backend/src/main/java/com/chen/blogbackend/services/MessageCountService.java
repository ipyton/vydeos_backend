package com.chen.blogbackend.services;

import com.chen.blogbackend.entities.MessageCountEntity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;

@Service
public class MessageCountService {

    @Autowired
    JedisPool jedisPool;

    public MessageCountEntity getGroupChatCount(String groupId, String userId) {
        String groupCount = jedisPool.getResource().get("group_chat_count_" + groupId + "_" + userId);
        //String totalMessageCount = jedisPool.getResource().get("single_message_count_" + userId);
        long count = groupCount == null ? 0 : Long.parseLong(groupCount);
        return new MessageCountEntity(0, count);
    }

    public MessageCountEntity getTotalMessageCount(String userId) {
        String s = jedisPool.getResource().get("total_message_count_" + userId);
        long count = s == null ? 0 : Long.parseLong(s);
        return new MessageCountEntity(count, 0);
    }

    public MessageCountEntity getSingleMessageCount(String fromUserId, String toUserId) {
        String s = jedisPool.getResource().get("single_message_count_" + toUserId + "_" + fromUserId);
        long count = s == null ? 0 : Long.parseLong(s);
        return new MessageCountEntity(0, count);
    }


    public  void updateGroupMessageCount(String userId, String groupId, int change) {
        String luaScript = """
            redis.call('INCRBY', KEYS[1], ARGV[1])
            redis.call('INCRBY', KEYS[2], ARGV[1])
            return 'OK'
        """;

        String groupReceiverKey = String.format("group_chat_count_{%s}_{%s}", groupId, userId);
        String receiverUserTotalKey = String.format("total_message_count_{%s}", userId);

        try (Jedis jedis = jedisPool.getResource()) {
            jedis.eval(luaScript,
                    2,
                    groupReceiverKey, receiverUserTotalKey,
                    String.valueOf(change));
        }
    }


    public void updateUserMessageCount(String from_userid, String to_userid, int change) {
        String luaScript = """
            redis.call('INCRBY', KEYS[1], ARGV[1])
            redis.call('INCRBY', KEYS[2], ARGV[1])
            return 'OK'
        """;

        //String fromUserKey = String.format("user:{%s}:to:{%s}", fromUserId, receiverId);
        String receiverUserKey = String.format("single_message_count_{%s}_{%s}", to_userid, from_userid);
        String receiverUserTotalKey = String.format("total_message_count_{%s}", to_userid);

        try (Jedis jedis = jedisPool.getResource()) {
            jedis.eval(luaScript,
                    2,
                     receiverUserKey, receiverUserTotalKey,
                    String.valueOf(change));
        }

    }



}
