package com.chen.blogbackend.util;

import com.chen.blogbackend.entities.Account;
import com.chen.blogbackend.entities.Auth;

import java.util.regex.Pattern;

public class AccountInfoValidator {

    public static boolean testRegex(String name, String regex) {
        return Pattern.compile(regex).matcher(name).matches();
    }


    public static boolean validateUserName(String username) {
        if (username.length() < 5) return false;
        for (char c: username.toCharArray()) {
            if (!Character.isLetterOrDigit(c)) return false;
        }
        return true;
    }


    public static boolean validateUserEmail(String userId) {
        if (userId.length() < 5) return false;
//        for (char c: userId.toCharArray()) {
//            if (!Character.isLetterOrDigit(c)) return false;
//        }
        boolean result =  testRegex(userId,"^(.+)@(\\S+)$");
        return result;
    }




    public static boolean validateUserPassword(String password) {
        boolean uppercase = false, lowercase = false, numbers = false;
        for (char c: password.toCharArray()) {
            uppercase |= Character.isUpperCase(c);
            lowercase |= Character.isLowerCase(c);
            numbers |= Character.isDigit(c);
        }
        return uppercase && lowercase && numbers;
    }

    public static boolean validateAccount(Auth account){
        //System.out.println(validateUserName(account.getUserName()));
        //System.out.println(validateUserPassword(account.getPassword()));
        return validateUserName(account.getUserId()) && validateUserPassword(account.getPassword());
    }

}
