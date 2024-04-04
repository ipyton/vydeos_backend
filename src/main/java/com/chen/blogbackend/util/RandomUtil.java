package com.chen.blogbackend.util;

import java.util.Random;

public class RandomUtil {
    public static String getHash(String name){
        return Integer.toString(name.hashCode()).substring(0,2);
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
