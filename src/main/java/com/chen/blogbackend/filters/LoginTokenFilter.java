package com.chen.blogbackend.filters;

import com.alibaba.fastjson.JSON;
import com.chen.blogbackend.ResponseMessage.LoginMessage;
import com.chen.blogbackend.Util.TokenUtil;
import com.chen.blogbackend.services.AccountService;
import jakarta.servlet.*;
import jakarta.servlet.annotation.WebFilter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;

@WebFilter(filterName = "tokenChecker")
public class LoginTokenFilter implements Filter {

    @Autowired
    AccountService accountService;

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        Filter.super.init(filterConfig);
    }

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain chain) throws IOException, ServletException {
        HttpServletRequest request = (HttpServletRequest) servletRequest;
        HttpServletResponse response = (HttpServletResponse) servletResponse;
        response.addHeader("Access-Control-Allow-Origin","*");
        response.addHeader("Access-Control-Allow-Methods","*");
        response.addHeader("Access-Control-Allow-Headers","*");
//
//        String token = request.getHeader("token");
//        boolean result = accountService.haveValidLogin(request.getHeader("token"));
//        if(null == token) {
//            System.out.println(request.getRequestURI());
//            if (request.getRequestURI().equals("/account/login") || request.getRequestURI().equals("/account/register") || request.getRequestURI().equals("/account/verifyToken") ) {
//                chain.doFilter(request, response);
//            }
//            else {
//                servletResponse.setContentType("application/json");
//                servletResponse.getOutputStream().write(JSON.toJSONString(new LoginMessage(-2, "please login first")).getBytes(StandardCharsets.UTF_8));
//            }
//            return;
//        }
//        else {
//            if(!result) {
//                servletResponse.setContentType("application/json");
//                servletResponse.getOutputStream().write(JSON.toJSONString(new LoginMessage(-1, "wrong password or username!")).getBytes(StandardCharsets.UTF_8));
//                return;
//            }
//        }
//        request.setAttribute("userEmail", TokenUtil.resolveToken(token).getUserEmail());
        chain.doFilter(request, response);
    }

    @Override
    public void destroy() {
        Filter.super.destroy();
    }
}
