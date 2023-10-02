package com.esq.rbac.service.contact.customuserinfo.domain;

import com.esq.rbac.service.calendar.domain.Calendar;
import com.esq.rbac.service.user.domain.User;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.io.Serializable;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CustomUserInfo implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;
    private Integer userId;
    private String userName;
    private Integer groupId;
    private String firstName;
    private String lastName;
    private String emailAddress;
    private String homeEmailAddress;
    private String phoneNumber;
    private String homePhoneNumber;
    private Long organizationId;
    private Calendar userCalendar;
    private Calendar orgCalendar;

    public static CustomUserInfo getUserInfo(User user){
        CustomUserInfo userInfo = new CustomUserInfo();
        userInfo.setUserId(user.getUserId());
        userInfo.setUserName(user.getUserName());
        userInfo.setFirstName(user.getFirstName());
        userInfo.setLastName(user.getLastName());
        userInfo.setEmailAddress(user.getEmailAddress());
        userInfo.setHomeEmailAddress(user.getHomeEmailAddress());
        userInfo.setPhoneNumber(user.getPhoneNumber());
        userInfo.setHomePhoneNumber(user.getHomePhoneNumber());
        userInfo.setGroupId(user.getGroupId());
        userInfo.setOrganizationId(user.getOrganizationId());
        userInfo.setOrgCalendar(user.getOrgCalendar());
        userInfo.setUserCalendar(user.getUserCalendar());
        return userInfo;
    }
}
