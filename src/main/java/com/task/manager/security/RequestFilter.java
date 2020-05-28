package com.task.manager.security;


import org.springframework.stereotype.Component;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Objects;
import java.util.logging.Logger;

@Component
public class RequestFilter implements Filter {

    private static final Logger LOG = Logger.getLogger(RequestFilter.class.getName());

    @Override
    public void doFilter(ServletRequest request,
                         ServletResponse response,
                         FilterChain chain) throws IOException, ServletException {

        HttpServletRequest req = (HttpServletRequest) request;
        var authHeader = req.getHeader("X-GREEN-APP-ID");

        LOG.info("-------------------------------------------------------------------------------------------");
        LOG.info(" /" + req.getMethod());
        LOG.info(" Request: " + req.getRequestURI());
        LOG.info("-------------------------------------------------------------------------------------------");

        if (authHeader == null || !Objects.equals(authHeader,"GREEN")) {
            HttpServletResponse httpResponse = (HttpServletResponse) response;
            httpResponse.setContentType("application/json");
            httpResponse.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Required headers not specified in the request or incorrect");
            return;
        }
        chain.doFilter(request, response);
    }

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        LOG.warning("Auth filter initialization");
    }
}