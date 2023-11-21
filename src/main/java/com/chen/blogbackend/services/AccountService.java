package com.chen.blogbackend.services;

import com.chen.blogbackend.entities.Account;
import com.chen.blogbackend.mappers.AccountMapper;
import org.apache.ibatis.io.Resources;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.InputStream;

@Service
public class AccountService {
    @Autowired
    SqlSessionFactory sqlSessionFactory;
    SqlSession session = sqlSessionFactory.openSession();
    public AccountService() throws IOException {
    }

    public boolean insert(Account account) {
        AccountMapper mapper = session.getMapper(AccountMapper.class);
        mapper.getAccount()

        return true;
    }

    public Account selectAccount(Account account) {
        return new Account();
    }

    public Account selectAccount(String accountID) {
        Account account = new Account();
        account.setUserID(accountID);

        return account;
    }

    public String validatePassword(String username,String password){
        return "";
    }

}
