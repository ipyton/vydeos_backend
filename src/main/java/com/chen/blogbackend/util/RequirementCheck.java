package com.chen.blogbackend.util;

import java.util.HashMap;
import java.util.Map;

public class RequirementCheck {

    private static String[] listSeparator(String list) {
        String substring = list.substring(1, -1);
        return substring.split(",");

    }


    public static boolean check(HashMap<String,String> requirements, HashMap<String, String> environment) {
        for (HashMap.Entry<String,String> entry : requirements.entrySet()) {
            String[] split = entry.getKey().split("/?");
            String name = split[0], operator = split[1];
            if (null == environment.get(entry.getKey())) {
                return false;
            }
            if (operator.equals(">")){
                if (Integer.parseInt(entry.getValue()) > Integer.parseInt(environment.get(entry.getKey()))) {
                    return false;
                }
            }
            else if (operator.equals("<")){
                if (Integer.parseInt(entry.getValue()) < Integer.parseInt(environment.get(entry.getKey()))) {
                    return false;
                }
            }
            else if (operator.equals("=")) {
                if (Integer.parseInt(entry.getValue()) == Integer.parseInt(environment.get(entry.getKey()))) {
                    return false;
                }
            }
            else if(operator.equals("equals")) {
                if (!entry.getValue().equals(environment.get(entry.getKey()))) {
                    return false;
                }
            }
            else if(operator.equals("in")) {
                String[] strings = listSeparator(entry.getValue());
                String env = environment.get(entry.getKey());
                for (String string : strings) {
                    if (string.equals(env)) {
                        return true;
                    }
                }
                return false;
            }

        }
        return true;
    }
}
