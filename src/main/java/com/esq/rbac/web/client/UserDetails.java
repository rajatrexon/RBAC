/*
 * Copyright (c)2013 ESQ Management Solutions Pvt Ltd. All Rights Reserved.
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
import com.esq.rbac.web.vo.UserInfo;
import com.esq.rbac.web.vo.UserInfoDetails;
import org.springframework.security.core.GrantedAuthority;

import java.util.*;

public class UserDetails implements org.springframework.security.core.userdetails.UserDetails {

    private final UserInfo userInfo;
    
    private final Date lastSuccessfulLoginTime;

    /*public UserDetails(UserInfo userInfo) {
        this.userInfo = userInfo;
    }*/
    
    public UserDetails(UserInfoDetails userInfoDetails) {
        this.userInfo = UserInfo.fromUserInfoDetails(userInfoDetails);
        this.lastSuccessfulLoginTime = userInfoDetails.getLastSuccessfulLoginTime();
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return Collections.emptyList();
    }

    @Override
    public String getPassword() {
        return null;
    }

    @Override
    public String getUsername() {
        return userInfo.getUserName();
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }

    public UserInfo getUserInfo() {
        return userInfo;
    }

    public Map<String, List<String>> getPermissions() {
        return userInfo.getPermissions();
    }

	public Date getLastSuccessfulLoginTime() {
		return lastSuccessfulLoginTime;
	}
       
}
