package com.chen.blogbackend.services;

import com.chen.blogbackend.entities.*;
import com.chen.blogbackend.mappers.RelationshipParser;
import com.chen.blogbackend.util.RandomUtil;
import com.datastax.oss.driver.api.core.CqlSession;
import com.datastax.oss.driver.api.core.cql.*;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import redis.clients.jedis.Jedis;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.concurrent.ThreadPoolExecutor;

@Service
public class FriendsService {

    @Autowired
    CqlSession session;

    @Autowired
    Jedis redisClient;

    @Autowired
    KeyService keyService;


    private PreparedStatement addUsersInGroups;
    private PreparedStatement addUserOwnedGroups;
    private PreparedStatement delFriendByUserId;
    private PreparedStatement delUsersInGroups;
    private PreparedStatement delOwnGroups;
    private PreparedStatement delUserGroup;
    private PreparedStatement getFollowersByUserId;
    private PreparedStatement getUsersIntro;
    private PreparedStatement initUsersIntro;
//  private   PreparedStatement updateFriendDirectionByIdolId;
//  private   PreparedStatement updateFriendDirectionByUserId;
    private PreparedStatement getFollowRelationship;
    private PreparedStatement block;
    private PreparedStatement unBlock;
    private PreparedStatement deleteFriend;
    private PreparedStatement insertFollowRelationship;
    private PreparedStatement deleteFollowRelationship;
    private PreparedStatement addFriend;
    private PreparedStatement getAllFriends;
    private PreparedStatement getIdolsById;

    private PreparedStatement createGroup;


    ThreadPoolExecutor executor;


    private static boolean stringLessThan(String str1, String str2) {
        // 使用compareTo方法进行比较
        return str1.compareTo(str2) < 0;
    }

    @PostConstruct
    public void init() throws IOException {
            delFriendByUserId = session.prepare("delete from relationship.following_relationship where user_id=? and friend_id = ?;");
            initUsersIntro = session.prepare("insert into userInfo.user_information (user_id, user_name)  values (?,?) ");
            getUsersIntro = session.prepare("select * from userInfo.user_information where user_id=?;");
            insertFollowRelationship = session.prepare("insert into relationship.following_relationship (user_id, friend_id) values(?, ?)");
            deleteFollowRelationship = session.prepare("delete from relationship.following_relationship where user_id = ? and friend_id = ?");
            deleteFriend= session.prepare("delete from relationship.following_relationship where user_id = ? and friend_id = ?");
            addFriend = session.prepare("insert into relationship.following_relationship (user_id, friend_id, name) values(?, ?, ?);");
            getIdolsById = session.prepare("select * from relationship.following_relationship where user_id = ?");
            getFollowersByUserId = session.prepare("select * from relationship.following_relationship where friend_id =?;");
            getFollowRelationship = session.prepare("select * from relationship.following_relationship where user_id = ? and friend_id = ?");

            ///createGroup = session.prepare("INSERT INTO group_chat.chat_group_details (group_id, group_name, group_description, owner, config, avatar) VALUES (?,?,?,?,?,?);");
    }

    public List<Relationship> getFollowersByUserId(String userId) {
        ResultSet execute = session.execute(getIdolsById.bind(userId));
        return RelationshipParser.parseToRelationship(execute);
    }

    public List<Relationship> getFriendsByUserId(String userId) {
        ArrayList<Relationship> result = new ArrayList<>();

        ResultSet execute = session.execute(getFollowersByUserId.bind(userId));
        List<Relationship> relationships = RelationshipParser.parseToRelationship(execute);
        HashSet<String> hashSet = new HashSet<>();
        for (Relationship relationship : relationships) {
            hashSet.add(relationship.getFriendId());
        }
        ResultSet execute1 = session.execute(getIdolsById.bind(userId));
        List<Relationship> relationships1 = RelationshipParser.parseToRelationship(execute1);
        for (Relationship relationship : relationships1) {
            if (hashSet.contains(relationship.getUserId())) {
                result.add(relationship);
            }
        }


        return result;
    }

    public int getRelationship(String userid, String userIdToFollow) throws Exception {
        boolean flag = false;

        ResultSet follow = session.execute(getFollowRelationship.bind(userid, userIdToFollow));
        if (!follow.all().isEmpty()) flag = true;

        ResultSet reverseFollow = session.execute(getFollowRelationship.bind(userIdToFollow, userid));
        List<Row> all = reverseFollow.all();
        if (all.size() == 0) {
            if (flag) return 10;
            else return 0;
        }
        else if (all.size() == 1) {
            if (flag) {
                return 11;
            }
            else {
                return 1;
            }
        } else throw new Exception("error relationship occurred internal!");
    }


    public List<Relationship> getIdolsByUserId(String userId){
        ResultSet set = session.execute(getFollowersByUserId.bind(userId));
        return RelationshipParser.parseToRelationship(set);
    }

