package com.chen.blogbackend.mappers;

import com.chen.blogbackend.entities.Account;
import com.chen.blogbackend.entities.Token;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.Date;

@Mapper
public interface AccountMapper {

    @Select("select * from user_info where user_email = #{userEmail}")
    Account getAccount(String email);

    @Insert("insert into user_info(user_email, user_name, password, introduction, avatar, date_of_birth) values(#{userEmail}," +
            "#{userName},#{password},#{introduction},#{avatar}, #{dateOfBirth})")
    int insertAccount(Account account);

    @Select("select * from token where token_string = #{token}")
    Token getToken(String token);


    @Insert("insert into token(user_email, token_string, expire_datetime) values(#{userEmail}, #{tokenString}, #{expireDatetime})")
    int setToken(Token token);

    @Update("")
    int updateAccount(Account account);

    @Update("")
    int changePassword(Account account);





}
