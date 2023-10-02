package com.esq.rbac.service.lookup;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TimeZone;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.concurrent.ConcurrentHashMap;

import com.esq.rbac.service.application.childapplication.appurldata.AppUrlData;
import com.esq.rbac.service.application.childapplication.domain.ChildApplication;
import com.esq.rbac.service.application.domain.Application;
import com.esq.rbac.service.codes.domain.Code;
import com.esq.rbac.service.group.domain.Group;
import com.esq.rbac.service.masterattributes.domain.MasterAttributes;
import com.esq.rbac.service.organization.domain.Organization;
import com.esq.rbac.service.role.domain.Role;
import com.esq.rbac.service.role.operationsubdomain.domain.Operation;
import com.esq.rbac.service.role.targetsubdomain.domain.Target;
import com.esq.rbac.service.scope.domain.Scope;
import com.esq.rbac.service.tenant.domain.Tenant;
import com.esq.rbac.service.tenant.emaddable.TenantIdentifier;
import com.esq.rbac.service.timezonemaster.domain.TimeZoneMaster;
import com.esq.rbac.service.user.domain.User;
import com.esq.rbac.service.util.RBACUtil;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.google.common.collect.BiMap;
import com.google.common.collect.HashBasedTable;
import com.google.common.collect.HashBiMap;
import com.google.common.collect.Table;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.w3c.dom.ls.LSOutput;

@Component
@Slf4j
public class Lookup {

    private static class CaseInsensitiveStringSerializer extends JsonSerializer<CaseInsensitiveString> {

        @Override
        public void serialize(CaseInsensitiveString value, JsonGenerator jgen, SerializerProvider provider)
                throws IOException, JsonProcessingException {
            jgen.writeString(value.s);
        }

    }

    @JsonSerialize(using = CaseInsensitiveStringSerializer.class)
    static class CaseInsensitiveString implements Comparable<CaseInsensitiveString>{
        String s;

        CaseInsensitiveString(String s){
            this.s = s;
        }

        public String get(){
            return s;
        }

        @Override
        public int hashCode(){
            return s.toLowerCase().hashCode();
        }

        @Override
        public boolean equals(Object o){
            if(!(o instanceof CaseInsensitiveString)){
                return false;
            }
            return s.toLowerCase().equals(((CaseInsensitiveString)o).s.toLowerCase());
        }

        @Override
        public int compareTo(CaseInsensitiveString o) {
            return s.toLowerCase().compareTo(o.s.toLowerCase());
        }

        @Override
        public String toString(){
            return s.toString();
        }
    }

    private static TreeMap<String, ApplicationValue> applicationLookupTable = new TreeMap<String, ApplicationValue>();

    private static class ApplicationValue {

        private Integer applicationId;
        private TreeMap<String, TargetValue> targetLookupTable = new TreeMap<String, TargetValue>();
    }

    private static class TargetValue {

        private Integer targetId;
        private TreeMap<String, Integer> operationLokupTable = new TreeMap<String, Integer>();
    }
    private static BiMap<Integer, CaseInsensitiveString> groupLookupTable = HashBiMap.create();
    private static BiMap<Integer, CaseInsensitiveString> userLookupTable = HashBiMap.create();
    private static TreeMap<Integer, String> scopeNamesLookupTable = new TreeMap<Integer, String>();
    private static BiMap<Integer, CaseInsensitiveString> scopeKeysLookupTable = HashBiMap.create();
    private static TreeMap<Integer, String> roleNamesLookupTable = new TreeMap<Integer, String>();
    private static TreeMap<Integer, String> operationNamesLookupTable = new TreeMap<Integer, String>();
    private static TreeMap<Integer, String> operationTargetLookupTable = new TreeMap<Integer, String>();
    private static Map<Integer, String> childAppNameLookupTable = new TreeMap<Integer, String>();
    private static Map<Integer, String> masterAttributeNameLookupTable = new HashMap<Integer, String>();
    private static final Set<String> serviceUrlsList = new TreeSet<String>(new ServiceUrlSorter());
    private static final Map<String, AppUrlData> serviceUrlData = new HashMap<String, AppUrlData>();
    private static final Map<String, Set<String>> appKeyServiceUrlsList = new HashMap<String, Set<String>>();
    private static final Map<String, ChildApplication> appKeyChilAppMap = new HashMap<String, ChildApplication>();
    private static final Map<Integer, String> childAppIdAppKeyMap = new HashMap<Integer, String>();
    private static final Map<String, String> childAppNameKeyMap = new HashMap<String, String>();
    private static final Map<Integer, AppUrlData> appUrlIdData = new HashMap<Integer, AppUrlData>();
    private static final Map<String,String> hostAppURLTagMap = new HashMap<String, String>();
    private static BiMap<Long, CaseInsensitiveString> tenantTable = HashBiMap.create();
    private static ConcurrentHashMap<Long, Tenant> tenantDetailsTable = new ConcurrentHashMap<Long, Tenant>();
    private static ConcurrentHashMap<Long, String> organizationTable = new ConcurrentHashMap<Long, String>();
    private static ConcurrentHashMap<Long, Long> organizationParentTable = new ConcurrentHashMap<Long, Long>();
    private static ConcurrentHashMap<Long, Long> organizationTenantTable = new ConcurrentHashMap<Long, Long>();
    //getOrganizationIdByNameWithTenantId
    //Map<TenantId, Map<OrganizationName, OrganizationId>>
    private static Table<Long, String, Long> organizationIdByNameWithTenantId = HashBasedTable.create();
    private static TreeMap<Long, Code> codesTable = new TreeMap<Long, Code>();
    private static final ConcurrentHashMap<Long, Long> organizationDefaultWorkCalendarMap = new ConcurrentHashMap<Long, Long>();
    private static final ConcurrentHashMap<Long, List<Long>> organizationDefaultHolidayCalendarMap = new ConcurrentHashMap<Long, List<Long>>();
    private static Tenant hostTenant;
    public static List<Code> codesTableAll = new ArrayList<Code>(); //RBAC-1562
    public static List<TimeZoneMaster> timeZoneListLookup  =new ArrayList<TimeZoneMaster>();
    private static List<String> timezoneDisplayNames = new ArrayList<String>();
    private static ConcurrentHashMap<String, TimeZoneMaster> timeZoneMasterMap = new ConcurrentHashMap<String, TimeZoneMaster>();

