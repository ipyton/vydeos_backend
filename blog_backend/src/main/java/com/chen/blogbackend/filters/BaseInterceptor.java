package com.chen.blogbackend.filters;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpHeaders;
import org.springframework.web.servlet.HandlerInterceptor;


public class BaseInterceptor implements HandlerInterceptor {
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {

        String origin  = request.getHeader(HttpHeaders.ORIGIN);
        if (origin != null) {
            response.setHeader("Access-Control-Allow-Origin", "*");
            response.setHeader("Access-Control-Allow-Methods", "POST, GET, OPTIONS, DELETE, PUT, HEAD");
            //这里设置允许的自定义header参数
            response.setHeader("Access-Control-Allow-Headers", "Content-Type, Token, token, adminID");
            response.setHeader("Access-Control-Max-Age", "3600");
        }
        return true;
    }
}
