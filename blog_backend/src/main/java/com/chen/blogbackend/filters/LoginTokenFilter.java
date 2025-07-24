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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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

    private static final Logger logger = LoggerFactory.getLogger(LoginTokenFilter.class);

    @Autowired
    AuthorizationService authorizationService;

    @Autowired
    AccountService accountService;

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        Filter.super.init(filterConfig);
        logger.info("LoginTokenFilter initialized");
    }

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain chain)
            throws IOException, ServletException {

        HttpServletRequest request = (HttpServletRequest) servletRequest;
        HttpServletResponse response = (HttpServletResponse) servletResponse;

        request.setAttribute(Globals.ASYNC_SUPPORTED_ATTR, true);

        String requestURI = request.getRequestURI();
        String method = request.getMethod();

        logger.debug("Processing request: {} {}", method, requestURI);

        // Skip authentication for public endpoints
        if (isPublicEndpoint(requestURI)) {
            logger.debug("Public endpoint accessed: {}", requestURI);
            chain.doFilter(request, response);
            return;
        }

        // Extract and validate token
        String token = request.getHeader("Token");
        if (token == null) {
            logger.warn("No token provided for protected endpoint: {} {}", method, requestURI);
            sendUnauthorizedResponse(response, "Please login");
            return;
        }

        Token parsedToken;
        try {
            parsedToken = TokenUtil.resolveToken(token);
            logger.debug("Token parsed successfully for user: {}", parsedToken != null ? parsedToken.getUserId() : "null");
        } catch (Exception e) {
            logger.error("Failed to parse token for request {} {}: {}", method, requestURI, e.getMessage(), e);
            sendUnauthorizedResponse(response, "Expired token");
            return;
        }

        if (parsedToken == null) {
            logger.warn("Invalid token received for request {} {}", method, requestURI);
            sendUnauthorizedResponse(response, "Invalid token");
            return;
        }

        // Check authorization (role -1 appears to be admin/bypass role)
        try {
            boolean hasAccess = parsedToken.getRoleId() == -1 || parsedToken.getRoleId() == 0 || authorizationService.hasAccess(parsedToken.getRoleId(), requestURI);

            if (!hasAccess) {
                logger.warn("Access denied for user {} (role: {}) to {} {}",
                        parsedToken.getUserId(), parsedToken.getRoleId(), method, requestURI);
                sendUnauthorizedResponse(response, "Insufficient permissions");
                return;
            }
        } catch (Exception e) {
            logger.error("Error during authorization check for user {} (role: {}) accessing {} {}: {}",
                    parsedToken.getUserId(), parsedToken.getRoleId(), method, requestURI, e.getMessage(), e);
            sendUnauthorizedResponse(response, "Authorization error");
        }
        logger.debug("Access granted for user {} (role: {}) to {}",
                parsedToken.getUserId(), parsedToken.getRoleId(), requestURI);
        System.out.println(parsedToken);
        request.setAttribute("userEmail", parsedToken.getUserId().toLowerCase());
        chain.doFilter(request, response);
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
                "/metrics",
                "/actuator/prometheus",
                "/account/google",
                "/account/getAvatar"};

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
        try {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED); // Sets HTTP 401 status
            response.setContentType("application/json");
            response.setCharacterEncoding("UTF-8");

            LoginMessage loginMessage = new LoginMessage(401, message);
            String jsonResponse = JSON.toJSONString(loginMessage);

            response.getOutputStream().write(jsonResponse.getBytes(StandardCharsets.UTF_8));

            logger.debug("Sent 401 unauthorized response: {}", message);
        } catch (Exception e) {
            logger.error("Failed to send unauthorized response: {}", e.getMessage(), e);
            throw e;
        }
    }

    @Override
    public void destroy() {
        logger.info("LoginTokenFilter destroyed");
        Filter.super.destroy();
    }
}