    public static class ServiceUrlSorter implements Comparator<String>{
        @Override
        public int compare(String o1, String o2) {
            if (o1.length() > o2.length()) {
                return -1;
            } else if (o1.length() < o2.length()) {
                return 1;
            }
            return o1.compareTo(o2);
        }
    }
    public static Set<String> getChildAppNameList(){
        return childAppNameKeyMap.keySet();
    }

    public static void fillTimeZoneMasterLookupTable(List<TimeZoneMaster> timeZoneMasterList) {
        timeZoneListLookup.clear();
        timezoneDisplayNames.clear();
        if(timeZoneMasterList!=null && !timeZoneMasterList.isEmpty()) {
            timeZoneListLookup = timeZoneMasterList;
        }
        for(TimeZoneMaster tz: timeZoneListLookup) {
            timeZoneMasterMap.put(tz.getTimezoneValue(), tz);
            timezoneDisplayNames.add(tz.getTimezoneValue());
        }
    }
    public static List<TimeZoneMaster> getTimeZoneMaster(){
        return timeZoneListLookup;
    }
    public static TimeZoneMaster getTimeZoneFromTimeZoneName(String timeZoneValue) {
        return timeZoneMasterMap.get(timeZoneValue);
    }

    public static ChildApplication getChildApplicationByNameNew(String childApplicationName){
        if(childApplicationName!=null && !childApplicationName.isEmpty()){
            childApplicationName = childApplicationName.toLowerCase();
            if(childAppNameKeyMap.containsKey(childApplicationName)){
                return appKeyChilAppMap.get(childAppNameKeyMap.get(childApplicationName).toLowerCase());
            }
        }
        return null;
    }

    public static String getHomeUrlByApplicationNameAndHost(String applicationName, String url){
        String host = null;
        if(url!=null && !url.isEmpty()){
            try {
                host = new URL(url.toLowerCase()).getHost();
            } catch (MalformedURLException e) {
                log.error("getHomeUrlByApplicationNameAndHost; url={}; applicationName={}; Exception={};", url, applicationName, e);
                return null;
            }
        }
        if(applicationName!=null && !applicationName.isEmpty()){
            ChildApplication childApp = getChildApplicationByNameNew(applicationName);
            if(childApp!=null && childApp.getAppUrlDataSet()!=null && !childApp.getAppUrlDataSet().isEmpty()){
                if(host!=null && !host.isEmpty()){
                    String tag = hostAppURLTagMap.get(host.toLowerCase());
                    if(tag!=null && !tag.isEmpty()){
                        for(AppUrlData appUrlData: childApp.getAppUrlDataSet()){
                            if(tag.equalsIgnoreCase(appUrlData.getTag())){
                                return appUrlData.getHomeUrl();
                            }
                        }
                    }
                }
            }
        }
        return null;
    }

    public static AppUrlData getAppUrlDataByServiceUrlNew(String serviceUrl){
        if(serviceUrl!=null && !serviceUrl.isEmpty()){
            // try service url exact match
            serviceUrl = serviceUrl.toLowerCase();
            if(serviceUrlData.containsKey(serviceUrl)){
                log.trace("getAppUrlDataByServiceUrlNew; exact match found for serviceUrl={};", serviceUrl);
                return serviceUrlData.get(serviceUrl);
            }
            //try service url partial match
            if(serviceUrlsList!=null && !serviceUrlsList.isEmpty()){
                String serviceUrlToMatchWithoutLastSlash = serviceUrl
                        .substring(0,
                                serviceUrl.lastIndexOf("/") + 1 == serviceUrl
                                        .length() ? serviceUrl.length() - 1
                                        : serviceUrl.length());
                for(String serviceUrlInList: serviceUrlsList){
                    if (serviceUrlInList != null
                            && !serviceUrlInList.isEmpty()
                            && serviceUrlToMatchWithoutLastSlash
                            .toLowerCase()
                            .startsWith(
                                    serviceUrlInList
                                            .substring(
                                                    0,
                                                    serviceUrlInList
                                                            .lastIndexOf("/") + 1 == serviceUrlInList
                                                            .length() ? serviceUrlInList
                                                            .length() - 1
                                                            : serviceUrlInList
                                                            .length())
                                            .toLowerCase())) {
                        log.trace(
                                "getAppUrlDataByServiceUrlNew; partial match found for serviceUrl={}; with serviceUrlToMatchWithoutLastSlash={}; serviceUrlInList={};",
                                serviceUrl, serviceUrlToMatchWithoutLastSlash, serviceUrlInList);
                        return serviceUrlData.get(serviceUrlInList);
                    }
                }
            }

        }
        return null;
    }

    public static AppUrlData getAppUrlDataByAppUrlIdNew(Integer appUrlId){
        return appUrlIdData.get(appUrlId);
    }
    public static ChildApplication getChildApplicationByServiceUrlNew(String serviceUrl){
        return getChildApplicationByUrl(serviceUrl, false);
    }

    public static ChildApplication getChildApplicationByServiceUrlNew(String serviceUrl, boolean ignoreAppKey){
        return getChildApplicationByUrl(serviceUrl, ignoreAppKey);
    }

