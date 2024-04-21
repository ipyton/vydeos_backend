package com.chen.blogbackend.services;

import com.chen.blogbackend.entities.Auth;
import com.chen.blogbackend.entities.Friend;
import com.chen.blogbackend.mappers.AccountParser;
import com.chen.blogbackend.util.PasswordEncryption;
import com.chen.blogbackend.util.TokenUtil;
import com.chen.blogbackend.entities.Account;
import com.chen.blogbackend.entities.Token;
import com.datastax.oss.driver.api.core.CqlSession;
import com.datastax.oss.driver.api.core.cql.PreparedStatement;
import com.datastax.oss.driver.api.core.cql.ResultSet;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;

@Service
public class AccountService {


    @Autowired
    CqlSession session;


    @Autowired
    SearchService searchService;

    @Autowired
    FriendsService friendsService;


    PreparedStatement insertAccount;
    PreparedStatement getAccount;
    PreparedStatement setToken;
    PreparedStatement getToken;

    PreparedStatement insertUserDetails;
    PreparedStatement getUserDetails;

    PreparedStatement updateEmail;
    PreparedStatement updatePassword;
    PreparedStatement updatePhoneNumber;

    PreparedStatement searchResult;

    PreparedStatement insertUserName;
    PreparedStatement insertPassword;
    PreparedStatement getIsTemp;
    PreparedStatement revertTempStat;

    @PostConstruct
    public void init(){
//        AppMapper appMapper = new AppMapperBuilder(session).withDefaultKeyspace("apps").build();
//        appDao = appMapper.appDao();
//        getApplicationsSimple = session.prepare("select applicationId, name, ratings, pictures, author from apps.application;");
//        getDetailedIntroduction = session.prepare("select * from apps.application where applicationId=?;");
//        getInstalledApps = session.prepare("select * from apps.user_apps where userId=?;");
//        deleteApp = session.prepare("delete from apps.user_apps where userId = ? and applicationId = ?");
//        saveComment = session.prepare("insert into apps.app_comment (appId, commentId , userId , userName, userAvatar, comment , rate, commentDateTime) values (?,?,?,?,?,?,?,?)");
//        saveApplication = session.prepare("insert into apps.application (applicationid, version," +
//                " author, history_versions, hot_comments,introduction, lastmodified, name, pictures, ratings, system_requirements, type) values (?,?,?,?,?,?,?,?,?,?,?,?)");
        insertAccount = session.prepare("insert into userinfo.user_auth (userid, email, password, telephone) values(?,?,?,?)");
        getAccount = session.prepare("select * from userinfo.user_auth where userid=?");
        setToken = session.prepare("insert into userinfo.user_tokens (user_token, userId, invalid_date) values (?,?,?)");
        getToken = session.prepare("select * from userinfo.user_tokens where user_token=?");
        searchResult = session.prepare("select user_id, user_name, intro, avatar from userinfo.user_information where user_id=?");


        insertUserDetails = session.prepare("insert into userinfo.user_information (user_id, apps, avatar, " +
                "birthdate, gender, intro, user_name,location) values(?,?,?,?,?,?,?,?);");
        getUserDetails = session.prepare("select * from userinfo.user_information where user_id = ?");

        updateEmail = session.prepare("update userinfo.user_auth set email = ? where userId = ?");
        updatePassword = session.prepare("update userinfo.user_auth set password = ? where userId = ?");
        updatePhoneNumber = session.prepare("update userinfo.user_auth set telephone = ? where userId = ?");
        insertUserName = session.prepare("insert into userinfo.user_auth (userid, email, temp) values(?,?, true)");

        insertPassword = session.prepare("update userinfo.user_auth set password=?, temp=false where userid=?");
        getIsTemp = session.prepare("select temp from userinfo.user_auth where userid=?");


    }


    public List<Account> searchUserById(String userId) {
        ResultSet execute = session.execute(searchResult.bind(userId));
        return AccountParser.userDetailParser(execute);
    }


