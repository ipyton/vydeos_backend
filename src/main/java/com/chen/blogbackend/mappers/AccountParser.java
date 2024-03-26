package com.chen.blogbackend.mappers;

import com.chen.blogbackend.entities.Account;
import com.chen.blogbackend.entities.Friend;
import com.chen.blogbackend.entities.Token;
import com.datastax.oss.driver.api.core.cql.ResultSet;
import com.datastax.oss.driver.api.core.cql.Row;

import java.util.ArrayList;
import java.util.List;


public class AccountParser {
    public static List<Account> accountParser(ResultSet set) {
        ArrayList<Account> list = new ArrayList<>();
        for (Row row:
             set.all()) {
            list.add(new Account(row.getString("userId"), row.getString("password"),
                    row.getString("email"), row.getString("telephone")));
        }
        return list;

    }

    public static List<Token> tokenParser(ResultSet set) {
        ArrayList<Token> list = new ArrayList<>();
        for (Row row:
                set.all()) {
            list.add(new Token(row.getString("userId"), row.getInstant("invalid_date"),
                    row.getString("user_token")));
        }
        return list;
    }

    public static List<Account> userDetailParser(ResultSet set) {
        ArrayList<Account> accounts = new ArrayList<>();
        for (Row row:
                set.all()) {
            accounts.add(new Account(row.getString("user_id"), row.getList("apps", String.class),
                    row.getString("avatar"), row.getLocalDate("birthdate"), row.getBoolean("gender"),
                    row.getString("intro"), row.getString("user_name")));
        }
        return accounts;
    }
    public static Friend FriendDetailParser(ResultSet set) {
        Row row = set.all().get(0);
        return new Friend(row.getString("user_id"),row.getString("friend_id"),
                row.getString("avatar"),
                row.getString("intro"), row.getString("name"),null,0,row.getString("location"), row.getLocalDate("dateOfBirth"));
    }

}
