package com.chen.blogbackend.services;

import com.chen.blogbackend.DAO.FriendDao;
import com.chen.blogbackend.DAO.UserGroupDao;
import com.chen.blogbackend.entities.Friend;
import com.chen.blogbackend.entities.UserGroup;
import com.chen.blogbackend.mappers.FriendMapper;
import com.chen.blogbackend.mappers.FriendMapperBuilder;
import com.chen.blogbackend.mappers.UserGroupMapper;
import com.chen.blogbackend.mappers.UserGroupMapperBuilder;
import com.chen.blogbackend.responseMessage.PagingMessage;
import com.datastax.oss.driver.api.core.CqlSession;
import com.datastax.oss.driver.api.core.DefaultConsistencyLevel;
import com.datastax.oss.driver.api.core.PagingIterable;
import com.datastax.oss.driver.api.core.cql.*;
import com.datastax.oss.protocol.internal.request.Prepare;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.FileReader;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

@Service
public class FriendsService {

    @Autowired
    CqlSession session;

    FriendDao friendDao;
    UserGroupDao userGroupDao;

    PreparedStatement addFriendUserId;
    PreparedStatement addFriendByIdol;
    PreparedStatement addUsersInGroups;
    PreparedStatement addUserOwnedGroups;
    PreparedStatement delFriendByUserId;
    PreparedStatement delFriendByIdolId;
    PreparedStatement delUsersInGroups;
    PreparedStatement delOwnGroups;
    PreparedStatement delUserGroup;
    PreparedStatement getFollowersByUserId;
    PreparedStatement getFollowersByIdolId;
    PreparedStatement getUsersIntro;
    PreparedStatement updateFriendDirectionByIdolId;
    PreparedStatement updateFriendDirectionByUserId;
    PreparedStatement block;
    PreparedStatement unBlock;


    @PostConstruct
    public void init(){
        try {
            friendDao = new FriendMapperBuilder(session).build().getDao();
            userGroupDao = new UserGroupMapperBuilder(session).build().getDao();
            addFriendByIdol = session.prepare("insert into followers_by_user_id values(?,?,?,?,?);");
            addFriendUserId = session.prepare("insert into followers_by_idol_id values(?,?,?,?,?);");
            addUsersInGroups = session.prepare("insert into users_in_groups values(?,?,?,?);");
            addUserOwnedGroups = session.prepare("insert into user_own_groups values(?,?,?,?);");
            delFriendByIdolId = session.prepare("delete from followers_by_idol_id where idol_id=?;");
            delFriendByUserId = session.prepare("delete from followers_by_user_id where user_id=?;");
            getFollowersByUserId = session.prepare("select * from followers_by_user_id where user_id=?;");
            getUsersIntro = session.prepare("select * from user_information where user_id=?;");
            delUsersInGroups = session.prepare("delete from users_in_groups where group_id = ? and user_id = ?");
            delOwnGroups = session.prepare("delete from user_owned_groups where user_id=? and group_id = ?");
            delUserGroup = session.prepare("delete from user_group where group_id = ?");
            updateFriendDirectionByIdolId = session.prepare("update followers_by_user_id set bi_direction=?;");
            updateFriendDirectionByUserId = session.prepare("update followers_by_idol_id set bi_direction=?;");
            //getFollowersByIdolId = session.prepare("select * from followers_by_idol_id where idol_id=?;");
        }
        catch (Exception e) {
            System.out.println(e);
        }
    }

    public PagingMessage<Friend> getFollowersByUserId(String userId, String pagingState) {
        PagingIterable<Friend> friends = friendDao.selectUserFollowers(userId);
        PagingMessage<Friend> message = new PagingMessage<>(friends.all(), pagingState, 0);
        return message;
    }

    public PagingMessage<Friend> getIdolsByUserId(String userId, String pagingState){
        PagingIterable<Friend> friends = friendDao.selectUserFollows(userId);
        PagingMessage<Friend> message = new PagingMessage<>(friends.all(), pagingState, 0);
        return message;
    }

    public List<String> getIdolIdsByUserId(String userId) {
        PagingIterable<String> ids = friendDao.selectUserIdsFollows(userId);
        return ids.all();
    }


    public List<Friend> getFriendsByGroupId(String userId, String groupId) {
        PagingIterable<Friend> userGroups = userGroupDao.selectFriendsByGroupId(userId, groupId);
        return userGroups.all();
    }

    public List<String> getFriendIdsByGroupId(String groupId) {
        PagingIterable<String> strings = userGroupDao.selectUserIdByGroupID(groupId);
        return strings.all();
    }