    private static ChildApplication getChildApplicationByUrl(String serviceUrl, boolean ignoreAppKey){
        if(serviceUrl!=null && !serviceUrl.isEmpty()){
            if(!ignoreAppKey) {
                //try app key
                String appKey = RBACUtil.getAppKeyFromUrl(serviceUrl);
                if(appKey!=null && !appKey.isEmpty()){
                    log.trace("getChildApplicationByServiceUrlNew; using appKey={}; for serviceUrl={};", appKey, serviceUrl);
                    return appKeyChilAppMap.get(appKey.toLowerCase());
                }
            }
            // try service url exact match
            serviceUrl = serviceUrl.toLowerCase();
            if(serviceUrlData.containsKey(serviceUrl)){
                log.trace("getChildApplicationByServiceUrlNew; exact match found for serviceUrl={};", serviceUrl);
                return appKeyChilAppMap.get(serviceUrlData.get(serviceUrl).getChildApplication().getAppKey().toLowerCase());
            }
            //try service url partial match
            if(serviceUrlsList!=null && !serviceUrlsList.isEmpty()){
                String serviceUrlToMatchWithoutLastSlash = serviceUrl
                        .substring(0,
                                serviceUrl.lastIndexOf("/") + 1 == serviceUrl
                                        .length() ? serviceUrl.length() - 1
                                        : serviceUrl.length());
                for(String serviceUrlInList: serviceUrlsList){
                    if (serviceUrlInList != null
                            && !serviceUrlInList.isEmpty()
                            && serviceUrlToMatchWithoutLastSlash
                            .toLowerCase()
                            .startsWith(
                                    serviceUrlInList
                                            .substring(
                                                    0,
                                                    serviceUrlInList
                                                            .lastIndexOf("/") + 1 == serviceUrlInList
                                                            .length() ? serviceUrlInList
                                                            .length() - 1
                                                            : serviceUrlInList
                                                            .length())
                                            .toLowerCase())) {
                        log.trace(
                                "getChildApplicationByServiceUrlNew; partial match found for serviceUrl={}; with serviceUrlToMatchWithoutLastSlash={}; serviceUrlInList={};",
                                serviceUrl, serviceUrlToMatchWithoutLastSlash, serviceUrlInList);
                        return appKeyChilAppMap.get(serviceUrlData.get(serviceUrlInList).getChildApplication().getAppKey().toLowerCase());
                    }
                }
            }

        }
        return null;
    }

    public static String getHomeUrlByServiceUrlNew(String serviceUrl){
        if(serviceUrl!=null && !serviceUrl.isEmpty()){
            List<String> serviceUrlList = new ArrayList<String>();
            serviceUrlList.addAll(serviceUrlsList);
            //try app key
            String appKey = RBACUtil.getAppKeyFromUrl(serviceUrl);
            if(appKey!=null && !appKey.isEmpty()){
                serviceUrlList.clear();
                Set<String> urlSet = appKeyServiceUrlsList.get(appKey.toLowerCase());
                if(urlSet!=null){
                    serviceUrlList.addAll(urlSet);
                }
            }
            log.trace("getHomeUrlByServiceUrlNew; using serviceUrlList={}; for serviceUrl={};", serviceUrlList, serviceUrl);
            serviceUrl = serviceUrl.toLowerCase();
            // try service url exact match
            if(serviceUrlList.contains(serviceUrl)){
                return serviceUrlData.get(serviceUrl).getHomeUrl();
            }
            //try service url partial match
            if(serviceUrlList!=null && !serviceUrlList.isEmpty()){
                String serviceUrlToMatchWithoutLastSlash = serviceUrl
                        .substring(0,
                                serviceUrl.lastIndexOf("/") + 1 == serviceUrl
                                        .length() ? serviceUrl.length() - 1
                                        : serviceUrl.length());
                for(String serviceUrlInList: serviceUrlList){
                    if (serviceUrlInList != null
                            && !serviceUrlInList.isEmpty()
                            && serviceUrlToMatchWithoutLastSlash
                            .toLowerCase()
                            .startsWith(
                                    serviceUrlInList
                                            .substring(
                                                    0,
                                                    serviceUrlInList
                                                            .lastIndexOf("/") + 1 == serviceUrlInList
                                                            .length() ? serviceUrlInList
                                                            .length() - 1
                                                            : serviceUrlInList
                                                            .length())
                                            .toLowerCase())) {
                        return serviceUrlData.get(serviceUrlInList).getHomeUrl();
                    }
                }
            }
        }
        return null;
    }

    public static String getLogoutUrlByServiceUrlNew(String serviceUrl){
        if(serviceUrl!=null && !serviceUrl.isEmpty()){
            List<String> serviceUrlList = new ArrayList<String>();
            serviceUrlList.addAll(serviceUrlsList);
            //try app key
            String appKey = RBACUtil.getAppKeyFromUrl(serviceUrl);
            if(appKey!=null && !appKey.isEmpty()){
                serviceUrlList.clear();
                serviceUrlList.addAll(appKeyServiceUrlsList.get(appKey.toLowerCase()));
            }
            log.trace("getLogoutUrlByServiceUrlNew; using serviceUrlList={}; for serviceUrl={};", serviceUrlList, serviceUrl);
            serviceUrl = serviceUrl.toLowerCase();
            // try service url exact match
            if(serviceUrlList.contains(serviceUrl)){
                return serviceUrlData.get(serviceUrl).getLogoutServiceUrl();
            }
            //try service url partial match
            if(serviceUrlList!=null && !serviceUrlList.isEmpty()){
                String serviceUrlToMatchWithoutLastSlash = serviceUrl
                        .substring(0,
                                serviceUrl.lastIndexOf("/") + 1 == serviceUrl
                                        .length() ? serviceUrl.length() - 1
                                        : serviceUrl.length());
                for(String serviceUrlInList: serviceUrlList){
                    if (serviceUrlInList != null
                            && !serviceUrlInList.isEmpty()
                            && serviceUrlToMatchWithoutLastSlash
                            .toLowerCase()
                            .startsWith(
                                    serviceUrlInList
                                            .substring(
                                                    0,
                                                    serviceUrlInList
                                                            .lastIndexOf("/") + 1 == serviceUrlInList
                                                            .length() ? serviceUrlInList
                                                            .length() - 1
                                                            : serviceUrlInList
                                                            .length())
                                            .toLowerCase())) {
                        return serviceUrlData.get(serviceUrlInList).getLogoutServiceUrl();
                    }
                }
            }
        }
        return null;
    }

