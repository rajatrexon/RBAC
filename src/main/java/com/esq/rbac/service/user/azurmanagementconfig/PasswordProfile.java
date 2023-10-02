package com.esq.rbac.service.user.azurmanagementconfig;

public class PasswordProfile {
    Boolean forceChangePasswordNextSignIn;
    String password;
    public Boolean getForceChangePasswordNextSignIn() {
        return forceChangePasswordNextSignIn;
    }
    public void setForceChangePasswordNextSignIn(Boolean forceChangePasswordNextSignIn) {
        this.forceChangePasswordNextSignIn = forceChangePasswordNextSignIn;
    }
    public String getPassword() {
        return password;
    }
    public void setPassword(String password) {
        this.password = password;
    }
    @Override
    public String toString() {
        return "PasswordProfile [forceChangePasswordNextSignIn=" + forceChangePasswordNextSignIn + ", password="
                + password + "]";
    }
}
