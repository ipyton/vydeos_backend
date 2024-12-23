package com.chen.blogbackend.mappers;

import com.chen.blogbackend.entities.Account;
import com.chen.blogbackend.entities.Auth;
import com.chen.blogbackend.entities.Friend;
import com.chen.blogbackend.entities.Token;
import com.datastax.oss.driver.api.core.cql.ColumnDefinitions;
import com.datastax.oss.driver.api.core.cql.ResultSet;
import com.datastax.oss.driver.api.core.cql.Row;

import java.util.ArrayList;
import java.util.List;


public class AccountParser {
    public static List<Auth> accountParser(ResultSet set) {
        ArrayList<Auth> list = new ArrayList<>();
        for (Row row:
             set.all()) {
            ColumnDefinitions columnDefinitions = row.getColumnDefinitions();

            list.add(new Auth(!columnDefinitions.contains("userId")?null:row.getString("userId"),!columnDefinitions.contains("password")?null: row.getString("password"),
                    !columnDefinitions.contains("email")?null:row.getString("email"), !columnDefinitions.contains("telephone")?null:row.getString("telephone"), !columnDefinitions.contains("roleid")?null:row.getInt("roleid")));
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
            ColumnDefinitions columnDefinitions = row.getColumnDefinitions();
            accounts.add(new Account(!columnDefinitions.contains("user_id")?null:row.getString("user_id"),
                    !columnDefinitions.contains("user_email")?null:row.getString("user_email"),
                    !columnDefinitions.contains("user_name")?null:row.getString("user_name"),
                    !columnDefinitions.contains("intro")?null:row.getString("intro"),
                    !columnDefinitions.contains("avatar")?null:row.getString("avatar"),
                    !columnDefinitions.contains("birthdate")?null:row.getLocalDate("birthdate"),
                    !columnDefinitions.contains("telephone")?null:row.getString("telephone"),
                    !columnDefinitions.contains("gender") || row.getBoolean("gender"),
                    !columnDefinitions.contains("relationship")?0:row.getInt("relationship"),
                    !columnDefinitions.contains("location")?"":row.getString("location"),
                    !columnDefinitions.contains("apps")?null:row.getList("apps", String.class)));
        }
        return accounts;
    }

    public static List<Account> userSearchParser(ResultSet set) {
        ArrayList<Account> accounts = new ArrayList<>();

        for (Row row:
                set.all()) {
            ColumnDefinitions columnDefinitions = row.getColumnDefinitions();

            accounts.add(new Account(columnDefinitions.contains("user_id")?null:row.getString("user_id"),
                    columnDefinitions.contains("apps")?null:row.getList("apps", String.class),
                    columnDefinitions.contains("avatar")?null:row.getString("avatar"),
                    columnDefinitions.contains("birthdate")?null:row.getLocalDate("birthdate"),
                    columnDefinitions.contains("gender")?null:row.getBoolean("gender"),
                    columnDefinitions.contains("intro")?null:row.getString("intro"),
                    columnDefinitions.contains("user_name")?null:row.getString("user_name")));
        }
        return accounts;
    }

//    public static Friend FriendDetailParser(ResultSet set) {
//        //return for friends page
////        System.out.println(set.all().size());
////        if(set.all().size() == 0) return null;
////        Row row = set.one();
////        assert row != null;
////        System.out.println(row.getFormattedContents());
//
//        for (Row row:
//                set.all()) {
//            return new Friend(null, row.getString("user_id"),
//                    row.getString("avatar"),
//                    row.getString("intro"), row.getString("user_name"),null,0,row.getString("location"),
//                    row.getLocalDate("birthdate"));
//        }
//        return null;
//    }

}
