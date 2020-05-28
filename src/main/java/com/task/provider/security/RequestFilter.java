package com.task.provider.security;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Objects;
import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import static java.util.Objects.isNull;

@Component
public class RequestFilter implements Filter {

    private static final Logger LOG = LoggerFactory.getLogger(RequestFilter.class);

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {

        HttpServletRequest req = (HttpServletRequest) request;
        var authHeader = req.getHeader("X-GREEN-APP-ID");

        LOG.info("-------------------------------------------------------------------------------------------");
        LOG.info(" /" + req.getMethod());
        LOG.info(" Request: " + req.getRequestURI());
        LOG.info("-------------------------------------------------------------------------------------------");

        if (isNull(authHeader) || !Objects.equals(authHeader, "GREEN")) {
            HttpServletResponse httpResponse = (HttpServletResponse) response;
            httpResponse.setContentType("application/json");
            httpResponse.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Required headers not specified in the request or incorrect");
            return;
        }
        chain.doFilter(request, response);
    }

    @Override
    public void init(FilterConfig filterConfig) {
        LOG.warn("Auth filter initialization");
    }
}