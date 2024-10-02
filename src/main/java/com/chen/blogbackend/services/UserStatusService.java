package com.chen.blogbackend.services;

import com.chen.blogbackend.NotificationSrvGrpc;
import com.chen.blogbackend.UserStatus;
import com.chen.blogbackend.entities.GroupMessage;
import com.chen.blogbackend.entities.OnlineGroupMessage;
import com.chen.blogbackend.entities.SingleMessage;
import io.grpc.Server;
import io.grpc.ServerBuilder;
import io.grpc.stub.StreamObserver;
import jakarta.annotation.PostConstruct;
import org.apache.catalina.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

//This is an endpoint of protobuf to handle the request for end point users. And to request their status.
@Service
public class UserStatusService {


    @PostConstruct
    public void init() throws IOException, InterruptedException {
        Server server = ServerBuilder.forPort(10010).addService(new UserStatusImpl()).build().start();
        System.out.println("server started, listening on " + server.getPort());
        server.awaitTermination();
    }

    @Component
    static
    class UserStatusImpl extends NotificationSrvGrpc.NotificationSrvImplBase {
        @Autowired
        SingleMessageService singleMessageService;
        @Autowired
        ChatGroupService chatGroupService;
        @Override
        public void onlineHandler(UserStatus.UserOnlineInformation request, StreamObserver<UserStatus.MessageResponse> responseObserver) {
            super.onlineHandler(request, responseObserver);
            List<SingleMessage> singleRecords = singleMessageService.getNewRecords(request.getUserId(), request.getLastReceivedTimestamp());
            List<OnlineGroupMessage> groupRecords = chatGroupService.getOnlineGroupMessageByUserID(request.getUserId());

            List<UserStatus.SingleMessage> singleList = new ArrayList<>();
            List<UserStatus.GroupMessage> groupList = new ArrayList<>();
            for (SingleMessage singleMessage : singleRecords) {
                UserStatus.SingleMessage message = UserStatus.SingleMessage.newBuilder()
                        .setMessageId(singleMessage.getMessageId())
                        .setUserId(singleMessage.getUserId())
                        .setReceiverId(singleMessage.getReceiverId())
                        .setType(singleMessage.getType())
                        .setSendTime(singleMessage.getSendTime().getEpochSecond())
                        .setContent(singleMessage.getContent())
                        .setReferMessageId(singleMessage.getReferMessageId())
                        .addReferUserIds("user3")
                        .build();
                singleList.add(message);
            }
            for (OnlineGroupMessage groupMessage : groupRecords) {
                UserStatus.GroupMessage newGroupMessage = UserStatus.GroupMessage.newBuilder()
                        .setMessageId("2")
                        .setUserId("user1")
                        .setGroupId("group1")
                        .setType("text")
                        .setTimestamp(groupMessage.getLatestTimestamp().getEpochSecond())
                        .setContent("Hello Group1")
                        .setMedia("image.png")
                        .setReferMessageId("0")
                        .setReferUserId("user4")
                        .build();
                groupList.add(newGroupMessage);
            }
            UserStatus.MessageResponse.Builder builder = UserStatus.MessageResponse.newBuilder();
            builder.addAllGroupMessages(groupList);
            builder.addAllSingleMessages(singleList);
            responseObserver.onNext(builder.build());
            responseObserver.onCompleted();
        }
    }

}

