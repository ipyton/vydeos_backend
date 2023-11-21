package com.chen.blogbackend.mappers;

import com.chen.blogbackend.entities.Account;
import com.chen.blogbackend.entities.Token;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.Date;

@Mapper
public interface AccountMapper {

    @Select("select * from user_info where user_email = #{email}")
    Account getAccount(String email);

    @Select("select * from token  where token = #{token}")
    Token getToken(String token);

    int addAccount(Account account);

    @Insert("insert token(user_email, token, expire_datetime) values(#{email}, #{token}, #{date})")
    int setToken(String email, String token, Date date);
}
