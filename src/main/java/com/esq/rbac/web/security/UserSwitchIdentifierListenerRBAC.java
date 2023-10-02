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

import com.esq.rbac.web.client.RestClient;
import com.esq.rbac.web.client.SSORestClient;
import jakarta.servlet.http.HttpSessionEvent;
import jakarta.servlet.http.HttpSessionListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;

public class UserSwitchIdentifierListenerRBAC implements HttpSessionListener {

	private static final Logger log = LoggerFactory
			.getLogger(UserSwitchIdentifierListenerRBAC.class);
	private RestClient restClient;
	private SSORestClient sSORestClient;
	
	@Override
	public void sessionCreated(final HttpSessionEvent event) {
		//do some initialization, set restClient here
		if(sSORestClient == null) {
			synchronized(this) {
				ApplicationContext ctx = WebApplicationContextUtils
						.getWebApplicationContext(event.getSession()
								.getServletContext());
				restClient = ctx.getBean(RestClient.class);
				sSORestClient = ctx.getBean(SSORestClient.class);
}
		}
	}

	@Override
	public void sessionDestroyed(final HttpSessionEvent event) {
//		log.debug("sessionDestroyed; sessionHash={}", RBACUtil.hashString(event.getSession().getId()));
//		UserSwitchIdentifierFilterRBAC.getUserSessionMap().removeBySessionById(event.getSession().getId());
//		log.debug("sessionDestroyed; casTicket={}", event.getSession().getAttribute(RBACUtil.CAS_TICKET_SESSION_ATTRIBUTE));
//		HttpSession session = event.getSession();
//		if (session.getAttribute("userName") != null
//				&& session.getAttribute(RBACUtil.CAS_TICKET_SESSION_ATTRIBUTE) != null) {
//			String casTicket = session.getAttribute(RBACUtil.CAS_TICKET_SESSION_ATTRIBUTE).toString();
//			String userName = session.getAttribute("userName").toString();
//			SessionRegistryLogoutRequest request = new SessionRegistryLogoutRequest();
//			request.setUserName(userName);
//			request.setTicketToLogout(casTicket);
//			request.setLogoutAction(RBACUtil.LOGOUT_ACTION.LOGOUT_SSO_TICKET);
//			request.setLogoutType(LogoutRequest.LOGOUT_TYPE_SESSION_TIMEOUT);
//			request.setRequestId(RBACUtil.generateLogoutRequestId());
//			request.setClientIp(session.getAttribute("clientIp") != null ? session
//					.getAttribute("clientIp").toString() : null);
//			log.info("sessionInfo; sessionDestroyed; casTicket={}; maxInActiveIntervalInSeconds={};",
//					casTicket, session.getMaxInactiveInterval());
//			if(restClient.getAppKeyHeader()!=null && !restClient.getAppKeyHeader().isEmpty()){
//				request.setAppKey(restClient.getAppKeyHeader());
//				sSORestClient.resource("login", "sessionRegistryLogout")
//				.header("loggedInUserName", userName)
//				.header(RBACUtil.APP_KEY_IDENTIFIER_HEADER, restClient.getAppKeyHeader())
//				.entity(request, MediaType.APPLICATION_JSON).post();
//			}
//			else{
//				sSORestClient.resource("login", "sessionRegistryLogout")
//					.header("loggedInUserName", userName)
//					.entity(request, MediaType.APPLICATION_JSON).post();
//			}
//			session.invalidate();
//		}
	}

}
