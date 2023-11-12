package com.chen.blogbackend.services;

import com.chen.blogbackend.entities.Account;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class AccountService {

    public boolean insert(Account account) {
        return true;
    }

    public Account selectAccount(Account account) {
        return new Account();
    }

    public Account selectAccount(String accountID) {
        return new Account();
    }

    public String validatePassword(String username,String password){
        return "";
    }

}