    public boolean follow(String fanId, String idolId, String name) throws Exception {
        if (fanId == null || idolId == null) return false;
//        if (compareStrings(fanId,idolId))
//        String smaller = ;
//        String bigger = ;
        ResultSet execute = session.execute(insertFollowRelationship.bind(fanId,idolId));
        //int relationship = getRelationship(fanId, idolId);

        return execute.getExecutionInfos().get(0).getErrors().size() == 0;
    }




    public boolean unfollow(String fanId, String idolId ) {
//        BatchStatementBuilder batchStatementBuilder = BatchStatement.builder(BatchType.UNLOGGED);
//        batchStatementBuilder.addStatement(delFriendByUserId.bind(fanId,idolId));
//        batchStatementBuilder.addStatement(delFriendByUserId.bind(idolId,fanId));
//        // reduce
//
//        ResultSet execute = session.execute(batchStatementBuilder.build());
//         modify

        ResultSet execute = session.execute(deleteFollowRelationship.bind(fanId, idolId));
        ResultSet execute1 = session.execute(deleteFollowRelationship.bind(idolId, fanId));

//        ResultSet set3 = session.execute(deleteIdol.bind(idolId,fanId));
        return execute.getExecutionInfo().getErrors().size() == 0 &&
                execute1.getExecutionInfo().getErrors().size() == 0;

    }

    // user group not group chat.
//    public boolean createGroup(UserGroup group) {
//        group.setGroupId(keyService.getIntKey("group_id"));
//        userGroupDao.insert(group.getGroupId(), group.getName(), group.getGroup_avatar(), group.getCount());
//        return true;
//    }
//
//    public boolean removeGroup(String groupId) {
//        List<String> friendIdsByGroupId = getFriendIdsByGroupId(groupId);
//        BatchStatementBuilder batchStatementBuilder = BatchStatement.builder(BatchType.UNLOGGED);
//        for (String userId : friendIdsByGroupId) {
//            batchStatementBuilder.addStatements(delUsersInGroups.bind(userId, groupId), delOwnGroups.bind(groupId, userId));
//        }
//        batchStatementBuilder.addStatements(delUserGroup.bind(groupId));
//        ResultSet execute = session.execute(batchStatementBuilder.build());
//        // modify
//        return execute.getExecutionInfo().getErrors().size() == 0;
//    }
//
//    public List<Friend> batchGetUsers(List<String> users) {
//        BatchStatementBuilder batchStatementBuilder = BatchStatement.builder(BatchType.UNLOGGED);
//        for (String userId : users) {
//            batchStatementBuilder.addStatement(getUsersIntro.bind(userId));
//        }
//        ResultSet execute = session.execute(batchStatementBuilder.build());
//        return friendDao.getEntity(execute).all();
//    }

//    public boolean moveToGroup(String userId, List<String> friendId, String groupId) {
//        BatchStatementBuilder batchStatementBuilder = BatchStatement.builder(BatchType.UNLOGGED);
//        List<Friend> friends = batchGetUsers(friendId);
//        for (Friend friend: friends) {
//            batchStatementBuilder.addStatements(addUsersInGroups.bind(userId, groupId, friend.getUserId(), friend.getName(),friend.getAvatar()));
//        }
//        ResultSet execute = session.execute(batchStatementBuilder.build());
//        return execute.getExecutionInfo().getErrors().size() == 0;
//    }

//    public boolean moveToGroup(String userId, String friendId, String groupId) {
//        ArrayList<String> friendsId = new ArrayList<>();
//        friendsId.add(friendId);
//        return moveToGroup(userId, friendsId, groupId);
//    }

    public boolean deleteFromGroup(String user, List<String> usersToRemove, String groupFrom) {
        BatchStatementBuilder batchStatementBuilder = BatchStatement.builder(BatchType.UNLOGGED);
        for (String userId:usersToRemove) {
            batchStatementBuilder.addStatements(delUsersInGroups.bind(groupFrom, userId));
        }
        ResultSet execute = session.execute(batchStatementBuilder.build());
        return execute.getExecutionInfo().getErrors().size() == 0;
    }

    public boolean deleteFromGroup(String user, String userToRemove, String groupFrom) {
        List<String> friends = new ArrayList<>();
        friends.add(userToRemove);
        return deleteFromGroup(user, friends, groupFrom);
    }

    public boolean initUserIntro(String userId) {
        ResultSet execute = session.execute(initUsersIntro.bind(userId, RandomUtil.generateRandomName()));
        return execute.getExecutionInfo().getErrors().size() == 0;
    }

    public boolean sendInvitation(Invitation invitation) {
        return true;
    }

    public boolean approveInvitation(int invitationId) {
        return true;
    }

    public List<Invitation> getInvitations(String userId) {
        return new ArrayList<Invitation>() ;
    }



}
