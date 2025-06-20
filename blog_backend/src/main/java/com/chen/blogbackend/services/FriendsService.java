package com.chen.blogbackend.services;

import com.chen.blogbackend.entities.*;
import com.chen.blogbackend.mappers.InvitationMapper;
import com.chen.blogbackend.mappers.RelationshipParser;
import com.chen.blogbackend.util.RandomUtil;
import com.datastax.oss.driver.api.core.CqlSession;
import com.datastax.oss.driver.api.core.cql.*;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import redis.clients.jedis.Jedis;

import java.io.IOException;
import java.time.Duration;
import java.time.Instant;
import java.time.temporal.TemporalUnit;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.concurrent.ThreadPoolExecutor;

@Service
public class FriendsService {

    private static final Logger logger = LoggerFactory.getLogger(FriendsService.class);

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
    private PreparedStatement getFollowRelationship;
    private PreparedStatement block;
    private PreparedStatement unBlock;
    private PreparedStatement deleteFriend;
    private PreparedStatement insertFollowRelationship;
    private PreparedStatement deleteFollowRelationship;
    private PreparedStatement addFriend;
    private PreparedStatement getAllFriends;
    private PreparedStatement getIdolsById;
    private PreparedStatement getInvitations;
    private PreparedStatement createGroup;

    private PreparedStatement setInvitations;

    ThreadPoolExecutor executor;
    @Autowired
    private ChatGroupService chatGroupService;

    private static boolean stringLessThan(String str1, String str2) {
        // 使用compareTo方法进行比较
        return str1.compareTo(str2) < 0;
    }

    @PostConstruct
    public void init() throws IOException {
        logger.info("Initializing FriendsService prepared statements");
        try {
            delFriendByUserId = session.prepare("delete from relationship.following_relationship where user_id=? and friend_id = ?;");
            initUsersIntro = session.prepare("insert into userInfo.user_information (user_id, user_name)  values (?,?) ");
            getUsersIntro = session.prepare("select * from userInfo.user_information where user_id=?;");
            insertFollowRelationship = session.prepare("insert into relationship.following_relationship (user_id, friend_id) values(?, ?)");
            deleteFollowRelationship = session.prepare("delete from relationship.following_relationship where user_id = ? and friend_id = ?");
            deleteFriend = session.prepare("delete from relationship.following_relationship where user_id = ? and friend_id = ?");
            addFriend = session.prepare("insert into relationship.following_relationship (user_id, friend_id, name) values(?, ?, ?);");
            getIdolsById = session.prepare("select * from relationship.following_relationship where user_id = ?");
            getFollowersByUserId = session.prepare("select * from relationship.following_relationship where friend_id =?;");
            getFollowRelationship = session.prepare("select * from relationship.following_relationship where user_id = ? and friend_id = ?");

            setInvitations = session.prepare("insert into chat.invitation (type, group_id, user_id, expire_time, code, create_time) values(?,?,?,?,?,?);");
            getInvitations = session.prepare("select * from chat.invitation where type = ? and group_id = ? and user_id = ?;");
            logger.info("FriendsService prepared statements initialized successfully");
        } catch (Exception e) {
            logger.error("Failed to initialize FriendsService prepared statements", e);
            throw e;
        }
    }

    public List<Relationship> getFollowersByUserId(String userId) {
        logger.debug("Getting followers for user: {}", userId);
        try {
            ResultSet execute = session.execute(getFollowersByUserId.bind(userId));
            List<Relationship> relationships = RelationshipParser.parseToRelationship(execute);
            logger.debug("Found {} followers for user: {}", relationships.size(), userId);
            return relationships;
        } catch (Exception e) {
            logger.error("Error getting followers for user: {}", userId, e);
            throw e;
        }
    }

    public List<Relationship> getFriendsByUserId(String userId) {
        logger.debug("Getting friends for user: {}", userId);
        ArrayList<Relationship> result = new ArrayList<>();

        try {
            ResultSet execute = session.execute(getFollowersByUserId.bind(userId));
            List<Relationship> relationships = RelationshipParser.parseToRelationship(execute);
            HashSet<String> hashSet = new HashSet<>();
            for (Relationship relationship : relationships) {
                hashSet.add(relationship.getUserId());
            }

            ResultSet execute1 = session.execute(getIdolsById.bind(userId));
            List<Relationship> relationships1 = RelationshipParser.parseToRelationship(execute1);
            for (Relationship relationship : relationships1) {
                if (hashSet.contains(relationship.getFriendId())) {
                    result.add(relationship);
                }
            }

            logger.debug("Found {} friends for user: {}", result.size(), userId);
            return result;
        } catch (Exception e) {
            logger.error("Error getting friends for user: {}", userId, e);
            throw e;
        }
    }

