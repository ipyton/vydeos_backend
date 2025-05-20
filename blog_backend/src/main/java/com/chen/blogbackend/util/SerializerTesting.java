package com.chen.blogbackend.util;

import com.alibaba.fastjson.JSON;
import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.interfaces.Claim;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.chen.blogbackend.entities.Token;

import java.util.ArrayList;
import java.util.HashMap;

public class SerializerTesting {
    //A baseline for serializing all friends information in the tokens.
    //
    //400-500 ms when arraylist length is less than 100000 elements.
    // when number of elements is 100000, the result size id nearly 3371693, and the original size is 2000000
    // deserialize 2000 elements only takes 52 ms.
    //100000 elements takes 100ms


    static ArrayList<String> list;

    public static String serialize() {
        list = new ArrayList<>();
        long length = 0;
        for (int i = 0; i < 2000; i ++) {
            String element = RandomUtil.generateRandomName() + i;
            list.add(element);
            length += element.length();

        }
        long time = System.currentTimeMillis();
        String s= JSON.toJSONString(list);
        String tokenString = JWT.create().withHeader(new HashMap<>()).withClaim("userId", s)
                .sign(Algorithm.HMAC256("abcdefghijklmn"));

        long end = System.currentTimeMillis();
        System.out.println(tokenString.length());
        System.out.println(end - time);
        System.out.println(length);
        return tokenString;

    }

    public static void deSerialize(String str) {
        JWTVerifier jwtVerifier = JWT.require(Algorithm.HMAC256("abcdefghijklmn")).build();

        long start = System.currentTimeMillis();
        DecodedJWT decodedJWT = jwtVerifier.verify(str);
        Claim userId = decodedJWT.getClaim("userId");
//        if (decodedJWT.getExpiresAt().before(cal.getTime())) return null;
        ArrayList list = JSON.parseObject(userId.asString(), ArrayList.class);
        long end = System.currentTimeMillis();

        for (int i = 0; i <list.size(); i ++) {
            System.out.println(list.get(i));
        }
        System.out.println(end - start);

    }


    public static void main(String[] args) {
        String serialize = serialize();
        deSerialize(serialize);

    }
}
