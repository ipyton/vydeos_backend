package com.chen.blogbackend.services;

import com.chen.blogbackend.DAO.SingleMessageDao;
import com.chen.blogbackend.entities.NotificationMessage;
import com.chen.blogbackend.entities.SendingReceipt;
import com.chen.blogbackend.entities.deprecated.SingleMessage;
import com.chen.blogbackend.mappers.MessageParser;
import com.chen.blogbackend.responseMessage.PagingMessage;
import com.datastax.oss.driver.api.core.CqlSession;

import com.datastax.oss.driver.api.core.PagingIterable;
import com.datastax.oss.driver.api.core.cql.PagingState;
import com.datastax.oss.driver.api.core.cql.PreparedStatement;
import com.datastax.oss.driver.api.core.cql.ResultSet;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Service
public class SingleMessageService {

    @Autowired
    CqlSession session;

    @Autowired
    NotificationProducer producer;


    @Autowired
    FriendsService friendsService;

    @Autowired
    KeyService keyService;

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
                "send_time, type, refer_message_id, refer_user_id ) values (?,?,?,?,?,?,?,?);");
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



    public SendingReceipt sendMessage(String userId, String receiverId, String content, String messageType) throws Exception {

        Instant now = Instant.now();
        SendingReceipt receipt = new SendingReceipt();
        NotificationMessage singleMessage =  new NotificationMessage(null, userId, receiverId,0, null, null, "single", content, -1, now,-1);
        System.out.println(singleMessage.getSenderId());
        System.out.println(singleMessage.getReceiverId());
        if (friendsService.getRelationship(singleMessage.getSenderId(), singleMessage.getReceiverId()) != 11) {
            System.out.println("they are not friends");
            receipt.sequenceId = -1;
            receipt.result = false;
            return receipt;
        }

        receipt.sequenceId = keyService.getIntKey("singleMessage");
        singleMessage.setMessageId(receipt.sequenceId);
        //(user_id, receiver_id, message_id, content, send_time, type, messageType, count, refer_message_id, refer_user_id )
//        ResultSet execute = session.execute(setRecordById.bind(singleMessage.getSenderId(),
//                singleMessage.getReceiverId(), singleMessage.getMessageId(), singleMessage.getContent(),
//                singleMessage.getTime(), singleMessage.getType(),
//                singleMessage.getReferMessageId(), new ArrayList<>()));
//        if (!execute.getExecutionInfo().getErrors().isEmpty()) {
//            System.out.println(execute.getExecutionInfo().getErrors());
//        }
        //judge if a user can send message
        producer.sendNotification(singleMessage);
        receipt.result = true;

        //    private String userId;
        //    private String title;
        //    private String content;
        //    private String type;
        //    private String time;
        //producer.sendNotification(singleMessage);

        //producer.sendNotification(new Notification());
        return receipt;
    }

    public boolean recall(String userId, String receiverId, String messageId){
        ResultSet set = session.execute(recall.bind(userId, receiverId, messageId));
        return set.getExecutionInfo().getErrors().isEmpty();

    }

    public List<NotificationMessage> getNewRecords( long receiverId, Long timestamp, String pagingState) {
        System.out.println(receiverId);
        ResultSet execute;
        if (null == timestamp) {
            //get the things the user send
            execute = session.execute(getNewestRecord.bind(receiverId, receiverId).setPagingState(PagingState.fromString(pagingState)).setPageSize(10));
        }
        else {
            execute = session.execute(getNewestRecord.bind(receiverId, receiverId + "_" + timestamp));
        }

        return MessageParser.parseToNotificationMessage(execute);
    }








}
