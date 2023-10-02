package com.esq.rbac.service.user.domain;

import com.esq.rbac.service.attributes.domain.AttributesData;
import com.esq.rbac.service.calendar.domain.Calendar;
import com.esq.rbac.service.restriction.domain.Restriction;
import com.esq.rbac.service.user.embedded.UserIdentity;
import com.esq.rbac.service.util.DeploymentUtil;
import com.esq.rbac.service.util.SpecialCharValidator;
import com.esq.rbac.service.util.UtcDateConverter;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.annotations.Where;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.Date;
import java.util.List;
import java.util.Set;

@Slf4j
@Entity
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Where(clause = "userId > 0 and isDeleted = 0")
@Table(name = "userTable", schema = "rbac")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.TABLE, generator = "userIdGenerator")
    @TableGenerator(name = "userIdGenerator", schema = "rbac", table = "idSequence", pkColumnName = "idName", valueColumnName = "idValue", pkColumnValue = "userId", initialValue = 1, allocationSize = 10)
    @Column(name = "userId")
    private Integer userId;

    @Column(name = "groupId")
    private Integer groupId;

    @Column(name = "isEnabled")
    private Boolean isEnabled;

    @Column(name = "firstName", length = 64)
    @Size(min = 0, max = 64)
    @SpecialCharValidator
    private String firstName;


    @Size(min = 0, max = 64)
    @SpecialCharValidator
    @Column(name = "lastName", length = 64)
    private String lastName;

    @Size(max = 254)
    @SpecialCharValidator
    @Column(name = "userName", length = 32, nullable = false)
    private String userName;

    @Transient
    private String changePassword;

    @Column(name = "passwordSalt", length = 200)
    private String passwordSalt;

    @Column(name = "passwordHash", length = 200)
    private String passwordHash;

    @Column(name = "passwordSetTime")
    @Convert(converter = UtcDateConverter.class)
    private Date passwordSetTime;

    @Size(min = 0, max = 254)
    @Pattern(regexp = DeploymentUtil.EMAIL_PATTERN, message = "Invalid Email Address")
    private String emailAddress;

    @Size(min = 0, max = 254)
    @Pattern(regexp = DeploymentUtil.EMAIL_PATTERN, message = "Invalid Email Address")
    private String homeEmailAddress;

    @Pattern(regexp = DeploymentUtil.MOBILE_PATTERN, message = "Invalid Phone Number. Only Numbers and length between 7 and 15 are allowed ")
    private String phoneNumber;

    @Pattern(regexp = DeploymentUtil.MOBILE_PATTERN, message = "Invalid Phone Number. Only Numbers and length between 7 and 15 are allowed ")
    private String homePhoneNumber;

    @Column(name = "isShared")
    private Boolean isShared;

    @Size(min = 0, max = 500)
    @SpecialCharValidator
    private String notes;

    @Column(name = "changePasswordFlag")
    private Boolean changePasswordFlag;

    @Column(name = "loginTime")
    @Convert(converter = UtcDateConverter.class)
    private Date loginTime;

    @Column(name = "lastSuccessfulLoginTime")
    @Convert(converter = UtcDateConverter.class)
    private Date lastSuccessfulLoginTime;

    @Column(name = "failedLoginTime")
    @Convert(converter = UtcDateConverter.class)
    private Date failedLoginTime;

    @Column(name = "consecutiveLoginFailures")
    private Integer consecutiveLoginFailures;

    @Column(name = "isLocked")
    private Boolean isLocked;

    @NotNull
    @Column(name = "organizationId", nullable = false)
    private Long organizationId;

    @Column(name = "createdBy")
    private Integer createdBy;

    @Column(name = "createdOn")
    @Convert(converter = UtcDateConverter.class)
    private Date createdOn;

    @Column(name = "twoFactorAuthChannelType")
    private String twoFactorAuthChannelType;

    @Column(name = "updatedBy")
    private Integer updatedBy;

    @Column(name = "updatedOn")
    @Convert(converter = UtcDateConverter.class)
    private Date updatedOn;

    @Column(name = "makerCheckerId")
    private Long makerCheckerId;

    @Column(name = "isStatus")
    private Integer isStatus;

    @Column(name = "timeZone")
    private String timeZone;

    @Column(name = "preferredLanguage")
    private String preferredLanguage;

    @Column(name = "useEmailAsUserid")
    private Boolean useEmailAsUserid;

    @Column(name = "isDeleted")
    private Boolean isDeleted = false;

    private Integer consecutiveIVRLoginFailures;

    @Size(min = 0, max = 8)
    @Pattern(regexp = "^$|([0-9]{5,8})$", message = "IVR UserId must be only digits and between 5 and 8 digits")
    private String ivrUserId;

    @Transient
    @Size(min = 0, max = 6)
    @Pattern(regexp = "^$|([0-9]{6})$", message = "IVR Pin must be only digits and length must be 6 digits")
    private String ivrPin;
    private String ivrPinSalt;
    private String ivrPinHash;
    private Boolean isIVRUserLocked;


    @Transient
    private Boolean isPasswordExpired;

    @Transient
    private Boolean generatePasswordFlag;

    @Transient
    private String accountLockedReason;

    @Transient
    private String userLDAPDistinguishedName;

    @Transient
    private Long loggedInTenantId;

    @Transient
    private String organizationName;

    @Transient
    private Boolean editable;

    //End
    /* RBAC-1259 START */
    @Transient
    private String externalRecordId;

    //    @OneToMany(mappedBy = "user", orphanRemoval = true, cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JsonManagedReference("AttributesDataUser")
    private Set<AttributesData> attributesData;

    @ElementCollection(fetch = FetchType.LAZY)
    @CollectionTable(schema = "rbac", name = "label", joinColumns = @JoinColumn(name = "userId"))
    @Column(name = "labelName")
    private Set<String> labels;

    @Valid
    @OneToOne(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JoinColumn(name = "restrictionId")
    private Restriction restrictions;

    @OneToOne(cascade = CascadeType.MERGE, fetch = FetchType.LAZY)
    @JoinColumn(name = "calendarId")
    private Calendar userCalendar;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "orgCalendarId")
    private Calendar orgCalendar;

    @ElementCollection(fetch = FetchType.LAZY)
    @OrderColumn(name = "seqNum")
    @CollectionTable(schema = "rbac", name = "userIdentity", joinColumns = @JoinColumn(name = "userId"))
    private List<UserIdentity> identities;

    @Transient
    private Boolean isChannelTypeEmail = false;
    @Transient
    private Boolean isChannelTypeSMS = false;


    public User(String userName) {
        this.userName = userName;
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
            byte[] result = mDigest.digest(input.getBytes(StandardCharsets.UTF_8));
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

    public static String newSHA1HashPassword(String name, String password) {
        try {
            MessageDigest mDigest = MessageDigest.getInstance("SHA1");
            String input = password.concat(name.toUpperCase());
            byte[] result = mDigest.digest(input.getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < result.length; i++) {
                sb.append(Integer.toHexString((result[i] & 0xff) + 0x100).substring(1));
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
            byte[] result = mDigest.digest(password.getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < result.length; i++) {
                sb.append(Integer.toHexString((result[i] & 0xff) + 0x100).substring(1));
            }
            return sb.toString();

        } catch (Exception e) {
            log.error("newSHA1HashPassword; exception={}", e);
        }

        return null;
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

        if (this.ivrUserId == null || this.ivrUserId.isEmpty()) return false;

        String hash = User.hashPassword(this.ivrPinSalt, ivrPin);
        return hash.equals(this.ivrPinHash) && this.ivrUserId.equalsIgnoreCase(ivrUserId);
    }

    public boolean checkSHA1Password(String password) {
        if (passwordSalt != null && passwordSalt.equals("SHA1")) {
            String sha1Hash = User.newSHA1HashPasswordWithoutName(password);
            return sha1Hash.equalsIgnoreCase(passwordHash);
        }
        return false;
    }

    @Override
    public String toString() {
        String sb = "User{userId=" + userId +
                "; userName=" + userName +
                "; passwordSalt=" + "***********" +
                "; passwordHash=" + "***********" +
                "; enabled=" + isEnabled +
                "; groupId=" + groupId +
                "; firstName=" + firstName +
                "; lastName=" + lastName +
                "; emailAddress=" + emailAddress +
                "; homeEmailAddress=" + homeEmailAddress +
                "; phoneNumber=" + phoneNumber +
                "; homePhoneNumber=" + homePhoneNumber +
                "; ivrUserID=" + ivrUserId +
                "; ivrPinSalt=" + "***********" +
                "; ivrPinHash=" + "***********" +
                "; changePasswordFlag=" + changePasswordFlag +
                "; labels=" + labels +
                "; isShared=" + isShared +
                "; attributesData=" + attributesData +
                /*   sb.append("; variables=").append(variables);*/
                "; restrictions=" + restrictions +
                "; calendar=" + userCalendar +
                "; orgCalendar=" + orgCalendar +
                "; identities=" + identities +
                "; organizationId=" + organizationId +
                "; userLDAPDistinguishedName=" + userLDAPDistinguishedName +
                "; preferredLanguage=" + preferredLanguage +
                "; timeZone=" + timeZone +
                "}";
        return sb;
    }
}
