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
package com.esq.rbac.web.rest;

import com.esq.rbac.web.client.RestClient;
import com.esq.rbac.web.client.UserDetailsService;
import com.esq.rbac.web.exception.ClientHandlerException;
import com.esq.rbac.web.util.DeploymentUtil;
import com.esq.rbac.web.util.RBACUtil;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.reactive.function.client.ClientResponse;
import java.time.LocalDateTime;

@RestController
@RequestMapping("currentSession")
public class CurrentUserSessionRest {
	private static final Logger log = LoggerFactory
			.getLogger(CurrentUserSessionRest.class);
	
	private UserDetailsService userDetailsService;
	private DeploymentUtil deploymentUtil;
	private RestClient restClient;

	@Autowired
	public void setRestClient(RestClient restClient) {
		log.trace("setRestClient; {}", restClient);
		this.restClient = restClient;
	}


	@Autowired
	public void setDeploymentUtil(DeploymentUtil deploymentUtil) {
		this.deploymentUtil = deploymentUtil;
	}
	@Autowired
	public void setUserDetailsService(UserDetailsService userDetailsService) {
		log.trace("setUserDetailService; {}", userDetailsService);
		this.userDetailsService = userDetailsService;
	}

	@GetMapping("/initiateSessionWindow")
	public String initiateSessionWindow(HttpServletRequest httpRequest) {
		HttpSession session = httpRequest.getSession(true);
		if(session!=null){
			synchronized (session) {
				session.setAttribute("userName", userDetailsService
						.getCurrentUserDetails().getUsername());
				session.setAttribute("clientIp", RBACUtil.getRemoteAddress(httpRequest));
				/*here condition put because whenever initiateSessionWindow call initiated new random uid generated */
				log.trace("initiateSessionWindow; clientIp={}; userName={};",
						RBACUtil.getRemoteAddress(httpRequest),
						session.getAttribute("userName"));
			}
		}
		return session!=null?session.getAttribute("userName").toString():"";
	}
	@GetMapping("/getCSRFToken")
	public String getCSRFToken(HttpServletRequest httpRequest) {
		log.debug("/getCSRFToken called");
		log.debug((String)httpRequest.getSession(false).getAttribute("sessionCSRFToken"));
		return (String)httpRequest.getSession(false).getAttribute("sessionCSRFToken");
	}

	@GetMapping("/destroySessionWindow")
	public ResponseEntity destroySessionWindow(HttpServletRequest httpRequest) {
		HttpSession session = httpRequest.getSession(false);
		if(session!=null){
			session.invalidate();
		}
		return ResponseEntity.ok().build();
	}

	@GetMapping("/extendUserSession")
	public ResponseEntity extendUserSession(HttpServletRequest httpRequest) {
		log.debug("/extendUserSession called at {}",LocalDateTime.now());
		HttpSession session = httpRequest.getSession(false);
		if(session!=null){
			session.setMaxInactiveInterval(deploymentUtil.getSessionInactivityTimeoutSeconds());
			log.debug("Session extended to {}",session.getMaxInactiveInterval());
		}
		return ResponseEntity.ok().build();
	}

	@GetMapping("/checkSession")
	public ResponseEntity<String> checkSession(HttpServletRequest httpRequest) {
		log.trace("/checkSession called at {}",LocalDateTime.now());
		HttpSession session = httpRequest.getSession(false);
		Integer userSessionDataCount = 0;
		if(session!=null){
			String casTicket = (String) httpRequest.getSession().getAttribute(RBACUtil.CAS_TICKET_SESSION_ATTRIBUTE);

			ClientResponse clientResponse = null;
			
			MultiValueMap<String, String> queryMap = new LinkedMultiValueMap<>();
			queryMap.add(RBACUtil.TICKET_HEADER_IDENTIFIER, casTicket);
			queryMap.add("userName", (String)httpRequest.getSession().getAttribute("userName"));

			log.trace("sessionHash {}",casTicket);
			try {
				log.trace("Get userSession Data List of All applications");
				clientResponse = restClient.resource("login", "userSessionDataCount")
						.build().get()
						.uri(uriBuilder -> uriBuilder
								.queryParams(queryMap).build())
						.accept(MediaType.APPLICATION_JSON)
								.retrieve().toEntity(ClientResponse.class)
						.block().getBody();
				log.trace("clientResponse {}",clientResponse);
				int httpStatus = clientResponse.statusCode().value();
				log.trace("httpStatus {}",httpStatus);
				if (ClientResponse.create(HttpStatus.OK).statusCode(HttpStatus.OK).equals(httpStatus)) {
					userSessionDataCount = clientResponse.bodyToMono(Integer.class).block();
					log.trace("userSessionDataCount {}",userSessionDataCount);
				}
				} catch (ClientHandlerException ce) {
					log.debug("getUserSessionData; ClientHandlerException={}",
							ce.getMessage());
				}

		}
		return ResponseEntity.ok().body(userSessionDataCount.toString());
	}
}
