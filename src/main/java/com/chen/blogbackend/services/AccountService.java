package com.chen.blogbackend.services;

import com.chen.blogbackend.Util.PasswordEncryption;
import com.chen.blogbackend.entities.Account;
import com.chen.blogbackend.entities.Token;
import com.chen.blogbackend.mappers.AccountMapper;
import org.apache.ibatis.io.Resources;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.InputStream;
import java.util.Date;

@Service
public class AccountService {

    @Autowired
    SqlSessionFactory sqlSessionFactory;

    public boolean insert(Account account) {

        return true;
    }

    public Account selectAccount(String accountID) {
        SqlSession session = sqlSessionFactory.openSession();
        AccountMapper mapper = session.getMapper(AccountMapper.class);
        System.out.println(mapper.getAccount("9999999"));
        session.close();
        return new Account();
    }

    public boolean haveValidLogin(String token) {
        SqlSession session = sqlSessionFactory.openSession();
        AccountMapper mapper = session.getMapper(AccountMapper.class);
        Token tokenGet = mapper.getToken(token);
        return tokenGet != null && tokenGet.getExpiresDateAndTime().after(new Date());
    }

    public boolean validatePassword(String email,String password){
        password = PasswordEncryption.encryption(password);
        SqlSession session = sqlSessionFactory.openSession();
        AccountMapper mapper = session.getMapper(AccountMapper.class);
        Account account = mapper.getAccount(email);
        if (account.getPassword().equals(password)) {
            return true;
        }
        return false;
    }

    public int setToken(String email,String token) {
        SqlSession session = sqlSessionFactory.openSession();
        AccountMapper mapper = session.getMapper(AccountMapper.class);
        return mapper.setToken(email, token, new Date());
    }
}
