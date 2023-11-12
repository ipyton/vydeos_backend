package com.chen.blogbackend.Util;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.interfaces.Claim;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.chen.blogbackend.entities.Account;
import com.chen.blogbackend.entities.Token;

import java.util.Base64;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;

//using symmetric encryption option.
public class TokenUtil {

    private static String pubKey = "Asdasd1";

    public static String createToken(Token token){
        return JWT.create().withHeader(new HashMap<>()).withClaim("userID", token.getUserID())
                .withClaim("userName", token.getUsername()).withExpiresAt(token.getExpiresDateAndTime())
                .sign(Algorithm.HMAC256(pubKey));
    }


    public static Token resolveToken(String tokenString){
        Calendar cal = Calendar.getInstance();
        JWTVerifier jwtVerifier = JWT.require(Algorithm.HMAC256(pubKey)).build();
        DecodedJWT decodedJWT = jwtVerifier.verify(tokenString);
        Claim userId = decodedJWT.getClaim("userId");
        Claim userName = decodedJWT.getClaim("userName");
        if (decodedJWT.getExpiresAt().before(cal.getTime())) return null;
        return new Token(userId.asString(), userName.asString(), decodedJWT.getExpiresAt());
    }


}
