package com.chen.blogbackend.services;

import com.chen.blogbackend.DAO.SingleMessageDao;
import com.chen.blogbackend.entities.GroupMessage;
import com.chen.blogbackend.entities.Notification;
import com.chen.blogbackend.entities.SingleMessage;
import com.chen.blogbackend.mappers.MessageParser;
import com.chen.blogbackend.responseMessage.PagingMessage;
import com.chen.blogbackend.util.RandomUtil;
import com.datastax.oss.driver.api.core.CqlSession;

import com.datastax.oss.driver.api.core.PagingIterable;
import com.datastax.oss.driver.api.core.cql.PagingState;
import com.datastax.oss.driver.api.core.cql.PreparedStatement;
import com.datastax.oss.driver.api.core.cql.ResultSet;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateSerializer;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;

@Service
public class SingleMessageService {

    @Autowired
    CqlSession session;

    @Autowired
    NotificationProducer producer;

    PreparedStatement getRecord;
    PreparedStatement setRecordById;
    PreparedStatement block;
    PreparedStatement unBlock;
    PreparedStatement recall;
    PreparedStatement getNewestRecord;


    SingleMessageDao messageDao;

    @PostConstruct
    public void init(){
        setRecordById = session.prepare("insert into chat.chat_records (user_id, receiver_id, message_id, content, " +
                "send_time, type, messageType, count, refer_message_id, refer_user_id ) values (?,?,?,?,?,?,?,?, ?, ?);");
        getRecord = session.prepare("select * from chat.chat_records where user_id = ? and receiver_id = ?");
        block = session.prepare("insert into userinfo.black_list (user_id, black_user_id, black_user_name, black_user_avatar) values(?, ?, ?, ?)");
        unBlock = session.prepare("delete from userInfo.black_list where user_id = ? and black_user_id = ?");
        recall = session.prepare("update chat.chat_records set del=true where user_id = ? and message_id= ?");
        getNewestRecord = session.prepare("select * from chat.chat_records where user_id = ? and message_id > ?");

    }

    public boolean blockUser(String userId, String blockUser) {
        ResultSet execute = session.execute(block.bind(userId, blockUser));
        return execute.getExecutionInfo().getErrors().size() == 0;
    }

    public boolean unblockUser(String userId, String unBlockUser) {
        ResultSet execute = session.execute(unBlock.bind(userId, unBlockUser));
        return execute.getExecutionInfo().getErrors().size() == 0;
    }


    public PagingMessage<SingleMessage> getMessageByUserId(String userId, String receiverId, String pageState) {
        ResultSet execute = session.execute(getRecord.bind(userId, receiverId).setPagingState(PagingState.fromString(pageState)));
        PagingState state = execute.getExecutionInfo().getSafePagingState();
        PagingIterable<SingleMessage> convert = messageDao.convert(execute);

        assert state != null;
        return new PagingMessage<>(convert.all(), state.toString(), 1);
    }

    public String sendMessage(SingleMessage singleMessage) {
        String messageId =  RandomUtil.generateMessageId(singleMessage.getUserId());
        ResultSet execute = session.execute(setRecordById.bind(singleMessage.getUserId(), singleMessage.getReceiverId(), messageId,
                singleMessage.getType(),singleMessage.getMessageType(), 0, singleMessage.getReferMessageId(), singleMessage.getReferMessageId()));
        //producer.sendNotification(new Notification(avatar, userName, userId));
        if (execute.getExecutionInfo().getErrors().size()!=0) {
            return "";
        }
        //producer.sendNotification(new Notification());
        return messageId;
    }

    public boolean recall(String userId, String receiverId, String messageId){
        ResultSet set = session.execute(recall.bind(userId, receiverId, messageId));
        return set.getExecutionInfo().getErrors().size() == 0;

    }

    public List<SingleMessage> getNewRecords(String userId, long timestamp) {
        ResultSet execute = session.execute(getNewestRecord.bind(userId, Instant.ofEpochMilli(timestamp)));
        return MessageParser.parseToSingleMessage(execute);
    }








}