    public static ChildApplication getChildApplicationByAppKeyNew(String appKey){
        if(appKey!=null && !appKey.isEmpty()){
            return appKeyChilAppMap.get(appKey.toLowerCase());
        }
        return null;
    }

    public static String getAppKeyByChildAppId(Integer childAppId){
        if(childAppId!=null){
            return childAppIdAppKeyMap.get(childAppId);
        }
        return null;
    }

    public static void fillMasterAttributeNameLookupTable(List<MasterAttributes> masterAttrList) {
        masterAttributeNameLookupTable.clear();
        if(masterAttrList!=null && !masterAttrList.isEmpty()){
            for(MasterAttributes masterAttr: masterAttrList){
                masterAttributeNameLookupTable.put(masterAttr.getAttributeId(), masterAttr.getAttributeName());

            }
        }
    }



    public static String getMasterAttributeNameById(Integer attributeId){
        if(attributeId!=null){
            return masterAttributeNameLookupTable.get(attributeId);
        }
        return null;
    }

    public static void fillGroupLookupTable(List<Map<String,Object>> groupList) {
        groupLookupTable.clear();
        if(groupList!=null && !groupList.isEmpty()){
            for (Map<String,Object> grp : groupList) {
                if(grp.get("groupId")!=null){
                    Integer groupId = Integer.parseInt(grp.get("groupId").toString());
                    if(grp.get("name")!=null){
                        groupLookupTable.put(groupId, new CaseInsensitiveString(grp.get("name").toString()));
                    }
                }
            }
        }
    }

    public static String getGroupName(Integer groupId) {
        String groupName = null;
        if (groupId != null) {
            CaseInsensitiveString temp = groupLookupTable.get(groupId);
            groupName = temp!=null?temp.get():null;
        }
        return groupName != null ? groupName : new String();
    }

    public static Integer getGroupId(String groupName) {
        if (groupName != null && !groupName.isEmpty()) {
            Integer groupId = groupLookupTable.inverse().get(new CaseInsensitiveString(groupName));
            if (groupId != null) {
                return groupId;
            }
        }
        return -1;
    }

    public static void updateGroupLookupTable(Group group) {
        if (group != null && group.getGroupId()!=null) {
            groupLookupTable.forcePut(group.getGroupId(), new CaseInsensitiveString(group.getName()));
        }
    }

    public static void deleteFromGroupLookupTable(Integer groupId) {
        if (groupId != null) {
            groupLookupTable.remove(groupId);
        }
    }

    public static void fillUserLookupTable(List<Map<String,Object>> userList) {
        userLookupTable.clear();
        if(userList!=null && !userList.isEmpty()){
            for (Map<String,Object> user : userList) {
                if(user.get("userId")!=null){
                    Integer userId = Integer.parseInt(user.get("userId").toString());
                    if(user.get("userName")!=null){
                        userLookupTable.put(userId, new CaseInsensitiveString(user.get("userName").toString()));
                    }
                }
            }
        }
    }

    public static String getUserName(Integer userId) {
        String userName = null;
        if (userId != null) {
            CaseInsensitiveString temp = userLookupTable.get(userId);
            userName = temp!=null?temp.get():null;
        }
        return userName != null ? userName : "";
    }

    public static String getUserNameWithNull(Integer userId) {
        String userName = null;
        if (userId != null) {
            CaseInsensitiveString temp = userLookupTable.get(userId);
            userName = temp!=null?temp.get():null;
        }
        return userName;
    }

    public static Integer getUserId(String userName) {
        if(userName!=null && !userName.isEmpty()){
            Integer userId = userLookupTable.inverse().get(new CaseInsensitiveString(userName));
            if(userId!=null){
                return userId;
            }
        }
        return -1;
    }

    public static void updateUserLookupTable(User user) {
        if (user != null && user.getUserId()!=null) {
            userLookupTable.forcePut(user.getUserId(), new CaseInsensitiveString(user.getUserName()));
        }
    }

    public static void deleteFromUserLookupTable(Integer userId) {
        if (userId != null) {
            userLookupTable.remove(userId);
        }
    }

    public static synchronized void fillScopeLookupTable(List<Scope> scopeList) {
        scopeNamesLookupTable.clear();
        scopeKeysLookupTable.clear();
        for (Scope scp : scopeList) {
            scopeNamesLookupTable.put(scp.getScopeId(), scp.getName());
            scopeKeysLookupTable.put(scp.getScopeId(), new CaseInsensitiveString(scp.getScopeKey()));
        }
    }

    public static String getScopeName(Integer scopeId) {
        String scopeName = null;
        if (scopeId != null) {
            scopeName = scopeNamesLookupTable.get(scopeId);
        }
        return scopeName != null ? scopeName : new String();
    }

    public static String getScopeKey(Integer scopeId) {
        String scopeKey = null;
        if (scopeId != null) {
            CaseInsensitiveString temp = scopeKeysLookupTable.get(scopeId);
            scopeKey = temp!=null?temp.get():null;
        }
        return scopeKey != null ? scopeKey : new String();
    }

    public static Integer getScopeIdByKey(String scopeKey) {
        return scopeKeysLookupTable.inverse().get(new CaseInsensitiveString(scopeKey));
    }

    public static void fillRoleLookupTable(List<Role> roleList) {
        roleNamesLookupTable.clear();
        for (Role role : roleList) {
            roleNamesLookupTable.put(role.getRoleId(), role.getName());
        }
    }