    public int getRelationship(String userid, String userIdToFollow) throws Exception {
        logger.debug("Getting relationship between user: {} and user: {}", userid, userIdToFollow);
        try {
            boolean flag = false;

            ResultSet follow = session.execute(getFollowRelationship.bind(userid, userIdToFollow));
            if (!follow.all().isEmpty()) flag = true;

            ResultSet reverseFollow = session.execute(getFollowRelationship.bind(userIdToFollow, userid));
            List<Row> all = reverseFollow.all();

            int relationshipType;
            if (all.size() == 0) {
                relationshipType = flag ? 10 : 0;
            } else if (all.size() == 1) {
                relationshipType = flag ? 11 : 1;
            } else {
                logger.error("Invalid relationship state between users: {} and {}", userid, userIdToFollow);
                throw new Exception("error relationship occurred internal!");
            }

            logger.debug("Relationship type {} between user: {} and user: {}", relationshipType, userid, userIdToFollow);
            return relationshipType;
        } catch (Exception e) {
            logger.error("Error getting relationship between user: {} and user: {}", userid, userIdToFollow, e);
            throw e;
        }
    }

    public List<Relationship> getIdolsByUserId(String userId) {
        logger.debug("Getting idols for user: {}", userId);
        try {
            ResultSet set = session.execute(getIdolsById.bind(userId));
            List<Relationship> relationships = RelationshipParser.parseToRelationship(set);
            logger.debug("Found {} idols for user: {}", relationships.size(), userId);
            return relationships;
        } catch (Exception e) {
            logger.error("Error getting idols for user: {}", userId, e);
            throw e;
        }
    }

    public boolean follow(String fanId, String idolId, String name) throws Exception {
        logger.info("User {} attempting to follow user {}", fanId, idolId);

        if (fanId == null || idolId == null) {
            logger.warn("Follow operation failed: fanId or idolId is null");
            return false;
        }

        try {
            ResultSet execute = session.execute(insertFollowRelationship.bind(fanId, idolId));
            boolean success = execute.getExecutionInfos().get(0).getErrors().size() == 0;

            if (success) {
                logger.info("User {} successfully followed user {}", fanId, idolId);
            } else {
                logger.warn("Follow operation failed for user {} to follow user {}", fanId, idolId);
            }

            return success;
        } catch (Exception e) {
            logger.error("Error during follow operation: user {} trying to follow user {}", fanId, idolId, e);
            throw e;
        }
    }

    public boolean unfollow(String fanId, String idolId) {
        logger.info("User {} attempting to unfollow user {}", fanId, idolId);

        try {
            ResultSet execute = session.execute(deleteFollowRelationship.bind(fanId, idolId));
            //ResultSet execute1 = session.execute(deleteFollowRelationship.bind(idolId, fanId));

            boolean success = execute.getExecutionInfo().getErrors().size() == 0;
//                    &&execute1.getExecutionInfo().getErrors().size() == 0;

            if (success) {
                logger.info("User {} successfully unfollowed user {}", fanId, idolId);
            } else {
                logger.warn("Unfollow operation failed for user {} to unfollow user {}", fanId, idolId);
            }

            return success;
        } catch (Exception e) {
            logger.error("Error during unfollow operation: user {} trying to unfollow user {}", fanId, idolId, e);
            throw e;
        }
    }

    // user group not group chat.
//    public boolean createGroup(UserGroup group) {
//        logger.info("Creating group: {}", group.getName());
//        try {
//            group.setGroupId(keyService.getIntKey("group_id"));
//            userGroupDao.insert(group.getGroupId(), group.getName(), group.getGroup_avatar(), group.getCount());
//            logger.info("Group created successfully with ID: {}", group.getGroupId());
//            return true;
//        } catch (Exception e) {
//            logger.error("Error creating group: {}", group.getName(), e);
//            throw e;
//        }
//    }
//
//    public boolean removeGroup(String groupId) {
//        logger.info("Removing group: {}", groupId);
//        try {
//            List<String> friendIdsByGroupId = getFriendIdsByGroupId(groupId);
//            BatchStatementBuilder batchStatementBuilder = BatchStatement.builder(BatchType.UNLOGGED);
//            for (String userId : friendIdsByGroupId) {
//                batchStatementBuilder.addStatements(delUsersInGroups.bind(userId, groupId), delOwnGroups.bind(groupId, userId));
//            }
//            batchStatementBuilder.addStatements(delUserGroup.bind(groupId));
//            ResultSet execute = session.execute(batchStatementBuilder.build());
//
//            boolean success = execute.getExecutionInfo().getErrors().size() == 0;
//            if (success) {
//                logger.info("Group {} removed successfully", groupId);
//            } else {
//                logger.warn("Failed to remove group: {}", groupId);
//            }
//            return success;
//        } catch (Exception e) {
//            logger.error("Error removing group: {}", groupId, e);
//            throw e;
//        }
//    }

