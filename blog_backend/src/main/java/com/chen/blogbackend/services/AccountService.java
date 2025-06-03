package com.chen.blogbackend.services;

import com.chen.blogbackend.entities.*;
import com.chen.blogbackend.mappers.AccountParser;
import com.chen.blogbackend.mappers.CodeMapper;
import com.chen.blogbackend.util.EmailSender;
import com.chen.blogbackend.util.PasswordEncryption;
import com.chen.blogbackend.util.RandomUtil;
import com.chen.blogbackend.util.TokenUtil;
import com.datastax.oss.driver.api.core.CqlSession;
import com.datastax.oss.driver.api.core.cql.*;
import com.datastax.oss.driver.api.querybuilder.QueryBuilder;
import com.datastax.oss.driver.api.querybuilder.insert.Insert;
import com.datastax.oss.driver.api.querybuilder.select.Select;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.time.Instant;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Service
public class AccountService {

    private static final Logger logger = LoggerFactory.getLogger(AccountService.class);

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
    PreparedStatement getPassword;


    PreparedStatement insertUserDetails;
    PreparedStatement getUserDetails;


    PreparedStatement updateEmail;
    PreparedStatement updatePassword;
    PreparedStatement updatePhoneNumber;

    PreparedStatement searchResult;

    PreparedStatement insertUserName;
    PreparedStatement insertPasswordAndRoleId;
    PreparedStatement getIsTemp;
    PreparedStatement revertTempStat;

    PreparedStatement getAuth;

    PreparedStatement setVerificationCode;
    PreparedStatement getVerificationCode;
    PreparedStatement deleteToken;
    PreparedStatement invalidateVerificationCode;


    @Autowired
    private CqlSession cqlSession;

    @PostConstruct
    public void init(){

        insertAccount = session.prepare("insert into userinfo.user_auth (userid, email, password, telephone) values(?,?,?,?)");
        getAccount = session.prepare("select * from userinfo.user_auth where userid=?");
        setToken = session.prepare("insert into userinfo.user_tokens (user_token, userId, invalid_date) values (?,?,?)");
        getToken = session.prepare("select * from userinfo.user_tokens where user_token=?");
        deleteToken = session.prepare("delete from userinfo.user_tokens where user_token = ?");
        searchResult = session.prepare("select user_id, user_name, intro, avatar from userinfo.user_information where user_id=?");

        getPassword = session.prepare("select password from userinfo.user_auth where userid=?");
        getAuth = session.prepare("select userid, roleid from userinfo.user_auth where userid=?");

        insertUserDetails = session.prepare("insert into userinfo.user_information (user_id, apps, avatar, " +
                "birthdate, gender, intro, user_name,location,language,country) values(?,?,?,?,?,?,?,?,?,?);");
        getUserDetails = session.prepare("select * from userinfo.user_information where user_id = ?");

        updateEmail = session.prepare("update userinfo.user_auth set email = ? where userId = ?");
        updatePassword = session.prepare("update userinfo.user_auth set password = ? where userId = ?");
        updatePhoneNumber = session.prepare("update userinfo.user_auth set telephone = ? where userId = ?");
        insertUserName = session.prepare("insert into userinfo.user_auth (userid, email, temp) values(?,?, true)");

        insertPasswordAndRoleId = session.prepare("update userinfo.user_auth set password=?, temp=false, roleid = ? where userid=?");
        getIsTemp = session.prepare("select temp from userinfo.user_auth where userid=?");

        getVerificationCode = session.prepare("select * from userinfo.user_registration_code where kind = ? and userid = ?;");
        setVerificationCode = session.prepare("insert into userinfo.user_registration_code (kind, userid, code, expire_time) values(?, ?, ?, ?);");
        invalidateVerificationCode = session.prepare("delete from userinfo.user_registration_code where kind = ? and userid = ?;");
    }


    public List<Account> searchUserById(String userId) {
        ResultSet execute = session.execute(searchResult.bind(userId));
        return AccountParser.userDetailParser(execute);
    }

