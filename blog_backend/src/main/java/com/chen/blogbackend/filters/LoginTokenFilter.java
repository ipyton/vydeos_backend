package com.chen.blogbackend.filters;

import com.alibaba.fastjson.JSON;
import com.chen.blogbackend.entities.Path;
import com.chen.blogbackend.entities.PathDTO;
import com.chen.blogbackend.entities.Token;
import com.chen.blogbackend.responseMessage.LoginMessage;
import com.chen.blogbackend.services.AccountService;
import com.chen.blogbackend.services.AuthorizationService;
import com.chen.blogbackend.util.TokenUtil;
import com.datastax.oss.driver.api.core.CqlSession;
import com.datastax.oss.driver.api.core.cql.ResultSet;
import jakarta.annotation.PostConstruct;
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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

@Component
public class LoginTokenFilter implements Filter {


    @Autowired
    AuthorizationService authorizationService;

    @Autowired
    AccountService accountService;

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        Filter.super.init(filterConfig);
    }



    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain chain)
            throws IOException, ServletException {

        HttpServletRequest request = (HttpServletRequest) servletRequest;
        HttpServletResponse response = (HttpServletResponse) servletResponse;

        request.setAttribute(Globals.ASYNC_SUPPORTED_ATTR, true);
        System.out.println("Request URI: " + request.getRequestURI());

        // Skip authentication for public endpoints
        if (isPublicEndpoint(request.getRequestURI())) {
            chain.doFilter(request, response);
            return;
        }

        // Extract and validate token
        String token = request.getHeader("Token");
        if (token == null) {
            sendUnauthorizedResponse(response, "Please login");
            return;
        }

        Token parsedToken;
        try {
            parsedToken = TokenUtil.resolveToken(token);
        } catch (Exception e) {
            sendUnauthorizedResponse(response, "Expired token");
            return;
        }

        if (parsedToken == null) {
            sendUnauthorizedResponse(response, "Invalid token");
            return;
        }

        // Check authorization (role -1 appears to be admin/bypass role)
        if (parsedToken.getRoleId() == -1 || authorizationService.hasAccess(parsedToken.getRoleId(), request.getRequestURI())) {
            request.setAttribute("userEmail", parsedToken.getUserId().toLowerCase());
            chain.doFilter(request, response);
        } else {
            sendUnauthorizedResponse(response, "Insufficient permissions");
        }
    }

    /**
     * Check if the endpoint is public and doesn't require authentication
     */
    private boolean isPublicEndpoint(String requestURI) {
        String[] publicPaths = {
                "/account/register",
                "/account/login",
                "/account/changePassword",
                "/account/verifyToken",
                "/account/sendVerificationCode",
                "/authorization/hasPermission",
                "/hello",
                "/metrics",
                "/actuator/prometheus",
                "/google"
        };

        for (String path : publicPaths) {
            if (requestURI.startsWith(path)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Send a proper 401 Unauthorized response
     */
    private void sendUnauthorizedResponse(HttpServletResponse response, String message) throws IOException {
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED); // Sets HTTP 401 status
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        LoginMessage loginMessage = new LoginMessage(401, message);
        String jsonResponse = JSON.toJSONString(loginMessage);

        response.getOutputStream().write(jsonResponse.getBytes(StandardCharsets.UTF_8));
    }

    @Override
    public void destroy() {
        Filter.super.destroy();
    }
}