    public boolean deleteFromGroup(String user, List<String> usersToRemove, String groupFrom) {
        logger.info("User {} deleting {} users from group {}", user, usersToRemove.size(), groupFrom);

        try {
            BatchStatementBuilder batchStatementBuilder = BatchStatement.builder(BatchType.UNLOGGED);
            for (String userId : usersToRemove) {
                batchStatementBuilder.addStatements(delUsersInGroups.bind(groupFrom, userId));
            }
            ResultSet execute = session.execute(batchStatementBuilder.build());

            boolean success = execute.getExecutionInfo().getErrors().size() == 0;
            if (success) {
                logger.info("Successfully deleted {} users from group {}", usersToRemove.size(), groupFrom);
            } else {
                logger.warn("Failed to delete users from group {}", groupFrom);
            }

            return success;
        } catch (Exception e) {
            logger.error("Error deleting users from group {}", groupFrom, e);
            throw e;
        }
    }

    public boolean deleteFromGroup(String user, String userToRemove, String groupFrom) {
        logger.debug("User {} deleting user {} from group {}", user, userToRemove, groupFrom);
        List<String> friends = new ArrayList<>();
        friends.add(userToRemove);
        return deleteFromGroup(user, friends, groupFrom);
    }

    public boolean initUserIntro(String userId) {
        logger.info("Initializing user introduction for user: {}", userId);

        try {
            String randomName = RandomUtil.generateRandomName();
            ResultSet execute = session.execute(initUsersIntro.bind(userId, randomName));

            boolean success = execute.getExecutionInfo().getErrors().size() == 0;
            if (success) {
                logger.info("User introduction initialized successfully for user: {} with name: {}", userId, randomName);
            } else {
                logger.warn("Failed to initialize user introduction for user: {}", userId);
            }

            return success;
        } catch (Exception e) {
            logger.error("Error initializing user introduction for user: {}", userId, e);
            throw e;
        }
    }

    public boolean verifyInvitation(String userId, String invitationCode) throws Exception {
        logger.warn("Verifying invitation: {} for {}", invitationCode, userId );
        ResultSet execute = session.execute(getInvitations.bind(invitationCode));
        List<Invitation> invitations = InvitationMapper.parseInvitation(execute);
        if (invitations.isEmpty() || invitations.get(0).getExpireTime().isBefore(Instant.now())) {
            throw new Exception("error code");
        } else {
            String targetType = invitations.get(0).getTargetType();
            if (targetType.equals("group")) {
                Long groupId = invitations.get(0).getGroupId();
                return chatGroupService.joinGroup(userId, groupId);

            } else if (targetType.equals("single")) {
                String targetUserId = invitations.get(0).getUserId();

                boolean follow = follow(targetUserId, userId, "");
                boolean follow1 = follow(userId, targetUserId, "");

                return follow1 && follow;
            }
        }
        return true;
    }



    public String getInvitations(String type, String userId, Long groupId) throws Exception {
        if (type.equals("group")) {
            ResultSet execute = session.execute(getInvitations.bind(type, groupId, userId));
            List<Invitation> invitations = InvitationMapper.parseInvitation(execute);
            if (invitations.isEmpty() || invitations.get(0).getExpireTime().isBefore(Instant.now())) {
                String randomString = RandomUtil.generateRandomString(8);
                ResultSet execute1 = session.execute(setInvitations.bind(type, groupId, userId, Instant.now().plus(Duration.ofDays(7)), randomString,Instant.now()));
                if (execute1.getExecutionInfo().getErrors().isEmpty()) {
                    return  randomString;
                } else {
                    throw new Exception(execute1.getExecutionInfo().getErrors().toString());
                }
            } else {
                Invitation invitation = invitations.get(0);
                return invitation.getToken();
            }
        } else if (type.equals("single")) {
            ResultSet execute = session.execute(getInvitations.bind(type, groupId, userId));
            List<Invitation> invitations = InvitationMapper.parseInvitation(execute);
            if (invitations.isEmpty() || invitations.get(0).getExpireTime().isBefore(Instant.now())) {
                String randomString = RandomUtil.generateRandomString(8);
                ResultSet execute1 = session.execute(setInvitations.bind(type, groupId, userId, Instant.now().plus(Duration.ofMinutes(5)), randomString,Instant.now()));
                if (execute1.getExecutionInfo().getErrors().isEmpty()) {
                    return randomString;
                } else {
                    throw new Exception(execute1.getExecutionInfo().getErrors().toString());
                }
            } else {
                Invitation invitation = invitations.get(0);
                return invitation.getToken();
            }

        } else {
            throw new Exception("Unsupported invitation type: " + type);
        }
    }
}