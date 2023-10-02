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

import com.esq.rbac.web.util.RBACUtil;
import org.apache.commons.codec.digest.DigestUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jakarta.servlet.http.*;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class HashMapBackedUserSwitchMappingStorage {

	private static final Logger log = LoggerFactory
			.getLogger(HashMapBackedUserSwitchMappingStorage.class);

	private final Set<String> USER_SWITCH_HASH = new HashSet<String>();
	private final Map<String, String> SESSION_ID_TO_USER_SWITCH_HASH = new HashMap<String, String>();
	private final Map<String, String> SESSION_ID_TO_CAS_TICKET = new HashMap<String, String>();
	private final Map<String, HttpSession> CAS_TICKET_TO_SESSION = new HashMap<String, HttpSession>();

	public static final String USER_SWITCH_HASH_KEY = "userSwitchHash";

	public synchronized void addSessionById(String casTicket,
			HttpSession session, Integer timeoutInSeconds) {
		String userSwitchHash = generateUserSwitchHash(casTicket, session.getId());
		USER_SWITCH_HASH.add(userSwitchHash);
		SESSION_ID_TO_USER_SWITCH_HASH.put(session.getId(), userSwitchHash);
		SESSION_ID_TO_CAS_TICKET.put(session.getId(), casTicket);
		CAS_TICKET_TO_SESSION.put(casTicket, session);
		if(session.getAttribute(USER_SWITCH_HASH_KEY)!=null){
			USER_SWITCH_HASH.remove(session.getAttribute(USER_SWITCH_HASH_KEY).toString());
		}
		session.setAttribute(USER_SWITCH_HASH_KEY, userSwitchHash);
		session.setAttribute(RBACUtil.CAS_TICKET_SESSION_ATTRIBUTE, casTicket);
//		log.debug("addSessionById; web.xml maxInActiveInterval={}", session.getMaxInactiveInterval()); 
		session.setMaxInactiveInterval(timeoutInSeconds);
//		log.debug("addSessionById; new MaxInActiveInterval={}", session.getMaxInactiveInterval()); 
//		log.info("sessionInfo; sessionCreated; casTicket={}; maxInActiveIntervalInSeconds={}; sessionHash={}; ",
//				casTicket, session.getMaxInactiveInterval(),
//				RBACUtil.hashString(session.getId()));
//		log.debug("addSessionById; Session=[" + RBACUtil.hashString(session.getId()) + "] USER_SWITCH_HASH_KEY={};" , session.getAttribute(USER_SWITCH_HASH_KEY));
	}

	public synchronized void removeBySessionById(String sessionId) {
		log.debug("Attempting to remove Session=[" + RBACUtil.hashString(sessionId) + "]");
		final String casTicket = SESSION_ID_TO_CAS_TICKET.get(sessionId);
		if (casTicket==null) {
			log.debug("No casTicket for session found. Ignoring. sessionHash={};", RBACUtil.hashString(sessionId));
		}
		final String userSwitchHash = SESSION_ID_TO_USER_SWITCH_HASH
				.get(sessionId);
		if (userSwitchHash==null) {
			log.debug("No userSwitchHash for session found. Ignoring. sessionHash={};", RBACUtil.hashString(sessionId));
		}
		CAS_TICKET_TO_SESSION.remove(casTicket);
		SESSION_ID_TO_CAS_TICKET.remove(sessionId);
		SESSION_ID_TO_USER_SWITCH_HASH.remove(sessionId);
		USER_SWITCH_HASH.remove(userSwitchHash);
	}

	public synchronized HttpSession removeByCasTicket(String casTicket) {
		log.debug("Attempting to remove casTicket=[" + casTicket + "]");
		final HttpSession session = CAS_TICKET_TO_SESSION.get(casTicket);
		if (session != null) {
			String id = session.getId();
//			removeBySessionById(session.getId());
			removeBySessionById(id);
			session.invalidate();
		} else {
			log.debug("No sessionId for casTicket found. Ignoring. casTicket={};", casTicket);
		}
		return session;
	}
	
	public synchronized boolean isUserSwitchHashValid(String userSwitchHash){
		return USER_SWITCH_HASH.contains(userSwitchHash);
	}
	
	private synchronized String generateUserSwitchHash(String casTicket,
			String sessionId) {
		String digest = null;
		try {
			digest = DigestUtils.md5Hex( casTicket + sessionId );
		}
		catch (Exception e) {
			log.error("generateUserSwitchHash; Exception={}", e);
		}
		return digest;
	}
}
