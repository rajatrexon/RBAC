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
package com.esq.rbac.web.util;

import jakarta.servlet.http.HttpServletRequest;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.BeanCreationException;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

@Component
public class DeploymentUtil {

	//sso related
	private String baseFolderName = "a"; //web login page folder
	private String mobileBaseFolderName = "m"; //mobile login page folder
	private String maintenancePage = "maintenance.jsp"; //name of maintenance page found in rbac-web-root when application is under maintenance
	private String defaultApp = "Enterprise Monitoring"; // default application context name
	private boolean allowMultipleLogins = false; //for web sso apps, to allow multiple logins from the same user
	private String ssoNotAuthorizedPage = "notAuthorized.jsp"; // not authorized page for SSO
	private Boolean includeNonSSOAppsInSwitcher = false; // applications marked as web non sso would be included in switcher, making it true makes no sense.
	private List<String> headersToLog = Arrays.asList(new String[]{"User-Agent", "Host", "Origin", "Via", "X-Forwarded-For"}); //headers to login in database for sessions
	private static List<String> HEADERS_TO_LOG = Arrays.asList(new String[]{"User-Agent", "Host", "Origin", "Via", "X-Forwarded-For"});
	private String sessionRegistryStartAction = RBACUtil.SESSION_REGISTRY_ACTION_DESTROY; //on rbac app server startup, remove all old sessions
	private String sessionRegistryStopAction = RBACUtil.SESSION_REGISTRY_ACTION_DESTROY; //on rbac app server shutdown, remove all old sessions
	private String defaultLocaleForRestMessages = "en_US"; //locale for rest messages used in native login etc present in conf/default/restMessages folder
	private boolean logoutUserOnDeletion = true; //logout user when the user is deleted from RBAC UAM
	private boolean logoutUserOnDeactivationOrLock = true; //logout user when the user is disabled or deactivated
	private int ssoLogoutConnTimeoutMs = 10000; //RBAC-796 sometimes applications don't respond immediately to logout calls, so added connection timeout for such cases
	private int ssoLogoutSoTimeoutMs = 10000; //RBAC-796 sometimes applications don't respond immediately to logout calls, so added read timeout for such cases
	private boolean logoutViaWebLayer = true; //send logout calls to other apps via sso layer
	private String siteMinderRequestHeader = "SM-USER"; //header in which the userName coming from siteMinder is present
	private String[] webLayerLoginTypes = null; //RBAC-1360 Allow default authentication type to be defined for each SSO Web server separately
	private long loginInfoCacheExpiryInSeconds = 60L; //RBAC-1360 to enable cache for the login types on the web layer for the 300sec,
													  //after this interval, it will be reloaded from app layer(rbac.configuration table).
	private boolean refreshSSOCookie = false; //RBAC-1388, a workaround to generate new session id cookie on each login attempt.
	private boolean logoutPreviousOnSSOCookieRefresh = true; // if(refreshSSOCookie==true && logoutPreviousOnSSOCookieRefresh==true), logout all

	//security related
	//kept header name static as rbac-lib-client goes to various applications & there is no common way to inject this value till we move to variables/REST call
	private static String CLIENT_IP_HEADER = "X-Forwarded-For";
	@SuppressWarnings("unused")
	private String clientIpHeader = CLIENT_IP_HEADER; //header which contains the clientip in case of rbac sso running behind reverse proxy/ARR
	private Boolean validateServiceUrl = Boolean.TRUE; //RBAC-644 whether to validate service url before issuing a ticket
	private boolean validateNullServiceUrl = true; //RBAC-1300 stop login if serviceUrl is null
	private Integer sessionInactivityTimeoutSeconds = 1800; //default rbac integrated app session timeout in seconds
	private Boolean useOnlyTicketInServiceValidate = Boolean.TRUE; //to validate a ticket check only ticket no. and ignore app,appkey etc. //unsafe but kept false as apps dont send appkey or diff urls while validating a ticket
	private Boolean timeRestrictionIncCounter = Boolean.TRUE; //increment invalid attempt counter for time restriction error RBAC-793
	private Boolean ipRestrictionIncCounter = Boolean.TRUE; //increment invalid attempt counter for ip restriction error RBAC-793
	private String xssRegex = "["
			+ "{\"regexData\":\"<script>(.*?)</script>\", \"type\":[\"CASE_INSENSITIVE\"]}"
			+ ",{\"regexData\":\"src[\\r\\n]*=[\\r\\n]*\\\\'(.*?)\\\\'\", \"type\":[\"CASE_INSENSITIVE\", \"MULTILINE\", \"DOTALL\"]}"
			+ ",{\"regexData\":\"src[\\r\\n]*=[\\r\\n]*\\\\\\\"(.*?)\\\\\\\"\", \"type\":[\"CASE_INSENSITIVE\", \"MULTILINE\", \"DOTALL\"]}"
			+ ",{\"regexData\":\"</script>\", \"type\":[\"CASE_INSENSITIVE\"]}"
			+ ",{\"regexData\":\"<script(.*?)>\", \"type\":[\"CASE_INSENSITIVE\", \"MULTILINE\", \"DOTALL\"]}"
			+ ",{\"regexData\":\"eval\\\\((.*?)\\\\)\", \"type\":[\"CASE_INSENSITIVE\", \"MULTILINE\", \"DOTALL\"]}"
			+ ",{\"regexData\":\"expression\\\\((.*?)\\\\)\", \"type\":[\"CASE_INSENSITIVE\", \"MULTILINE\", \"DOTALL\"]}"
			+ ",{\"regexData\":\"javascript:\", \"type\":[\"CASE_INSENSITIVE\"]}"
			+ ",{\"regexData\":\"vbscript:\", \"type\":[\"CASE_INSENSITIVE\"]}"
			+ ",{\"regexData\":\"onload(.*?)=\", \"type\":[\"CASE_INSENSITIVE\", \"MULTILINE\", \"DOTALL\"]}"
			+ ",{\"regexData\":\"onerror(.*?)=\", \"type\":[\"CASE_INSENSITIVE\", \"MULTILINE\", \"DOTALL\"]}"
			+ ",{\"regexData\":\"alert\\\\((.*?)\\\\)\", \"type\":[\"CASE_INSENSITIVE\", \"MULTILINE\", \"DOTALL\"]}"
			+ ",{\"regexData\":\"prompt\\\\((.*?)\\\\)\", \"type\":[\"CASE_INSENSITIVE\", \"MULTILINE\", \"DOTALL\"]}"
			+ ",{\"regexData\":\"confirm\\\\((.*?)\\\\)\", \"type\":[\"CASE_INSENSITIVE\", \"MULTILINE\", \"DOTALL\"]}"
			+ ",{\"regexData\":\"document\\\\.\", \"type\":[\"CASE_INSENSITIVE\", \"MULTILINE\", \"DOTALL\"]}"
			+ ",{\"regexData\":\"<(.*?)\", \"type\":[\"CASE_INSENSITIVE\"]}"
			+ ",{\"regexData\":\"<(.*\\\\\\\\S.*)>\", \"type\":[\"CASE_INSENSITIVE\", \"MULTILINE\", \"DOTALL\"]}"
			+ "]";// regex used for XSS validation

