package com.chen.blogbackend.Util;

import com.chen.blogbackend.entities.Account;

import java.util.Calendar;

public class TokenUtil {

    private String pubKey = "asdasd1";

    public Account createToken(String token, int validSeconds){
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.SECOND, validSeconds);
        String token =
    }


    public String resolveToken(){

    }


}
