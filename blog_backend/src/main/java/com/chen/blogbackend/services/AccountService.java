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
import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;
import com.google.api.client.googleapis.util.Utils;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.time.Instant;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

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
        logger.info("Initializing AccountService and preparing database statements");

        try {
            insertAccount = session.prepare("insert into userinfo.user_auth (userid, email, password, telephone, roleId) values(?,?,?,?,?)");
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

            logger.info("Successfully initialized all prepared statements for AccountService");
        } catch (Exception e) {
            logger.error("Failed to initialize AccountService prepared statements", e);
            throw new RuntimeException("AccountService initialization failed", e);
        }
    }

    public List<Account> searchUserById(String userId) {
        logger.debug("Searching for user by ID: {}", userId);
        try {
            ResultSet execute = session.execute(searchResult.bind(userId));
            List<Account> accounts = AccountParser.userDetailParser(execute);
            logger.debug("Found {} accounts for user ID: {}", accounts.size(), userId);
            return accounts;
        } catch (Exception e) {
            logger.error("Error searching for user by ID: {}", userId, e);
            return new ArrayList<>();
        }
    }

    public Account getProfileById(String userId) {
        logger.debug("Getting profile for user ID: {}", userId);
        try {
            ResultSet execute = session.execute(searchResult.bind(userId));
            List<Account> accounts = AccountParser.userDetailParser(execute);
            if (accounts.isEmpty()) {
                logger.warn("No profile found for user ID: {}", userId);
                return null;
            }
            logger.debug("Successfully retrieved profile for user ID: {}", userId);
            return accounts.get(0);
        } catch (Exception e) {
            logger.error("Error getting profile for user ID: {}", userId, e);
            return null;
        }
    }

    public boolean invalidateTokenByToken(String token) {
        logger.debug("Invalidating token");
        try {
            session.execute(deleteToken.bind(token));
            logger.info("Successfully invalidated token");
            return true;
        } catch (Exception e) {
            logger.error("Error invalidating token", e);
            return false;
        }
    }

    public boolean insertUserDetails(Account userDetail) {
        logger.debug("Inserting user details for user ID: {}", userDetail.getUserId());
        try {
            ResultSet execute = session.execute(insertUserDetails.bind(userDetail.getUserId(), userDetail.getApps(),
                    userDetail.getAvatar(), userDetail.getDateOfBirth(), userDetail.getGender(),
                    userDetail.getIntroduction(), userDetail.getUserName(), userDetail.getLocation(),
                    userDetail.getLanguage(), userDetail.getCountry()));
            boolean success = execute.getExecutionInfo().getErrors().isEmpty();
            if (success) {
                logger.info("Successfully inserted user details for user ID: {}", userDetail.getUserId());
            } else {
                logger.error("Failed to insert user details for user ID: {}, errors: {}",
                        userDetail.getUserId(), execute.getExecutionInfo().getErrors());
            }
            return success;
        } catch (Exception e) {
            logger.error("Error inserting user details for user ID: {}", userDetail.getUserId(), e);
            return false;
        }
    }

    public Account getAccountDetailsById(String userId) {
        if (userId == null || userId.isEmpty()) {
            logger.warn("Attempted to get account details with null or empty user ID");
            return null;
        }

        logger.debug("Getting account details for user ID: {}", userId);
        try {
            ResultSet execute = session.execute(getUserDetails.bind(userId));
            List<Account> account = AccountParser.userDetailParser(execute);
            if (account.isEmpty()) {
                logger.warn("No account details found for user ID: {}", userId);
                return null;
            }
            logger.debug("Successfully retrieved account details for user ID: {}", userId);
            return account.get(0);
        } catch (Exception e) {
            logger.error("Error getting account details for user ID: {}", userId, e);
            return null;
        }
    }

    public Account getFriendDetailsById(String userId, String userIdToFollow) throws Exception {
        logger.debug("Getting friend details - user: {}, friend: {}", userId, userIdToFollow);
        try {
            ResultSet execute = session.execute(getUserDetails.bind(userIdToFollow));
            List<Account> friendSet = AccountParser.userDetailParser(execute);

            if (friendSet.isEmpty()) {
                logger.warn("No friend details found for user ID: {}", userIdToFollow);
                return null;
            }

            Account friend = friendSet.get(0);
            logger.debug("Found friend details for user ID: {}", userIdToFollow);

            if (friend != null) {
                friend.setRelationship(friendsService.getRelationship(userId, userIdToFollow));
                logger.debug("Set relationship for users {} and {}", userId, userIdToFollow);
            }
            return friend;
        } catch (Exception e) {
            logger.error("Error getting friend details - user: {}, friend: {}", userId, userIdToFollow, e);
            throw e;
        }
    }

    public boolean insert(Auth account) {
        logger.debug("Inserting new auth account for email: {}", account.getEmail());
        try {
            ResultSet execute = session.execute(insertAccount.bind(account.getEmail(), account.getEmail(),
                    account.getPassword(), account.getTelephone(),1));
            boolean success = execute.getExecutionInfo().getErrors().isEmpty();
            if (success) {
                logger.info("Successfully inserted auth account for email: {}", account.getEmail());
            } else {
                logger.error("Failed to insert auth account for email: {}, errors: {}",
                        account.getEmail(), execute.getExecutionInfo().getErrors());
            }
            return success;
        } catch (Exception e) {
            logger.error("Error inserting auth account for email: {}", account.getEmail(), e);
            return false;
        }
    }

    public Account selectAccount(String accountID) {
        logger.debug("Selecting account for ID: {}", accountID);
        try {
            ResultSet execute = session.execute(getAccount.bind(accountID));
            List<Account> tokens = AccountParser.userDetailParser(execute);

            if (!execute.getExecutionInfo().getErrors().isEmpty() || tokens.size() != 1) {
                logger.error("Error selecting account or invalid result count for ID: {}, errors: {}, count: {}",
                        accountID, execute.getExecutionInfo().getErrors(), tokens.size());
                return null;
            }
            logger.debug("Successfully selected account for ID: {}", accountID);
            return tokens.get(0);
        } catch (Exception e) {
            logger.error("Error selecting account for ID: {}", accountID, e);
            return null;
        }
    }

    public boolean sendVerificationEmail(String email) {
        logger.info("Sending verification email to: {}", email);
        try {
            email = email.toLowerCase();
            ResultSet execute = session.execute(getVerificationCode.bind("step2", email));
            Verification verification = CodeMapper.codeMapper(execute);

            if (verification != null) {
                if (Instant.now().isBefore(verification.getExpiration().minusSeconds(540))) {
                    logger.warn("Verification code still valid for email: {}, not sending new code", email);
                    return false;
                }
            }

            String code = RandomUtil.generateRandomInt(6);
            session.execute(setVerificationCode.bind("step2", email, code, Instant.now().plusSeconds(600)));
            EmailSender.send("noah@vydeo.xyz", email, code);
            logger.info("Successfully sent verification email to: {}", email);
            return true;
        } catch (Exception e) {
            logger.error("Error sending verification email to: {}", email, e);
            return false;
        }
    }

    public String verifyCode(String email, String code) {
        logger.debug("Verifying code for email: {}", email);
        try {
            ResultSet execute = session.execute(getVerificationCode.bind("step2", email));
            Verification verification = CodeMapper.codeMapper(execute);

            if (verification == null) {
                logger.warn("No verification code found for email: {}", email);
                return null;
            }

            if (Instant.now().isAfter(verification.getExpiration())) {
                logger.warn("Verification code expired for email: {}", email);
                return null;
            }

            if (verification.getCode().equals(code)) {
                String step3Code = RandomUtil.generateRandomString(10);
                ResultSet execute1 = session.execute(invalidateVerificationCode.bind("step2", email));
                ResultSet execute2 = session.execute(setVerificationCode.bind("step3", email, step3Code, Instant.now().plusSeconds(600)));

                if (!execute1.getExecutionInfo().getErrors().isEmpty() || !execute2.getExecutionInfo().getErrors().isEmpty()) {
                    logger.error("Error updating verification codes for email: {}", email);
                    return null;
                }
                logger.info("Successfully verified code and generated step3 code for email: {}", email);
                return step3Code;
            } else {
                logger.warn("Invalid verification code provided for email: {}", email);
            }
            return null;
        } catch (Exception e) {
            logger.error("Error verifying code for email: {}", email, e);
            return null;
        }
    }

    public boolean haveValidLogin(String token) {
        logger.debug("Validating login token");
        try {
            if (null == token || token.isEmpty()) {
                logger.warn("Empty or null token provided for validation");
                return false;
            }

            Token token1 = TokenUtil.resolveToken(token);
            if (token1 == null) {
                logger.warn("Failed to resolve token");
                return false;
            }

            if (null == token1.getUserId()) {
                logger.warn("Token has null user ID");
                return false;
            }

            boolean isValid = token1.getExpireDatetime().isAfter(Instant.now());
            logger.debug("Token validation result: {}", isValid);
            return isValid;
        } catch (Exception e) {
            logger.error("Error validating login token", e);
            return false;
        }
    }

    public Auth validatePassword(String userId, String password) {
        logger.debug("Validating password for user ID: {}", userId);
        try {
            password = RandomUtil.getMD5(password);
            ResultSet account = session.execute(getAccount.bind(userId));
            List<Auth> accounts = AccountParser.accountParser(account);

            if (accounts.size() != 1) {
                logger.warn("Invalid account count for user ID: {} - expected 1, got {}", userId, accounts.size());
                return null;
            }
            logger.debug("Successfully validated password for user ID: {}", userId);
            return accounts.get(0);
        } catch (Exception e) {
            logger.error("Error validating password for user ID: {}", userId, e);
            return null;
        }
    }

    public boolean setToken(Token token) {
        logger.debug("Setting token for user ID: {}", token.getUserId());
        try {
            ResultSet set = session.execute(setToken.bind(token.getTokenString(), token.getUserId(),
                    token.getExpireDatetime()));
            boolean success = set.getExecutionInfo().getErrors().isEmpty();
            if (success) {
                logger.info("Successfully set token for user ID: {}", token.getUserId());
            } else {
                logger.error("Failed to set token for user ID: {}, errors: {}",
                        token.getUserId(), set.getExecutionInfo().getErrors());
            }
            return success;
        } catch (Exception e) {
            logger.error("Error setting token for user ID: {}", token.getUserId(), e);
            return false;
        }
    }

    public boolean updateEmail(String userId, String email) {
        logger.debug("Updating email for user ID: {}", userId);
        try {
            ResultSet set = session.execute(updateEmail.bind(email, userId));
            boolean success = set.getExecutionInfo().getErrors().isEmpty();
            if (success) {
                logger.info("Successfully updated email for user ID: {}", userId);
            } else {
                logger.error("Failed to update email for user ID: {}, errors: {}",
                        userId, set.getExecutionInfo().getErrors());
            }
            return success;
        } catch (Exception e) {
            logger.error("Error updating email for user ID: {}", userId, e);
            return false;
        }
    }

    public boolean updatePhone(String userId, String phone) {
        logger.debug("Updating phone for user ID: {}", userId);
        try {
            ResultSet set = session.execute(updatePhoneNumber.bind(phone, userId));
            boolean success = set.getExecutionInfo().getErrors().isEmpty();
            if (success) {
                logger.info("Successfully updated phone for user ID: {}", userId);
            } else {
                logger.error("Failed to update phone for user ID: {}, errors: {}",
                        userId, set.getExecutionInfo().getErrors());
            }
            return success;
        } catch (Exception e) {
            logger.error("Error updating phone for user ID: {}", userId, e);
            return false;
        }
    }

    public boolean updatePassword(String userId, String password) {
        logger.debug("Updating password for user ID: {}", userId);
        try {
            ResultSet set = session.execute(updatePassword.bind(password, userId));
            boolean success = set.getExecutionInfo().getErrors().isEmpty();
            if (success) {
                logger.info("Successfully updated password for user ID: {}", userId);
            } else {
                logger.error("Failed to update password for user ID: {}, errors: {}",
                        userId, set.getExecutionInfo().getErrors());
            }
            return success;
        } catch (Exception e) {
            logger.error("Error updating password for user ID: {}", userId, e);
            return false;
        }
    }

    public boolean updateIndex(Friend friend) throws IOException, InterruptedException {
        logger.debug("Updating search index for friend: {}", friend.getUserId());
        try {
            searchService.setUserIndex(friend);
            logger.info("Successfully updated search index for friend: {}", friend.getUserId());
            return true;
        } catch (Exception e) {
            logger.error("Error updating search index for friend: {}", friend.getUserId(), e);
            throw e;
        }
    }

    public boolean addApplication() {
        logger.debug("Adding application - placeholder method");
        return true;
    }

    public boolean insertStep1(String userId) {
        logger.debug("Executing registration step 1 for user ID: {}", userId);
        try {
            ResultSet judge = session.execute(getIsTemp.bind(userId));
            List<Row> rows = judge.all();

            if (!rows.isEmpty()) {
                boolean isTemp = rows.get(0).getBoolean("temp");
                logger.debug("User {} already exists with temp status: {}", userId, isTemp);
                return isTemp;
            }

            ResultSet execute = session.execute(insertUserName.bind(userId, userId));
            boolean success = execute.getExecutionInfo().getErrors().isEmpty();
            if (success) {
                logger.info("Successfully completed registration step 1 for user ID: {}", userId);
            } else {
                logger.error("Failed registration step 1 for user ID: {}, errors: {}",
                        userId, execute.getExecutionInfo().getErrors());
            }
            return success;
        } catch (Exception e) {
            logger.error("Error in registration step 1 for user ID: {}", userId, e);
            return false;
        }
    }

    public boolean insertStep2(String userId) {
        logger.debug("Executing registration step 2 for user ID: {} - placeholder method", userId);
        return false;
    }

    public boolean insertStep3(String code, String password, String userId) {
        logger.debug("Executing registration step 3 for user ID: {}", userId);
        try {
            String encrypted = RandomUtil.getMD5(password);
            ResultSet execute1 = session.execute(getVerificationCode.bind("step3", userId));

            if (!execute1.getExecutionInfo().getErrors().isEmpty()) {
                logger.error("Error retrieving verification code for step 3, user ID: {}", userId);
                return false;
            }

            Verification verification = CodeMapper.codeMapper(execute1);
            if (verification != null && Instant.now().isBefore(verification.getExpiration()) && verification.getCode().equals(code)) {
                ResultSet execute = session.execute(insertPasswordAndRoleId.bind(encrypted, 1, userId));
                boolean success = execute.getExecutionInfo().getErrors().isEmpty();
                if (success) {
                    logger.info("Successfully completed registration step 3 for user ID: {}", userId);
                } else {
                    logger.error("Failed to complete registration step 3 for user ID: {}, errors: {}",
                            userId, execute.getExecutionInfo().getErrors());
                }
                return success;
            } else {
                logger.warn("Invalid verification code or expired for user ID: {} in step 3", userId);
            }
            return false;
        } catch (Exception e) {
            logger.error("Error in registration step 3 for user ID: {}", userId, e);
            return false;
        }
    }

    public boolean resetPassword(Object email, String oldPassword, String newPassword) {
        logger.debug("Resetting password for email: {}", email);
        try {
            ResultSet password = session.execute(getPassword.bind(email));
            if (password.getExecutionInfo().getErrors().isEmpty()) {
                List<Row> all = password.all();
                if (all.isEmpty()) {
                    logger.warn("No user found for email: {}", email);
                    return false;
                }

                Row row = all.get(0);
                String storedPassword = row.getString("password");
                String encrypted = RandomUtil.getMD5(oldPassword);

                if (storedPassword != null && storedPassword.equals(encrypted)) {
                    ResultSet execute = session.execute(updatePassword.bind(RandomUtil.getMD5(newPassword), email));
                    if (execute.getExecutionInfo().getErrors().isEmpty()) {
                        logger.info("Successfully reset password for email: {}", email);
                        return true;
                    } else {
                        logger.error("Failed to update password for email: {}, errors: {}",
                                email, execute.getExecutionInfo().getErrors());
                        return false;
                    }
                } else {
                    logger.warn("Invalid old password provided for email: {}", email);
                    return false;
                }
            } else {
                logger.error("Error querying password for email: {}, errors: {}",
                        email, password.getExecutionInfo().getErrors());
                return false;
            }
        } catch (Exception e) {
            logger.error("Error resetting password for email: {}", email, e);
            return false;
        }
    }

    public Auth getAccountRoleById(String userEmail) {
        logger.debug("Getting account role for email: {}", userEmail);
        try {
            ResultSet execute = session.execute(getAuth.bind(userEmail));
            List<Auth> auths = AccountParser.accountParser(execute);
            if (auths.isEmpty()) {
                logger.warn("No auth record found for email: {}", userEmail);
                return null;
            }
            logger.debug("Successfully retrieved account role for email: {}", userEmail);
            return auths.get(0);
        } catch (Exception e) {
            logger.error("Error getting account role for email: {}", userEmail, e);
            return null;
        }
    }

    public boolean deleteUser(String userId) {
        logger.info("Deleting user: {}", userId);
        try {
            String query = "DELETE FROM userinfo.user_auth WHERE userid = ?";
            PreparedStatement preparedStatement = session.prepare(query);
            session.execute(preparedStatement.bind(userId));
            logger.info("Successfully deleted user: {}", userId);
            return true;
        } catch (Exception e) {
            logger.error("Error deleting user: {}", userId, e);
            return false;
        }
    }

    public boolean resetStep1(String userId) {
        logger.debug("Resetting step 1 for user ID: {}", userId);
        try {
            ResultSet judge = session.execute(getIsTemp.bind(userId));
            List<Row> rows = judge.all();

            if (rows.isEmpty()) {
                logger.warn("No user found for reset step 1, user ID: {}", userId);
                return false;
            }

            boolean success = judge.getExecutionInfo().getErrors().isEmpty();
            if (success) {
                logger.info("Successfully reset step 1 for user ID: {}", userId);
            } else {
                logger.error("Failed to reset step 1 for user ID: {}, errors: {}",
                        userId, judge.getExecutionInfo().getErrors());
            }
            return success;
        } catch (Exception e) {
            logger.error("Error resetting step 1 for user ID: {}", userId, e);
            return false;
        }
    }


    public Token signInWithGoogle(String tokenString) throws GeneralSecurityException, IOException {
        NetHttpTransport transport = new NetHttpTransport();
        JsonFactory jsonFactory = Utils.getDefaultJsonFactory();

        GoogleIdTokenVerifier verifier = new GoogleIdTokenVerifier.Builder(transport, jsonFactory)
                .setAudience(Collections.singletonList("282080402821-e07eo98flrekqkh8p4rja2kr5f387psi.apps.googleusercontent.com"))
                .build();

        GoogleIdToken idToken = verifier.verify(tokenString);
        if (idToken != null) {
            GoogleIdToken.Payload payload = idToken.getPayload();

            String googleUserId = payload.getSubject();
            String email = payload.getEmail();
            String name = (String) payload.get("name");
            String pictureUrl = (String) payload.get("picture");
            Boolean emailVerified = payload.getEmailVerified();

            // Validate email is verified
            if (!emailVerified) {
                throw new GeneralSecurityException("Invalid email verified");
            }

            // Check if user exists in database
            ResultSet execute = session.execute(getAccount.bind(email));
            Token token = TokenUtil.createToken(new Token(email, 1,Instant.now().plus((long) (30 * 3600 * 24), ChronoUnit.SECONDS), email));

            List<Row> all = execute.all();
            if (all.size() == 0) {
//                // Register new user
                session.execute(insertAccount.bind(email,email, "","",1));
                session.execute(setToken.bind(token.getTokenString(), email, token.getExpireDatetime()));
                logger.info("New user registered via Google: {}", email);
            } else {

                logger.info("Existing user logged in via Google: {}", email);
                session.execute(setToken.bind(token.getTokenString(),email, token.getExpireDatetime()));
            }

            return token;
    } else {
            throw new GeneralSecurityException("Invalid token verified");
        }

}
}