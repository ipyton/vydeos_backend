package com.chen.blogbackend.services;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import com.chen.blogbackend.entities.Friend;
import com.chen.blogbackend.util.PasswordEncryption;
import com.chen.blogbackend.util.TokenUtil;
import com.chen.blogbackend.entities.Account;
import com.chen.blogbackend.entities.Token;
import com.chen.blogbackend.mappers.AccountMapper;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.Date;

@Service
public class AccountService {

    @Autowired
    SqlSessionFactory sqlSessionFactory;

    @Autowired
    SearchService searchService;



    public boolean insert(Account account) {
        SqlSession session = sqlSessionFactory.openSession();
        AccountMapper mapper = session.getMapper(AccountMapper.class);
        int result = mapper.insertAccount(account);
        session.commit();
        session.close();
        return result != 0;
    }

    public Account selectAccount(String accountID) {
        SqlSession session = sqlSessionFactory.openSession();
        AccountMapper mapper = session.getMapper(AccountMapper.class);
        System.out.println(mapper.getAccount(accountID));
        session.close();
        return new Account();
    }


    public boolean haveValidLogin(String token) {
        if (null == token) return false;
        SqlSession session = sqlSessionFactory.openSession();
        AccountMapper mapper = session.getMapper(AccountMapper.class);
        Token tokenGet = mapper.getToken(token);
        session.close();

        if(null == tokenGet || null == TokenUtil.resolveToken(token).getUserEmail() ||
                !TokenUtil.resolveToken(token).getUserEmail().equals(tokenGet.getUserEmail())){
            return false;
        }
        return tokenGet.getExpireDatetime().after(new Date());
    }

    public boolean validatePassword(String email,String password){
        password = PasswordEncryption.encryption(password);
        SqlSession session = sqlSessionFactory.openSession();
        AccountMapper mapper = session.getMapper(AccountMapper.class);
        Account account = mapper.getAccount(email);
        session.close();
        if (null != account && account.getPassword().equals(password)) {
            return true;
        }
        return false;
    }

    public int setToken(Token token) {
        SqlSession session = sqlSessionFactory.openSession();
        AccountMapper mapper = session.getMapper(AccountMapper.class);
        if (null != mapper.getToken(token.getUserEmail())) return 0;
        int result = mapper.setToken(token);
        session.commit();
        session.close();
        return result;
    }

    public boolean updateAccount(Account account) {
        return true;
    }


    public boolean update(Friend friend) throws IOException, InterruptedException {
        searchService.setUserIndex(friend);

        return true;
    }

    public boolean addApplication() {


        return true;
    }

}