	private boolean disableXSS = false; // disable XSS validation in XSSFilter & XSSSafeStringDeserializer
	private boolean xssActionError = true; // if set to false, it only cleanses XSS, doesn't stop the request, need to test
	private boolean sendTicketInPost = true; // RBAC-931, send ticket in POST call, added config to avoid upgrade of existing applications
	private boolean ignoreAppKeyInRedirectValidation = true; //RBAC-644, if true appKey is not considered for url validation,
	 														// works only when validateServiceUrl is true
	private boolean logoutRedirectParamInSession = true; //RBAC-644, used by logoutRedirect.jsp to store logout redirect urls in session,
														//may not work for load balancers
	private String overrideLanguageCode;
	//password change related
	private boolean expireSessionOnPasswordChange = true; //when user changes password, expire the logged in session
	private String changePasswordUrl; //change password sso url
	private Integer changePasswordWithoutSessionTimeoutSeconds = 60; //RBAC-748, temporary cookie timeout when changing password without session, password expired, must channge password
	private Boolean useHomeUrlInMustChangePassword = Boolean.FALSE; //RBAC-710, keep true for case when RBAC-710 like deployment is there

	//app dashboard related
	private Boolean showAppDashboardInSwitcher = Boolean.FALSE; //show app dashboard in switcher
	private Integer appDashboardApplicationId = 50; //app dashboard applicationId, it should not change if scripts are proper
	private Boolean showAppDashboardToAllUsers = Boolean.TRUE; //dont check permissions for appDashboard, open to all users

	//reports related
	private int auditLogReportPDFMaxRecords = 1000; //max records to fetch for Auditlog pdf report export
	private int scopeDetailsReportPDFMaxRecords = 1000; //max records to fetch for Scope details pdf report export
	private int reportPDFMaxRecords = 10000; //max records to fetch for other pdf report export
	private int pdfReportMaxCellLength = 5000; //max length of data for a cell in a pdf table
	private String pdfReportCellTruncationMessage = "... *truncated"; //text appended to truncated cell
	private int reportRestReadTimeout = 250000; //rest client timeout for csv/pdf export
	private String tempReportFolder = "D:\\RBAC\\RBAC Reports\\"; //report generation pdf temporary folder
	private int reportResponseWriteSizeBytes = 1024; // stream read buffer size for reports
	private int auditLogreportIterationDataSize = 100; // no. of records to read from database for reports
	private int scopeDetailsreportIterationDataSize = 100; // no. of records to read from database for reports
	private int reportIterationDataSize = 1000; // no. of records to read from database for reports
	private int reportSemaphoreSize = 5; // no. of concurrent requests to prevent DOS attacks for reports
	private int reportSemaphoreTimeoutSecs = 15; // no. of seconds to wait for a lock on semaphore for reports
	private int pdfReportXsltLockCount = 3; // no. of parallel xslt's allowed
	private int globalUserSearchreportIterationDataSize = 100; // no. of records to read from database for reports
	private int globalUserSearchReportPDFMaxRecords = 1000; //max records to fetch for globalUserSearch pdf report export
	//scopes related
	private String inListScopedApplications = null; // comma separated list of applications on which INLIST types of scopes will be shown with filtered(scoped) data. e.g. OB, IMS
	private List<String> inListScopedApplicationsList = new ArrayList<String>();

	//application url related
	private String regexForUrlAuhtorityValidation = "^([a-zA-Z0-9]+(?:(?:\\.|\\-)[a-zA-Z0-9]+)+(?:\\:\\d+)?(?:\\/[\\w\\-]+)*(?:\\/?|\\/\\w+\\.[a-zA-Z]{2,4}(?:\\?[\\w]+\\=[\\w\\-]+)?)?(?:\\&[\\w]+\\=[\\w\\-]+)*)$"; //RBAC-746 url validator regex for application screen

	//user image related
	private boolean validateUserImage = true;

	//contacts related
	private String queryForDispatch = " select u from User u left join Variable v on (v.userId = u.userId and v.variableName = :variableName) ";
	private String sortOrderForDispatch = " CASE WHEN v.variableValue is NULL THEN 99999 ELSE CAST(v.variableValue AS INT) END  ";
	private String variableForUserIVRDispatchSeq = "IVRCallSequence";
	private Pattern userMatchingRegexForDispatch = Pattern.compile("(?i)%Contact_[A-O]%");
	private String percentageChannelVariable = "%CHANNEL%";
	private String percentageChannelVariableSuffix = "_CHANNEL";
	/* Use this property to show details of email in case of %branch%
	*  If true is selected then it will show only the name of the branch irrespective of the channel
	*  if false it will show the contacts according to the channel selected */
	private boolean showBranchNameforPercentageBranch= true;
	private String forgeRockRequestHeader = "SM_USER"; //header in which the userName coming from ForgeRock is present

	private boolean enableAuditInfoInAuditLog = false; //RBAC-1450	Audit info is needed when user access RBAC to get audit reports for Credibanco
	private boolean useMakerCheckerForDelete=false;
	//calendar related
	//timezone offset included for com.esq.rbac.rest.app.CalendarRest.getOrganizationCalendars(UriInfo)
	private boolean includeTZOForOrgCal = false;

	private boolean enableDistributionGroup = false; //RBAC-1680

	//REGEX For SERVER SIDE VALIDATION
	public static final String EMAIL_PATTERN = "^$|([0-9a-zA-Z\".%+-]([\"-.\\w]*[0-9a-zA-Z\".%+-])*@(([0-9a-zA-Z\".%+-]([\"-.\\w]*[0-9a-zA-Z\".%+-]))|[0-9a-zA-Z\".%+-]))$";
	public static final String DATE_PATTERN = "^(\\d{4})-(\\d{2})-(\\d{2})$";
	public static final String NUMBER_PATTERN = "^([0-9]*)$";
	public static final String DOW_PATTERN = "^([0-9\\-]*)$";
	public static final String MOBILE_PATTERN = "^$|(([+])?[0-9]{7,15})$";
	public static final String MOBILE_PATTERN_COUNTRY_CODE = "\\+[1-9]{1}[0-9]{7,15}$";
	public static final String URL_PATTERN = "^((http|https)\\:\\/{2})(\\w*.*)$";
	private Boolean isMakercheckerActivated = Boolean.FALSE;


	//RBAC-2077 Start
	private String extChangePswdRedirectUrl = null;
	private String extLogoutRedirectUrl = null;
	private boolean extLogoutRedirectFlag = false;
	//RBAC-2077 End
	private Boolean useHeaderAuthPostLogin= false; //RBAC-2129


	//RBAC-1859
	private boolean validateApplicationTagWithLoggedInTag=true;

