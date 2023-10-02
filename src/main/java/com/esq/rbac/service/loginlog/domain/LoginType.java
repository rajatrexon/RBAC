package com.esq.rbac.service.loginlog.domain;

public class LoginType {

    private String loginName;

    public LoginType(){

    }
    public LoginType(String loginName){
        this.loginName = loginName;
    }

    public String getLoginName() {
        return loginName;
    }

    public void setLoginName(String loginName) {
        this.loginName = loginName;
    }

    public static final String LOGIN_RBAC = "rbac";
    public static final String LOGIN_INTEGRATED_WINDOWS = "loginAuto";
    public static final String LOGIN_SITEMINDER = "loginSiteMinder";
    public static final String LOGIN_WINDOWS_AD = "loginWindows";//LDAP Login
    public static final String LOGIN_NATIVE = "rbacNativeLogin";
    public static final String LOGIN_IVR="ivr";
    public static final String LOGIN_AZURE_ACTIVE_DIRECTORY="azureActiveDirectoryLogin";
    public static final String LOGIN_FORGEROCK = "loginForgeRock";
    public static final String LOGIN_GENERIC_HEADER_AUTH = "loginGenericHeaderAuth";
    //more fields can be added here

    @Override
    public boolean equals(Object o) {
        if (o == null)
            return false;
        if (!(o instanceof LoginType))
            return false;
        if (o == this)
            return true;
        return this.loginName.equalsIgnoreCase(((LoginType) o).loginName);
    }

    @Override
    public int hashCode() {
        return this.loginName.toLowerCase().hashCode();
    }

    @Override
    public String toString() {
        return loginName;
    }
}
