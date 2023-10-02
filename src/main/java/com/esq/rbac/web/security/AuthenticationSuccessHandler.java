///*
// * Copyright (c)2014 ESQ Management Solutions Pvt Ltd. All Rights Reserved.
// *
// * Permission to use, copy, modify, and distribute this software requires
// * a signed licensing agreement.
// *
// * IN NO EVENT SHALL ESQ BE LIABLE TO ANY PARTY FOR DIRECT, INDIRECT, SPECIAL,
// * INCIDENTAL, OR CONSEQUENTIAL DAMAGES, INCLUDING LOST PROFITS, ARISING OUT OF
// * THE USE OF THIS SOFTWARE AND ITS DOCUMENTATION, EVEN IF ESQ HAS BEEN ADVISED
// * OF THE POSSIBILITY OF SUCH DAMAGE. ESQ SPECIFICALLY DISCLAIMS ANY WARRANTIES,
// * INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND
// * FITNESS FOR A PARTICULAR PURPOSE.
// */
//package com.esq.rbac.web.security;
//
//import com.esq.rbac.web.util.RBACUtil;
//import jakarta.servlet.ServletException;
//import jakarta.servlet.http.HttpServletRequest;
//import jakarta.servlet.http.HttpServletResponse;
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;
//import org.springframework.security.core.Authentication;
//import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
//import org.springframework.security.web.savedrequest.HttpSessionRequestCache;
//import org.springframework.security.web.savedrequest.RequestCache;
//import org.springframework.security.web.savedrequest.SavedRequest;
//import org.springframework.util.StringUtils;
//import java.io.IOException;
//
//public class AuthenticationSuccessHandler extends
//		SimpleUrlAuthenticationSuccessHandler {
//
//	private static final Logger log = LoggerFactory
//			.getLogger(AuthenticationSuccessHandler.class);
//
//	private RequestCache requestCache = new HttpSessionRequestCache();
//
//	@Override
//	public void onAuthenticationSuccess(HttpServletRequest request,
//										HttpServletResponse response, Authentication authentication)
//			throws ServletException, IOException {
//		SavedRequest savedRequest = requestCache.getRequest(request, response);
//
//		if (savedRequest == null) {
//			super.onAuthenticationSuccess(request, response, authentication);
//
//			return;
//		}
//		log.trace("onAuthenticationSuccess; savedRequest={};", savedRequest.getRedirectUrl());
//		String targetUrlParameter = getTargetUrlParameter();
//		if (isAlwaysUseDefaultTargetUrl()
//				|| (targetUrlParameter != null && StringUtils.hasText(request
//						.getParameter(targetUrlParameter)))) {
//			requestCache.removeRequest(request, response);
//			super.onAuthenticationSuccess(request, response, authentication);
//
//			return;
//		}
//
//		clearAuthenticationAttributes(request);
//
//		//if AJAX request, redirect to default URL
//		if (savedRequest.getHeaderValues("X-Requested-With")!=null && savedRequest.getHeaderValues("X-Requested-With").contains("XMLHttpRequest")) {
//			 	log.info("onAuthenticationSuccess; ajax request found; host={}; redirecting to default url; savedRequestUrl={}", RBACUtil.getRemoteAddress(request), savedRequest.getRedirectUrl());
//				getRedirectStrategy().sendRedirect(request, response, getDefaultTargetUrl());
//				return;
//		}
//		// Use the DefaultSavedRequest URL
//		String targetUrl = savedRequest.getRedirectUrl();
//		log.debug("onAuthenticationSuccess; host={}; redirecting to defaultSavedRequestUrl={}",  RBACUtil.getRemoteAddress(request), targetUrl);
//		getRedirectStrategy().sendRedirect(request, response, targetUrl);
//	}
//
//	public void setRequestCache(RequestCache requestCache) {
//		this.requestCache = requestCache;
//	}
//}
