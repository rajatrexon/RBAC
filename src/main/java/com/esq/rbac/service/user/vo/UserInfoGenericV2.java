package com.esq.rbac.service.user.vo;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import java.util.Date;

public class UserInfoGenericV2 extends UserInfoGeneric {
    private Integer groupId;
    private Long tenantId;
    private String tenantName;
    private String tenantLogoPath;
    private String tenantType;
    private String tenantSubType;
    /* Added By Fazia 19-Dec-2018
     * This Flag was added to store the status of maker checker feature for the tenant*/
    private boolean makerCheckerEnabledInTenant;
    //End
    private boolean isSystemMultiTenant = false;

    public Integer getGroupId() {
        return groupId;
    }
    public void setGroupId(Integer groupId) {
        this.groupId = groupId;
    }
    public Long getTenantId() {
        return tenantId;
    }
    public void setTenantId(Long tenantId) {
        this.tenantId = tenantId;
    }
    public String getTenantName() {
        return tenantName;
    }
    public void setTenantName(String tenantName) {
        this.tenantName = tenantName;
    }
    public String getTenantLogoPath() {
        return tenantLogoPath;
    }
    public void setTenantLogoPath(String tenantLogoPath) {
        this.tenantLogoPath = tenantLogoPath;
    }

    public boolean isMakerCheckerEnabledInTenant() {
        return makerCheckerEnabledInTenant;
    }
    public void setMakerCheckerEnabledInTenant(boolean makerCheckerEnabledInTenant) {
        this.makerCheckerEnabledInTenant = makerCheckerEnabledInTenant;
    }
    public String getTenantType() {
        return tenantType;
    }
    public void setTenantType(String tenantType) {
        this.tenantType = tenantType;
    }
    public String getTenantSubType() {
        return tenantSubType;
    }
    public void setTenantSubType(String tenantSubType) {
        this.tenantSubType = tenantSubType;
    }

    @JsonSerialize()
    @JsonInclude(value= JsonInclude.Include.ALWAYS)
    public Date getLastSuccessfulLoginTime() {
        return super.getLastSuccessfulLoginTime();
    }

    public boolean isSystemMultiTenant() {
        return isSystemMultiTenant;
    }
    public void setSystemMultiTenant(boolean isSystemMultiTenant) {
        this.isSystemMultiTenant = isSystemMultiTenant;
    }

    public static UserInfoGeneric toUserInfoGeneric(UserInfoGenericV2 userInfoGenericV2){
        UserInfoGeneric userInfoGeneric = new UserInfoGeneric();
        userInfoGeneric.setApplicationName(userInfoGenericV2.getApplicationName());
        userInfoGeneric.setDisplayName(userInfoGenericV2.getDisplayName());
        userInfoGeneric.setFirstName(userInfoGenericV2.getFirstName());
        userInfoGeneric.setGroup(userInfoGenericV2.getGroup());
        userInfoGeneric.setLastName(userInfoGenericV2.getLastName());
        userInfoGeneric.setLastSuccessfulLoginTime(userInfoGenericV2.getLastSuccessfulLoginTime());
        userInfoGeneric.setPermissions(userInfoGenericV2.getPermissions());
        userInfoGeneric.setRoles(userInfoGenericV2.getRoles());
        userInfoGeneric.setScopes(userInfoGenericV2.getScopes());
        userInfoGeneric.setUserId(userInfoGenericV2.getUserId());
        userInfoGeneric.setUserName(userInfoGenericV2.getUserName());
        userInfoGeneric.setVariables(userInfoGenericV2.getVariables());
        if(userInfoGenericV2.getAdditionalData()!=null && !userInfoGenericV2.getAdditionalData().isEmpty()){
            for(String key:userInfoGenericV2.getAdditionalData().keySet()){
                userInfoGeneric.addAdditionalData(key, userInfoGenericV2.getAdditionalData().get(key));
            }
        }
        return userInfoGeneric;
    }

    public static UserInfoDetails toUserInfoDetails(UserInfoGenericV2 userInfoGenericV2){
        if(userInfoGenericV2!=null){
            UserInfoDetails userInfoDetails = new UserInfoDetails(UserInfo.fromUserInfoGenericV2(userInfoGenericV2));
            userInfoDetails.setLastSuccessfulLoginTime(userInfoGenericV2.getLastSuccessfulLoginTime());
            return userInfoDetails;
        }
        return null;
    }

}
