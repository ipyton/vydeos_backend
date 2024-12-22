package com.chen.blogbackend.filters;

import com.alibaba.fastjson.JSON;
import com.chen.blogbackend.responseMessage.LoginMessage;
import com.chen.blogbackend.services.AccountService;
import com.chen.blogbackend.util.TokenUtil;
import jakarta.servlet.*;
import jakarta.servlet.annotation.WebFilter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.apache.catalina.Globals;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

@Component
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
        request.setAttribute(Globals.ASYNC_SUPPORTED_ATTR, true);
        System.err.println(request.getRequestURI());
        System.out.println(request.getRequestURI());
        System.out.flush();
        if (request.getRequestURI().startsWith("/account/register") || request.getRequestURI().startsWith("/account/login")
                || request.getRequestURI().startsWith("/account/changePassword")
                ||  request.getRequestURI().startsWith("/account/verifyToken" )) {
            chain.doFilter(request, response);
            return;
        }

        String token = request.getHeader("Token");
        System.out.println("_____" + request.getHeader("Token"));
//        request.getHeaderNames().asIterator().forEachRemaining(System.out::println);
//        System.out.println(request.getHeader("token"));
        if (token == null) {
            System.out.println("unauthorized token is null");
            servletResponse.setContentType("application/json");
            servletResponse.getOutputStream().write(JSON.toJSONString(new LoginMessage(-1, "please login first")).getBytes(StandardCharsets.UTF_8));
            return;
        }
        else {
            System.out.printf("Success");
        }
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
        System.out.printf(TokenUtil.resolveToken(token).getUserId());
        request.setAttribute("userEmail", TokenUtil.resolveToken(token).getUserId());
        chain.doFilter(request, response);
    }

    @Override
    public void destroy() {
        Filter.super.destroy();
    }
}