    public boolean insertUserDetails(Account userDetail) {
        ResultSet execute = session.execute(insertUserDetails.bind(userDetail.getUserId(), userDetail.getApps(),
                userDetail.getAvatar(), userDetail.getDateOfBirth(), userDetail.getGender(),
                userDetail.getIntroduction(), userDetail.getUserName(), userDetail.getLocation()));
        return execute.getExecutionInfo().getErrors().size() == 0;
    }

    public Account getAccountDetailsById(String userId) {
        if (userId == null || userId.length() == 0 ) {
            return null;
        }
        ResultSet execute = session.execute(getUserDetails.bind(userId));
        List<Account> account = AccountParser.userDetailParser(execute);
        if (account.size() == 0) {
            return null;
        }
        return account.get(0);
    }


    public Account getFriendDetailsById(String userId, String userIdToFollow) throws Exception {
        System.out.println("friend details " + userIdToFollow) ;
        ResultSet execute = session.execute(getUserDetails.bind(userIdToFollow));

        List<Account> friendSet = AccountParser.userDetailParser(execute);
        if (friendSet.size() == 0) {
            return null;
        }
        Account friend = friendSet.get(0);
        System.out.println("find friend" + friend);
        if (friend != null)  friend.setRelationship(friendsService.getRelationship(userId, userIdToFollow));
        return friend;
    }



    public boolean insert(Auth account) {
        ResultSet execute = session.execute(insertAccount.bind(account.getEmail(), account.getEmail(),
                account.getPassword(), account.getTelephone()));
        return 0 == execute.getExecutionInfo().getErrors().size();
    }

    public Account selectAccount(String accountID) {
        ResultSet execute = session.execute(getAccount.bind(accountID));
        List<Account> tokens = AccountParser.userDetailParser(execute);

        if (0 != execute.getExecutionInfo().getErrors().size() || tokens.size() != 1) {
            System.out.println("error!!!!");
            return null;
        }
        return tokens.get(0);

    }


    public boolean haveValidLogin(String token) {
        if (null == token || 0 == token.length()) return false;
        Token token1 = TokenUtil.resolveToken(token);
        //token1.getUserId().equals(userId)
        if( null == token1.getUserId()){
            return false;
        }
        return token1.getExpireDatetime().isAfter(Instant.now());
    }

    public boolean validatePassword(String userId,String password){
        password = PasswordEncryption.encryption(password);
        ResultSet account = session.execute(getAccount.bind(userId));
        List<Auth> accounts = AccountParser.accountParser(account);
        if (accounts.size() != 1) return false;
        System.out.println(accounts.get(0).getPassword());
        return 1 == accounts.size() && accounts.get(0).getPassword().equals(password);
    }

    public boolean setToken(Token token) {
        ResultSet set = session.execute(setToken.bind(token.getTokenString(), token.getUserId(),
                token.getExpireDatetime()));
        return set.getExecutionInfo().getErrors().size() == 0;
    }

    public boolean updateEmail(String userId, String email) {
        ResultSet set = session.execute(setToken.bind(email,userId));
        return set.getExecutionInfo().getErrors().size() == 0;
    }

    public boolean updatePhone(String userId, String phone) {
        ResultSet set = session.execute(setToken.bind(phone, userId));
        return set.getExecutionInfo().getErrors().size() == 0;
    }

    public boolean updatePassword(String userId, String password) {
        ResultSet set = session.execute(setToken.bind(password, userId));
        return set.getExecutionInfo().getErrors().size() == 0;
    }

    public boolean updateIndex(Friend friend) throws IOException, InterruptedException {
        searchService.setUserIndex(friend);
        return true;
    }

    public boolean addApplication() {
        return true;
    }

    public boolean insertStep1(String userId) {
        ResultSet judge = session.execute(getIsTemp.bind(userId));
        System.out.println(judge.all().size());

        if (judge.all().size() != 0) {
            return judge.all().get(0).getBoolean("temp");
        }
        ResultSet execute = session.execute(insertUserName.bind(userId, userId));
        return execute.getExecutionInfo().getErrors().size()==0;
    }

    public boolean insertStep3(String password,String userId) {
        ResultSet execute = session.execute(insertPassword.bind(password, userId));
        if (friendsService.initUserIntro(userId)) {
            return execute.getExecutionInfo().getErrors().size()==0;
        }
        return false;
    }
}
