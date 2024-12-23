package com.chen.blogbackend.util;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;
import java.util.Random;

public class RandomUtil {
    public static String getHash(String name){
        return Integer.toString(name.hashCode()).substring(0,2);
    }

    public static String getBase64(String content){
        return Base64.getEncoder().encodeToString(content.getBytes());
    }

    int count = 0;
    public static String generateRandomString(int length) {
        String characters = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
        StringBuilder sb = new StringBuilder(length);
        Random random = new Random();

        for (int i = 0; i < length; i++) {
            int index = random.nextInt(characters.length());
            sb.append(characters.charAt(index));
        }

        return sb.toString();
    }
    public static String getMD5(String input) {
        try {
            // 获取 MD5 算法的 MessageDigest 实例
            MessageDigest md = MessageDigest.getInstance("MD5");

            // 对输入的字符串进行哈希运算
            byte[] messageDigest = md.digest(input.getBytes());

            // 将字节数组转换为十六进制字符串
            StringBuilder hexString = new StringBuilder();
            for (byte b : messageDigest) {
                // 将每个字节转换为两位十六进制
                hexString.append(String.format("%02x", b));
            }

            // 返回十六进制字符串
            return hexString.toString();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
            return null;
        }
    }
    public static String generate6RandomInt() {
        String characters = "1234567890";
        StringBuilder sb = new StringBuilder(6);
        Random random = new Random();

        for (int i = 0; i < 6; i++) {
            int index = random.nextInt(characters.length());
            sb.append(characters.charAt(index));
        }

        return sb.toString();
    }

    public static String generateMessageId(String userId) {
        return userId + "_" + String.valueOf(System.currentTimeMillis());
    }


    public static String generateRandomName() {
        String[] fruits = {
                "Apple",
        "Banana" ,
        "Mango" ,
        "Orange",
        "Grapes",
        "Pineapple",
        "Watermelon",
        "Papaya",
        "Guava",
        "Pomegranate"};


        String[] adjs = {
                "Loving","Adoring", "Fond",
                "Pleasing",
                "Smiable",
                "Dmart",
                "Charming",
                "Creative",
                "Determined",
                "Diligent",
                "Diplomatic",
                "Dynamic",
                "Energetic",
                "Friendly",
                "Generous",
                "Gregarious",
                "Hardworking",
                "Helpful",
                "Kind",
                "Friendly",
                "Loyal",
                "Patient",
                "Polite",
                "Heartfelt",
        };
        Random random = new Random();
        return adjs[(random.nextInt() & Integer.MAX_VALUE) % adjs.length] + " " + fruits[(random.nextInt() & Integer.MAX_VALUE) % fruits.length];

    }

}
