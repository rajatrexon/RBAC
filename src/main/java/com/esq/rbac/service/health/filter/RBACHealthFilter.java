package com.esq.rbac.service.health.filter;

import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;


import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

@Slf4j
@Component
public class RBACHealthFilter implements Filter {

    public static ConcurrentMap<String, Date> healthMap;


    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        healthMap = new ConcurrentHashMap<String, Date>();
    }

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {
        if (servletRequest instanceof HttpServletRequest) {
            String pathInfo = null;
            try {
                HttpServletRequest request = (HttpServletRequest) servletRequest;
                System.out.printf("path1 %s , path2 %s , path3 %s",request.getRequestURI() , request.getPathInfo(),request.getQueryString());
                pathInfo = ((HttpServletRequest) servletRequest).getPathInfo();
                pathInfo = request.getRequestURI();
                pathInfo = pathInfo.substring(pathInfo.lastIndexOf("/"));
            } catch (Exception e) {
                log.error(e.getMessage());
            }

            if (pathInfo != null && !pathInfo.trim().equals("")) {
                if (pathInfo.lastIndexOf("/") > 0) {
                    try {
                        if (StringUtils.isNumeric(pathInfo.substring(pathInfo.lastIndexOf("/") + 1))) {
                            pathInfo = pathInfo.substring(0, pathInfo.lastIndexOf("/"));
                        }

                    } catch (Exception e) {
                        log.error(e.getMessage());

                    }
                }

            }

            ZonedDateTime zdt = ZonedDateTime.now(ZoneOffset.UTC);

            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMdd HH:mm:ss");

            DateFormat format = new SimpleDateFormat("yyyyMMdd HH:mm:ss");

            String dateStr = zdt.format(formatter);

            Date utcDate = null;

            try {

                utcDate = format.parse(dateStr);

            } catch (Exception e) {
            }

            healthMap.put(pathInfo, utcDate);
            //DateTime.now().toDate()
        }
        filterChain.doFilter(servletRequest, servletResponse);
    }
}

