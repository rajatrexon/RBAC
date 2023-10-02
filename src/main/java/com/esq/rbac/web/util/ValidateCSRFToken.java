package com.esq.rbac.web.util;

import com.esq.rbac.web.exception.NoStackTraceException;
import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public class ValidateCSRFToken implements Filter {
	private static final Logger log = LoggerFactory.getLogger(ValidateCSRFToken.class);
	private List<String> excludedUrls;
	String excludePattern;

	ConcurrentMap<String, String> csrfMap=new ConcurrentHashMap<String, String>();

	@Override
	public void init(FilterConfig filterConfig) throws ServletException {
		// TODO Auto-generated method stub
		 excludePattern = filterConfig.getInitParameter("excludedUrls");
		 excludedUrls = Arrays.asList(excludePattern.split(","));

	}

	@Override
	public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
			throws IOException, ServletException {
		// TODO Auto-generated method stub
		// Assume its HTTP


		HttpServletRequest httpReq = (HttpServletRequest) request;
		HttpServletResponse httpRes = (HttpServletResponse) response;

		HttpSession httpSession = httpReq.getSession(false);

		if(httpReq.getRequestURI().contains("/login/cas")) {
			if (httpReq.getParameter("appKey") != null) {
				if (httpReq.getParameter("appKey").equals("RBAC")) {
					String csrfToken = UUID.randomUUID().toString().replace("-", "");
					httpSession.setAttribute("sessionCSRFToken", csrfToken);
					csrfMap.put(httpSession.getId(), csrfToken);
					chain.doFilter(request, response);
					return;
				}
                //RBAC-2141 Start
                else if (httpReq.getParameter("appKey").equals("PROFILE")) {
					String csrfToken = UUID.randomUUID().toString().replace("-", "");
                    //No session created so far for Profile app, so creating a new session.
                    httpSession = httpReq.getSession(true);
					httpSession.setAttribute("sessionCSRFToken", csrfToken);
					csrfMap.put(httpSession.getId(), csrfToken);
					chain.doFilter(request, response);
					return;
				}
                //End
                else {
					chain.doFilter(httpReq, httpRes);
					return;
				}
			}else {
				chain.doFilter(httpReq, httpRes);
				return;
			}
		}

		if(httpReq.getSession(false) == null) {
	    	   httpRes.resetBuffer();
	    	   httpRes.setStatus(ErrorConstants.HTTP_RESPONSE_SESSION_EXPIRED);
	    	   httpRes.setHeader("Content-Type", "application/json");
	    	   httpRes.getOutputStream().print("{\"errorCode\":\"sessionExpired\"}");
	    	   httpRes.flushBuffer();
	    	   String message = "ValidateCSRFToken; ajax request found; host={" + RBACUtil.getRemoteAddress(httpReq)
						+ "}; throwing sessionExpired(" + ErrorConstants.HTTP_RESPONSE_SESSION_EXPIRED + ") for request={"
						+ httpReq.getPathInfo() + "}";
	    	   log.error(message);
	    	   throw new NoStackTraceException(message, null, false, false);
		}else {
				checkCsrfToken(httpReq, httpRes, chain);
			}

		if (httpReq.getRequestURI().contains("/csrfTokenSessionAlive")) {

			if (checkCsrfToken(httpReq, httpRes, chain)) {

				String csrfToken = UUID.randomUUID().toString().replace("-", "");
				httpSession.setAttribute("sessionCSRFToken", csrfToken);
				csrfMap.put(httpSession.getId(), csrfToken);
				chain.doFilter(request, response);
				return;
			}

		}

	}

	public Boolean checkCsrfToken(HttpServletRequest httpReq, HttpServletResponse httpRes, FilterChain chain)
			throws IOException, ServletException {

		HttpSession httpSession = httpReq.getSession(false);
		String uriPath = httpReq.getRequestURI();

		if (!excludedUrls.contains(uriPath)) {

			String headerCSRFToken = httpReq.getHeader("X-Csrf-Token");
			if (csrfMap.get(httpSession.getId()) != null) {

				if (csrfMap.get(httpSession.getId()).equals(headerCSRFToken)) {

					chain.doFilter(httpReq, httpRes);
					return true;
				}else {
					httpRes.sendError(HttpServletResponse.SC_UNAUTHORIZED);
					return false;
				}
			}else {
				log.error("Ther given session is not valid and the CSRF token is not generated for it");
				httpRes.sendError(HttpServletResponse.SC_UNAUTHORIZED);
				return false;
			}
		}else {
			chain.doFilter(httpReq, httpRes);
		}
		return false;
	}

	@Override
	public void destroy() {
		// TODO Auto-generated method stub

	}
}