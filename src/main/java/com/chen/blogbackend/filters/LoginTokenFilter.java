package com.chen.blogbackend.filters;

import com.alibaba.fastjson.JSON;
import com.chen.blogbackend.ResponseMessage.LoginMessage;
import com.chen.blogbackend.services.AccountService;
import jakarta.servlet.*;
import jakarta.servlet.annotation.WebFilter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.PrintWriter;

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

        if (request.getRequestURI().equals("/account/login") || request.getRequestURI().equals("/account/register")) {
            chain.doFilter(request, response);
        }
        if (request.getHeader("token") == null || !accountService.haveValidLogin(request.getHeader("token"))) {
            response.setContentType("application/json");
            PrintWriter writer = response.getWriter();
            writer.write(JSON.toJSONString(new LoginMessage(-1, "error")));
            writer.close();
            return;

        }
        chain.doFilter(request, response);
    }

    @Override
    public void destroy() {
        Filter.super.destroy();
    }
}
