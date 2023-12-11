package com.chen.blogbackend.util;

public class StringUtil {
    public static String getHash(String name){
        return Integer.toString(name.hashCode()).substring(0,2);
    }
}