    private synchronized static void fillAppUrlData(ChildApplication childApplication, Set<AppUrlData> appUrlDataList){
        if(appUrlDataList!=null && !appUrlDataList.isEmpty()){
            for(AppUrlData urlData: appUrlDataList){
                if(urlData.getAppUrlId()!=null){
                    appUrlIdData.put(urlData.getAppUrlId(), urlData);
                }
                if(urlData.getTag()!=null){
                    try {
                        hostAppURLTagMap.put(new URL(urlData.getServiceUrl().toLowerCase()).getHost(), urlData.getTag());
                    } catch (MalformedURLException e) {
                        log.error("fillAppUrlData; urlData={}; Exception={};", urlData, e);
                    }
                }
                serviceUrlsList.add(urlData.getServiceUrl().toLowerCase());
                serviceUrlData.put(urlData.getServiceUrl().toLowerCase(), urlData);
                if(appKeyServiceUrlsList.containsKey(childApplication.getAppKey().toLowerCase()) == false){
                    appKeyServiceUrlsList.put(childApplication.getAppKey().toLowerCase(), new HashSet<String>());
                }
                appKeyServiceUrlsList.get(childApplication.getAppKey().toLowerCase()).add(urlData.getServiceUrl().toLowerCase());
            }
        }
    }

    public static void fillLookupTables(List<Application> appList) {
        log.trace("fillLookupTables; called");
        childAppNameLookupTable.clear();
        appKeyChilAppMap.clear();
        childAppIdAppKeyMap.clear();
        childAppNameKeyMap.clear();
        serviceUrlsList.clear();
        serviceUrlData.clear();
        appUrlIdData.clear();
        hostAppURLTagMap.clear();
        for (Application app : appList) {
            ApplicationValue av = new ApplicationValue();
            av.applicationId = app.getApplicationId();

            if(app.getTargets()!=null && !app.getTargets().isEmpty()){
                for (Target trgt : new ArrayList<Target>(app.getTargets())) {
                    TargetValue tv = new TargetValue();
                    tv.targetId = trgt.getTargetId();
                    av.targetLookupTable.put(trgt.getName(), tv);

                    for (Operation op : new ArrayList<Operation>(trgt.getOperations())) {
                        tv.operationLokupTable.put(op.getName(), op.getOperationId());
                        operationNamesLookupTable.put(op.getOperationId(), op.getName());
                        operationTargetLookupTable.put(op.getOperationId(), trgt.getName());
                    }
                }
            }
            applicationLookupTable.put(app.getName(), av);

            if(app.getChildApplications()!=null && !app.getChildApplications().isEmpty()){
                for(ChildApplication childApp : app.getChildApplications())
                {
                    fillAppUrlData(childApp, childApp.getAppUrlDataSet());
                    childAppNameLookupTable.put(childApp.getChildApplicationId(), childApp.getChildApplicationName());
                    if(childApp.getAppKey()==null || childApp.getAppKey().isEmpty()){
                        log.error("fillLookupTables; appKey is null; childApplicationName={};", childApp.getChildApplicationName());
                        throw new ExceptionInInitializerError();
                    }
                    appKeyChilAppMap.put(childApp.getAppKey().toLowerCase(), childApp);
                    childAppIdAppKeyMap.put(childApp.getChildApplicationId(), childApp.getAppKey());
                    childAppNameKeyMap.put(childApp.getChildApplicationName().toLowerCase(), childApp.getAppKey());
                }
            }
        }
    }

    public static synchronized void fillTenants(List<Map<String,Object>> tenantList, Tenant host) {
        tenantTable.clear();
        if(tenantList!=null && !tenantList.isEmpty()){
            for(Map<String,Object> tenant: tenantList){
                if(tenant.get("tenantId")!=null){
                    Long tenantId = Long.parseLong(tenant.get("tenantId").toString());
                    if(tenant.get("tenantName")!=null){
                        tenantTable.put(tenantId, new CaseInsensitiveString(tenant.get("tenantName").toString()));
                    }
                }
            }
        }
        hostTenant = host;
    }
    public static synchronized void fillDetailTenants(List<Tenant> tenantList) {
        tenantDetailsTable.clear();
        if(tenantList!=null && !tenantList.isEmpty()){
            for(Tenant tenant: tenantList){
                tenantDetailsTable.put(tenant.getTenantId(), tenant);
            }
        }
    }

    public static String getTenantNameById(Long tenantId){
        if(tenantId!=null){
            CaseInsensitiveString temp = tenantTable.get(tenantId);
            return temp!=null?temp.get():null;
        }
        return null;
    }

    public static Long getTenantIdByName(String tenantName) {
        if(tenantName!=null && !tenantName.isEmpty()){
            return tenantTable.inverse().get(new CaseInsensitiveString(tenantName));
        }
        return null;
    }

    public static void updateTenantLookupTable(Tenant tenant) {
        if (tenant != null && tenant.getTenantId()!=null) {
            tenantDetailsTable.put(tenant.getTenantId(), tenant);
            tenantTable.forcePut(tenant.getTenantId(), new CaseInsensitiveString(tenant.getTenantName()));
            if (tenant.getTenantType() != null
                    && RBACUtil.HOST_TENANT_TYPE_CODE_VALUE.equalsIgnoreCase(tenant.getTenantType().getCodeValue())) {
                hostTenant = tenant;
            }
            tenantDetailsTable.put(tenant.getTenantId(),tenant);
        }
    }

    public static void deleteFromTenantLookupTable(Long tenantId) {
        if (tenantId != null) {
            tenantTable.remove(tenantId);
            organizationIdByNameWithTenantId.row(tenantId).clear();
        }
    }

    public static Tenant getHostTenant(){
        return hostTenant;
    }

    public static synchronized void fillorganizations(List<Map<String, Object>> organizationList) {
        organizationTable.clear();
        organizationIdByNameWithTenantId.clear();
        organizationParentTable.clear();
        organizationTenantTable.clear();
        if (organizationList != null && !organizationList.isEmpty()) {
            for (Map<String, Object> organization : organizationList) {
                if(organization.get("organizationId")!=null){
                    Long organizationId = Long.parseLong(organization.get("organizationId").toString());
                    if (organization.get("parentOrganizationId") != null) {
                        organizationParentTable.put(organizationId,
                                Long.parseLong(organization.get("parentOrganizationId").toString()));
                    }
                    if(organization.get("organizationName")!=null){
                        organizationTable.put(organizationId, organization.get("organizationName").toString());
                        if ((organization.get("tenantId")) != null) {
                            Long tenantId = Long.parseLong(organization.get("tenantId").toString());
                            organizationTenantTable.put(organizationId, tenantId);
                            organizationIdByNameWithTenantId.row(tenantId).put(organization.get("organizationName").toString().toLowerCase(),
                                    organizationId);
                        }
                    }
                }
            }
        }
    }

