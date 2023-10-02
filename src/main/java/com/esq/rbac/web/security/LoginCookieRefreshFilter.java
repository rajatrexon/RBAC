/*
 * Copyright (c)2018 ESQ Management Solutions Pvt Ltd. All Rights Reserved.
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

import com.esq.rbac.web.util.RBACUtil;
import jakarta.servlet.*;
import jakarta.servlet.http.*;
import org.apereo.cas.client.session.SingleSignOutHandler;
import org.apereo.cas.client.util.CommonUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.io.IOException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/*
 * RBAC-1388 - To prevent session fixation, it will create a new session, 
 * every time a ticket is received & session is known to server.
 */
public class LoginCookieRefreshFilter implements Filter {

	public static final SingleSignOutHandler handler = new SingleSignOutHandler();
	
	private static final Logger log = LoggerFactory.getLogger(LoginCookieRefreshFilter.class);
	private static final String ATTRIBUTES_RETAIN_PARAM = "SESSION_ATTRIBUTES_TO_RETAIN";
	private List<String> attributesToRetain = new ArrayList<String>();
	private List<String> excludedUrls;
	String excludePattern;
	
	private String artifactParameterName = "ticket";
	private String logoutParameterName = "logoutRequest";
	public List<String> safeParameters = new ArrayList<String>();
	
	@Override
	public void init(FilterConfig filterConfig) throws ServletException {
		
		handler.setArtifactParameterName(artifactParameterName);
		handler.setLogoutParameterName(logoutParameterName);
		
		
		attributesToRetain.clear();	
		safeParameters.add(artifactParameterName);
		safeParameters.add(logoutParameterName);
		
		try{
			attributesToRetain.addAll(generateRetainAttributes(filterConfig.getInitParameter(ATTRIBUTES_RETAIN_PARAM)));
			
		}
		catch(Exception e){
			//continue, we shouldn't worry about exceptions here, client may be ignorant
			log.error("init; Exception={};", e);
		}
		log.info("init; filter loaded;");

//		excludePattern = filterConfig.getInitParameter("excludedUrls");
//		excludedUrls = Arrays.asList(excludePattern.split(","));
	}

	public boolean isTokenRequest(final HttpServletRequest request) {
		return CommonUtils.isNotBlank(CommonUtils.safeGetParameter(request, artifactParameterName, safeParameters));
	}
	
	@Override
	public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
			throws IOException, ServletException {
		HttpServletRequest httpRequest = (HttpServletRequest) request;
		HttpServletResponse httpResponse = (HttpServletResponse) response;
		Method singleSignOutHandler = null;
		// check if it is the token login request(having ticket)
		try {
			singleSignOutHandler = SingleSignOutHandler.class.getDeclaredMethod("isTokenRequest", HttpServletRequest.class);
			singleSignOutHandler.setAccessible(true);
			handler.init();
		boolean isTokenRefresh = (boolean) singleSignOutHandler.invoke(handler, httpRequest);
	//	log.info("isToeknRefresh boolean value===" + isTokenRefresh);
		if (isTokenRequest(httpRequest)) {
		//	log.info("isTokenRequest:====" + (boolean) singleSignOutHandler.invoke(handler, httpRequest));
			HttpSession session = httpRequest.getSession();
			// remove previous session if it is known to server
			if (!session.isNew()) {
			//	log.info("session.isNew:====" + session.isNew());
				String previousSessionHash = RBACUtil.hashString(session.getId());
				Map<String, Object> attributesMap = new HashMap<String, Object>();
				if(!attributesToRetain.isEmpty()){
					for(String attrKey: attributesToRetain){
						if(session.getAttribute(attrKey)!=null){
							attributesMap.put(attrKey, session.getAttribute(attrKey));
						}
					}
				}				
				session.invalidate();
				httpRequest.getSession();
				if(!attributesMap.isEmpty()){
					for(String attrKey: attributesMap.keySet()){
						httpRequest.getSession().setAttribute(attrKey, attributesMap.get(attrKey));	
					}
				}
				
//				  log.info("doFilter; previous sessionHash = {}; renewed sessionHash = {};",
//				  previousSessionHash, RBACUtil.hashString(httpRequest.getSession().getId()));
				 
			}
		}
	}catch (Exception e) {
		// TODO Auto-generated catch block
		log.error("error: {}", e);
	}
		/* String refererHeader = httpRequest.getHeader("referer");
        // no need to continue if the header is missing
        if (refererHeader == null && (!httpRequest.getRequestURI().equals("/rbac") && !httpRequest.getRequestURI().equals("/rbac/j_spring_cas_security_check") && !httpRequest.getRequestURI().equals("/rbac/") && !httpRequest.getRequestURI().equals("/rbac/j_spring_cas_security_check/")) ) {
            //response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
        	log.error("Error : referer is not found for "+ httpRequest.getRequestURI());
        	HttpServletResponse httpResponse=(HttpServletResponse)response;
        	httpResponse.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            return;
        	
        } */
		chain.doFilter(httpRequest, response);
	}

	@Override
	public void destroy() {
		log.info("destroy; filter unloaded;");
	}
	
	public List<String> generateRetainAttributes(String retainAttributesParam){
		List<String> returnList = new ArrayList<>();
		if(retainAttributesParam!=null && !retainAttributesParam.isEmpty()){
			String[] attributes = retainAttributesParam.split(",");
			if(attributes!=null && attributes.length > 0){
				for(String attribute: attributes){
					if(attribute!=null && attribute.length()> 0 && (attribute.trim().length()>0)){
						returnList.add(attribute.trim());
					}
				}
			}
		}
		return returnList;
	}
}
