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




    public static ValidationResult validateUserPassword(String password) {
        boolean uppercase = false, lowercase = false, numbers = false;

        // 遍历密码中的字符，检查是否包含大写字母、小写字母和数字
        for (char c : password.toCharArray()) {
            uppercase |= Character.isUpperCase(c);
            lowercase |= Character.isLowerCase(c);
            numbers |= Character.isDigit(c);
        }

        // 根据条件返回验证结果
        if (!uppercase) {
            return new ValidationResult("Password must contain at least one uppercase letter.", 1);
        } else if (!lowercase) {
            return new ValidationResult("Password must contain at least one lowercase letter.", 2);
        } else if (!numbers) {
            return new ValidationResult("Password must contain at at least one number.", 3);
        } else {
            return new ValidationResult("Password is valid.", 0); // 0表示密码有效
        }
    }
    public static boolean validateAccount(Auth account){
        //System.out.println(validateUserName(account.getUserName()));
        //System.out.println(validateUserPassword(account.getPassword()));
        return validateUserName(account.getUserId()) && validateUserPassword(account.getPassword()).code == 0;
    }

}