    public static String getOrganizationNameById(Long organizationId){
        if(organizationId!=null){
            return organizationTable.get(organizationId);
        }
        return null;
    }

    public static List<Long> getOrganizationIdByName(String organizationName) {
        List<Long> returnList = null;
        if(organizationName!=null && !organizationName.isEmpty()){
            for (Entry<Long, String> entry : organizationTable.entrySet()) {
                if (organizationName.equalsIgnoreCase(entry.getValue()!=null?entry.getValue():null)) {
                    if(returnList==null){
                        returnList = new LinkedList<Long>();
                    }
                    returnList.add(entry.getKey());
                }
            }
        }
        return returnList;
    }

    public static Long getOrganizationIdByNameWithTenantId(String organizationName, Long tenantId) {
        if(organizationName!=null && !organizationName.isEmpty()){
            return organizationIdByNameWithTenantId.row(tenantId).get(organizationName.toLowerCase());
        }
        return null;
    }

    public static Long getTenantIdByOrganizationId(Long organizationId){
        if(organizationId!=null){
            return organizationTenantTable.get(organizationId);
        }
        return null;
    }

    public static void updateOrganizationLookupTable(Organization org) {
        String oldOrgName = organizationTable.get(org.getOrganizationId());
        organizationTable.remove(org.getOrganizationId());
        if (oldOrgName != null) {
            Map<String, Long> conHashMapObj = organizationIdByNameWithTenantId.row(org.getTenantId());
            if (oldOrgName != null && conHashMapObj != null) {
                conHashMapObj.remove(oldOrgName);
            }
        }
        organizationIdByNameWithTenantId.put(org.getTenantId(), org.getOrganizationName().toLowerCase(), org.getOrganizationId());
        organizationTable.put(org.getOrganizationId(), org.getOrganizationName());
        organizationTenantTable.put(org.getOrganizationId(), org.getTenantId());
        organizationParentTable.remove(org.getOrganizationId());
        if (org.getParentOrganizationId() != null) {
            organizationParentTable.put(org.getOrganizationId(), org.getParentOrganizationId());
        }
    }

    public static void deleteFromOrganizationtLookupTable(List<Organization> orgIdList) {
        for (Organization organization : orgIdList) {
            if (organization.getOrganizationId() != null) {
                String oldOrgName = organizationTable.get(organization.getOrganizationId());
                organizationTable.remove(organization.getOrganizationId());
                organizationParentTable.remove(organization.getOrganizationId());
                organizationTenantTable.remove(organization.getOrganizationId());
                if (oldOrgName != null) {
                    long tenantId = organization.getTenantId();
                    if (organizationIdByNameWithTenantId.row(tenantId) != null && oldOrgName != null
                            && !oldOrgName.isEmpty()) {
                        organizationIdByNameWithTenantId.remove(tenantId, oldOrgName.toLowerCase());
                    }
                }
            }
        }
    }


    public static synchronized void fillOrganizationDefaultCalendars(
            Map<Long, Long> organizationWorkCalMap,
            Map<Long, List<Long>> organizationHolidayCalMap) {
        organizationDefaultWorkCalendarMap.clear();
        if(organizationWorkCalMap!=null && !organizationWorkCalMap.isEmpty()){
            for(Long key: organizationWorkCalMap.keySet()){
                organizationDefaultWorkCalendarMap.put(key, organizationWorkCalMap.get(key));
            }
        }
        organizationDefaultHolidayCalendarMap.clear();
        if(organizationHolidayCalMap!=null && !organizationHolidayCalMap.isEmpty()){
            for(Long key: organizationHolidayCalMap.keySet()){
                organizationDefaultHolidayCalendarMap.put(key, organizationHolidayCalMap.get(key));
            }
        }
    }

    public static String getRoleName(Integer roleId) {
        String roleName = null;
        if (roleId != null) {
            roleName = roleNamesLookupTable.get(roleId);
        }
        return roleName != null ? roleName : new String();
    }

    public static String getOperationName(Integer operationId) {
        String operationName = null;
        if (operationId != null) {
            operationName = operationNamesLookupTable.get(operationId);
        }
        return operationName != null ? operationName : new String();
    }

    public static Integer getApplicationId(String applicationName) {
        ApplicationValue appValue = applicationLookupTable.get(applicationName);
        return appValue != null ? appValue.applicationId : -1;
    }

    public static Integer getTargetId(String applicationName, String targetName) {
        ApplicationValue appValue = applicationLookupTable.get(applicationName);
        TargetValue trgValue = null;
        if (appValue != null) {
            trgValue = appValue.targetLookupTable.get(targetName);
        }

        return trgValue != null ? trgValue.targetId : -1;
    }

    public static Integer getOperationId(String applicationName, String targetName, String operationName) {
        ApplicationValue appValue = applicationLookupTable.get(applicationName);
        TargetValue trgValue = null;
        if (appValue != null) {
            trgValue = appValue.targetLookupTable.get(targetName);
        }
        Integer opId = null;
        if (trgValue != null) {
            opId = trgValue.operationLokupTable.get(operationName);
        }

        return opId != null ? opId : -1;
    }

    public static String getApplicationName(Integer applicationId) {
        for (Entry<String, ApplicationValue> entry : applicationLookupTable.entrySet()) {
            if (applicationId.equals(entry.getValue().applicationId)) {
                return entry.getKey();
            }
        }
        return new String();
    }

