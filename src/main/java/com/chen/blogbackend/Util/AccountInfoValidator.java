package com.chen.blogbackend.Util;

import com.chen.blogbackend.entities.Account;

public class AccountInfoValidator {

    public static boolean validateUserName(String username) {
        if (username.length() < 5) return false;
        for (char c: username.toCharArray()) {
            if (!Character.isLetterOrDigit(c)) return false;
        }
        return true;
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

    public static boolean validateAccount(Account account){
        //System.out.println(validateUserName(account.getUserName()));
        //System.out.println(validateUserPassword(account.getPassword()));
        return validateUserName(account.getUserName()) && validateUserPassword(account.getPassword());
    }
}
