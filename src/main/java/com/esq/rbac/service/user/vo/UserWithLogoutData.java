package com.esq.rbac.service.user.vo;

import com.esq.rbac.service.user.domain.User;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;

import java.util.List;
@Builder
@Data
public class UserWithLogoutData {

    private final User user;
    private final List<SSOLogoutData> ssoLogoutDataList;

    @JsonCreator
    public UserWithLogoutData(
            @JsonProperty("user") User user,
            @JsonProperty("ssoLogoutDataList") List<SSOLogoutData> ssoLogoutDataList) {
        this.user = user;
        this.ssoLogoutDataList = ssoLogoutDataList;
    }

    public User getUser() {
        return user;
    }

    public List<SSOLogoutData> getSsoLogoutDataList() {
        return ssoLogoutDataList;
    }


}