    public static String getChildApplicationName(Integer childApplicationId) {
        if(childApplicationId!=null){
            return childAppNameLookupTable.get(childApplicationId);
        }
        return new String();
    }

    public static Integer getChildApplicationIdByName(String childApplicationName) {
        if (childApplicationName != null) {
            for (Entry<Integer, String> entry : childAppNameLookupTable.entrySet()) {
                if (entry.getValue().equalsIgnoreCase(childApplicationName)) {
                    return entry.getKey();
                }
            }
        }
        return null;

    }

    public static String getTargetName(Integer applicationId, Integer targetId) {
        for (Entry<String, ApplicationValue> entry : applicationLookupTable.entrySet()) {
            if (applicationId.equals(entry.getValue().applicationId)) {
                for (Entry<String, TargetValue> tv : entry.getValue().targetLookupTable.entrySet()) {
                    if (targetId.equals(tv.getValue().targetId)) {
                        return tv.getKey();
                    }
                }
            }
        }
        return new String();
    }

    public static String getOperationName(Integer applicationId, Integer targetId, Integer operationId) {
        for (Entry<String, ApplicationValue> entry : applicationLookupTable.entrySet()) {
            if (applicationId.equals(entry.getValue().applicationId)) {
                for (Entry<String, TargetValue> tv : entry.getValue().targetLookupTable.entrySet()) {
                    if (targetId.equals(tv.getValue().targetId)) {
                        for (Entry<String, Integer> op : tv.getValue().operationLokupTable.entrySet()) {
                            if (operationId.equals(op.getValue())) {
                                return op.getKey();
                            }
                        }
                    }
                }
            }
        }
        return new String();
    }

    public static String getTargetOperationName(Integer operationId){
        String operationName = null;
        if (operationId != null) {
            operationName = operationNamesLookupTable.get(operationId);
        }
        return (operationName!=null)?operationTargetLookupTable.get(operationId)+"."+operationName:null;
    }


    public static Long getDefaultWorkCalendarIdByOrganization(Long organizationId) {
        if(!organizationDefaultWorkCalendarMap.isEmpty()){
            Long defaultCalendarId = null;
            Long currentOrganizationId = organizationId;
            while(defaultCalendarId==null && currentOrganizationId!=null){
                defaultCalendarId = organizationDefaultWorkCalendarMap.get(currentOrganizationId);
                currentOrganizationId = organizationParentTable.get(currentOrganizationId);
            }
            return defaultCalendarId;
        }
        return null;
    }

    public static List<Long> getDefaultHolidayCalendarsIdByOrganization(Long organizationId) {
        if(!organizationDefaultHolidayCalendarMap.isEmpty()){
            List<Long> defaultCalendarId = null;
            Long currentOrganizationId = organizationId;
            while(defaultCalendarId==null && currentOrganizationId!=null){
                defaultCalendarId = organizationDefaultHolidayCalendarMap.get(currentOrganizationId);
                currentOrganizationId = organizationParentTable.get(currentOrganizationId);
            }
            return defaultCalendarId;
        }
        return null;
    }

    public static synchronized void fillCodesTable(List<Code> codes){
        codesTable.clear();
        codesTableAll.clear(); //RBAC-1562
        for (Code code : codes) {
            codesTable.put(code.getCodeId(), code);
        }
        codesTableAll.addAll(codes); //RBAC-1562
    }
    public static String getCodeValueById(Long codeId){
        if(codeId!=null){
            Code code = codesTable.get(codeId);
            if(code !=null){
                return code.getCodeValue();
            }
        }
        return null;
    }

    /**
     * @Description To check twoFactorAuthEnabled flag for the Tenant
     * @param tenantId
     * @return
     * @JIRAID RBAC-1562
     */
    public static boolean checkTwoFactorAuthEnabledInTenant(Long tenantId) {
        boolean isTwoFactor = false;
        if (tenantId != null) {
            Tenant tenant = tenantDetailsTable.get(tenantId);
            if (tenant != null) {
                isTwoFactor = tenant.isTwoFactorAuthEnabled();
            }
        }
        return isTwoFactor;
    }

    public static String getTenantIdentifiersById(Long tenantId) {
        String strIdentifier = "";
        if (tenantId != null) {
            Tenant tenant = tenantDetailsTable.get(tenantId);
            if (tenant != null && tenant.getIdentifiers() != null && tenant.getIdentifiers().size() > 0) {
                for (TenantIdentifier iden : tenant.getIdentifiers()) {
                    strIdentifier += "," + iden.getTenantIdentifier();
                }
                if (strIdentifier != "")
                    strIdentifier = strIdentifier.substring(1);
            }
        }
        return strIdentifier;
    }
    public static boolean getTenantIsHostById(Long tenantId) {
        boolean isHost = false;
        if (tenantId != null) {
            Tenant tenant = tenantDetailsTable.get(tenantId);
            if (tenant != null) {
                Code tenantType = tenant.getTenantType();
                if(tenantType.getName() != null && tenantType.getName().equalsIgnoreCase("Host"))
                    isHost = true;
            }
        }
        return isHost;
    }

