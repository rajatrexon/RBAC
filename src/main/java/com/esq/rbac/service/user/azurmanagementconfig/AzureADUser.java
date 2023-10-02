package com.esq.rbac.service.user.azurmanagementconfig;

import java.util.HashSet;
import java.util.Set;

public class AzureADUser {
    Boolean accountEnabled;
    String displayName;
    String givenName;
    String surname;
    String mail;

    Set<String> otherMails = new HashSet<String>();
    String mailNickname;
    String mobilePhone;

    Set<String> businessPhones = new HashSet<String>();
    //
//	String userPrincipalName;
    String userType;
    Set<AzureIdentities> identities;

    String passwordPolicies;
    PasswordProfile passwordProfile;
    String extension_3b1724e0cfb042908690028a932e5d69_RBACRedirectUrl;
    String extension_3b1724e0cfb042908690028a932e5d69_RBACCloudexaClientID;
//	String extension_3b1724e0cfb042908690028a932e5d69_RBACCloudexaUserPrincipalName;

    public String getMailNickname() {
        return mailNickname;
    }

    public void setMailNickname(String mailNickname) {
        this.mailNickname = mailNickname;
    }

    public String getExtension_3b1724e0cfb042908690028a932e5d69_RBACRedirectUrl() {
        return extension_3b1724e0cfb042908690028a932e5d69_RBACRedirectUrl;
    }

    public void setExtension_3b1724e0cfb042908690028a932e5d69_RBACRedirectUrl(
            String extension_3b1724e0cfb042908690028a932e5d69_RBACRedirectUrl) {
        this.extension_3b1724e0cfb042908690028a932e5d69_RBACRedirectUrl = extension_3b1724e0cfb042908690028a932e5d69_RBACRedirectUrl;
    }

    public String getExtension_3b1724e0cfb042908690028a932e5d69_RBACCloudexaClientID() {
        return extension_3b1724e0cfb042908690028a932e5d69_RBACCloudexaClientID;
    }

    public void setExtension_3b1724e0cfb042908690028a932e5d69_RBACCloudexaClientID(
            String extension_3b1724e0cfb042908690028a932e5d69_RBACCloudexaClientID) {
        this.extension_3b1724e0cfb042908690028a932e5d69_RBACCloudexaClientID = extension_3b1724e0cfb042908690028a932e5d69_RBACCloudexaClientID;
    }

    public Boolean getAccountEnabled() {
        return accountEnabled;
    }

    public void setAccountEnabled(Boolean accountEnabled) {
        this.accountEnabled = accountEnabled;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public String getGivenName() {
        return givenName;
    }

    public void setGivenName(String givenName) {
        this.givenName = givenName;
    }

    public String getSurname() {
        return surname;
    }

    public void setSurname(String surname) {
        this.surname = surname;
    }

    public String getMail() {
        return mail;
    }

    public void setMail(String mail) {
        this.mail = mail;
    }

    public Set<String> getOtherMails() {
        return otherMails;
    }

    public void setOtherMails(Set<String> otherMails) {
        this.otherMails = otherMails;
    }

    public Set<String> getBusinessPhones() {
        return businessPhones;
    }

    public void setBusinessPhones(Set<String> businessPhones) {
        this.businessPhones = businessPhones;
    }

    public String getMobilePhone() {
        return mobilePhone;
    }

    public void setMobilePhone(String mobilePhone) {
        this.mobilePhone = mobilePhone;
    }

//	public String getUserPrincipalName() {
//		return userPrincipalName;
//	}
//
//	public void setUserPrincipalName(String userPrincipalName) {
//		this.userPrincipalName = userPrincipalName;
//	}

    public String getUserType() {
        return userType;
    }

    public void setUserType(String userType) {
        this.userType = userType;
    }

    public String getPasswordPolicies() {
        return passwordPolicies;
    }

    public void setPasswordPolicies(String passwordPolicies) {
        this.passwordPolicies = passwordPolicies;
    }

    public PasswordProfile getPasswordProfile() {
        return passwordProfile;
    }

    public void setPasswordProfile(PasswordProfile passwordProfile) {
        this.passwordProfile = passwordProfile;
    }

    public Set<AzureIdentities> getIdentities() {
        return identities;
    }

    public void setIdentities(Set<AzureIdentities> identities) {
        this.identities = identities;
    }

    @Override
    public String toString() {
        return "AzureADUser [accountEnabled=" + accountEnabled + ", displayName=" + displayName + ", givenName="
                + givenName + ", surname=" + surname + ", mail=" + mail + ", otherMails=" + otherMails
                + ", mailNickname=" + mailNickname + ", mobilePhone=" + mobilePhone + ", businessPhones="
                + businessPhones + ", userType=" + userType + ", identities=" + identities + ", passwordPolicies="
                + passwordPolicies + ", passwordProfile=" + passwordProfile
                + ", extension_3b1724e0cfb042908690028a932e5d69_RBACRedirectUrl="
                + extension_3b1724e0cfb042908690028a932e5d69_RBACRedirectUrl
                + ", extension_3b1724e0cfb042908690028a932e5d69_RBACCloudexaClientID="
                + extension_3b1724e0cfb042908690028a932e5d69_RBACCloudexaClientID + "]";
    }


//	public String getExtension_3b1724e0cfb042908690028a932e5d69_RBACCloudexaUserPrincipalName() {
//		return extension_3b1724e0cfb042908690028a932e5d69_RBACCloudexaUserPrincipalName;
//	}
//
//	public void setExtension_3b1724e0cfb042908690028a932e5d69_RBACCloudexaUserPrincipalName(
//			String extension_3b1724e0cfb042908690028a932e5d69_RBACCloudexaUserPrincipalName) {
//		this.extension_3b1724e0cfb042908690028a932e5d69_RBACCloudexaUserPrincipalName = extension_3b1724e0cfb042908690028a932e5d69_RBACCloudexaUserPrincipalName;
//	}
}
