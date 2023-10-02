/*
 * Copyright (c)2013,2014 ESQ Management Solutions Pvt Ltd. All Rights Reserved.
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
package com.esq.rbac.web.vo;

import java.util.*;

public class CurrentUser {

    private Integer userId;
    private String userName;
    private String firstName;
    private String lastName;
    private String displayName;
    private String applicationName;
    private Set<String> permissions;
    private Map<String, String> scopes;
    private Integer groupId;
    private String groupName;
    private Date lastSuccessfulLoginTime;
    private String tenantLogoUrl;
    private List<Long> selectedTenantList = new LinkedList<Long>();
    private Long tenantId;
    private boolean isSystemMultiTenant;
    private boolean isHostLoggedIn; 
    /* Start
	 * Added By Fazia 19-Dec-2018
	 * This Flag was added to check enabled or disabled maker checker feature for the tenant*/
    private boolean makerCheckerEnabledInTenant;
    //End
    private String applicationContextName;
	private String timezone;
	private String preferredLanguage;
	private String lastSuccessfulLoginTimeDisplay;
	private String dateTimeDisplayFormat;
	
    public String getLastSuccessfulLoginTimeDisplay() {
		return lastSuccessfulLoginTimeDisplay;
	}

	public void setLastSuccessfulLoginTimeDisplay(String lastSuccessfulLoginTimeDisplay) {
		this.lastSuccessfulLoginTimeDisplay = lastSuccessfulLoginTimeDisplay;
	}

	public String getDateTimeDisplayFormat() {
		return dateTimeDisplayFormat;
	}

	public void setDateTimeDisplayFormat(String dateTimeDisplayFormat) {
		this.dateTimeDisplayFormat = dateTimeDisplayFormat;
	}

	public String getTimezone() {
		return timezone;
	}

	public void setTimezone(String timezone) {
		this.timezone = timezone;
	}

	public String getPreferredLanguage() {
		return preferredLanguage;
	}

	public void setPreferredLanguage(String preferredLanguage) {
		this.preferredLanguage = preferredLanguage;
	}

	public String getApplicationContextName() {
		return applicationContextName;
	}

	public void setApplicationContextName(String applicationContextName) {
		this.applicationContextName = applicationContextName;
	}
    
	public Integer getUserId() {
        return userId;
    }

    public void setUserId(Integer userId) {
        this.userId = userId;
    }

	public boolean isMakerCheckerEnabledInTenant() {
		return makerCheckerEnabledInTenant;
	}

	public void setMakerCheckerEnabledInTenant(boolean makerCheckerEnabledInTenant) {
		this.makerCheckerEnabledInTenant = makerCheckerEnabledInTenant;
	}

	public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public String getApplicationName() {
        return applicationName;
    }

    public void setApplicationName(String applicationName) {
        this.applicationName = applicationName;
    }

    public Set<String> getPermissions() {
        return permissions;
    }

    public void setPermissions(Set<String> permissions) {
        this.permissions = permissions;
    }

    public Map<String, String> getScopes() {
        return scopes;
    }

    public void setScopes(Map<String, String> scopes) {
        this.scopes = scopes;
    }

	public Date getLastSuccessfulLoginTime() {
		return lastSuccessfulLoginTime;
	}

	public void setLastSuccessfulLoginTime(Date lastSuccessfulLoginTime) {
		this.lastSuccessfulLoginTime = lastSuccessfulLoginTime;
	}

	public String getTenantLogoUrl() {
		return tenantLogoUrl;
	}

	public void setTenantLogoUrl(String tenantLogoUrl) {
		this.tenantLogoUrl = tenantLogoUrl;
	}

	public Integer getGroupId() {
		return groupId;
	}

	public void setGroupId(Integer groupId) {
		this.groupId = groupId;
	}

	public String getGroupName() {
		return groupName;
	}

	public void setGroupName(String groupName) {
		this.groupName = groupName;
	}

	public List<Long> getSelectedTenantList() {
		return selectedTenantList;
	}

	public void setSelectedTenantList(List<Long> selectedTenantList) {
		this.selectedTenantList = selectedTenantList;
	}

	public Long getTenantId() {
		return tenantId;
	}

	public void setTenantId(Long tenantId) {
		this.tenantId = tenantId;
	}

	public boolean isSystemMultiTenant() {
		return isSystemMultiTenant;
	}

	public void setSystemMultiTenant(boolean isSystemMultiTenant) {
		this.isSystemMultiTenant = isSystemMultiTenant;
	}

	public boolean isHostLoggedIn() {
		return isHostLoggedIn;
	}

	public void setHostLoggedIn(boolean isHostLoggedIn) {
		this.isHostLoggedIn = isHostLoggedIn;
	}
}
