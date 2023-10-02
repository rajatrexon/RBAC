package com.esq.rbac.service.user.embedded;

import com.esq.rbac.service.user.domain.User;

public class OrganizationInfoUser extends User {

    private String groupName;

    public OrganizationInfoUser(User user) {
        super();
        this.setUserId(user.getUserId());
        this.setUserName(user.getUserName());
        this.setEmailAddress(user.getEmailAddress());
        this.setHomeEmailAddress(user.getHomeEmailAddress());
        this.setHomePhoneNumber(user.getHomePhoneNumber());
        this.setIsEnabled(user.getIsEnabled());
        this.setIsLocked(user.getIsLocked());
        this.setNotes(user.getNotes());
        this.setOrganizationId(user.getOrganizationId());
        this.setPhoneNumber(user.getPhoneNumber());
        this.setGroupId(user.getGroupId());
    }

    public String getGroupName() {
        return groupName;
    }

    public void setGroupName(String groupName) {
        this.groupName = groupName;
    }

}