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

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonManagedReference;

import java.util.HashSet;
import java.util.Set;
import java.util.TreeSet;

public class ChildApplication implements Comparable<ChildApplication> {
    private Integer childApplicationId;

    private String childApplicationName;
    @JsonBackReference("ApplicationChild")
    private Application application;
    private Boolean isDefault;
    private Integer appType;
    private Boolean allowMultipleLogins;

    private String appKey;

    @JsonManagedReference("ApplicationUrls")
    private Set<AppUrlData> appUrlDataSet;
    private String permissionValidator;
    private String permissionValidatorData;
    private String description;
    private ChildApplicationLicense childApplicationLicense;
    private ChildApplicationLicense changeLicense;

    public ChildApplication() {

    }

    @JsonIgnore
    public static boolean isSSO(Integer code) {
        return APP_TYPE.SSO.getCode().equals(code);
    }

    @JsonIgnore
    public static boolean isNonSSO(Integer code) {
        return APP_TYPE.NON_SSO.getCode().equals(code);
    }

    @JsonIgnore
    public static boolean isNative(Integer code) {
        return APP_TYPE.NATIVE.getCode().equals(code);
    }

    public Set<AppUrlData> getAppUrlDataSet() {
        if (appUrlDataSet != null && !(appUrlDataSet instanceof TreeSet)) {
            appUrlDataSet = new TreeSet<AppUrlData>(appUrlDataSet);
        }
        return appUrlDataSet;
    }

    public void setAppUrlDataSet(Set<AppUrlData> appUrlDataSet) {
        this.appUrlDataSet = appUrlDataSet;
    }

    public Integer getChildApplicationId() {
        return childApplicationId;
    }

    public void setChildApplicationId(Integer childApplicationId) {
        this.childApplicationId = childApplicationId;
    }

    public String getChildApplicationName() {
        return childApplicationName;
    }

    public void setChildApplicationName(String childApplicationName) {
        this.childApplicationName = childApplicationName;
    }

    public Application getApplication() {
        return application;
    }

    public final void setApplication(Application application) {
        // set new application
        this.application = application;

        // add this ChildApplication to newly set application
        if (this.application != null) {
            if (this.application.getChildApplications() == null) {
                this.application.setChildApplications(new HashSet<ChildApplication>());
            }
            if (!this.application.getChildApplications().contains(this)) {
                this.application.getChildApplications().add(this);
            }
        }
    }

    public Boolean getIsDefault() {
        return isDefault;
    }

    public void setIsDefault(Boolean isDefault) {
        this.isDefault = isDefault;
    }

    public Integer getAppType() {
        return appType;
    }

    public void setAppType(Integer appType) {
        this.appType = appType;
    }

    public Boolean isAllowMultipleLogins() {
        return allowMultipleLogins;
    }

    public void setAllowMultipleLogins(Boolean allowMultipleLogins) {
        this.allowMultipleLogins = allowMultipleLogins;
    }

    public String getAppKey() {
        return appKey;
    }

    public void setAppKey(String appKey) {
        this.appKey = appKey;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    @JsonIgnore
    public Boolean isNonSSOAllowMultipleLogins() {
        if (appType != null && appType.equals(APP_TYPE.NON_SSO.getCode())) {
            return allowMultipleLogins != null && allowMultipleLogins.equals(Boolean.TRUE);
        }
        return false;
    }

    @Override
    public int compareTo(ChildApplication o) {
        if (this.childApplicationName != null) {
            return this.childApplicationName.compareTo(o.childApplicationName);
        }
        return 0;
    }

    @JsonIgnore
    public String getFirstServiceUrl() {
        if (appUrlDataSet != null && !appUrlDataSet.isEmpty()) {
            for (AppUrlData urlData : appUrlDataSet) {
                return urlData.getServiceUrl();
            }
        }
        return null;
    }

    @JsonIgnore
    public String getFirstHomeUrl() {
        if (appUrlDataSet != null && !appUrlDataSet.isEmpty()) {
            for (AppUrlData urlData : appUrlDataSet) {
                return urlData.getHomeUrl();
            }
        }
        return null;
    }

    @JsonIgnore
    public String getHomeUrlByTag(String tag) {
        if (appUrlDataSet != null && !appUrlDataSet.isEmpty() && tag != null && !tag.isEmpty()) {
            for (AppUrlData urlData : appUrlDataSet) {
                //RBAC-774, handling for case when tag is missed/null and only one url is present. If more than one url is present,
                //there is no way to determine which one to return.
                if (urlData.getTag() == null && appUrlDataSet.size() == 1) {
                    return urlData.getHomeUrl();
                }
                if (tag.equalsIgnoreCase(urlData.getTag())) {
                    return urlData.getHomeUrl();
                }
            }
        }
        return null;
    }

    @JsonIgnore
    public String getPermissionValidator() {
        return permissionValidator;
    }

    public void setPermissionValidator(String permissionValidator) {
        this.permissionValidator = permissionValidator;
    }

    @JsonIgnore
    public String getPermissionValidatorData() {
        return permissionValidatorData;
    }

    public void setPermissionValidatorData(String permissionValidatorData) {
        this.permissionValidatorData = permissionValidatorData;
    }

    @JsonIgnore
    //need to hide this from everywhere except license API
    public ChildApplicationLicense getChildApplicationLicense() {
        return childApplicationLicense;
    }

    @JsonIgnore
    public void setChildApplicationLicense(ChildApplicationLicense childApplicationLicense) {
        this.childApplicationLicense = childApplicationLicense;
    }

    public ChildApplicationLicense getChangeLicense() {
        return changeLicense;
    }

    public void setChangeLicense(ChildApplicationLicense changeLicense) {
        this.changeLicense = changeLicense;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("Context(name=").append(childApplicationName == null ? "" : childApplicationName);
        sb.append("; appType=").append(appType == null ? "" : APP_TYPE.getByCode(appType));
        sb.append("; appKey=").append(appKey == null ? "" : appKey);
        sb.append("; allowMultipleLogins=").append(allowMultipleLogins == null ? "" : allowMultipleLogins);
        sb.append("; description=").append(description == null ? "" : description);
        if (appUrlDataSet != null && appUrlDataSet.size() > 0) {
            sb.append("; deploymentUrls=(").append(new TreeSet<AppUrlData>(appUrlDataSet) + ")");
        }
        if (changeLicense != null && changeLicense.getLicense() != null && !changeLicense.getLicense().isEmpty()) {
            sb.append("; license=").append("************");
        }
        if (changeLicense != null && changeLicense.getAdditionalData() != null && !changeLicense.getAdditionalData().isEmpty()) {
            sb.append("; licenseAdditonalData=").append(changeLicense.getAdditionalData());
        }
        sb.append(")");
        return sb.toString();
    }

    public enum APP_TYPE {
        SSO(0), NON_SSO(1), NATIVE(2);
        private final Integer code;

        APP_TYPE(Integer code) {
            this.code = code;
        }

        public static APP_TYPE getByCode(Integer code) {
            switch (code) {
                case 0:
                    return SSO;
                case 1:
                    return NON_SSO;
                case 2:
                    return NATIVE;
                default:
                    return SSO;
            }
        }

        public Integer getCode() {
            return code;
        }

        public boolean equals(Integer code) {
            return this.code.equals(code);
        }
    }
}