	private boolean	enableForgeRockIntegration=false; //RBAC-2263
	private boolean enableLamIntegration=false; //RBAC-2262

	private Boolean passwordPolicyVisibility = Boolean.TRUE;

	private boolean userSoftDelete = true;

	private Integer audioCaptchaTimeoutInSeconds = 15;

	private boolean enableStandardPasswordHashing = false;

	private boolean assertPasswords = false;

	public boolean isAssertPasswords() {
		return assertPasswords;
	}

	public void setAssertPasswords(boolean assertPasswords) {
		this.assertPasswords = assertPasswords;
	}

	public boolean isEnableStandardPasswordHashing() {
		return enableStandardPasswordHashing;
	}
	public void setEnableStandardPasswordHashing(boolean enableStandardPasswordHashing) {
		this.enableStandardPasswordHashing = enableStandardPasswordHashing;
	}

	public boolean isUserSoftDelete() {
		return userSoftDelete;
	}

	public void setUserSoftDelete(boolean userSoftDelete) {
		this.userSoftDelete = userSoftDelete;
	}

	public Boolean getPasswordPolicyVisibility() {
		return passwordPolicyVisibility;
	}
	public void setPasswordPolicyVisibility(Boolean passwordPolicyVisibility) {
		this.passwordPolicyVisibility = passwordPolicyVisibility;
	}
	public boolean isEnableForgeRockIntegration() {
		return enableForgeRockIntegration;
	}
	public void setEnableForgeRockIntegration(boolean enableForgeRockIntegration) {
		this.enableForgeRockIntegration = enableForgeRockIntegration;
	}
	public boolean isEnableLamIntegration() {
		return enableLamIntegration;
	}
	public void setEnableLamIntegration(boolean enableLamIntegration) {
		this.enableLamIntegration = enableLamIntegration;
	}

	private String clientNameForGenericLogin=null;
    private String clientRequestHeaderForGenericLogin=null;

	public String getClientNameForGenericLogin() {
		return clientNameForGenericLogin;
	}
	public void setClientNameForGenericLogin(String clientNameForGenericLogin) {
		this.clientNameForGenericLogin = clientNameForGenericLogin;
	}
	public String getClientRequestHeaderForGenericLogin() {
		return clientRequestHeaderForGenericLogin;
	}
	public void setClientRequestHeaderForGenericLogin(String clientRequestHeaderForGenericLogin) {
		this.clientRequestHeaderForGenericLogin = clientRequestHeaderForGenericLogin;
	}
	public boolean isValidateApplicationTagWithLoggedInTag() {
		return validateApplicationTagWithLoggedInTag;
	}
	public void setValidateApplicationTagWithLoggedInTag(boolean validateApplicationTagWithLoggedInTag) {
		this.validateApplicationTagWithLoggedInTag = validateApplicationTagWithLoggedInTag;
	}


	public Boolean useHeaderAuthPostLogin() {
		return useHeaderAuthPostLogin;
	}
	public void setUseHeaderAuthPostLogin(Boolean useHeaderAuthPostLogin) {
		this.useHeaderAuthPostLogin = useHeaderAuthPostLogin;
	}
	public boolean isExtLogoutRedirectFlag() {
		return extLogoutRedirectFlag;
	}
	public void setExtLogoutRedirectFlag(boolean extLogoutRedirectFlag) {
		this.extLogoutRedirectFlag = extLogoutRedirectFlag;
	}
	public String getExtLogoutRedirectUrl() {
		return extLogoutRedirectUrl;
	}
	public void setExtLogoutRedirectUrl(String extLogoutRedirectUrl) {
		this.extLogoutRedirectUrl = extLogoutRedirectUrl;
	}
	public String getExtChangePswdRedirectUrl() {
		return extChangePswdRedirectUrl;
	}
	public void setExtChangePswdRedirectUrl(String extChangePswdRedirectUrl) {
		this.extChangePswdRedirectUrl = extChangePswdRedirectUrl;
	}

	//RBAC-1747 Start
	private Integer enableCaptchaCount = -1;
	private Integer captchaTimeoutInMinutes = 2;
	//RBAC-1747 End
	private String specialCharacterRegex = "<>=%&#!;'";

	// RBAC-1562 Starts
	private Boolean enableTwoFactorAuth = false;
	private String enableTwoFactorForLoginTypes="";
	// RBAC-1562 Ends
	private Integer userNameMinLength=6; // RBAC-1902

	// satrt RBAC-1916
	private String csvExportDateTimeFormat = "dd/MM/yyyy HH:mm:ss";

	private boolean uniqueEmailId = false;

	private Integer nodeLimitForOrgGridView = 1000; // RBAC-1656
	private Integer batchSizeForGridData = 100; // RBAC-1656

	public boolean isUniqueEmailId() {
		return uniqueEmailId;
	}


	public void setUniqueEmailId(boolean uniqueEmailId) {
		this.uniqueEmailId = uniqueEmailId;
	}

	public String getForgeRockRequestHeader() {
		return forgeRockRequestHeader;
	}

	public void setForgeRockRequestHeader(String forgeRockRequestHeader) {
		this.forgeRockRequestHeader = forgeRockRequestHeader;
	}
	public Integer getBatchSizeForGridData() {
		return batchSizeForGridData;
	}


	public void setBatchSizeForGridData(Integer batchSizeForGridData) {
		this.batchSizeForGridData = batchSizeForGridData;
	}


	public Integer getNodeLimitForOrgGridView() {
		return nodeLimitForOrgGridView;
	}


	public void setNodeLimitForOrgGridView(Integer nodeLimitForOrgGridView) {
		this.nodeLimitForOrgGridView = nodeLimitForOrgGridView;
	}


	public String getCsvExportDateTimeFormat() {
		return csvExportDateTimeFormat;
	}

	public void setCsvExportDateTimeFormat(String csvExportDateTimeFormat) {
		this.csvExportDateTimeFormat = csvExportDateTimeFormat;
	}
	// end RBAC-1916


	public Integer getUserNameMinLength() {
		return userNameMinLength;
	}
	public void setUserNameMinLength(Integer userNameMinLength) {
		this.userNameMinLength = userNameMinLength;
	}


	public Boolean isEnableTwoFactorAuth() {
		return enableTwoFactorAuth;
	}

	public String getEnableTwoFactorForLoginTypes() {
		return enableTwoFactorForLoginTypes;
	}

	public void setEnableTwoFactorForLoginTypes(String enableTwoFactorForLoginTypes) {
		this.enableTwoFactorForLoginTypes = enableTwoFactorForLoginTypes;
	}

	public void setEnableTwoFactorAuth(Boolean enableTwoFactorAuth) {
		this.enableTwoFactorAuth = enableTwoFactorAuth;
	}