    public List<UserGroup> getGroupById(String userId) {
        PagingIterable<UserGroup> userGroups = userGroupDao.selectGroupByGroupID(userId);
        return userGroups.all();
    }

    public boolean follow(String fanId, String idolId) {
        Friend fan = friendDao.selectUserInformation(fanId);
        Friend idol = friendDao.selectUserInformation(idolId);
        ResultSet execute2 = session.execute(getFollowersByIdolId.bind(fanId));
        boolean isBidirectional = execute2.getExecutionInfos().size() != 0;

        ResultSet execute = session.execute(addFriendUserId.bind(fanId, idolId, idol.getAvatar(), idol.getGroupId(), isBidirectional));
        ResultSet execute1 = session.execute(addFriendByIdol.bind(idolId, fanId, fan.getAvatar(), fan.getGroupId(), isBidirectional));
        return execute1.getExecutionInfos().get(0).getErrors().size() == 0 && execute.getExecutionInfos().get(0).getErrors().size() == 0;
    }


    public boolean unfollow(String fanId, String idolId) {
        BatchStatementBuilder batchStatementBuilder = BatchStatement.builder(BatchType.UNLOGGED);
        batchStatementBuilder.addStatement(delFriendByIdolId.bind(idolId));
        batchStatementBuilder.addStatement(delFriendByUserId.bind(fanId));
        // reduce
        batchStatementBuilder.addStatements(updateFriendDirectionByIdolId.bind(0),
                updateFriendDirectionByUserId.bind(0));
        ResultSet execute = session.execute(batchStatementBuilder.build());
        // modify
        return execute.getExecutionInfos().get(0).getErrors().size() == 0;
    }

    public boolean createGroup(UserGroup group) {
        userGroupDao.insert(group.getGroupId(), group.getName(), group.getGroup_avatar(), group.getCount());
        return true;
    }

    public boolean removeGroup(String groupId) {
        List<String> friendIdsByGroupId = getFriendIdsByGroupId(groupId);
        BatchStatementBuilder batchStatementBuilder = BatchStatement.builder(BatchType.UNLOGGED);
        for (String userId : friendIdsByGroupId) {
            batchStatementBuilder.addStatements(delUsersInGroups.bind(userId, groupId), delOwnGroups.bind(groupId, userId));
        }
        batchStatementBuilder.addStatements(delUserGroup.bind(groupId));
        ResultSet execute = session.execute(batchStatementBuilder.build());
        // modify
        return execute.getExecutionInfos().get(0).getErrors().size() == 0;
    }

    public List<Friend> batchGetUsers(List<String> users) {
        BatchStatementBuilder batchStatementBuilder = BatchStatement.builder(BatchType.UNLOGGED);
        for (String userId : users) {
            batchStatementBuilder.addStatement(getUsersIntro.bind(userId));
        }
        ResultSet execute = session.execute(batchStatementBuilder.build());
        return friendDao.getEntity(execute).all();
    }

    public boolean moveToGroup(String userId, List<String> friendId, String groupId) {
        BatchStatementBuilder batchStatementBuilder = BatchStatement.builder(BatchType.UNLOGGED);
        List<Friend> friends = batchGetUsers(friendId);
        for (Friend friend: friends) {
            batchStatementBuilder.addStatements(addUsersInGroups.bind(userId, groupId, friend.getUserId(), friend.getName(),friend.getAvatar()));
        }
        ResultSet execute = session.execute(batchStatementBuilder.build());
        return execute.getExecutionInfos().get(0).getErrors().size() == 0;
    }

    public boolean moveToGroup(String userId, String friendId, String groupId) {
        ArrayList<String> friendsId = new ArrayList<>();
        friendsId.add(friendId);
        return moveToGroup(userId, friendsId, groupId);
    }

    public boolean deleteFromGroup(String user, List<String> usersToRemove, String groupFrom) {
        BatchStatementBuilder batchStatementBuilder = BatchStatement.builder(BatchType.UNLOGGED);
        for (String userId:usersToRemove) {
            batchStatementBuilder.addStatements(delUsersInGroups.bind(groupFrom, userId));
        }
        ResultSet execute = session.execute(batchStatementBuilder.build());
        return execute.getExecutionInfos().get(0).getErrors().size() == 0;
    }

    public boolean deleteFromGroup(String user, String userToRemove, String groupFrom) {
        List<String> friends = new ArrayList<>();
        friends.add(userToRemove);
        return deleteFromGroup(user, friends, groupFrom);
    }


}
