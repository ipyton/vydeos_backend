package com.chen.blogbackend.services;

import com.chen.blogbackend.DAO.FriendDao;
import com.chen.blogbackend.DAO.UserGroupDao;
import com.chen.blogbackend.entities.Account;
import com.chen.blogbackend.entities.Friend;
import com.chen.blogbackend.entities.Relationship;
import com.chen.blogbackend.entities.UserGroup;
import com.chen.blogbackend.mappers.AccountParser;
import com.chen.blogbackend.mappers.RelationshipParser;
import com.chen.blogbackend.responseMessage.PagingMessage;
import com.chen.blogbackend.util.BloomFilterIOTool;
import com.chen.blogbackend.util.RandomUtil;
import com.datastax.oss.driver.api.core.CqlSession;
import com.datastax.oss.driver.api.core.PagingIterable;
import com.datastax.oss.driver.api.core.cql.*;
import com.datastax.oss.driver.shaded.guava.common.hash.BloomFilter;
import com.datastax.oss.driver.shaded.guava.common.hash.Funnels;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import redis.clients.jedis.Jedis;

import javax.management.relation.Relation;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadPoolExecutor;

@Service
public class FriendsService {

    @Autowired
    CqlSession session;

    @Autowired
    Jedis redisClient;

    FriendDao friendDao;
    UserGroupDao userGroupDao;

    private PreparedStatement addUsersInGroups;
    private PreparedStatement addUserOwnedGroups;
    private PreparedStatement delFriendByUserId;
    private PreparedStatement delUsersInGroups;
    private PreparedStatement delOwnGroups;
    private PreparedStatement delUserGroup;
    private PreparedStatement getFollowersByUserId;
    private PreparedStatement getFollowersByIdolId;
    private PreparedStatement getUsersIntro;
    private PreparedStatement initUsersIntro;
//  private   PreparedStatement updateFriendDirectionByIdolId;
//  private   PreparedStatement updateFriendDirectionByUserId;
    private PreparedStatement block;
    private PreparedStatement unBlock;
    private PreparedStatement deleteFriend;
    private PreparedStatement follows;
    private PreparedStatement insertFollowRelationship;
    private PreparedStatement deleteFollowRelationship;
    private PreparedStatement addFriend;
    private PreparedStatement getAllFriends;
    private PreparedStatement getIdolsById;
    private PreparedStatement addIdol;
    private PreparedStatement deleteIdol;


    ThreadPoolExecutor executor;




    @PostConstruct
    public void init() throws IOException {
            delFriendByUserId = session.prepare("delete from relationship.followers_by_user_id where user_id=? and friend_id = ?;");
            getFollowersByUserId = session.prepare("select * from relationship.followers_by_user_id where user_id=?;");
            initUsersIntro = session.prepare("insert into userInfo.user_information (user_id, user_name)  values (?,?) ");
            getUsersIntro = session.prepare("select * from userInfo.user_information where user_id=?;");
            follows = session.prepare("select * from relationship.followers_by_user_id where user_id=? and friend_id = ?;");
            insertFollowRelationship = session.prepare("insert into relationship.followers_by_user_id (user_id, friend_id) values(?, ?)");
            deleteFollowRelationship = session.prepare("delete from relationship.followers_by_user_id where user_id = ? and friend_id = ?");
            deleteFriend= session.prepare("delete from relationship.friends_by_user_id where user_id = ? and friend_id = ?");
            addFriend = session.prepare("insert into relationship.friends_by_user_id (user_id, friend_id, name) values(?, ?, ?);");
            getAllFriends = session.prepare("select * from relationship.friends_by_user_id where user_id = ?;");
            getIdolsById = session.prepare("select * from relationship.idol_by_user_id where user_id = ?");
            addIdol = session.prepare("insert into relationship.idol_by_user_id (user_id, friend_id) values (?,?) ");
            deleteIdol = session.prepare("delete from relationship.idol_by_user_id  where user_id=? and friend_id=?");

    }

    public List<Relationship> getFollowersByUserId(String userId) {
        ResultSet execute = session.execute(getIdolsById.bind(userId));
        return RelationshipParser.parseToRelationship(execute);
    }

    public int getRelationship(String userid, String userIdToFollow) throws Exception {
        boolean flag = false;
        System.out.println(follows);
        ResultSet follow = session.execute(follows.bind(userid, userIdToFollow));
        if (follow.all().size() > 0) flag = true;

        ResultSet reverseFollow = session.execute(follows.bind(userIdToFollow, userid));
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

    public boolean follow(String fanId, String idolId, String name) throws Exception {
        if (fanId == null || idolId == null) return false;
        ResultSet execute = session.execute(insertFollowRelationship.bind(fanId,idolId));
        ResultSet set3 = session.execute(addIdol.bind(idolId,fanId));
        int relationship = getRelationship(fanId, idolId);
        if (relationship == 11) {
            ResultSet set1 = session.execute(addFriend.bind(fanId, idolId, name));
            ResultSet set2 = session.execute(addFriend.bind(idolId, fanId, name));

            return execute.getExecutionInfo().getErrors().size() == 0 &&
                    set1.getExecutionInfo().getErrors().size() == 0 &&
                    set2.getExecutionInfo().getErrors().size() == 0 &&
                    set3.getExecutionInfo().getErrors().size() == 0;

        }
        return execute.getExecutionInfos().get(0).getErrors().size() == 0 &&
                set3.getExecutionInfo().getErrors().size() == 0;
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
        ResultSet set1 = session.execute(deleteFriend.bind(fanId, idolId));
        ResultSet set2 = session.execute(deleteFriend.bind(idolId, fanId));
        ResultSet set3 = session.execute(deleteIdol.bind(idolId,fanId));
        return execute.getExecutionInfo().getErrors().size() == 0 &&
                set1.getExecutionInfo().getErrors().size() == 0 &&
                set2.getExecutionInfo().getErrors().size() == 0 &&
                set3.getExecutionInfo().getErrors().size() == 0;
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
        return execute.getExecutionInfo().getErrors().size() == 0;
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
        return execute.getExecutionInfo().getErrors().size() == 0;
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

    public List<Relationship> getFriends(String userId) {
        System.out.println(userId);
        ResultSet execute = session.execute(getAllFriends.bind(userId));
        return RelationshipParser.parseToRelationship(execute);

    }



}
