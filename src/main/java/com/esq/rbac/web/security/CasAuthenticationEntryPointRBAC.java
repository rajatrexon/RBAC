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
import com.esq.rbac.web.util.ErrorConstants;
import com.esq.rbac.web.util.RBACUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.cas.web.CasAuthenticationEntryPoint;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;

public class CasAuthenticationEntryPointRBAC extends
		CasAuthenticationEntryPoint {

	private static final Logger log = LoggerFactory
			.getLogger(CasAuthenticationEntryPointRBAC.class);
	
	@Override
	protected void preCommence(final HttpServletRequest request,
			final HttpServletResponse response) {
		 if ("XMLHttpRequest".equals(request.getHeader("X-Requested-With"))) {
	        	try{
	        		response.resetBuffer();
	        		response.setStatus(ErrorConstants.HTTP_RESPONSE_SESSION_EXPIRED);
	        		response.setHeader("Content-Type", "application/json");
	        		response.getOutputStream().print("{\"errorCode\":\"sessionExpired\"}");
	        		response.flushBuffer();
					String message = "CasAuthenticationEntryPointRBAC; ajax request found; host={"
							+ RBACUtil.getRemoteAddress(request) + "}; throwing sessionExpired("
							+ ErrorConstants.HTTP_RESPONSE_SESSION_EXPIRED + ") for request={" + request.getPathInfo()
							+ "}";
					log.error("message");
					throw new NoStackTraceException(message, null, false, false);
	        	}
	        	catch(IOException io){
	        		log.error("preCommence; Exception={}", io);
	        	}
	        }
	        else {
	            super.preCommence(request, response);
	        }
	}
}
