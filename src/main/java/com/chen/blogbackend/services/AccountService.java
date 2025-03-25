package com.chen.blogbackend.services;

import com.chen.blogbackend.entities.*;
import com.chen.blogbackend.mappers.AccountParser;
import com.chen.blogbackend.util.PasswordEncryption;
import com.chen.blogbackend.util.RandomUtil;
import com.chen.blogbackend.util.TokenUtil;
import com.datastax.oss.driver.api.core.CqlSession;
import com.datastax.oss.driver.api.core.cql.*;
import com.datastax.oss.driver.api.querybuilder.QueryBuilder;
import com.datastax.oss.driver.api.querybuilder.insert.Insert;
import com.datastax.oss.driver.api.querybuilder.select.Select;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.time.Instant;
import java.time.LocalDate;
import java.util.ArrayList;
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
    PreparedStatement getPassword;


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

    PreparedStatement getAuth;
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

        getPassword = session.prepare("select password from userinfo.user_auth where userid=?");
        getAuth = session.prepare("select userid, roleid from userinfo.user_auth where userid=?");

        insertUserDetails = session.prepare("insert into userinfo.user_information (user_id, apps, avatar, " +
                "birthdate, gender, intro, user_name,location,language,country) values(?,?,?,?,?,?,?,?,?,?);");
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

    public Account getProfileById(String userId) {
        ResultSet execute = session.execute(searchResult.bind(userId));

        List<Account> accounts = AccountParser.userDetailParser(execute);
        if (accounts.isEmpty()) {
            return null;
        }
        return accounts.get(0);
    }


    public boolean insertUserDetails(Account userDetail) {
        ResultSet execute = session.execute(insertUserDetails.bind(userDetail.getUserId(), userDetail.getApps(),
                userDetail.getAvatar(), userDetail.getDateOfBirth(), userDetail.getGender(),
                userDetail.getIntroduction(), userDetail.getUserName(), userDetail.getLocation(),
                userDetail.getLanguage(), userDetail.getCountry()));
        return execute.getExecutionInfo().getErrors().size() == 0;
    }

    public Account getAccountDetailsById(String userId) {
        if (userId == null || userId.length() == 0 ) {
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
        System.out.println(token1);
        //token1.getUserId().equals(userId)
        if( null == token1.getUserId()){
            System.out.println("not find userId");
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
        return execute.getExecutionInfo().getErrors().size() == 0;
    }

    public boolean insertStep2(String userId) {

        return false;
    }


    public boolean insertStep3(String password,String userId) {
        String encrypted = RandomUtil.getMD5(password);
        ResultSet execute = session.execute(insertPassword.bind(encrypted, userId));
        if (friendsService.initUserIntro(userId)) {
            return execute.getExecutionInfo().getErrors().size()==0;
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

    public boolean upsertRole(Role role) {
            // Prepare the CQL query to insert or update the role data
        Insert insert = QueryBuilder.insertInto("userInfo", "roles")
                .value("roleId", QueryBuilder.literal(role.getRoleId()))
                .value("roleName", QueryBuilder.literal(role.getRoleName()))
                .value("allowedPaths", QueryBuilder.literal(role.getAllowedPaths()));

        // Execute the query
        session.execute(insert.build());
        System.out.println("Role upserted: " + role.getRoleName());
        return true;
    }
    
    public Role getRoleById(int roleId) {
        Select select = QueryBuilder.selectFrom("userInfo", "roles")
                .column("roleId")
                .column("roleName")
                .column("allowedPaths")
                .whereColumn("roleId").isEqualTo(QueryBuilder.literal(roleId));

        ResultSet resultSet = session.execute(select.build());
        List<Row> all = resultSet.all();

        if (all.isEmpty()) {
            System.out.println("No role found with the given roleId: " + roleId);
            return null;
        }

        // Extract the result from the ResultSet
        var row = resultSet.one();
        String roleName = row.getString("roleName");
        List<String> allowedPaths = row.getList("allowedPaths", String.class);

        return new Role(roleId, roleName, allowedPaths);
    }

    public List<Role> getRoles() {
        Select select = QueryBuilder.selectFrom("userInfo", "roles")
                .column("roleId")
                .column("roleName")
                .column("allowedPaths");

        ResultSet resultSet = session.execute(select.build());

        List<Role> roles = new ArrayList<>();

        // Iterate over the result set and convert rows to Role objects
        resultSet.forEach(row -> {
            int roleId = row.getInt("roleId");
            String roleName = row.getString("roleName");
            List<String> allowedPaths = row.getList("allowedPaths", String.class);
            roles.add(new Role(roleId, roleName, allowedPaths));
        });

        return roles;
    }
    public boolean updateUserRole( String userId, int newRoleId) {
        // 1. 检查用户是否存在
        ResultSet resultSet = session.execute("SELECT roleId FROM userinfo.user_auth WHERE userId = ?", userId);
        if (resultSet.all().isEmpty()) {
            // 如果没有找到该用户，返回 false
            return false;
        }
        ResultSet resultSet1 = session.execute("SELECT roleId FROM userinfo.roles WHERE roleid = ?", newRoleId);
        if (resultSet1.all().isEmpty()) {
            return false;
        }
        // 2. 如果用户存在，执行更新操作
        // 更新用户的 roleId
        PreparedStatement updateRoleStmt = session.prepare("UPDATE userinfo.user_auth SET roleId = ? WHERE userId = ?");
        BoundStatement boundStatement = updateRoleStmt.bind(newRoleId, userId);
        session.execute(boundStatement);

        // 3. 确认是否更新成功
        resultSet = session.execute("SELECT roleId FROM userinfo.user_auth WHERE userId = ?", userId);
        Row row = resultSet.one();
        if (row != null && row.getInt("roleId") == newRoleId) {
            return true; // 更新成功
        }

        return false; // 更新失败
    }

    public boolean DeleteUserRole(int roleId) {
        String deleteQuery = "DELETE FROM userInfo.roles WHERE roleid = ?";
        try {
            session.execute(deleteQuery, roleId);
            return true; // 删除成功
        } catch (Exception e) {
            System.err.println("Error deleting user role: " + e.getMessage());
            return false; // 删除失败
        }
    }

    // 根据角色ID和请求的路径检查权限
    public boolean hasAccess(int roleId, String path) {
        // 1. 查询角色权限
        String query = "SELECT allowedPaths FROM userinfo.roles WHERE roleId = ?";
        SimpleStatement statement = SimpleStatement.newInstance(query, roleId);

        // 2. 执行查询并获取结果
        Row row = session.execute(statement).one();

        // 3. 如果没有找到角色，则返回没有权限
        if (row == null) {
            return false;
        }

        // 4. 获取该角色允许访问的路径列表
        List<String> allowedPaths = row.getList("allowedPaths", String.class);

        // 5. 检查路径是否在允许的路径列表中
        if (allowedPaths != null && allowedPaths.contains(path)) {
            return true;
        }

        return false;
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
            e.printStackTrace();
            return false;
        }
    }
}
