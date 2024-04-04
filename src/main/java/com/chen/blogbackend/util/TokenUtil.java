package com.chen.blogbackend.util;

import com.alibaba.fastjson.JSON;
import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.interfaces.Claim;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.chen.blogbackend.entities.FriendsToken;
import com.chen.blogbackend.entities.Token;

import java.util.HashMap;

//using symmetric encryption option.
public class TokenUtil {

    private static String pubKey = "Asdasd1";

    public static Token createToken(Token token){
        String tokenString = JWT.create().withHeader(new HashMap<>()).withClaim("userId", token.getUserId())
                .withExpiresAt(token.getExpireDatetime())
                .sign(Algorithm.HMAC256(pubKey));
        token.setTokenString(tokenString);
        return token;
    }


    //This method is for log in.
    public static Token resolveToken(String tokenString){
//        Calendar cal = Calendar.getInstance();
        JWTVerifier jwtVerifier = JWT.require(Algorithm.HMAC256(pubKey)).build();
        DecodedJWT decodedJWT = jwtVerifier.verify(tokenString);
        Claim userId = decodedJWT.getClaim("userId");
//        if (decodedJWT.getExpiresAt().before(cal.getTime())) return null;
        return new Token(userId.asString(),decodedJWT.getExpiresAtAsInstant(), null);
    }

    public static String createFriendsToken(FriendsToken token) {
        return JWT.create().withHeader(new HashMap<>()).withClaim("friendList", JSON.toJSONString(token.getFriendsId()))
                .withClaim("userId", token.getUserId()).sign(Algorithm.HMAC256(pubKey));
    }

    public static FriendsToken resolveFriendsToken(String token) {
        JWTVerifier jwtVerifier = JWT.require(Algorithm.HMAC256(pubKey)).build();
        DecodedJWT decodedJWT = jwtVerifier.verify(token);
        Claim userId = decodedJWT.getClaim("userId");
        Claim friendList = decodedJWT.getClaim("friendList");

        return new FriendsToken(userId.asString(), friendList.asList(String.class));



    }


}
