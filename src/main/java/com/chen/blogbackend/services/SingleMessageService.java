package com.chen.blogbackend.services;

import com.chen.blogbackend.entities.Message;
import com.chen.blogbackend.responseMessage.LoginMessage;
import com.chen.blogbackend.responseMessage.PagingMessage;
import com.datastax.oss.driver.api.core.CqlSession;

import com.datastax.oss.driver.api.core.cql.PagingState;
import com.datastax.oss.driver.api.core.cql.PreparedStatement;
import com.datastax.oss.driver.api.core.cql.ResultSet;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class SingleMessageService {

    @Autowired
    CqlSession session;

    PreparedStatement getRecord;
    PreparedStatement setRecordById;
    PreparedStatement block;
    PreparedStatement unBlock;



    @PostConstruct
    public void init(){
        setRecordById = session.prepare("insert into chat_record_by_id values(?,?,?,?,?,?,?);");
        getRecord = session.prepare("select * from chat_record_by_id where user_id = ? and receiver_id = ?");
        block = session.prepare("insert into black_list values(?, ?, ?, ?)");
        unBlock = session.prepare("delete from black_list where user_id = ? and black_id = ?");
    }

    public boolean blockUser(String userId, String blockUser) {
        session.execute(block.bind(userId, blockUser));
        return true;
    }

    public boolean unblockUser(String userId, String unBlockUser) {
        session.execute(unBlock.bind(userId, unBlockUser));
        return true;
    }


    public PagingMessage<Message> getMessageByUserId(String userId, String receiverId, String pageState) {
        ResultSet execute = session.execute(getRecord.bind(userId, receiverId).setPagingState(PagingState.fromString(pageState)));
        return new PagingMessage<>();
    }

    public boolean sendMessage(String userId, String to, Message message) {
        session.execute(setRecordById.bind(userId,to,message));
        return true;
    }

    public boolean recall(){
        return true;
    }






}
