package com.task.provider.security;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
import java.util.Enumeration;
import java.util.Objects;
import java.util.Scanner;

import static java.util.Objects.isNull;

@Component
public class RequestFilter implements Filter {

    private static final Logger LOG = LoggerFactory.getLogger(RequestFilter.class);

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {

        HttpServletRequest req = (HttpServletRequest) request;
        var authHeader = req.getHeader("X-GREEN-APP-ID");

//        LOG.info("-------------------------------------------------------------------------------------------");
//        LOG.info(" /" + req.getMethod());
//        LOG.info(" Request: " + req.getRequestURI());
//        LOG.info("-------------------------------------------------------------------------------------------");

        logrequest(req);
        if (isNull(authHeader) || !Objects.equals(authHeader, "GREEN")) {
            HttpServletResponse httpResponse = (HttpServletResponse) response;
            httpResponse.setContentType("application/json");
            httpResponse.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Required headers not specified in the request or incorrect");
            return;
        }
        chain.doFilter(request, response);
    }

    public void logrequest(HttpServletRequest httpRequest) {
        LOG.info(" \n\n Headers");

        Enumeration headerNames = httpRequest.getHeaderNames();
        while (headerNames.hasMoreElements()) {
            String headerName = (String) headerNames.nextElement();
            LOG.info(headerName + " = " + httpRequest.getHeader(headerName));
        }

        LOG.info("\n\nParameters");

        Enumeration params = httpRequest.getParameterNames();
        while (params.hasMoreElements()) {
            String paramName = (String) params.nextElement();
            LOG.info(paramName + " = " + httpRequest.getParameter(paramName));
        }

        LOG.info("\n\n Row data");
        LOG.info(extractPostRequestBody(httpRequest));
    }

    static String extractPostRequestBody(HttpServletRequest request) {
        if ("GET".equalsIgnoreCase(request.getMethod())) {
            Scanner s = null;
            try {
                s = new Scanner(request.getInputStream(), "UTF-8").useDelimiter("\\A");
            } catch (Exception e) {
                e.printStackTrace();
            }
            return s.hasNext() ? s.next() : "";
        }
        return "";
    }


    @Override
    public void init(FilterConfig filterConfig) {
        LOG.warn("Auth filter initialization");
    }
}