    public Account getProfileById(String userId) {
        ResultSet execute = session.execute(searchResult.bind(userId));

        List<Account> accounts = AccountParser.userDetailParser(execute);
        if (accounts.isEmpty()) {
            return null;
        }
        return accounts.get(0);
    }

    public boolean invalidateTokenByToken(String token) {
        session.execute(deleteToken.bind(token));
        return true;
    }

    public boolean insertUserDetails(Account userDetail) {
        ResultSet execute = session.execute(insertUserDetails.bind(userDetail.getUserId(), userDetail.getApps(),
                userDetail.getAvatar(), userDetail.getDateOfBirth(), userDetail.getGender(),
                userDetail.getIntroduction(), userDetail.getUserName(), userDetail.getLocation(),
                userDetail.getLanguage(), userDetail.getCountry()));
        return execute.getExecutionInfo().getErrors().isEmpty();
    }

    public Account getAccountDetailsById(String userId) {
        if (userId == null || userId.isEmpty()) {
            return null;
        }
        ResultSet execute = session.execute(getUserDetails.bind(userId));
        List<Account> account = AccountParser.userDetailParser(execute);
        if (account.isEmpty()) {
            return null;
        }
        return account.get(0);
    }

    //get user details
    public Account getFriendDetailsById(String userId, String userIdToFollow) throws Exception {
        System.out.println("friend details " + userIdToFollow) ;
        ResultSet execute = session.execute(getUserDetails.bind(userIdToFollow));

        List<Account> friendSet = AccountParser.userDetailParser(execute);
        if (friendSet.isEmpty()) {
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
        return execute.getExecutionInfo().getErrors().isEmpty();
    }

    public Account selectAccount(String accountID) {
        ResultSet execute = session.execute(getAccount.bind(accountID));
        List<Account> tokens = AccountParser.userDetailParser(execute);
        if (!execute.getExecutionInfo().getErrors().isEmpty() || tokens.size() != 1) {
            System.out.println("error!!!!");
            return null;
        }
        return tokens.get(0);
    }


    public boolean sendVerificationEmail(String email) {
        try {
            email = email.toLowerCase();
            ResultSet execute = session.execute(getVerificationCode.bind("step2", email));
            Verification verification = CodeMapper.codeMapper(execute);
            if (verification != null) {
                if (Instant.now().isBefore(verification.getExpiration().minusSeconds(540))) {
                    return false;
                }
            }
            String code = RandomUtil.generateRandomInt(6);
            session.execute(setVerificationCode.bind("step2", email, code, Instant.now().plusSeconds(600)));
            EmailSender.send("noah@vydeo.xyz", email, code);
        }
        catch (Exception e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    public String verifyCode(String email,String code) {

        ResultSet execute = session.execute(getVerificationCode.bind("step2",email));
        Verification verification = CodeMapper.codeMapper(execute);
        System.out.println(verification.getCode());
        if (verification == null) return null;
        if (Instant.now().isAfter(verification.getExpiration())) return null;
        if (verification.getCode().equals(code)) {
            String step3Code = RandomUtil.generateRandomString(10);
            ResultSet execute1 = session.execute(invalidateVerificationCode.bind("step2", email));
            ResultSet execute2 = session.execute(setVerificationCode.bind("step3", email, step3Code, Instant.now().plusSeconds(600)));
            if (!execute1.getExecutionInfo().getErrors().isEmpty() || !execute2.getExecutionInfo().getErrors().isEmpty()) {
                return null;
            }
            return step3Code;
        }
        return null;
    }

    public boolean haveValidLogin(String token) {
        if (null == token || token.isEmpty()) return false;
        Token token1 = TokenUtil.resolveToken(token);
        System.out.println(token1);
        //token1.getUserId().equals(userId)
        assert token1 != null;
        if( null == token1.getUserId()){
            return false;
        }
        System.out.println(token1.getExpireDatetime().isAfter(Instant.now()));
        return token1.getExpireDatetime().isAfter(Instant.now());
    }

    public Auth validatePassword(String userId,String password){
        password = RandomUtil.getMD5(password);
        ResultSet account = session.execute(getAccount.bind(userId));
        List<Auth> accounts = AccountParser.accountParser(account);
        if (accounts.size() != 1) return null;
        System.out.println(accounts.get(0).getPassword());
        System.out.println(password);
        return  accounts.get(0);
    }

    public boolean setToken(Token token) {
        ResultSet set = session.execute(setToken.bind(token.getTokenString(), token.getUserId(),
                token.getExpireDatetime()));
        return set.getExecutionInfo().getErrors().isEmpty();
    }

    public boolean updateEmail(String userId, String email) {
        ResultSet set = session.execute(setToken.bind(email,userId));
        return set.getExecutionInfo().getErrors().isEmpty();
    }

    public boolean updatePhone(String userId, String phone) {
        ResultSet set = session.execute(setToken.bind(phone, userId));
        return set.getExecutionInfo().getErrors().isEmpty();
    }

    public boolean updatePassword(String userId, String password) {
        ResultSet set = session.execute(setToken.bind(password, userId));
        return set.getExecutionInfo().getErrors().isEmpty();
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

        if (!judge.all().isEmpty()) {
            return judge.all().get(0).getBoolean("temp");
        }
        ResultSet execute = session.execute(insertUserName.bind(userId, userId));
        return execute.getExecutionInfo().getErrors().isEmpty();
    }

    public boolean insertStep2(String userId) {

        return false;
    }

    public boolean insertStep3(String code, String password,String userId) {
        String encrypted = RandomUtil.getMD5(password);
        ResultSet execute1 = session.execute(getVerificationCode.bind("step3", userId));
        if (!execute1.getExecutionInfo().getErrors().isEmpty()) {
            return false;
        }
        Verification verification = CodeMapper.codeMapper(execute1);
        if (verification != null && Instant.now().isBefore(verification.getExpiration()) && verification.getCode().equals(code)) {
            System.out.println("insert");
            ResultSet execute = session.execute(insertPasswordAndRoleId.bind(encrypted, 1, userId));
            return execute.getExecutionInfo().getErrors().isEmpty();
        }
        return false;
    }

    public boolean resetPassword(Object email, String oldPassword, String newPassword) {
        ResultSet password = session.execute(getPassword.bind(email));
        if (password.getExecutionInfo().getErrors().isEmpty()) {
            List<Row> all = password.all();
            if (all.isEmpty()) {
                return false;
            }
            Row row = all.get(0);
            String storedPassword = row.getString("password"); // 假设密码字段是 "password"
            String encrypted = RandomUtil.getMD5(oldPassword);
            // 假设你有一个方法来比较密码
            assert storedPassword != null;
            if (storedPassword.equals(encrypted)) {
                // 密码匹配，开始更新密码
                ResultSet execute = session.execute(updatePassword.bind(email, RandomUtil.getMD5(newPassword))); // 假设 newPassword 是新的密码
                if (execute.getExecutionInfo().getErrors().isEmpty()) {
                    return true; // 密码更新成功
                } else {
                    // 更新密码失败，处理错误
                    return false;
                }
            } else {
                // 密码不匹配
                return false;
            }
        } else {
            // 查询密码时出错
            return false;
        }
    }


    public Auth getAccountRoleById(String userEmail) {
        ResultSet execute = session.execute(getAuth.bind(userEmail));
        List<Auth> auths = AccountParser.accountParser(execute);
        if (auths.isEmpty()) {
            return null;
        }
        return auths.get(0);

    }

    public boolean deleteUser(String userId) {

        try {
            // Create a DELETE statement
            String query = "DELETE FROM userinfo.user_auth WHERE userid = ?";

            // Prepare the statement
            PreparedStatement preparedStatement = session.prepare(query);

            // Bind the userId value to the statement
            session.execute(preparedStatement.bind(userId));

            // Return true if the operation succeeded
            return true;
        } catch (Exception e) {
            // Log the error (this can be improved with proper logging)
            logger.error("Error deleting user: " + e.getMessage());
            return false;
        }
    }

    public boolean resetStep1(String userId) {
        ResultSet judge = session.execute(getIsTemp.bind(userId));

        if (judge.all().isEmpty()) {
            return false;
        }
        return judge.getExecutionInfo().getErrors().isEmpty();
    }

}
