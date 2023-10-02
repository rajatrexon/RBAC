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

import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpServletResponseWrapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.io.IOException;

public class RewriteResponseWrapper extends HttpServletResponseWrapper {

	private static final Logger log = LoggerFactory
			.getLogger(RewriteResponseWrapper.class);

	public RewriteResponseWrapper(HttpServletResponse response) {
		super(response);
	}

	/*
	 * it should be done by web server, just added this to support 1 deployment,
	 * should not be reused
	 */
	@Override
	public void sendRedirect(String location) throws IOException {
		log.debug("sendRedirect; original location={};", location);
		location = location.replaceAll(
				"(?i)" + RewriteResponseFilter.getHostName(),
				RewriteResponseFilter.getRedirectHostName());
		log.debug("sendRedirect; replaced location={};", location);
		String baseUrl = RewriteResponseFilter.getRedirectProtocol() + "://"
				+ RewriteResponseFilter.getRedirectHostName();
		if (location.toLowerCase().startsWith("http:") == false
				&& location.toLowerCase().startsWith("https:") == false
				&& location.toLowerCase().startsWith(baseUrl.toLowerCase()) == false) {
			location = baseUrl + location;
		}
		if (location.toLowerCase().startsWith(
				(RewriteResponseFilter.getRedirectProtocol() + ":")
						.toLowerCase()) == false) {
			location = location.replaceAll("http%3A",
					RewriteResponseFilter.getRedirectProtocol() + "%3A");
			location = location.replaceAll("https%3A",
					RewriteResponseFilter.getRedirectProtocol() + "%3A");
			location = location.replaceAll("http:",
					RewriteResponseFilter.getRedirectProtocol() + ":");
			location = location.replaceAll("https:",
					RewriteResponseFilter.getRedirectProtocol() + ":");
		}
		log.debug("sendRedirect; final location={};", location);
		super.sendRedirect(location);
	}

}
