package com.esq.rbac.service.user.azurmanagementconfig;

public class AzureIdentities {
    String signInType;
    String issuer;
    String issuerAssignedId;

    public String getSignInType() {
        return signInType;
    }

    public void setSignInType(String signInType) {
        this.signInType = signInType;
    }

    public String getIssuer() {
        return issuer;
    }

    public void setIssuer(String issuer) {
        this.issuer = issuer;
    }

    public String getIssuerAssignedId() {
        return issuerAssignedId;
    }

    public void setIssuerAssignedId(String issuerAssignedId) {
        this.issuerAssignedId = issuerAssignedId;
    }

    @Override
    public String toString() {
        return "AzureIdentities [signInType=" + signInType + ", issuer=" + issuer + ", issuerAssignedId="
                + issuerAssignedId + "]";
    }
}
