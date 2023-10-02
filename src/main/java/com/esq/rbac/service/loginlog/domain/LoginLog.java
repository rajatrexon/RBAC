package com.esq.rbac.service.loginlog.domain;

import com.esq.rbac.service.util.UtcDateConverter;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.apache.commons.collections4.map.CaseInsensitiveMap;

import java.util.Date;
import java.util.Map;


@AllArgsConstructor
@NoArgsConstructor
@Data
@Entity
@Table(name = "loginLog", schema = "rbac")
public class LoginLog {

    public static final String LOGIN_LOG_TABLE_LOGIN_PREFIX = "Login using ";
    public static final String LOGIN_LOG_TABLE_SWITCH_PREFIX = "Switch using ";
    public static final String LOGIN_TYPE_RBAC_LOGINLOG_ENTRY = "RBAC";
    public static final String LOGIN_TYPE_IVR_LOGINLOG_ENTRY = "IVR";
    public static final String LOGIN_TYPE_INTEGRATED_WINDOWS_LOGINLOG_ENTRY = "Integrated Windows Authentication";
    public static final String LOGIN_TYPE_SITEMINDER_LOGINLOG_ENTRY = "SiteMinder Authentication";
    public static final String LOGIN_TYPE_WINDOWS_AD_lOGINLOG_ENTRY = "Active Directory";
    public static final String LOGIN_TYPE_LDAP_lOGINLOG_ENTRY = "LDAP Authentication";
    public static final String LOGIN_TYPE_NATIVE_lOGINLOG_ENTRY = "Native Application";
    public static final String LOGIN_TYPE_AZURE_AD = "Azure AD Login";
    public static final String LOG_TYPE_LOGIN_RBAC = LOGIN_LOG_TABLE_LOGIN_PREFIX + LOGIN_TYPE_RBAC_LOGINLOG_ENTRY;
    public static final String LOG_TYPE_LOGIN_IVR = LOGIN_LOG_TABLE_LOGIN_PREFIX + LOGIN_TYPE_IVR_LOGINLOG_ENTRY;
    public static final String LOG_TYPE_LOGIN_INTEGRATED_WINDOWS = LOGIN_LOG_TABLE_LOGIN_PREFIX + LOGIN_TYPE_INTEGRATED_WINDOWS_LOGINLOG_ENTRY;
    public static final String LOG_TYPE_LOGIN_SITEMINDER = LOGIN_LOG_TABLE_LOGIN_PREFIX + LOGIN_TYPE_SITEMINDER_LOGINLOG_ENTRY;
    public static final String LOG_TYPE_LOGIN_WINDOWS_AD = LOGIN_LOG_TABLE_LOGIN_PREFIX + LOGIN_TYPE_WINDOWS_AD_lOGINLOG_ENTRY;
    public static final String LOG_TYPE_LOGIN_LDAP = LOGIN_LOG_TABLE_LOGIN_PREFIX + LOGIN_TYPE_LDAP_lOGINLOG_ENTRY;
    public static final String LOG_TYPE_LOGIN_AZURE_AD = LOGIN_LOG_TABLE_LOGIN_PREFIX + LOGIN_TYPE_AZURE_AD;
    public static final String LOG_TYPE_LOGIN_NATIVE = LOGIN_LOG_TABLE_LOGIN_PREFIX + LOGIN_TYPE_NATIVE_lOGINLOG_ENTRY;
    public static final String LOG_TYPE_LOGIN_NATIVE_LDAP = LOGIN_LOG_TABLE_LOGIN_PREFIX + LOGIN_TYPE_NATIVE_lOGINLOG_ENTRY + LOGIN_TYPE_LDAP_lOGINLOG_ENTRY;
    public static final String LOG_TYPE_CHANGE_PASSWORD_RBAC = "Change RBAC Password";
    public static final String LOG_TYPE_CHANGE_IVR_PASSWORD_RBAC = "Change RBAC IVR Password";
    public static final String LOG_TYPE_LOGOUT = "Logout";
    public static final String LOGIN_TYPE_FORGEROCK_LOGINLOG_ENTRY = "ForgeRock Authentication";
    public static final String LOG_TYPE_LOGIN_FORGEROCK = LOGIN_LOG_TABLE_LOGIN_PREFIX + LOGIN_TYPE_FORGEROCK_LOGINLOG_ENTRY;
    public static Integer LOG_BUFFER_MAX_SIZE = 4000;
    @SuppressWarnings("unchecked")
    public static Map<String, String> LOGIN_LOG_LOGOUT_MAP = new CaseInsensitiveMap();

