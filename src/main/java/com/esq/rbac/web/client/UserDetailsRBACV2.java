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
import com.esq.rbac.web.vo.Tenant;
import com.esq.rbac.web.vo.UserInfoGenericV2;
import com.esq.rbac.web.vo.UserInfoGenericV3;

import java.util.List;

/*
 * Doesn't support getTenantDetails & getTenantIds
 */
@SuppressWarnings("serial")
public class UserDetailsRBACV2 extends UserDetailsRBAC {

	public UserDetailsRBACV2(UserInfoGenericV2 userInfoGenericV2, List<Long> tenantIdList) {
		super(userInfoGenericV2);
		if(tenantIdList!=null && !tenantIdList.isEmpty()){
			selectedTenantList = tenantIdList;
		}
	}
	
	public UserDetailsRBACV2(UserInfoGenericV3 userInfoGenericV3, List<Long> tenantIdList) {
		super(userInfoGenericV3);
		if(tenantIdList!=null && !tenantIdList.isEmpty()){
			selectedTenantList = tenantIdList;
		}
	}
	
	public List<Tenant> getAvailableTenants() {
		throw new UnsupportedOperationException();
	}
	
	public List<Long> getAvailableTenantIds() {
		throw new UnsupportedOperationException();
	}
	
	public static UserDetailsRBACV2 fromUserDetailsRBAC(UserInfoGenericV2 userInfoGenericV2, List<Tenant> tenantList) {
		UserDetailsRBAC userDetailsRBAC = new UserDetailsRBAC(userInfoGenericV2, tenantList);
		return new UserDetailsRBACV2(userInfoGenericV2, userDetailsRBAC.getSelectedTenantList());
	}
	
	public static UserDetailsRBACV2 fromUserDetailsRBAC(UserInfoGenericV3 userInfoGenericV3, List<Tenant> tenantList) {
		UserDetailsRBAC userDetailsRBAC = new UserDetailsRBAC(userInfoGenericV3, tenantList);
		return new UserDetailsRBACV2(userInfoGenericV3, userDetailsRBAC.getSelectedTenantList());
	}

}
