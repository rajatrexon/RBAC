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
package com.esq.rbac.web.client;
import com.esq.rbac.web.vo.*;

import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class UserDetailsRBAC extends UserDetails {

	protected final String group;

	protected final Integer groupId;
	protected final List<String> roles = new LinkedList<String>();
	protected final Map<String, Object> additionalData = new LinkedHashMap<String, Object>();
	
	protected final Long tenantId;
	protected final String tenantName;
	protected final String tenantLogoPath;
	protected final String tenantType;
	protected final String tenantSubType;
	protected List<Long> selectedTenantList = new LinkedList<Long>();
	protected final List<Tenant> availableTenants = new LinkedList<Tenant>();
	protected final List<Long> availableTenantIds = new LinkedList<Long>();
	protected boolean isSystemMultiTenant = false;
	/* Added By Fazia 19-Dec-2018
	 * This Flag was added to store the status of maker checker feature for the tenant*/
	private boolean makerCheckerEnabledInTenant;

	//End
	
	public Long getTenantId() {
		return tenantId;
	}
	
	public String getTenantName() {
		return tenantName;
	}

	public String getTenantLogoPath() {
		return tenantLogoPath;
	}
	
	public String getTenantType() {
		return tenantType;
	}

	public String getTenantSubType() {
		return tenantSubType;
	}
	
	/*
	 * public UserDetails(UserInfo userInfo) { this.userInfo = userInfo; }
	 */

	
	public List<Tenant> getAvailableTenants() {
		return availableTenants;
	}

	public List<Long> getAvailableTenantIds() {
		return availableTenantIds;
	}
	
	public boolean isMakerCheckerEnabledInTenant() {
		return makerCheckerEnabledInTenant;
	}

	public void setMakerCheckerEnabledInTenant(boolean makerCheckerEnabledInTenant) {
		this.makerCheckerEnabledInTenant = makerCheckerEnabledInTenant;
	}

	public UserDetailsRBAC(UserInfoGenericV2 userInfoGenericV2) {
		super(UserInfoGenericV2.toUserInfoDetails(userInfoGenericV2));
		this.group = userInfoGenericV2.getGroup();
		this.groupId = userInfoGenericV2.getGroupId();
		this.tenantId = userInfoGenericV2.getTenantId();
		this.tenantName = userInfoGenericV2.getTenantName();
		this.tenantLogoPath = userInfoGenericV2.getTenantLogoPath();
		this.tenantType = userInfoGenericV2.getTenantType();
		this.tenantSubType = userInfoGenericV2.getTenantSubType();
		this.makerCheckerEnabledInTenant = userInfoGenericV2.isMakerCheckerEnabledInTenant(); /* Added By Fazia 19-Dec-2018 */
		this.roles.addAll(userInfoGenericV2.getRoles());
		this.additionalData.putAll(userInfoGenericV2.getAdditionalData());
		this.isSystemMultiTenant = userInfoGenericV2.isSystemMultiTenant();
	}
	
	public UserDetailsRBAC(UserInfoGenericV3 userInfoGenericV3) {
		super(UserInfoGenericV3.toUserInfoDetails(userInfoGenericV3));
		this.group = userInfoGenericV3.getGroup();
		this.groupId = userInfoGenericV3.getGroupId();
		this.tenantId = userInfoGenericV3.getTenantId();
		this.tenantName = userInfoGenericV3.getTenantName();
		this.tenantLogoPath = userInfoGenericV3.getTenantLogoPath();
		this.tenantType = userInfoGenericV3.getTenantType();
		this.tenantSubType = userInfoGenericV3.getTenantSubType();
		this.makerCheckerEnabledInTenant = userInfoGenericV3.isMakerCheckerEnabledInTenant(); /* Added By Fazia 19-Dec-2018 */
		this.roles.addAll(userInfoGenericV3.getRoles());
		this.additionalData.putAll(userInfoGenericV3.getAdditionalData());
		this.isSystemMultiTenant = userInfoGenericV3.isSystemMultiTenant();
	}
	
	public UserDetailsRBAC(UserInfoGenericV2 userInfoGenericV2, List<Tenant> tenantList) {
		super(UserInfoGenericV2.toUserInfoDetails(userInfoGenericV2));
		this.group = userInfoGenericV2.getGroup();
		this.groupId = userInfoGenericV2.getGroupId();
		this.tenantId = userInfoGenericV2.getTenantId();
		this.tenantName = userInfoGenericV2.getTenantName();
		this.tenantLogoPath = userInfoGenericV2.getTenantLogoPath();
		this.tenantType = userInfoGenericV2.getTenantType();
		this.tenantSubType = userInfoGenericV2.getTenantSubType();
		this.makerCheckerEnabledInTenant = userInfoGenericV2.isMakerCheckerEnabledInTenant(); /* Added By Fazia 19-Dec-2018 */
		this.roles.addAll(userInfoGenericV2.getRoles());
		this.additionalData.putAll(userInfoGenericV2.getAdditionalData());
		if(tenantList!=null && !tenantList.isEmpty()){
			selectedTenantList = getDefaultSelectedTenantList(userInfoGenericV2, tenantList);
			availableTenants.addAll(tenantList);
			for(Tenant t:tenantList){
				availableTenantIds.add(t.getTenantId());
			}
		}
		this.isSystemMultiTenant = userInfoGenericV2.isSystemMultiTenant();
	}
	
	public UserDetailsRBAC(UserInfoGenericV3 userInfoGenericV3, List<Tenant> tenantList) {
		super(UserInfoGenericV3.toUserInfoDetails(userInfoGenericV3));
		this.group = userInfoGenericV3.getGroup();
		this.groupId = userInfoGenericV3.getGroupId();
		this.tenantId = userInfoGenericV3.getTenantId();
		this.tenantName = userInfoGenericV3.getTenantName();
		this.tenantLogoPath = userInfoGenericV3.getTenantLogoPath();
		this.tenantType = userInfoGenericV3.getTenantType();
		this.tenantSubType = userInfoGenericV3.getTenantSubType();
		this.makerCheckerEnabledInTenant = userInfoGenericV3.isMakerCheckerEnabledInTenant(); /* Added By Fazia 19-Dec-2018 */
		this.roles.addAll(userInfoGenericV3.getRoles());
		this.additionalData.putAll(userInfoGenericV3.getAdditionalData());
		if(tenantList!=null && !tenantList.isEmpty()){
			selectedTenantList = getDefaultSelectedTenantList(userInfoGenericV3, tenantList);
			availableTenants.addAll(tenantList);
			for(Tenant t:tenantList){
				availableTenantIds.add(t.getTenantId());
			}
		}
		this.isSystemMultiTenant = userInfoGenericV3.isSystemMultiTenant();
	}
	
	@Deprecated
	public UserDetailsRBAC(UserInfoRBAC userInfoRBAC) {
		super(UserInfoRBAC.toUserInfoDetails(userInfoRBAC));
		this.group = userInfoRBAC.getGroup();
		this.groupId = userInfoRBAC.getGroupId();
		this.tenantId = null;
		this.tenantName = null;
		this.tenantLogoPath = null;
		this.tenantType = null;
		this.tenantSubType = null;
	}
	
	@Deprecated
	public UserDetailsRBAC(UserInfoDetails userInfoDetails) {
		super(userInfoDetails);
		this.group = null;
		this.groupId = null;
		this.tenantId = null;
		this.tenantName = null;
		this.tenantLogoPath = null;
		this.tenantType = null;
		this.tenantSubType = null;
	}

	public String getGroup() {
		return group;
	}

	public Integer getGroupId() {
		return groupId;
	}

	public List<Long> getSelectedTenantList() {
		return selectedTenantList;
	}

	public void setSelectedTenantList(List<Long> selectedTenantList) {
		this.selectedTenantList = selectedTenantList;
	}
	
	public List<String> getRoles() {
		return roles;
	}

	public Map<String, Object> getAdditionalData() {
		return additionalData;
	}

	public boolean isSystemMultiTenant() {
		return isSystemMultiTenant;
	}

	private static List<Long> getDefaultSelectedTenantList(UserInfoGenericV2 userInfoGenericV2, List<Tenant> tenantList){
		/*
		 *  tenantList empty      					:return userTenant
			tenantList size=1   					:return first in tenantList
			tenantList contains userTenant  		:return userTenant
			tenantList doesn't contain userTenant  	:return first in tenantList
		 * */
		List<Long> resultList = new LinkedList<Long>();
		if(tenantList==null || tenantList.isEmpty()){
			resultList.add(userInfoGenericV2.getTenantId());
		}
		else if(tenantList.size() == 1){
			resultList.add(tenantList.get(0).getTenantId());
		}
		else{
			boolean found = false;
			for(Tenant t: tenantList){
				if(t.getTenantId().equals(userInfoGenericV2.getTenantId())){
					resultList.add(userInfoGenericV2.getTenantId());
					found = true;
					break;
				}
			}
			if(!found){
				resultList.add(tenantList.get(0).getTenantId());
			}
		}
		return resultList;
	}
	
}
