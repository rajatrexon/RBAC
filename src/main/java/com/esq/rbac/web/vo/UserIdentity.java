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
package com.esq.rbac.web.vo;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

public class UserIdentity {

    public static final String WINDOWS_ACCOUNT = "windowsUser";
    public static final String LDAP_ACCOUNT = "ldapUser";
    public static final String SITEMINDER_ACCOUNT = "siteMinderUser";
    public static final String GOOGLE_OPENID = "googleOpenId";
    public static final String YAHOO_OPENID = "yahooOpenId";
    public static final String AZURE_AD_ACCOUNT="AzureADB2C_ObjectId";
    public static final String FORGEROCK_ACCOUNT = "forgeRockUser";
    public static final String GENERIC_USER_ACCOUNT = "genericHeaderAuthUser";
    private String identityType;
    private String identityId;
    private Boolean disabled;

    public UserIdentity() {
        this(null, null);
    }

    public UserIdentity(String identityType, String identityId) {
        this.identityType = identityType;
        this.identityId = identityId;
    }

    public String getIdentityType() {
        return identityType;
    }

    public void setIdentityType(String identityType) {
        this.identityType = identityType;
    }

    public String getIdentityId() {
        return identityId;
    }

    public void setIdentityId(String identityId) {
        this.identityId = identityId;
    }

    public Boolean getDisabled() {
        return disabled;
    }

    public void setDisabled(Boolean disabled) {
        this.disabled = disabled;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null) {
            return false;
        }
        if (o == this) {
            return true;
        }
        if (o.getClass() != getClass()) {
            return false;
        }
        UserIdentity u = (UserIdentity) o;
        return new EqualsBuilder()
                .append(getIdentityId(), u.getIdentityId())
                .append(getIdentityType(), u.getIdentityType())
                .append(getDisabled(), u.getDisabled())
                .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder()
                .append(getIdentityId())
                .append(getIdentityType())
                .append(getDisabled())
                .toHashCode();
    }
    
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("UserIdentity{identityType=").append(identityType);
        sb.append("; identityId=").append(identityId);
        sb.append("; disabled=").append(disabled);
        sb.append("}");
        return sb.toString();
    }
}