	//Azure Active Directory properties from init.properties
	private Boolean azureUserMgmtEnabled = Boolean.FALSE;
	private String azureTenantId = "fbccc6bd-ad1b-4f53-8f0f-f1aa2afd999";
	private String azureSsoServiceUrl = "https://login.microsoftonline.com/"+azureTenantId;
	private String endSessionEndpoint="https://login.microsoftonline.com/common/oauth2/v2.0/logout";
	private String oauth2ClientId = "9e02e7de-15d8-4792-a90d-393ae1f011ea";
	private String oauth2ClientSecret="3gmi.ZY8jy5ZU/_z8nrXibmM_1Pj6XV0";
	private String azureRedirectUri;
	private String azureRedirectApplicationKey = "RBAC";
	private String azureScopeApi;
	private String usernameValidationRegex;
	private String usernameValidationCustomMessage;
	private boolean createUsernameAsEmailId=false;
	private String azureUserIdentityIssuer;
	private String usernameIgnoreAzureUserMgmtRegex;

	public String getAzureRedirectApplicationKey() {
		return azureRedirectApplicationKey;
	}

	public void setAzureRedirectApplicationKey(String azureRedirectApplicationKey) {
		this.azureRedirectApplicationKey = azureRedirectApplicationKey;
	}

	public String getAzureUserIdentityIssuer() {
		return azureUserIdentityIssuer;
	}

	public void setAzureUserIdentityIssuer(String azureUserIdentityIssuer) {
		this.azureUserIdentityIssuer = azureUserIdentityIssuer;
	}

	public String getUsernameIgnoreAzureUserMgmtRegex() {
		return usernameIgnoreAzureUserMgmtRegex;
	}

	public void setUsernameIgnoreAzureUserMgmtRegex(String usernameIgnoreAzureUserMgmtRegex) {
		this.usernameIgnoreAzureUserMgmtRegex = usernameIgnoreAzureUserMgmtRegex;
	}

	public Boolean isAzureUserMgmtEnabled() {
		return azureUserMgmtEnabled;
	}
	public void setAzureUserMgmtEnabled(Boolean azureUserMgmtEnabled) {
		this.azureUserMgmtEnabled = azureUserMgmtEnabled;
	}
	public String getUsernameValidationRegex() {
		return usernameValidationRegex;
	}
	public void setUsernameValidationRegex(String usernameValidationRegex) {
		this.usernameValidationRegex = usernameValidationRegex;
	}
	public String getUsernameValidationCustomMessage() {
		return usernameValidationCustomMessage;
	}
	public void setUsernameValidationCustomMessage(String usernameValidationCustomMessage) {
		this.usernameValidationCustomMessage = usernameValidationCustomMessage;
	}

	private String groupScopePermission;
	public String getGroupScopePermission() {
		return groupScopePermission;
	}
	public void setGroupScopePermission(String groupScopePermission) {
		this.groupScopePermission = groupScopePermission;
	}
	public Boolean getIsMakercheckerActivated() {
		return isMakercheckerActivated;
	}
    public void setIsMakercheckerActivated(Boolean isMakercheckerActivated) {
		this.isMakercheckerActivated = isMakercheckerActivated;
	}


	public String getAzureScopeApi() {
		return azureScopeApi;
	}
	public void setAzureScopeApi(String azureScopeApi) {
		this.azureScopeApi = azureScopeApi;
	}
	public String getDeviceBaseFolderName(HttpServletRequest request){
//        Device device = DeviceUtils.getCurrentDevice(request);
//		if (device==null || (device!=null && device.isNormal()) ) {
			return getBaseFolderName();
//        } else {
//        	return getMobileBaseFolderName();
//        }
	}

	private String replaceUrlByApplication;//LTWOSUPP-2214



	public Integer getCaptchaTimeoutInMinutes() {
		return captchaTimeoutInMinutes;
	}
	public void setCaptchaTimeoutInMinutes(Integer captchaTimeoutInMinutes) {
		this.captchaTimeoutInMinutes = captchaTimeoutInMinutes;
	}
	public Integer getEnableCaptchaCount() {
		return enableCaptchaCount;
	}
	public void setEnableCaptchaCount(Integer enableCaptchaCount) {
		this.enableCaptchaCount = enableCaptchaCount;
	}
	public String getReplaceUrlByApplication() {
		return replaceUrlByApplication;
	}

	public void setReplaceUrlByApplication(String replaceUrlByApplication) {
		this.replaceUrlByApplication = replaceUrlByApplication;
	}

	public String getOverrideLanguageCode() {
		return overrideLanguageCode;
	}


	public void setOverrideLanguageCode(String overrideLanguageCode) {
		this.overrideLanguageCode = overrideLanguageCode;
	}




	public String getSpecialCharacterRegex() {
		return specialCharacterRegex;
	}

	public void setSpecialCharacterRegex(String specialCharacterRegex) {
		this.specialCharacterRegex = specialCharacterRegex;
	}
	public boolean isEnableAuditInfoInAuditLog() {
		return enableAuditInfoInAuditLog;
	}


	public boolean getEnableDistributionGroup() {
		return enableDistributionGroup;
	}
	public void setEnableDistributionGroup(boolean enableDistributionGroup) {
		this.enableDistributionGroup = enableDistributionGroup;
	}
	public void setEnableAuditInfoInAuditLog(boolean enableAuditInfoInAuditLog) {
		this.enableAuditInfoInAuditLog = enableAuditInfoInAuditLog;
	}

	public boolean isShowBranchNameforPercentageBranch() {
		return showBranchNameforPercentageBranch;
	}

	public void setShowBranchNameforPercentageBranch(boolean showBranchNameforPercentageBranch) {
		this.showBranchNameforPercentageBranch = showBranchNameforPercentageBranch;
	}


	public String getBaseFolderName() {
		return baseFolderName;
	}

	public void setBaseFolderName(String baseFolderName) {
		this.baseFolderName = baseFolderName;
	}

	public boolean isExpireSessionOnPasswordChange() {
		return expireSessionOnPasswordChange;
	}

	public void setExpireSessionOnPasswordChange(
			boolean expireSessionOnPasswordChange) {
		this.expireSessionOnPasswordChange = expireSessionOnPasswordChange;
	}

	public String getMaintenancePage() {
		return maintenancePage;
	}

	public void setMaintenancePage(String maintenancePage) {
		this.maintenancePage = maintenancePage;
	}

	public String getDefaultApp() {
		return defaultApp;
	}

	public void setDefaultApp(String defaultApp) {
		this.defaultApp = defaultApp;
	}

	public void setMobileBaseFolderName(String mobileBaseFolderName) {
		this.mobileBaseFolderName = mobileBaseFolderName;
	}

	public String getMobileBaseFolderName() {
		return mobileBaseFolderName;
	}

	public boolean isAllowMultipleLogins() {
		return allowMultipleLogins;
	}

	public void setAllowMultipleLogins(boolean allowMultipleLogins) {
		this.allowMultipleLogins = allowMultipleLogins;
	}

	public String getSsoNotAuthorizedPage() {
		return ssoNotAuthorizedPage;
	}

