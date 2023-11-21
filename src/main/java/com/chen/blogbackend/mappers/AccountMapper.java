package com.chen.blogbackend.mappers;

import com.chen.blogbackend.entities.Account;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Select;


public interface AccountMapper {

    @Select("select * from account;")
    Account getAccount();

    @Insert("insert into ")
    int addAccount(Account account);


}
