/*
 * Copyright (c)2014 ESQ Management Solutions Pvt Ltd. All Rights Reserved.
 *
 * Permission to use, copy, modify, and distribute this software requires
 * a signed licensing agreement.
 *
 * IN NO EVENT SHALL ESQ BE LIABLE TO ANY PARTY FOR DIRECT, INDIRECT, SPECIAL,
 * INCIDENTAL, OR CONSEQUENTIAL DAMAGES, INCLUDING LOST PROFITS, ARISING OUT OF
 * THE USE OF THIS SOFTWARE AND ITS DOCUMENTATION, EVEN IF ESQ HAS BEEN ADVISED
 * OF THE POSSIBILITY OF SUCH DAMAGE. ESQ SPECIFICALLY DISCLAIMS ANY WARRANTIES,
 * INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND
 * FITNESS FOR A PARTICULAR PURPOSE.
 */
package com.esq.rbac.web.security;

import com.esq.rbac.web.exception.NoStackTraceException;
import com.esq.rbac.web.client.UserDetailsService;
import com.esq.rbac.web.util.DeploymentUtil;
import com.esq.rbac.web.util.ErrorConstants;
import com.esq.rbac.web.util.RBACUtil;
import jakarta.servlet.*;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.apereo.cas.client.session.SingleSignOutHandler;
import org.apereo.cas.client.util.AbstractConfigurationFilter;
import org.apereo.cas.client.util.CommonUtils;
import org.apereo.cas.client.util.XmlUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class UserSwitchIdentifierFilterRBAC extends AbstractConfigurationFilter {

	private static final Logger log = LoggerFactory
			.getLogger(UserSwitchIdentifierFilterRBAC.class);
	private DeploymentUtil deploymentUtil;
    private UserDetailsService userDetailsService;
	private static final SingleSignOutHandler handler = new SingleSignOutHandler();

	private String artifactParameterName = "ticket";
	private String logoutParameterName = "logoutRequest";
	public List<String> safeParameters = new ArrayList<String>();
 
	/** The logout callback path configured at the CAS server, if there is one */
    private String logoutCallbackPath;

	private static final HashMapBackedUserSwitchMappingStorage userSessionMap = new HashMapBackedUserSwitchMappingStorage();
	
	public static String loggedInSessionId; 
	
	@Override
	public void destroy() {
	}

	public boolean isTokenRequest(final HttpServletRequest request) {
		return CommonUtils.isNotBlank(CommonUtils.safeGetParameter(request, artifactParameterName, safeParameters));
	}
	
	public boolean isMultipartRequest(final HttpServletRequest request) {
        return request.getContentType() != null && request.getContentType().toLowerCase().startsWith("multipart");
    }
	
	private String getPath(HttpServletRequest request) {
        return request.getServletPath() + CommonUtils.nullToEmpty(request.getPathInfo());
    }
	
	public boolean pathEligibleForLogout(HttpServletRequest request) {
        return logoutCallbackPath == null || logoutCallbackPath.equals(getPath(request));
    }
	
	public boolean isLogoutRequest(final HttpServletRequest request) {
//		if ("POST".equalsIgnoreCase(request.getMethod())) {
//            return !isMultipartRequest(request)
//                    && pathEligibleForLogout(request)
//                    && CommonUtils.isNotBlank(CommonUtils.safeGetParameter(request, logoutParameterName,
//                    safeParameters));
//        }
//        
//        if ("GET".equalsIgnoreCase(request.getMethod())) {
//            return CommonUtils.isNotBlank(CommonUtils.safeGetParameter(request, logoutParameterName, safeParameters));
//        }
//        return false;
		

        return "POST".equals(request.getMethod()) &&
            CommonUtils.isNotBlank(CommonUtils.safeGetParameter(request, this.logoutParameterName));
	}
	
//		 private void destroySession(final HttpServletRequest request) {
//		        String logoutMessage = CommonUtils.safeGetParameter(request, this.logoutParameterName, this.safeParameters);
//		        if (CommonUtils.isBlank(logoutMessage)) {
//		            logger.error("Could not locate logout message of the request from {}", this.logoutParameterName);
//		            return;
//		        }
//		        
//		        if (!logoutMessage.contains("SessionIndex")) {
//		            logoutMessage = uncompressLogoutMessage(logoutMessage);
//		        }
//		        
//		        logger.trace("Logout request:\n{}", logoutMessage);
//		        final String token = XmlUtils.getTextForElement(logoutMessage, "SessionIndex");
//		        if (CommonUtils.isNotBlank(token)) {
//		            final HttpSession session = this.sessionMappingStorage.removeSessionByMappingId(token);
//
//		            if (session != null) {
//		                final String sessionID = session.getId();
//		                logger.debug("Invalidating session [{}] for token [{}]", sessionID, token);
//
//		                try {
//		                    session.invalidate();
//		                } catch (final IllegalStateException e) {
//		                    logger.debug("Error invalidating session.", e);
//		                }
//		                this.logoutStrategy.logout(request);
//		            }
//		        }
//		    }
//	    }
	
	@Override
	public void doFilter(final ServletRequest servletRequest,
						 final ServletResponse servletResponse, final FilterChain filterChain)
			throws IOException, ServletException {
		
		safeParameters.add(artifactParameterName);
		safeParameters.add(logoutParameterName);		
		
		if(deploymentUtil==null){
			synchronized (this) {
				ApplicationContext ctx = WebApplicationContextUtils
						.getWebApplicationContext(servletRequest.getServletContext());
				deploymentUtil = ctx.getBean(DeploymentUtil.class);
			}
		}
		if(userDetailsService==null){
			synchronized (this) {
				ApplicationContext ctx = WebApplicationContextUtils
						.getWebApplicationContext(servletRequest.getServletContext());
				userDetailsService = ctx.getBean(UserDetailsService.class);
			}
		}
		final HttpServletRequest request = (HttpServletRequest) servletRequest;
		final HttpServletResponse resp = (HttpServletResponse) servletResponse;
		handler.init();
		try {	
			
		if (isTokenRequest(request)) {
			final String casTicket = CommonUtils.safeGetParameter(request,
					this.artifactParameterName, this.safeParameters);
			userSessionMap.addSessionById(casTicket, request.getSession(true), deploymentUtil.getSessionInactivityTimeoutSeconds());
		} else if (isLogoutRequest(request)) {
			final String logoutMessage = CommonUtils.safeGetParameter(request,
					this.logoutParameterName);
			final String casTicket = XmlUtils
					.getTextForElement(logoutMessage,
					"SessionIndex");
			userSessionMap.removeByCasTicket(casTicket);
			HttpSession session  = request.getSession(false);
//			if(session!=null) {
//				try {
//                    session.invalidate();
//                } catch (final IllegalStateException e) {
//                    logger.debug("Error invalidating session.", e);
//                }
//			}
			
		} else {
//	         if (!jSessionIdIsInSession)
//	         	ReportBean.needLogin = true;
			
			if(request.getSession(false)!=null && request.getSession(false).getAttribute("userName") == null){
				HttpSession userSession = request.getSession(false);
				if(userDetailsService.getCurrentUserDetails()!=null){
					userSession.setAttribute("userName", userDetailsService
							.getCurrentUserDetails().getUsername());
					userSession.setAttribute("clientIp", RBACUtil.getRemoteAddress(request));
					log.info("sessionInfo; userNameAddedToSession; maxInActiveIntervalInSeconds={}; sessionHash={}; userName={};",
							userSession.getMaxInactiveInterval(), RBACUtil.hashString(userSession.getId()), userDetailsService
							.getCurrentUserDetails().getUsername());
				}
			}
			//removed "XMLHttpRequest".equals(request.getHeader("X-Requested-With")) && from below if condition
			if ("XMLHttpRequest".equals(request.getHeader("X-Requested-With")) && request.getHeader("x-user-switch-header-rbac") != null) {
//				UserSwitchIdentifierFilterRBAC.getUserSessionMap().removeBySessionById(request.getSession(false).getId());
				Cookie[] cookies = request.getCookies();
		         boolean jSessionIdIsInSession = false;
		         for(Cookie cookie : cookies){
		         	if(cookie.getName().equalsIgnoreCase("JSESSIONID")){
		         		jSessionIdIsInSession = true;
		         		if(cookie.getValue().isEmpty() || cookie.getValue() == null || request.getSession(false) == null) {
		         			resp.resetBuffer();
			        		resp.setStatus(ErrorConstants.HTTP_RESPONSE_SESSION_EXPIRED);
			        		resp.setHeader("Content-Type", "application/json");
			        		resp.getOutputStream().print("{\"errorCode\":\"sessionExpired\"}");
			        		resp.flushBuffer();
							String message = "UserSwitchIdentifierFilterRBAC; ajax request found; host={"
									+ RBACUtil.getRemoteAddress(request) + "}; throwing sessionExpired("
									+ ErrorConstants.HTTP_RESPONSE_SESSION_EXPIRED + ") for request={"
									+ request.getPathInfo() + "}";
							log.error(message);
							throw new NoStackTraceException(message, null, false, false);
		         		}
							//ReportBean.needLogin = true;
					}
				 }

				if(!userSessionMap.isUserSwitchHashValid(request.getHeader("x-user-switch-header-rbac"))){
					if(request.getSession()!=null && request.getSession().getAttribute(HashMapBackedUserSwitchMappingStorage.USER_SWITCH_HASH_KEY)!=null){
						if(!((String)request.getSession().getAttribute(HashMapBackedUserSwitchMappingStorage.USER_SWITCH_HASH_KEY)).equals(request.getHeader("x-user-switch-header-rbac"))){
							log.info("doFilter; User Switched; host={}; request={} ", RBACUtil.getRemoteAddress(request), request.getPathInfo());
							try{
								HttpServletResponse response = (HttpServletResponse) servletResponse;
				        		response.resetBuffer();
				        		response.setStatus(ErrorConstants.HTTP_RESPONSE_USER_SWITCHED);
				        		response.setHeader("Content-Type", "application/json");
				        		response.getOutputStream().print("{\"errorCode\":\"userSwitched\"}");
				        		response.flushBuffer();
								String message = "UserSwitchIdentifierFilterRBAC; ajax request found; host={"
										+ RBACUtil.getRemoteAddress(request) + "} throwing userSwitched("
										+ ErrorConstants.HTTP_RESPONSE_USER_SWITCHED + ") for request={"
										+ request.getPathInfo() + "}";
								log.error(message);
								throw new NoStackTraceException(message, null, false, false);
				        	}
				        	catch(IOException io){
				        		log.error("doFilter; Exception={}", io);
				        	}
						}
					}
					else{
						try{
							HttpServletResponse response = (HttpServletResponse) servletResponse;
			        		response.resetBuffer();
			        		response.setStatus(ErrorConstants.HTTP_RESPONSE_USER_SWITCH_HASH_INVALID);
			        		response.setHeader("Content-Type", "application/json");
			        		response.getOutputStream().print("{\"errorCode\":\"userSwitchHashInvalid\"}");
			        		response.flushBuffer();
							String message = "UserSwitchIdentifierFilterRBAC; ajax request found; host={"
									+ RBACUtil.getRemoteAddress(request) + "} throwing userSwitchHashInvalid("
									+ ErrorConstants.HTTP_RESPONSE_USER_SWITCH_HASH_INVALID + ") for request={"
									+ request.getPathInfo() + "}";
							log.error(message);
							throw new NoStackTraceException(message, null, false, false);
			        	}
			        	catch(IOException io){
			        		log.error("doFilter; Exception={}", io);
			        	}
					}
					return;
				}
			}
		}
		
		}catch(Exception e) {
			log.error("error: {}", e);
		}
		filterChain.doFilter(servletRequest, servletResponse);
	}

	@Override
	public void init(final FilterConfig filterConfig) throws ServletException {
		if (!isIgnoreInitConfiguration()) {

			handler.setArtifactParameterName(artifactParameterName);
			handler.setLogoutParameterName(logoutParameterName);
			
		}
	}
	
	protected static HashMapBackedUserSwitchMappingStorage getUserSessionMap() {
        return userSessionMap;
    }

}