	public void setSsoNotAuthorizedPage(String ssoNotAuthorizedPage) {
		this.ssoNotAuthorizedPage = ssoNotAuthorizedPage;
	}

	public int getReportPDFMaxRecords() {
		return reportPDFMaxRecords;
	}

	public void setReportPDFMaxRecords(int reportPDFMaxRecords) {
		this.reportPDFMaxRecords = reportPDFMaxRecords;
	}

	public int getScopeDetailsReportPDFMaxRecords() {
		return scopeDetailsReportPDFMaxRecords;
	}

	public void setScopeDetailsReportPDFMaxRecords(
			int scopeDetailsReportPDFMaxRecords) {
		this.scopeDetailsReportPDFMaxRecords = scopeDetailsReportPDFMaxRecords;
	}

	public int getAuditLogReportPDFMaxRecords() {
		return auditLogReportPDFMaxRecords;
	}

	public void setAuditLogReportPDFMaxRecords(int auditLogReportPDFMaxRecords) {
		this.auditLogReportPDFMaxRecords = auditLogReportPDFMaxRecords;
	}

	public int getReportRestReadTimeout() {
		return reportRestReadTimeout;
	}

	public void setReportRestReadTimeout(int reportRestReadTimeout) {
		this.reportRestReadTimeout = reportRestReadTimeout;
	}

	public String getTempReportFolder() {
		return tempReportFolder;
	}

	public void setTempReportFolder(String tempReportFolder) {
		this.tempReportFolder = tempReportFolder;
	}

	public void setClientIpHeader(String clientIpHeader) {
		if(clientIpHeader!=null && !clientIpHeader.isEmpty() && clientIpHeader.length() > 0){
			this.clientIpHeader = clientIpHeader;
			CLIENT_IP_HEADER = clientIpHeader;
		}
	}

	public static String getCLIENT_IP_HEADER(){
		return CLIENT_IP_HEADER;
	}

	public String getInListScopedApplications() {
		return inListScopedApplications;
	}

	public void setInListScopedApplications(String inListScopedApplications) {
		inListScopedApplicationsList.clear();
		if(inListScopedApplications!=null){
			inListScopedApplications = inListScopedApplications.trim();
			if(!inListScopedApplications.isEmpty() && inListScopedApplications.length() > 1){
				if(inListScopedApplications.contains(",")){
					for(String app:inListScopedApplications.split(",")){
						inListScopedApplicationsList.add(app.trim().toLowerCase());
					}
				}
				else{
					inListScopedApplicationsList.add(inListScopedApplications.trim().toLowerCase());
				}
			}
		}
	}

	public List<String> getInListScopedApplicationsList(){
		return inListScopedApplicationsList;
	}

	public void setHeadersToLog(String hideHeadersForSessionLogString) {
		headersToLog = new ArrayList<String>();
		if(hideHeadersForSessionLogString!=null){
			hideHeadersForSessionLogString = hideHeadersForSessionLogString.trim();
			if(!hideHeadersForSessionLogString.isEmpty() && hideHeadersForSessionLogString.length() > 1){
				if(hideHeadersForSessionLogString.contains("*")){
					headersToLog = null;
				}
				else if(hideHeadersForSessionLogString.contains(",")){
					for(String header:hideHeadersForSessionLogString.split(",")){
						headersToLog.add(header.trim().toLowerCase());
					}
				}
				else{
					headersToLog.add(hideHeadersForSessionLogString.trim().toLowerCase());
				}
			}
		}
		HEADERS_TO_LOG = headersToLog;
	}

	public Boolean getValidateServiceUrl() {
		return validateServiceUrl;
	}

	public void setValidateServiceUrl(Boolean validateServiceUrl) {
		this.validateServiceUrl = validateServiceUrl;
	}

	public Boolean getIncludeNonSSOAppsInSwitcher() {
		return includeNonSSOAppsInSwitcher;
	}

	public void setIncludeNonSSOAppsInSwitcher(Boolean includeNonSSOAppsInSwitcher) {
		this.includeNonSSOAppsInSwitcher = includeNonSSOAppsInSwitcher;
	}

	public static List<String> getHEADERS_TO_LOG() {
		return HEADERS_TO_LOG;
	}

	public String getSessionRegistryStartAction() {
		return sessionRegistryStartAction;
	}

	public void setSessionRegistryStartAction(String sessionRegistryStartAction) {
		this.sessionRegistryStartAction = sessionRegistryStartAction;
	}

	public String getSessionRegistryStopAction() {
		return sessionRegistryStopAction;
	}

	public void setSessionRegistryStopAction(String sessionRegistryStopAction) {
		this.sessionRegistryStopAction = sessionRegistryStopAction;
	}

	public String getDefaultLocaleForRestMessages() {
		return defaultLocaleForRestMessages;
	}

	public void setDefaultLocaleForRestMessages(String defaultLocaleForRestMessages) {
		this.defaultLocaleForRestMessages = defaultLocaleForRestMessages;
	}

	public Integer getSessionInactivityTimeoutSeconds() {
		return sessionInactivityTimeoutSeconds;
	}

	public void setSessionInactivityTimeoutSeconds(
			Integer sessionInactivityTimeoutSeconds) {
		this.sessionInactivityTimeoutSeconds = sessionInactivityTimeoutSeconds;
	}

	public String getChangePasswordUrl() {
		return changePasswordUrl;
	}

	public void setChangePasswordUrl(String changePasswordUrl) {
		this.changePasswordUrl = changePasswordUrl;
	}

	public Boolean getUseOnlyTicketInServiceValidate() {
		return useOnlyTicketInServiceValidate;
	}

	public void setUseOnlyTicketInServiceValidate(
			Boolean useOnlyTicketInServiceValidate) {
		this.useOnlyTicketInServiceValidate = useOnlyTicketInServiceValidate;
	}

	public Integer getChangePasswordWithoutSessionTimeoutSeconds() {
		return changePasswordWithoutSessionTimeoutSeconds;
	}

	public void setChangePasswordWithoutSessionTimeoutSeconds(
			Integer changePasswordWithoutSessionTimeoutSeconds) {
		this.changePasswordWithoutSessionTimeoutSeconds = changePasswordWithoutSessionTimeoutSeconds;
	}

	public Boolean getUseHomeUrlInMustChangePassword() {
		return useHomeUrlInMustChangePassword;
	}

	public void setUseHomeUrlInMustChangePassword(
			Boolean useHomeUrlInMustChangePassword) {
		this.useHomeUrlInMustChangePassword = useHomeUrlInMustChangePassword;
	}

	public Boolean getShowAppDashboardInSwitcher() {
		return showAppDashboardInSwitcher;
	}

	public void setShowAppDashboardInSwitcher(Boolean showAppDashboardInSwitcher) {
		this.showAppDashboardInSwitcher = showAppDashboardInSwitcher;
	}

	public Boolean getShowAppDashboardToAllUsers() {
		return showAppDashboardToAllUsers;
	}

