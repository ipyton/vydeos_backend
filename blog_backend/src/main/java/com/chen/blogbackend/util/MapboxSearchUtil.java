package com.chen.blogbackend.util;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;

public class MapboxSearchUtil {

    private static final String MAPBOX_ACCESS_TOKEN = "pk.eyJ1IjoiY3poMTI3ODM0MTgzNCIsImEiOiJjbTIwam94YzQwanl0MmpvZWdieDFiM3FhIn0.u5q9dDn13zHlcgn8pd6UYg";

    /**
     * 根据关键词调用 Mapbox Suggest API
     *
     * @param keyword 搜索关键字
     * @return API 响应内容（JSON 字符串）
     */
    public static String searchByKeyword(String keyword) {
        try {
            String encodedKeyword = URLEncoder.encode(keyword, "UTF-8");

            String urlString = "https://api.mapbox.com/search/searchbox/v1/suggest"
                    + "?q=" + encodedKeyword
                    + "&access_token=" + MAPBOX_ACCESS_TOKEN;

            URL url = new URL(urlString);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();

            conn.setRequestMethod("GET");

            int responseCode = conn.getResponseCode();
            if (responseCode == HttpURLConnection.HTTP_OK) {
                BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                String inputLine;
                StringBuilder response = new StringBuilder();

                while ((inputLine = in.readLine()) != null) {
                    response.append(inputLine);
                }
                in.close();

                return response.toString();
            } else {
                System.err.println("Mapbox API request failed. Response code: " + responseCode);
                return null;
            }

        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }


}
