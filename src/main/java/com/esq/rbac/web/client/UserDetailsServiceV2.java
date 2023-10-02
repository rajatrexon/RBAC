/*
 * Copyright (c)2017 ESQ Management Solutions Pvt Ltd. All Rights Reserved.
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
package com.esq.rbac.web.client;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
/*
 * Doesn't fetching all the tenants in the REST call, doesn't support getTenantDetails & getTenantIds
 */

public class UserDetailsServiceV2 extends UserDetailsService {

	private static final Logger log = LoggerFactory.getLogger(UserDetailsServiceV2.class);

	@Override
	public UserDetailsRBACV2 loadUserByUsername(String userName) throws UsernameNotFoundException {
//        log.trace("loadUserByUsernameUserDetailsRBACV2; userName={}", userName);
//		Client client = ClientBuilder.newClient();
//		WebTarget baseTarget = client.target("http://localhost:8002/rbac/restapp");
//        boolean isRBACApp = false;
//        if(applicationName!=null && RBACUtil.RBAC_UAM_APPLICATION_NAME.equalsIgnoreCase(applicationName)){
//        	isRBACApp = true;
//        }
//        try {
//			UserInfoGenericV3 userInfoGenericV3 = baseTarget.path("userInfo/v3/detailsWithAttributes")
//                    .queryParam("userName", userName)
//                    .queryParam("applicationName", applicationName)
//                    .request(MediaType.APPLICATION_JSON)
//                    .get(UserInfoGenericV3.class);
//        	if(userInfoGenericV3.getTenantId()==null && isRBACApp){
//        		 log.error("loadUserByUsername; userInfo/v3/detailsWithAttributes; username={}; no organization/tenant found for user", userName);
//        	     throw new UsernameNotFoundException("no organization/tenant found for "+userName);
//        	}
//        	List<Long> selectedTenants = null;
//        	if(userInfoGenericV3.getTenantId()!=null) {
//        		try{
//	        		selectedTenants = restClient.resource(
//	                		"tenants", "defaultSelectedTenantList", userInfoGenericV3.getTenantId().toString())
//	        				.uriBuilderFactory(UriBuilder.fromUri())
//	        				.accept(MediaType.APPLICATION_JSON)
//	        				.get(new GenericType<List<Long>>() {
//	        				});
//        		}
//        		catch (RuntimeException e) {
//              	  	log.error("loadUserByUsername; tenants/defaultSelectedTenantList; username={}; exception={}", userName, e);
//              	  	if(isRBACApp){
//              	  		log.error("Request failed for tenants/defaultSelectedTenantList, won't try with any deprecated API, "
//              	  				+ "update RBAC or check the configuration");
//              	  		throw new UsernameNotFoundException(userName+" Request failed for defaultSelectedTenantList");
//              	  	}
//              	  	List<Tenant> allTenants = restClient.resource("tenants")
//				  				.queryParam("userName", userName)
//				  				.accept(MediaType.APPLICATION_JSON)
//				  				.get(new GenericType<List<Tenant>>() {
//				  				});
//              	  	return UserDetailsRBACV2.fromUserDetailsRBAC(userInfoGenericV3, allTenants);
//        		}
//        	}
//            return new UserDetailsRBACV2(userInfoGenericV3, selectedTenants);
//
//        } catch (RuntimeException e) {
//        	  log.error("loadUserByUsername; userInfo/v3/detailsWithAttributes; username={}; exception={}", userName, e);
//              log.error("Request failed for userInfo/v3/detailsWithAttributes, won't try with any deprecated API, update RBAC or check the configuration");
//        }
//        throw new UsernameNotFoundException(userName);
//    }
//
//	public List<Tenant> getTenantDetails() {
//		throw new UnsupportedOperationException();
//	}
////
//	public List<Long> getTenantIds() {
//		throw new UnsupportedOperationException();
//	}
        return null;
    }
}