	public void setShowAppDashboardToAllUsers(Boolean showAppDashboardToAllUsers) {
		this.showAppDashboardToAllUsers = showAppDashboardToAllUsers;
	}

	public Integer getAppDashboardApplicationId() {
		return appDashboardApplicationId;
	}

	public void setAppDashboardApplicationId(Integer appDashboardApplicationId) {
		this.appDashboardApplicationId = appDashboardApplicationId;
	}

	public String getRegexForUrlAuhtorityValidation() {
		return regexForUrlAuhtorityValidation;
	}

	public void setRegexForUrlAuhtorityValidation(
			String regexForUrlAuhtorityValidation) {
		this.regexForUrlAuhtorityValidation = regexForUrlAuhtorityValidation;
	}

	public boolean isLogoutUserOnDeletion() {
		return logoutUserOnDeletion;
	}

	public void setLogoutUserOnDeletion(boolean logoutUserOnDeletion) {
		this.logoutUserOnDeletion = logoutUserOnDeletion;
	}

	public boolean isLogoutUserOnDeactivationOrLock() {
		return logoutUserOnDeactivationOrLock;
	}

	public void setLogoutUserOnDeactivationOrLock(boolean logoutUserOnDeactivationOrLock) {
		this.logoutUserOnDeactivationOrLock = logoutUserOnDeactivationOrLock;
	}

	public int getSsoLogoutConnTimeoutMs() {
		return ssoLogoutConnTimeoutMs;
	}

	public void setSsoLogoutConnTimeoutMs(int ssoLogoutConnTimeoutMs) {
		this.ssoLogoutConnTimeoutMs = ssoLogoutConnTimeoutMs;
	}

	public int getSsoLogoutSoTimeoutMs() {
		return ssoLogoutSoTimeoutMs;
	}

	public void setSsoLogoutSoTimeoutMs(int ssoLogoutSoTimeoutMs) {
		this.ssoLogoutSoTimeoutMs = ssoLogoutSoTimeoutMs;
	}

	public boolean isLogoutViaWebLayer() {
		return logoutViaWebLayer;
	}

	public void setLogoutViaWebLayer(boolean logoutViaWebLayer) {
		this.logoutViaWebLayer = logoutViaWebLayer;
	}

	public int getReportResponseWriteSizeBytes() {
		return reportResponseWriteSizeBytes;
	}

	public void setReportResponseWriteSizeBytes(int reportResponseWriteSizeBytes) {
		this.reportResponseWriteSizeBytes = reportResponseWriteSizeBytes;
	}

	public int getReportIterationDataSize() {
		return reportIterationDataSize;
	}

	public void setReportIterationDataSize(int reportIterationDataSize) {
		this.reportIterationDataSize = reportIterationDataSize;
	}

	public int getReportSemaphoreSize() {
		return reportSemaphoreSize;
	}

	public void setReportSemaphoreSize(int reportSemaphoreSize) {
		this.reportSemaphoreSize = reportSemaphoreSize;
	}

	public int getReportSemaphoreTimeoutSecs() {
		return reportSemaphoreTimeoutSecs;
	}

	public void setReportSemaphoreTimeoutSecs(int reportSemaphoreTimeoutSecs) {
		this.reportSemaphoreTimeoutSecs = reportSemaphoreTimeoutSecs;
	}

	public Boolean getTimeRestrictionIncCounter() {
		return timeRestrictionIncCounter;
	}

	public void setTimeRestrictionIncCounter(Boolean timeRestrictionIncCounter) {
		this.timeRestrictionIncCounter = timeRestrictionIncCounter;
	}

	public Boolean getIpRestrictionIncCounter() {
		return ipRestrictionIncCounter;
	}

	public void setIpRestrictionIncCounter(Boolean ipRestrictionIncCounter) {
		this.ipRestrictionIncCounter = ipRestrictionIncCounter;
	}

	public int getPdfReportMaxCellLength() {
		return pdfReportMaxCellLength;
	}

	public void setPdfReportMaxCellLength(int pdfReportMaxCellLength) {
		this.pdfReportMaxCellLength = pdfReportMaxCellLength;
	}

	public String getPdfReportCellTruncationMessage() {
		return pdfReportCellTruncationMessage;
	}

	public void setPdfReportCellTruncationMessage(
			String pdfReportCellTruncationMessage) {
		this.pdfReportCellTruncationMessage = pdfReportCellTruncationMessage;
	}

	public int getAuditLogreportIterationDataSize() {
		return auditLogreportIterationDataSize;
	}

	public void setAuditLogreportIterationDataSize(
			int auditLogreportIterationDataSize) {
		this.auditLogreportIterationDataSize = auditLogreportIterationDataSize;
	}

	public int getScopeDetailsreportIterationDataSize() {
		return scopeDetailsreportIterationDataSize;
	}

	public void setScopeDetailsreportIterationDataSize(
			int scopeDetailsreportIterationDataSize) {
		this.scopeDetailsreportIterationDataSize = scopeDetailsreportIterationDataSize;
	}

	public int getPdfReportXsltLockCount() {
		return pdfReportXsltLockCount;
	}

	public void setPdfReportXsltLockCount(int pdfReportXsltLockCount) {
		this.pdfReportXsltLockCount = pdfReportXsltLockCount;
	}

	public String getSiteMinderRequestHeader() {
		return siteMinderRequestHeader;
	}

	public void setSiteMinderRequestHeader(String siteMinderRequestHeader) {
		this.siteMinderRequestHeader = siteMinderRequestHeader;
	}

	public String getXssRegex() {
		return xssRegex;
	}

	public void setXssRegex(String xssRegex) {
		this.xssRegex = xssRegex;
	}

	public boolean isDisableXSS() {
		return disableXSS;
	}

	public void setDisableXSS(boolean disableXSS) {
		this.disableXSS = disableXSS;
	}

	public boolean isXssActionError() {
		return xssActionError;
	}

	public void setXssActionError(boolean xssActionError) {
		this.xssActionError = xssActionError;
	}

	public boolean isValidateUserImage() {
		return validateUserImage;
	}

	public void setValidateUserImage(boolean validateUserImage) {
		this.validateUserImage = validateUserImage;
	}

	public String getQueryForDispatch() {
		return queryForDispatch;
	}

	public void setQueryForDispatch(String queryForDispatch) {
		this.queryForDispatch = queryForDispatch;
	}

	public String getSortOrderForDispatch() {
		return sortOrderForDispatch;
	}

	public void setSortOrderForDispatch(String sortOrderForDispatch) {
		this.sortOrderForDispatch = sortOrderForDispatch;
	}

	public String getVariableForUserIVRDispatchSeq() {
		return variableForUserIVRDispatchSeq;
	}

	public void setVariableForUserIVRDispatchSeq(String variableForUserIVRDispatchSeq) {
		this.variableForUserIVRDispatchSeq = variableForUserIVRDispatchSeq;
	}

