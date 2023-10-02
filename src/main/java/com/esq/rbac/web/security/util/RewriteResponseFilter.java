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

package com.esq.rbac.web.security.util;

import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.io.IOException;

public class RewriteResponseFilter implements Filter {
	private static final Logger log = LoggerFactory
			.getLogger(RewriteResponseFilter.class);
	private static String hostName;
	private static String redirectHostName;
	private static String redirectProtocol;

	protected static String getHostName() {
		return RewriteResponseFilter.hostName;
	}

	protected static String getRedirectHostName() {
		return RewriteResponseFilter.redirectHostName;
	}

	protected static String getRedirectProtocol() {
		return RewriteResponseFilter.redirectProtocol;
	}

	@Override
	public void init(FilterConfig filterConfig) throws ServletException {
		log.info("init; RewriteResponseFilter Setting Configuration;");
		hostName = filterConfig.getInitParameter("hostName");
		redirectHostName = filterConfig.getInitParameter("redirectHostName");
		redirectProtocol = filterConfig.getInitParameter("redirectProtocol");
		log.info(
				"init; RewriteResponseFilter Started; hostName={}; redirectHostName={}; redirectProtocol={}",
				hostName, redirectHostName, redirectProtocol);
	}

	/*
	 * it should be done by web server, just added this to support 1 deployment,
	 * should not be reused
	 */
	@Override
	public void doFilter(ServletRequest request, ServletResponse response,
						 FilterChain chain) throws IOException, ServletException {
		HttpServletRequest httpRequest = (HttpServletRequest) request;
		log.trace("doFilter; host={};", httpRequest.getServerName());
		if (httpRequest.getServerName().toLowerCase().contains(hostName)) {
			HttpServletResponse wrappedResponse = new RewriteResponseWrapper(
					(HttpServletResponse) response);
			chain.doFilter(request, wrappedResponse);
			return;
		}
		chain.doFilter(request, response);
	}

	@Override
	public void destroy() {
		log.info("init; RewriteResponseFilter Stopped;");
	}

}
