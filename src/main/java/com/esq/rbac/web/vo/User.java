/*
 * Copyright (c)2013,2014 ESQ Management Solutions Pvt Ltd. All Rights Reserved.
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
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.io.Serializable;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

//@Customizer(UserVariableCustomizer.class)
public class User implements Serializable {

    private static final Logger log = LoggerFactory.getLogger(User.class);
    private Integer userId;
    private Integer groupId;
    private Boolean isEnabled;
    @Size(min = 0, max = 64)
    //@SpecialCharValidator
    private String firstName;
    @Size(min = 0, max = 64)
    //@SpecialCharValidator
    private String lastName;
    @Size(max = 254)
    //@SpecialCharValidator
    private String userName;
    private String changePassword;
    private String passwordSalt;
    private String passwordHash;
    private Date passwordSetTime;
    @Size(min = 0, max = 254)
    //@Pattern(regexp = DeploymentUtil.EMAIL_PATTERN, message = "Invalid Email Address")
    private String emailAddress;
    //@Pattern(regexp = DeploymentUtil.MOBILE_PATTERN, message = "Invalid Phone Number. Only Numbers and length between 7 and 15 are allowed ")
    private String phoneNumber;
    @Size(min = 0, max = 254)
    //@Pattern(regexp = DeploymentUtil.EMAIL_PATTERN, message = "Invalid Email Address")
    private String homeEmailAddress;
    //@Pattern(regexp = DeploymentUtil.MOBILE_PATTERN, message = "Invalid Phone Number. Only Numbers and length between 7 and 15 are allowed ")
    private String homePhoneNumber;
    @Size(min = 0, max = 500)

    private String notes;
    private Boolean changePasswordFlag;
    private Date loginTime;
    private Date lastSuccessfulLoginTime;
	private Date failedLoginTime;
    private Integer consecutiveLoginFailures;
    private Integer consecutiveIVRLoginFailures;
    private Boolean isLocked;
    private Boolean isShared;
    private String timeZone;
    private Boolean useEmailAsUserid;

    /** RBAC-2730 */
    private Boolean isDeleted = false;

    public Boolean getIsDeleted() {
        return isDeleted;
    }

    public void setIsDeleted(Boolean deleted) {
        isDeleted = deleted;
    }
    /** End */
    
    @Size(min=0,max=8)
   // @Pattern(regexp = "^$|([0-9]{5,8})$", message = "IVR UserId must be only digits and between 5 and 8 digits")
    private String ivrUserId;
    //@Transient
    @Size(min=0,max=6)
    //@Pattern(regexp = "^$|([0-9]{6})$", message = "IVR Pin must be only digits and length must be 6 digits")
    private String ivrPin;
    private String ivrPinSalt;
    private String ivrPinHash;
    private Boolean isIVRUserLocked;
    
    
    //@Transient
    private Boolean isPasswordExpired;
    
    //@Transient
    private Boolean generatePasswordFlag;
    
    //@Transient
    private String accountLockedReason;
    //@Transient
    private String userLDAPDistinguishedName;
    private Integer createdBy;
    private Date createdOn;
    private Integer updatedBy;
    private Date updatedOn;
    private Set<String> labels;
    @JsonManagedReference("AttributesDataUser")
    private Set<AttributesData> attributesData;
    /*@JsonManagedReference("UserVariables")
    private Set<Variable> variables;*/
    //@Valid
    private Restriction restrictions;
    private Calendar userCalendar;
    private Calendar orgCalendar;
    private List<UserIdentity> identities = null;
    @NotNull
    private Long organizationId;

    //Added bY Fazia 
    //To suppport MakerChecker
    private Long makerCheckerId;
    private Integer isStatus;
    //@Transient
    private Long loggedInTenantId;
    //@Transient
    private String organizationName;
    //@Transient
    private Boolean editable;
    
    //End
		/* RBAC-1259 START */
   // @Transient
    private String externalRecordId;
		/* RBAC-1259 END */
    
    //RBAC-1562 Starts    
    private String twoFactorAuthChannelType;
    //@Transient
	private Boolean isChannelTypeEmail = false;
	//@Transient
	private Boolean isChannelTypeSMS= false;
	//RBAC-1562 Ends
	
	private String preferredLanguage;
	
	public String getPreferredLanguage() {
		return preferredLanguage;
	}

	public void setPreferredLanguage(String preferredLanguage) {
		this.preferredLanguage = preferredLanguage;
	
	}
	public String getTwoFactorAuthChannelType() {
		return twoFactorAuthChannelType;
	}

	public void setTwoFactorAuthChannelType(String twoFactorAuthChannelType) {
		this.twoFactorAuthChannelType = twoFactorAuthChannelType;
	}

	public Boolean getIsChannelTypeEmail() {
		return isChannelTypeEmail;
	}

	public void setIsChannelTypeEmail(Boolean isChannelTypeEmail) {
		this.isChannelTypeEmail = isChannelTypeEmail;
	}

	public Boolean getIsChannelTypeSMS() {
		return isChannelTypeSMS;
	}

	public void setIsChannelTypeSMS(Boolean isChannelTypeSMS) {
		this.isChannelTypeSMS = isChannelTypeSMS;
	}
	
    
    public String getUserLDAPDistinguishedName() {
  		return userLDAPDistinguishedName;
  	}


	public Boolean getEditable() {
		return editable;
	}


	public void setEditable(Boolean editable) {
		this.editable = editable;
	}


	public String getOrganizationName() {
		return organizationName;
	}


	public void setOrganizationName(String organizationName) {
		this.organizationName = organizationName;
	}


	public String getExternalRecordId() {
		return externalRecordId;
	}


	public void setExternalRecordId(String externalRecordId) {
		this.externalRecordId = externalRecordId;
	}


	public void setUserLDAPDistinguishedName(String userLDAPDistinguishedName) {
  		this.userLDAPDistinguishedName = userLDAPDistinguishedName;
  	}
    
    //End
    public User() {
        // empty
    }

	public Long getLoggedInTenantId() {
		return loggedInTenantId;
	}

	public void setLoggedInTenantId(Long loggedInTenantId) {
		this.loggedInTenantId = loggedInTenantId;
	}

	public User(String userName) {
        this.userName = userName;
    }

    public Integer getUserId() {
        return userId;
    }

    public void setUserId(Integer userId) {
        this.userId = userId;
    }

    public Integer getGroupId() {
        return groupId;
    }

    public void setGroupId(Integer groupId) {
        this.groupId = groupId;
    }

    public Boolean getIsEnabled() {
        return isEnabled;
    }

    public void setIsEnabled(Boolean isEnabled) {
        this.isEnabled = isEnabled;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getChangePassword() {
        return changePassword;
    }

    public void setChangePassword(String changePassword) {
        this.changePassword = changePassword;
    }

    public String getPasswordSalt() {
        return passwordSalt;
    }

    public void setPasswordSalt(String passwordSalt) {
        this.passwordSalt = passwordSalt;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public void setPasswordHash(String passwordHash) {
        this.passwordHash = passwordHash;
    }

    public Date getPasswordSetTime() {
        return passwordSetTime;
    }

    public void setPasswordSetTime(Date passwordSetTime) {
        this.passwordSetTime = passwordSetTime;
    }

    public String getEmailAddress() {
        return emailAddress;
    }

    public void setEmailAddress(String emailAddress) {
        this.emailAddress = emailAddress;
    }

    public String getPhoneNumber() {
		return phoneNumber;
	}

	public void setPhoneNumber(String phoneNumber) {
		this.phoneNumber = phoneNumber;
	}

	public String getHomeEmailAddress() {
		return homeEmailAddress;
	}

	public void setHomeEmailAddress(String homeEmailAddress) {
		this.homeEmailAddress = homeEmailAddress;
	}

	public String getHomePhoneNumber() {
		return homePhoneNumber;
	}

	public void setHomePhoneNumber(String homePhoneNumber) {
		this.homePhoneNumber = homePhoneNumber;
	}

	public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public Boolean getChangePasswordFlag() {
        return changePasswordFlag;
    }

    public void setChangePasswordFlag(Boolean changePasswordFlag) {
        this.changePasswordFlag = changePasswordFlag;
    }

    public Date getLoginTime() {
        return loginTime;
    }

    public void setLoginTime(Date loginTime) {
        this.loginTime = loginTime;
    }
    
    public Date getLastSuccessfulLoginTime() {
		return lastSuccessfulLoginTime;
	}

	public void setLastSuccessfulLoginTime(Date lastSuccessfulLoginTime) {
		this.lastSuccessfulLoginTime = lastSuccessfulLoginTime;
	}
	
    public Date getFailedLoginTime() {
        return failedLoginTime;
    }

    public void setFailedLoginTime(Date failedLoginTime) {
        this.failedLoginTime = failedLoginTime;
    }

    public Integer getConsecutiveLoginFailures() {
        return consecutiveLoginFailures;
    }

    public void setConsecutiveLoginFailures(Integer consecutiveLoginFailures) {
        this.consecutiveLoginFailures = consecutiveLoginFailures;
    }

    public Boolean getIsLocked() {
        return isLocked;
    }

    public void setIsLocked(Boolean isLocked) {
        this.isLocked = isLocked;
    }
    
    public Boolean getIsPasswordExpired() {
		return isPasswordExpired;
	}

	public void setIsPasswordExpired(Boolean isPasswordExpired) {
		this.isPasswordExpired = isPasswordExpired;
	}

	@JsonIgnore
	public String getAccountLockedReason() {
		return accountLockedReason;
	}

	public void setAccountLockedReason(String accountLockedReason) {
		this.accountLockedReason = accountLockedReason;
	}

	public Boolean getGeneratePasswordFlag() {
		return generatePasswordFlag;
	}

	public void setGeneratePasswordFlag(Boolean generatePasswordFlag) {
		this.generatePasswordFlag = generatePasswordFlag;
	}

	public Integer getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(Integer createdBy) {
        this.createdBy = createdBy;
    }

    public Date getCreatedOn() {
        return createdOn;
    }

    public void setCreatedOn(Date createdOn) {
        this.createdOn = createdOn;
    }

    public Integer getUpdatedBy() {
        return updatedBy;
    }

    public void setUpdatedBy(Integer updatedBy) {
        this.updatedBy = updatedBy;
    }

    public Date getUpdatedOn() {
        return updatedOn;
    }

    public void setUpdatedOn(Date updatedOn) {
        this.updatedOn = updatedOn;
    }

    public Calendar getUserCalendar() {
		return userCalendar;
	}

	public void setUserCalendar(Calendar userCalendar) {
		this.userCalendar = userCalendar;
	}

	public Calendar getOrgCalendar() {
		return orgCalendar;
	}

	public void setOrgCalendar(Calendar orgCalendar) {
		this.orgCalendar = orgCalendar;
	}

	public Restriction getRestrictions() {
        return restrictions;
    }

    public void setRestrictions(Restriction restrictions) {
        this.restrictions = restrictions;
    }

    public List<UserIdentity> getIdentities() {
        return identities;
    }

    public void setIdentities(List<UserIdentity> identities) {
        this.identities = identities;
    }

    public Set<String> getLabels() {
        return labels;
    }

    public void setLabels(Set<String> labels) {
        this.labels = labels;
    }
    
    
	public Long getOrganizationId() {
		return organizationId;
	}

	public void setOrganizationId(Long organizationId) {
		this.organizationId = organizationId;
	}

	public Set<AttributesData> getAttributesData() {
		if (attributesData != null && !(attributesData instanceof TreeSet)) {
			attributesData = new TreeSet<AttributesData>(attributesData);
		}
		return attributesData;
	}

    public void setAttributesData(Set<AttributesData> attributesData) {
        this.attributesData = attributesData;
    }

   /* public Set<Variable> getVariables() {
        return variables;
    }

    public void setVariables(Set<Variable> variables) {
        this.variables = variables;
    }*/

	public String getIvrPinSalt() {
		return ivrPinSalt;
	}

	public void setIvrPinSalt(String ivrPinSalt) {
		this.ivrPinSalt = ivrPinSalt;
	}

	public String getIvrPinHash() {
		return ivrPinHash;
	}

	public void setIvrPinHash(String ivrPinHash) {
		this.ivrPinHash = ivrPinHash;
	}

	public String getIvrPin() {
		return ivrPin;
	}

	public void setIvrPin(String ivrPin) {
		this.ivrPin = ivrPin;
	}

	public String getIvrUserId() {
		return ivrUserId;
	}

	public void setIvrUserId(String ivrUserId) {
		this.ivrUserId = ivrUserId;
	}	
    public boolean checkPassword(String password) {
        if (passwordSalt == null || passwordSalt.isEmpty()) {
            return false;
        }
        String hash = User.hashPassword(passwordSalt, password);
        return hash.equals(passwordHash);
    }
    
    public boolean checkIVRPin(String ivrPin, String ivrUserId) {
        if (this.ivrPinSalt == null || this.ivrPinSalt.isEmpty()) {
            return false;
        }
        
        if(this.ivrUserId == null || this.ivrUserId.isEmpty())
        	return false;
        
        String hash = User.hashPassword(this.ivrPinSalt, ivrPin);
        return hash.equals(this.ivrPinHash) && this.ivrUserId.equalsIgnoreCase(ivrUserId);
    }

    public boolean checkSHA1Password(String password) {
        if (passwordSalt!=null && passwordSalt.equals("SHA1")) {
            String sha1Hash = User.newSHA1HashPasswordWithoutName(password);
            return sha1Hash.equalsIgnoreCase(passwordHash);
        }
        return false;
    }

    public static String generateSalt(int length) {
        final char[] SALT_CHARS = "0123456789abcdefghijklmnoqrstyvwxyzABCDEFGHIJKLMNOQRSTYVWXYZ".toCharArray();

        SecureRandom random = new SecureRandom();
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < length; i++) {
            sb.append(SALT_CHARS[random.nextInt(SALT_CHARS.length)]);
        }
        return sb.toString();
    }

    public static String hashPassword(String salt, String password) {
        try {
            MessageDigest mDigest = MessageDigest.getInstance("SHA-512");
            String input = salt + password;
            byte[] result = mDigest.digest(input.getBytes("UTF-8"));
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < result.length; i++) {
                sb.append(Integer.toHexString((result[i] & 0xff) + 0x100).substring(1));
            }
            return sb.toString();

        } catch (Exception e) {
            log.error("hashPassword; exception={}", e);
        }

        return null;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("User{userId=").append(userId);
        sb.append("; userName=").append(userName);
        sb.append("; passwordSalt=").append("***********");
        sb.append("; passwordHash=").append("***********");
        sb.append("; enabled=").append(isEnabled);
        sb.append("; groupId=").append(groupId);
        sb.append("; firstName=").append(firstName);
        sb.append("; lastName=").append(lastName);
        sb.append("; emailAddress=").append(emailAddress);
        sb.append("; homeEmailAddress=").append(homeEmailAddress);
        sb.append("; phoneNumber=").append(phoneNumber);
        sb.append("; homePhoneNumber=").append(homePhoneNumber);
        sb.append("; ivrUserID=").append(ivrUserId);
        sb.append("; ivrPinSalt=").append("***********");
        sb.append("; ivrPinHash=").append("***********");
        sb.append("; changePasswordFlag=").append(changePasswordFlag);
        sb.append("; labels=").append(labels);
        sb.append("; isShared=").append(isShared);
        sb.append("; attributesData=").append(attributesData);
     /*   sb.append("; variables=").append(variables);*/
        sb.append("; restrictions=").append(restrictions);
        sb.append("; calendar=").append(userCalendar);
        sb.append("; orgCalendar=").append(orgCalendar);
        sb.append("; identities=").append(identities);
        sb.append("; organizationId=").append(organizationId);
        sb.append("; userLDAPDistinguishedName=").append(userLDAPDistinguishedName);
        sb.append("; preferredLanguage=").append(preferredLanguage);
        sb.append("; timeZone=").append(timeZone);
        sb.append("}");
        return sb.toString();
    }

    public static String newSHA1HashPassword(String name, String password) {
        try {
            MessageDigest mDigest = MessageDigest.getInstance("SHA1");
            String input = password.concat(name.toUpperCase());
            byte[] result = mDigest.digest(input.getBytes("UTF-8"));
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < result.length; i++) {
                sb.append(Integer.toHexString((result[i] & 0xff) + 0x100)
                        .substring(1));
            }
            return sb.toString();

        } catch (Exception e) {
            log.error("newSHA1HashPassword; exception={}", e);
        }

        return null;
    }
    //added to exclude name as per OB password implementation
    public static String newSHA1HashPasswordWithoutName(String password) {
        try {
            MessageDigest mDigest = MessageDigest.getInstance("SHA1");
            byte[] result = mDigest.digest(password.getBytes("UTF-8"));
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < result.length; i++) {
                sb.append(Integer.toHexString((result[i] & 0xff) + 0x100)
                        .substring(1));
            }
            return sb.toString();

        } catch (Exception e) {
            log.error("newSHA1HashPassword; exception={}", e);
        }

        return null;
    }

	public Boolean getIsShared() {
		return isShared;
	}

	public void setIsShared(Boolean isShared) {
		this.isShared = isShared;
	}

	public Boolean getIsIVRUserLocked() {
		return isIVRUserLocked;
	}

	public void setIsIVRUserLocked(Boolean isIVRUserLocked) {
		this.isIVRUserLocked = isIVRUserLocked;
	}

	public Integer getConsecutiveIVRLoginFailures() {
		return consecutiveIVRLoginFailures;
	}

	public void setConsecutiveIVRLoginFailures(
			Integer consecutiveIVRLoginFailures) {
		this.consecutiveIVRLoginFailures = consecutiveIVRLoginFailures;
	}

	

	public Long getMakerCheckerId() {
		return makerCheckerId;
	}

	public void setMakerCheckerId(Long makerCheckerId) {
		this.makerCheckerId = makerCheckerId;
	}

	public Integer getIsStatus() {
		return isStatus;
	}

	public void setIsStatus(Integer isStatus) {
		this.isStatus = isStatus;
	}


	public String getTimeZone() {
		return timeZone;
	}


	public void setTimeZone(String timeZone) {
		this.timeZone = timeZone;
	}
	public Boolean getUseEmailAsUserid() {
		return useEmailAsUserid;
	}


	public void setUseEmailAsUserid(Boolean useEmailAsUserid) {
		this.useEmailAsUserid = useEmailAsUserid;
	}
}
