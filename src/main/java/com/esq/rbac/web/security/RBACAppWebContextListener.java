/*
 * Copyright (c)2015 ESQ Management Solutions Pvt Ltd. All Rights Reserved.
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
import com.esq.rbac.web.util.RBACUtil;
import com.esq.rbac.web.vo.LogoutRequest;
import com.esq.rbac.web.vo.LogoutResponse;
import com.esq.rbac.web.vo.SessionRegistryLogoutRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import reactor.core.publisher.Mono;


public class RBACAppWebContextListener implements ApplicationListener<ContextRefreshedEvent> {
	private static final Logger log = LoggerFactory.getLogger(RBACAppWebContextListener.class);

	@Override
	public void onApplicationEvent(ContextRefreshedEvent event) {
		ApplicationContext ctx = event.getApplicationContext();
		RestClient restClient = ctx.getBean(RestClient.class);
		SSORestClient sSORestClient = ctx.getBean(SSORestClient.class);
		SessionRegistryLogoutRequest request = new SessionRegistryLogoutRequest();
		request.setLogoutAction(RBACUtil.LOGOUT_ACTION.LOGOUT_APP_KEY);
		request.setLogoutType(LogoutRequest.LOGOUT_TYPE_APP_RESTARTED);
		String requestId = RBACUtil.generateLogoutRequestId();
		request.setRequestId(requestId);
		request.setAppKey(restClient.getAppKeyHeader());
		request.setTag(restClient.getTag());
		log.info("onApplicationEvent; appStartSessionRequest; {}; appKey={}; tag={};", requestId, restClient.getAppKeyHeader(),
				restClient.getTag());
		try{
			Mono<ResponseEntity<LogoutResponse>> response = sSORestClient.resource("login", "sessionRegistryLogout").build()
                    .post()
					.accept(MediaType.APPLICATION_JSON)
					.bodyValue(request).retrieve()
							.bodyToMono(LogoutResponse.class)
									.map(ResponseEntity::ok);
			log.info("onApplicationEvent; appStartSessionResponse; {}; status={}; body={};", requestId, response.block().getStatusCode(),
				response.block().toString());
		}
		catch(Exception e){
			// log and forget, RBAC SSO may not be up at this time
			log.info("onApplicationEvent; {}; Exception={}; ", requestId, e);
		}
	}

}