    /** START: Added By Fazia to getMakerCheckerEnabled flag for the User **/
    public static boolean checkMakerCheckerEnabledInTenant(Long tenantId) {
        boolean makerChekr = false;
        if (tenantId != null) {
            Tenant tenant = tenantDetailsTable.get(tenantId);
            if (tenant != null) {
                makerChekr = tenant.isMakerCheckerEnabled();
            }
        }
        return makerChekr;
    }
    /** END: Added By Fazia to getMakerCheckerEnabled flag for the User **/
    /*public static String getApplicationNameByServiceUrlWithPartialMatch(String serviceUrl){
    	Map<String, String> serviceUrlTable = serviceUrlApplicationLookupTable;
    	if(DeploymentUtil.getIS_IGNORE_SERVICE_URL_HOST_PORT().equals(Boolean.TRUE)){
    		serviceUrlTable = serviceUrlIdentifierApplicationLookupTable;
    		serviceUrl = RBACUtil.getServiceIdentifier(serviceUrl);
        }
    	if(serviceUrl!=null && !serviceUrl.isEmpty() && !serviceUrl.equals("") && !serviceUrl.equals("/") && serviceUrlTable!=null && !serviceUrlTable.isEmpty()){
    		String exactServiceUrlMatch = serviceUrlTable.get(serviceUrl);
    		if(exactServiceUrlMatch!=null && !exactServiceUrlMatch.isEmpty()){
    			return exactServiceUrlMatch;
    		}
    		String serviceUrlToMatchWithoutLastSlash = serviceUrl.substring(0, serviceUrl.lastIndexOf("/")+1==serviceUrl.length()?serviceUrl.length()-1:serviceUrl.length());
    		for(String serviceUrlKey:serviceUrlTable.keySet()) {
    			if(serviceUrlKey!=null && !serviceUrlKey.isEmpty() && serviceUrlToMatchWithoutLastSlash.toLowerCase().startsWith(serviceUrlKey.substring(0, serviceUrlKey.lastIndexOf("/")+1==serviceUrlKey.length()?serviceUrlKey.length()-1:serviceUrlKey.length()).toLowerCase()))
    			{
    				return serviceUrlTable.get(serviceUrlKey);
    			}
    		}
    	}
    	return null;
    }*/

   /* public static String getChildApplicationNameByServiceUrlWithPartialMatch(String serviceUrl){
    	Map<String, String> serviceUrlTable = serviceUrlChildAppLookupTable;
    	if(DeploymentUtil.getIS_IGNORE_SERVICE_URL_HOST_PORT().equals(Boolean.TRUE)){
    		serviceUrlTable = serviceUrlIdentifierChildAppLookupTable;
    		serviceUrl = RBACUtil.getServiceIdentifier(serviceUrl);
        }
    	if(serviceUrl!=null && !serviceUrl.isEmpty() && !serviceUrl.equals("") && !serviceUrl.equals("/") && serviceUrlTable!=null && !serviceUrlTable.isEmpty()){
    		String exactServiceUrlMatch = serviceUrlTable.get(serviceUrl);
    		if(exactServiceUrlMatch!=null && !exactServiceUrlMatch.isEmpty()){
    			return exactServiceUrlMatch;
    		}
    		String serviceUrlToMatchWithoutLastSlash = serviceUrl.substring(0, serviceUrl.lastIndexOf("/")+1==serviceUrl.length()?serviceUrl.length()-1:serviceUrl.length());
    		for(String serviceUrlKey:serviceUrlTable.keySet()) {
    			if(serviceUrlKey!=null && !serviceUrlKey.isEmpty() && serviceUrlToMatchWithoutLastSlash.toLowerCase().startsWith(serviceUrlKey.substring(0, serviceUrlKey.lastIndexOf("/")+1==serviceUrlKey.length()?serviceUrlKey.length()-1:serviceUrlKey.length()).toLowerCase()))
    			{
    				return serviceUrlTable.get(serviceUrlKey);
    			}
    		}
    	}
    	return null;
    }*/

   /* public static Integer getChildApplicationIdByServiceUrlWithPartialMatch(String serviceUrl){
    	 String childApplicationName = getChildApplicationNameByServiceUrlWithPartialMatch(serviceUrl);
    	 if(childApplicationName!=null && !childApplicationName.isEmpty()){
	    	 if(serviceUrl!=null && !serviceUrl.isEmpty() && childAppNameLookupTable!=null && !childAppNameLookupTable.isEmpty()){
	    		 for (Entry<Integer, String> entry : childAppNameLookupTable.entrySet()) {
	                 if (childApplicationName.equals(entry.getValue())) {
	                     return entry.getKey();
	                 }
	             }
	    	 }
    	 }
         return -1;
    }*/

  /*  public static String getHomeUrlByChildApplicationName(String childApplicationName){
    	log.trace("getHomeUrlByChildApplicationName; chilApphomeUrlLookupTable={}",chilApphomeUrlLookupTable);
    	if(childApplicationName!=null){
    		return chilApphomeUrlLookupTable.get(childApplicationName);
    	}
    	return null;
    }*/

   /* public static String getHomeUrlByServiceUrl(String serviceUrl){
    	String childApplicationName = getChildApplicationNameByServiceUrlWithPartialMatch(serviceUrl);
    	if(childApplicationName!=null && !childApplicationName.isEmpty()){
    		return getHomeUrlByChildApplicationName(childApplicationName);
    	}
    	return null;
    }*/

	/*public static List<String> getLogoutServiceUrls(String serviceUrl){
		List<String> logoutUrls = new LinkedList<String>();
    	if(DeploymentUtil.getIS_IGNORE_SERVICE_URL_HOST_PORT().equals(Boolean.TRUE)){
        	String childApplicationName = Lookup.getChildApplicationNameByServiceUrlWithPartialMatch(serviceUrl);
        	if(childApplicationName!=null && !childApplicationName.isEmpty()){
        		for (Entry<String, String> entry : serviceUrlChildAppLookupTable.entrySet()) {
                    if (childApplicationName.equals(entry.getValue())) {
                    	logoutUrls.add(entry.getKey());
                    }
                }
        	}
        }
    	else{
    		logoutUrls.add(serviceUrl);
    	}
    	return logoutUrls;
	}*/
    public static Boolean isTimeZoneValid(String timezone){
        if(timezoneDisplayNames.contains(timezone)) {
            return true;
        }else {
            return false;
        }
    }

    public static Boolean isLanguageValid(String languageCode){
        if(languageCode == null || languageCode.isEmpty())
            return false;
        Locale[] availableCodes = Locale.getAvailableLocales();
        List<Locale> codes=Arrays.asList(availableCodes);
        Locale reqCode = Locale.forLanguageTag(languageCode);
        log.debug("languageCode from Req {}",reqCode);
        if(codes.contains(reqCode)) {
            return true;
        }
        return false;
    }

}

