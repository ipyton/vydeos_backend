package com.chen.blogbackend.util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class PathMatcher {

    public static boolean match(List<String> patterns, String path) {
        List<String> pathParts = splitAndClean(path);

        for (String pattern : patterns) {
            List<String> patternParts = splitAndClean(pattern);

            if (isMatch(patternParts, pathParts, 0, 0)) {
                return true;
            }
        }

        return false;
    }

    private static List<String> splitAndClean(String input) {
        String cleaned = input.replaceAll("^/+", "").replaceAll("/+$", "");
        if (cleaned.isEmpty()) return new ArrayList<>();
        return new ArrayList<>(Arrays.asList(cleaned.split("/")));
    }

    private static boolean isMatch(List<String> pattern, List<String> path, int pi, int si) {
        while (pi < pattern.size() && si < path.size()) {
            String pat = pattern.get(pi);
            String segment = path.get(si);

            if (pat.equals("**")) {
                if (pi == pattern.size() - 1) return true; // '**' at the end matches all
                for (int k = si; k <= path.size(); k++) {
                    if (isMatch(pattern, path, pi + 1, k)) {
                        return true;
                    }
                }
                return false;
            } else if (pat.equals("*") || pat.equals(segment)) {
                pi++;
                si++;
            } else {
                return false;
            }
        }

        // Skip remaining '**'
        while (pi < pattern.size() && pattern.get(pi).equals("**")) {
            pi++;
        }

        return pi == pattern.size() && si == path.size();
    }
}