	public Pattern getUserMatchingRegexForDispatch() {
		return userMatchingRegexForDispatch;
	}

	public void setUserMatchingRegexForDispatch(String userMatchingRegexForDispatch) {
		try{
			this.userMatchingRegexForDispatch = Pattern.compile(userMatchingRegexForDispatch);
		}
		catch(PatternSyntaxException p){
			//thrown this to fail tanuki wrapper
			throw new BeanCreationException("DeploymentUtil.setUserMatchingRegexForDispatch", p.getMessage());
		}
	}

	public boolean isIncludeTZOForOrgCal() {
		return includeTZOForOrgCal;
	}

	public void setIncludeTZOForOrgCal(boolean includeTZOForOrgCal) {
		this.includeTZOForOrgCal = includeTZOForOrgCal;
	}

	public boolean isValidateNullServiceUrl() {
		return validateNullServiceUrl;
	}

	public void setValidateNullServiceUrl(boolean validateNullServiceUrl) {
		this.validateNullServiceUrl = validateNullServiceUrl;
	}

	public String[] getWebLayerLoginTypes() {
		return webLayerLoginTypes;
	}

	public void setWebLayerLoginTypes(String webLayerLoginTypes) {
		if(webLayerLoginTypes!=null && !webLayerLoginTypes.isEmpty()){
			this.webLayerLoginTypes = StringUtils.split(webLayerLoginTypes.trim().
					replaceAll(" ", ""), ",");
		}
	}

	public long getLoginInfoCacheExpiryInSeconds() {
		return loginInfoCacheExpiryInSeconds;
	}

	public void setLoginInfoCacheExpiryInSeconds(long loginInfoCacheExpiryInSeconds) {
		this.loginInfoCacheExpiryInSeconds = loginInfoCacheExpiryInSeconds;
	}

	public String getPercentageChannelVariable() {
		return percentageChannelVariable;
	}

	public void setPercentageChannelVariable(String percentageChannelVariable) {
		this.percentageChannelVariable = percentageChannelVariable;
	}

	public String getPercentageChannelVariableSuffix() {
		return percentageChannelVariableSuffix;
	}

	public void setPercentageChannelVariableSuffix(String percentageChannelVariableSuffix) {
		this.percentageChannelVariableSuffix = percentageChannelVariableSuffix;
	}

	public boolean isSendTicketInPost() {
		return sendTicketInPost;
	}

	public void setSendTicketInPost(boolean sendTicketInPost) {
		this.sendTicketInPost = sendTicketInPost;
	}

	public boolean isRefreshSSOCookie() {
		return refreshSSOCookie;
	}

	public void setRefreshSSOCookie(boolean refreshSSOCookie) {
		this.refreshSSOCookie = refreshSSOCookie;
	}

	public boolean isLogoutPreviousOnSSOCookieRefresh() {
		return logoutPreviousOnSSOCookieRefresh;
	}

	public void setLogoutPreviousOnSSOCookieRefresh(boolean logoutPreviousOnSSOCookieRefresh) {
		this.logoutPreviousOnSSOCookieRefresh = logoutPreviousOnSSOCookieRefresh;
	}

	public boolean isIgnoreAppKeyInRedirectValidation() {
		return ignoreAppKeyInRedirectValidation;
	}

	public void setIgnoreAppKeyInRedirectValidation(boolean ignoreAppKeyInRedirectValidation) {
		this.ignoreAppKeyInRedirectValidation = ignoreAppKeyInRedirectValidation;
	}

	public boolean isLogoutRedirectParamInSession() {
		return logoutRedirectParamInSession;
	}

	public void setLogoutRedirectParamInSession(boolean logoutRedirectParamInSession) {
		this.logoutRedirectParamInSession = logoutRedirectParamInSession;
	}


	public int getGlobalUserSearchreportIterationDataSize() {
		return globalUserSearchreportIterationDataSize;
	}


	public void setGlobalUserSearchreportIterationDataSize(int globalUserSearchreportIterationDataSize) {
		this.globalUserSearchreportIterationDataSize = globalUserSearchreportIterationDataSize;
	}


	public int getGlobalUserSearchReportPDFMaxRecords() {
		return globalUserSearchReportPDFMaxRecords;
	}


	public void setGlobalUserSearchReportPDFMaxRecords(int globalUserSearchReportPDFMaxRecords) {
		this.globalUserSearchReportPDFMaxRecords = globalUserSearchReportPDFMaxRecords;
	}