    static {
        LOGIN_LOG_LOGOUT_MAP.put(LoginType.LOGIN_RBAC, LOGIN_TYPE_RBAC_LOGINLOG_ENTRY);
        LOGIN_LOG_LOGOUT_MAP.put(LoginType.LOGIN_SITEMINDER, LOGIN_TYPE_SITEMINDER_LOGINLOG_ENTRY);
        LOGIN_LOG_LOGOUT_MAP.put(LoginType.LOGIN_FORGEROCK, LOGIN_TYPE_FORGEROCK_LOGINLOG_ENTRY);
        LOGIN_LOG_LOGOUT_MAP.put(LoginType.LOGIN_INTEGRATED_WINDOWS, LOGIN_TYPE_INTEGRATED_WINDOWS_LOGINLOG_ENTRY);
        LOGIN_LOG_LOGOUT_MAP.put(LoginType.LOGIN_WINDOWS_AD, LOGIN_TYPE_LDAP_lOGINLOG_ENTRY);
        LOGIN_LOG_LOGOUT_MAP.put(LoginType.LOGIN_NATIVE, LOGIN_TYPE_NATIVE_lOGINLOG_ENTRY);
        LOGIN_LOG_LOGOUT_MAP.put(LoginType.LOGIN_AZURE_ACTIVE_DIRECTORY, LOGIN_TYPE_AZURE_AD);
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "loginLogId")
    private Integer loginLogId;
    @Convert(converter = UtcDateConverter.class)
    @Column(name = "createdTime", nullable = false)
    private Date createdTime;
    @Column(name = "userName")
    private String userName;
    @Column(name = "childApplicationId")
    private Integer childApplicationId;
    @Column(name = "logType")
    private String logType;
    @Column(name = "clientIp")
    private String clientIp;
    @Column(name = "serviceUrl")
    private String serviceUrl;
    @Column(name = "isAlertable", nullable = false)
    private boolean isAlertable;
    @Column(name = "isSuccess", nullable = false)
    private boolean isSuccess;
    @Column(name = "logBuffer")
    private String logBuffer;
    @Column(name = "sessionHash")
    private String sessionHash;
    @Column(name = "userId")
    private Integer userId;
    @Column(name = "appType")
    private Integer appType;
    @Transient
    private String appKey;

    public static LoginLog createLoginLog(String userName, String logType, boolean isSuccess, String clientIp, String serviceUrl, String logBuffer, String sessionHash, String appKey) {
        LoginLog loginLog = new LoginLog();
        loginLog.setCreatedTime(new Date());
        loginLog.setUserName(userName);
        loginLog.setClientIp(clientIp);
        loginLog.setServiceUrl(serviceUrl);
        loginLog.setAlertable(Boolean.FALSE);
        loginLog.setSuccess(isSuccess);
        loginLog.setLogType(logType);
        loginLog.setSessionHash(sessionHash);
        loginLog.setAppKey(appKey);
        if (logBuffer != null && logBuffer.length() > LoginLog.LOG_BUFFER_MAX_SIZE) {
            loginLog.setLogBuffer(logBuffer.substring(0, LoginLog.LOG_BUFFER_MAX_SIZE));
        } else {
            loginLog.setLogBuffer(logBuffer);
        }
        return loginLog;
    }
}