	public String stringify() {
		return "\n baseFolderName=" + baseFolderName + ",\n mobileBaseFolderName=" + mobileBaseFolderName
				+ ",\n maintenancePage=" + maintenancePage + ",\n defaultApp=" + defaultApp + ",\n allowMultipleLogins="
				+ allowMultipleLogins + ",\n ssoNotAuthorizedPage=" + ssoNotAuthorizedPage
				+ ",\n includeNonSSOAppsInSwitcher=" + includeNonSSOAppsInSwitcher + ",\n headersToLog=" + headersToLog
				+ ",\n sessionRegistryStartAction=" + sessionRegistryStartAction + ",\n sessionRegistryStopAction="
				+ sessionRegistryStopAction + ",\n defaultLocaleForRestMessages=" + defaultLocaleForRestMessages
				+ ",\n logoutUserOnDeletion=" + logoutUserOnDeletion + ",\n logoutUserOnDeactivationOrLock="
				+ logoutUserOnDeactivationOrLock + ",\n ssoLogoutConnTimeoutMs=" + ssoLogoutConnTimeoutMs
				+ ",\n ssoLogoutSoTimeoutMs=" + ssoLogoutSoTimeoutMs + ",\n logoutViaWebLayer=" + logoutViaWebLayer
				+ ",\n siteMinderRequestHeader=" + siteMinderRequestHeader + ",\n webLayerLoginTypes="
				+ Arrays.toString(webLayerLoginTypes) + ",\n loginInfoCacheExpiryInSeconds="
				+ loginInfoCacheExpiryInSeconds + ",\n refreshSSOCookie=" + refreshSSOCookie
				+ ",\n logoutPreviousOnSSOCookieRefresh=" + logoutPreviousOnSSOCookieRefresh + ",\n clientIpHeader="
				+ clientIpHeader + ",\n validateServiceUrl=" + validateServiceUrl + ",\n validateNullServiceUrl="
				+ validateNullServiceUrl + ",\n sessionInactivityTimeoutSeconds=" + sessionInactivityTimeoutSeconds
				+ ",\n useOnlyTicketInServiceValidate=" + useOnlyTicketInServiceValidate + ",\n timeRestrictionIncCounter="
				+ timeRestrictionIncCounter + ",\n ipRestrictionIncCounter=" + ipRestrictionIncCounter + ",\n xssRegex="
				+ xssRegex + ",\n disableXSS=" + disableXSS + ",\n xssActionError=" + xssActionError + ",\n sendTicketInPost="
				+ sendTicketInPost + ",\n ignoreAppKeyInRedirectValidation=" + ignoreAppKeyInRedirectValidation
				+ ",\n logoutRedirectParamInSession=" + logoutRedirectParamInSession + ",\n overrideLanguageCode="
				+ overrideLanguageCode + ",\n expireSessionOnPasswordChange=" + expireSessionOnPasswordChange
				+ ",\n changePasswordUrl=" + changePasswordUrl + ",\n changePasswordWithoutSessionTimeoutSeconds="
				+ changePasswordWithoutSessionTimeoutSeconds + ",\n useHomeUrlInMustChangePassword="
				+ useHomeUrlInMustChangePassword + ",\n showAppDashboardInSwitcher=" + showAppDashboardInSwitcher
				+ ",\n appDashboardApplicationId=" + appDashboardApplicationId + ",\n showAppDashboardToAllUsers="
				+ showAppDashboardToAllUsers + ",\n auditLogReportPDFMaxRecords=" + auditLogReportPDFMaxRecords
				+ ",\n scopeDetailsReportPDFMaxRecords=" + scopeDetailsReportPDFMaxRecords + ",\n reportPDFMaxRecords="
				+ reportPDFMaxRecords + ",\n pdfReportMaxCellLength=" + pdfReportMaxCellLength
				+ ",\n pdfReportCellTruncationMessage=" + pdfReportCellTruncationMessage + ",\n reportRestReadTimeout="
				+ reportRestReadTimeout + ",\n tempReportFolder=" + tempReportFolder + ",\n reportResponseWriteSizeBytes="
				+ reportResponseWriteSizeBytes + ",\n auditLogreportIterationDataSize=" + auditLogreportIterationDataSize
				+ ",\n scopeDetailsreportIterationDataSize=" + scopeDetailsreportIterationDataSize
				+ ",\n reportIterationDataSize=" + reportIterationDataSize + ",\n reportSemaphoreSize="
				+ reportSemaphoreSize + ",\n reportSemaphoreTimeoutSecs=" + reportSemaphoreTimeoutSecs
				+ ",\n pdfReportXsltLockCount=" + pdfReportXsltLockCount + ",\n globalUserSearchreportIterationDataSize="
				+ globalUserSearchreportIterationDataSize + ",\n globalUserSearchReportPDFMaxRecords="
				+ globalUserSearchReportPDFMaxRecords + ",\n inListScopedApplications=" + inListScopedApplications
				+ ",\n inListScopedApplicationsList=" + inListScopedApplicationsList + ",\n regexForUrlAuhtorityValidation="
				+ regexForUrlAuhtorityValidation + ",\n validateUserImage=" + validateUserImage + ",\n queryForDispatch="
				+ queryForDispatch + ",\n sortOrderForDispatch=" + sortOrderForDispatch
				+ ",\n variableForUserIVRDispatchSeq=" + variableForUserIVRDispatchSeq + ",\n userMatchingRegexForDispatch="
				+ userMatchingRegexForDispatch + ",\n percentageChannelVariable=" + percentageChannelVariable
				+ ",\n percentageChannelVariableSuffix=" + percentageChannelVariableSuffix
				+ ",\n showBranchNameforPercentageBranch=" + showBranchNameforPercentageBranch
				+ ",\n enableAuditInfoInAuditLog=" + enableAuditInfoInAuditLog + ",\n includeTZOForOrgCal="
				+ includeTZOForOrgCal + ",\n enableDistributionGroup=" + enableDistributionGroup
				+ ",\n isMakercheckerActivated=" + isMakercheckerActivated + ",\n enableCaptchaCount=" + enableCaptchaCount
				+ ",\n captchaTimeoutInMinutes=" + captchaTimeoutInMinutes + ",\n specialCharacterRegex="
				+ specialCharacterRegex + ",\n enableTwoFactorAuth=" + enableTwoFactorAuth
				+ ",\n enableTwoFactorForLoginTypes=" + enableTwoFactorForLoginTypes + ",\n userNameMinLength="
				+ userNameMinLength + ",\n csvExportDateTimeFormat=" + csvExportDateTimeFormat
				+ ",\n nodeLimitForOrgGridView=" + nodeLimitForOrgGridView + ",\n batchSizeForGridData="
				+ batchSizeForGridData + ",\n replaceUrlByApplication=" + replaceUrlByApplication;
	}
	public List<String> getHeadersToLog() {
		return headersToLog;
	}
	public void setHeadersToLog(List<String> headersToLog) {
		this.headersToLog = headersToLog;
	}

	//Azure Active Directory properties getters and setters
	public String getAzureTenantId() {
		return azureTenantId;
	}
	public void setAzureTenantId(String azureTenantId) {
		this.azureTenantId = azureTenantId;
	}
	public String getAzureSsoServiceUrl() {
		return azureSsoServiceUrl;
	}
	public void setAzureSsoServiceUrl(String azureSsoServiceUrl) {
		this.azureSsoServiceUrl = azureSsoServiceUrl;
	}
	public String getEndSessionEndpoint() {
		return endSessionEndpoint;
	}
	public void setEndSessionEndpoint(String endSessionEndpoint) {
		this.endSessionEndpoint = endSessionEndpoint;
	}
	public String getOauth2ClientId() {
		return oauth2ClientId;
	}
	public void setOauth2ClientId(String oauth2ClientId) {
		this.oauth2ClientId = oauth2ClientId;
	}
	public String getOauth2ClientSecret() {
		return oauth2ClientSecret;
	}
	public void setOauth2ClientSecret(String oauth2ClientSecret) {
		this.oauth2ClientSecret = oauth2ClientSecret;
	}

	public String getAzureRedirectUri() {
		return azureRedirectUri;
	}
	public void setAzureRedirectUri(String azureRedirectUri) {
		this.azureRedirectUri = azureRedirectUri;
	}

	public String getClientIpHeader() {
		return clientIpHeader;
	}
	public void setWebLayerLoginTypes(String[] webLayerLoginTypes) {
		this.webLayerLoginTypes = webLayerLoginTypes;
	}
	public void setInListScopedApplicationsList(List<String> inListScopedApplicationsList) {
		this.inListScopedApplicationsList = inListScopedApplicationsList;
	}
	public void setUserMatchingRegexForDispatch(Pattern userMatchingRegexForDispatch) {
		this.userMatchingRegexForDispatch = userMatchingRegexForDispatch;
	}
	public boolean isCreateUsernameAsEmailId() {
		return createUsernameAsEmailId;
	}
	public void setCreateUsernameAsEmailId(boolean createUsernameAsEmailId) {
		this.createUsernameAsEmailId = createUsernameAsEmailId;
	}
	public Integer getAudioCaptchaTimeoutInSeconds() {
		return audioCaptchaTimeoutInSeconds;
	}

	public void setAudioCaptchaTimeoutInSeconds(Integer audioCaptchaTimeoutInSeconds) {
		this.audioCaptchaTimeoutInSeconds = audioCaptchaTimeoutInSeconds;
	}
}
