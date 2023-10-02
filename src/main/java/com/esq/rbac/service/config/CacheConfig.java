package com.esq.rbac.service.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.CachingConfigurerSupport;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.concurrent.ConcurrentMapCache;
import org.springframework.cache.interceptor.KeyGenerator;
import org.springframework.cache.support.SimpleCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Arrays;

@Configuration
@EnableCaching
public class CacheConfig extends CachingConfigurerSupport {
	private static final Logger log = LoggerFactory.getLogger(CacheConfig.class);

	public static final String USER_BY_USERNAME_CACHE = "UserByUserNameCache";
	public static final String USER_SCOPE_CACHE = "UserScopesCache";
	public static final String USER_TARGET_OPERATION_CACHE = "UserTargetOperationsCache";
	public static final String USER_VARIABLE_CACHE = "UserVariablesCache";
	public static final String GROUP_BY_GROUPID_CACHE = "GroupByIdCache";
	public static final String USERS_IN_GROUP_CACHE = "GetUserListByGroupIdCache";
	public static final String GROUP_ID_NAMES_WITH_SCOPES_CACHE = "GetGroupIdNamesWithScopeCache";
	public static final String GROUP_ID_NAMES_CACHE = "GetGroupIdNamesCache";
	
	
	
	public static final String USER_ROLES_CACHE = "UserRolesCache";
	public static final String USER_ATTRIBUTES_CACHE = "UserAttributesCache";
	public static final String USER_LIST_SCOPES_CACHE = "UserInListScopesDetailsCache";
	public static final String CALENDAR_BY_CALENDARID_CACHE = "CalendarByIdCache";
	public static final String CALENDAR_BY_ASSIGNED_STATUS_COUNT_CACHE = "CountByAssignedStatusCache";
	public static final String CALENDAR_BY_ASSIGNED_STATUS_CACHE = "DataByAssignedStatusCache";
	public static final String CALENDAR_BY_SEARCH_LIST_CACHE = "CalendarSearchListCache";
	public static final String CALENDAR_BY_LIST_CACHE = "CalendarListCache";
	public static final String CALENDAR_BY_SEARCH_LIST_COUNT_CACHE = "CalendarSearchListCountCache";
	public static final String CALENDAR_BY_LIST_COUNT_CACHE = "CalendarListCountCache";
	
	public static final String USER_BY_USERID_CACHE = "UserByIdCache";
	public static final String ALL_USER_LIST_CACHE = "AllUsersListCache";
	public static final String ALL_ORG_LIST_CACHE = "AllOrgListCache";
	public static final String USER_AUTH_APPS_CACHE = "UserAuthorizedAppsCache";
	public static final String ATTRIBUTES_DATA_CACHE = "AttributesDataCache";
	public static final String USER_TENANT_SCOPE_CACHE = "UserTenantScopeCache";
	public static final String ORG_HIERARCHY_GRID_VIEW_CACHE = "OrganizationHierarchyGridViewCache";
	public static final String ORG_HIERARCHY_D3_VIEW_CACHE = "OrganizationHierarchyGridViewCache";
	public static final String ORG_LIST_BY_TENANT_CACHE = "OrganizationByTenantIdCache";
	public static final String USER_APP_PERMISSION_CACHE = "UserPermissionsCache";
	public static final String USER_BY_USERNAME_LIKE_CACHE = "UserByUserNameLikeCache";
	public static final String ALL_USER_BY_USERNAME = "AllUserNamesCache";
	public static final String USER_LIST_FOR_DISPATCH_CACHE = "ListForDispatchCache";
	public static final String USER_COUNT = "UserCountCache";
	public static final String USER_SEARCH_LIST_CACHE = "UserSearchListCache";
	public static final String USER_CUSTOM_INFO_CACHE = "CustomUserInfoCache";
	public static final String ORG_BY_NAME_CACHE = "OrganizationByOrganizationNameCache";
	public static final String ORG_CUSTOM_INFO_CACHE = "CustomOrganizationInfoCache";
	public static final String ORG_SEARCH_BOX_DATA_CACHE = "SearchBoxDataCache";
	public static final String CODE_CACHE = "CodeListCache";
	public static final String CODE_BY_APPLICATION_CACHE = "CodeListCache";
	
	
	// Tenant
	public static final String TENANT_BY_NAME_CACHE = "TenantByNameCache";
	public static final String TENANT_LIST_CACHE = "TenantListCache";
	public static final String TENANT_SEARCH_LIST_CACHE = "TenantSearchListCache";
	public static final String TENANT_ID_LIST_CACHE = "TenantIdListCache";
	public static final String TENANT_BY_ID_CACHE = "TenantByIdCache";
	public static final String TENANT_ID_NAMES_LIST_CACHE = "TenantIdNamesListCache";
	public static final String TENANT_ID_NAMES_SEARCH_LIST_CACHE = "TenantIdNamesSearchListCache";
	
	/*
	 * CUSTOM KEY
	 */

	public static final String CUSTOM_KEY_GENERATOR = "customKeyGenerator";

	@Bean
	public CacheManager cacheManager() {
		log.info("Caching config initialized");
		SimpleCacheManager cacheManager = new SimpleCacheManager();
		cacheManager.setCaches(Arrays.asList(new ConcurrentMapCache(USER_BY_USERNAME_CACHE),
				new ConcurrentMapCache(USER_SCOPE_CACHE), new ConcurrentMapCache(USER_TARGET_OPERATION_CACHE),
				new ConcurrentMapCache(USER_VARIABLE_CACHE), new ConcurrentMapCache(GROUP_BY_GROUPID_CACHE),
				new ConcurrentMapCache(USER_ROLES_CACHE), new ConcurrentMapCache(USER_ATTRIBUTES_CACHE),
				new ConcurrentMapCache(USER_LIST_SCOPES_CACHE), new ConcurrentMapCache(CALENDAR_BY_CALENDARID_CACHE),
				new ConcurrentMapCache(USER_BY_USERID_CACHE), new ConcurrentMapCache(ALL_USER_LIST_CACHE),
				new ConcurrentMapCache(ALL_ORG_LIST_CACHE), new ConcurrentMapCache(USER_AUTH_APPS_CACHE),
				new ConcurrentMapCache(ATTRIBUTES_DATA_CACHE), new ConcurrentMapCache(USER_TENANT_SCOPE_CACHE),
				new ConcurrentMapCache(ORG_HIERARCHY_GRID_VIEW_CACHE),
				new ConcurrentMapCache(ORG_HIERARCHY_D3_VIEW_CACHE), new ConcurrentMapCache(ORG_LIST_BY_TENANT_CACHE),
				new ConcurrentMapCache(USER_APP_PERMISSION_CACHE), new ConcurrentMapCache(USER_BY_USERNAME_LIKE_CACHE),
				new ConcurrentMapCache(ALL_USER_BY_USERNAME), new ConcurrentMapCache(USER_LIST_FOR_DISPATCH_CACHE),
				new ConcurrentMapCache(USER_COUNT), new ConcurrentMapCache(USER_SEARCH_LIST_CACHE),
				new ConcurrentMapCache(USER_CUSTOM_INFO_CACHE), new ConcurrentMapCache(ORG_BY_NAME_CACHE),
				new ConcurrentMapCache(ORG_CUSTOM_INFO_CACHE), new ConcurrentMapCache(ORG_SEARCH_BOX_DATA_CACHE),
				new ConcurrentMapCache(TENANT_BY_NAME_CACHE), new ConcurrentMapCache(TENANT_ID_LIST_CACHE),
				new ConcurrentMapCache(TENANT_LIST_CACHE), new ConcurrentMapCache(TENANT_BY_ID_CACHE),
				new ConcurrentMapCache(TENANT_SEARCH_LIST_CACHE), new ConcurrentMapCache(TENANT_ID_NAMES_LIST_CACHE),
				new ConcurrentMapCache(TENANT_ID_NAMES_SEARCH_LIST_CACHE),new ConcurrentMapCache(CODE_CACHE),new ConcurrentMapCache(CODE_BY_APPLICATION_CACHE),
				new ConcurrentMapCache(CALENDAR_BY_ASSIGNED_STATUS_CACHE),new ConcurrentMapCache(CALENDAR_BY_LIST_CACHE),
				new ConcurrentMapCache(CALENDAR_BY_SEARCH_LIST_COUNT_CACHE),new ConcurrentMapCache(CALENDAR_BY_LIST_COUNT_CACHE),
				new ConcurrentMapCache(CALENDAR_BY_ASSIGNED_STATUS_COUNT_CACHE),new ConcurrentMapCache(CALENDAR_BY_SEARCH_LIST_CACHE),
				new ConcurrentMapCache(USERS_IN_GROUP_CACHE),new ConcurrentMapCache(GROUP_ID_NAMES_WITH_SCOPES_CACHE),
				new ConcurrentMapCache(GROUP_ID_NAMES_CACHE)
				));
		return cacheManager;
	}
	@Bean(name = CacheConfig.CUSTOM_KEY_GENERATOR)
	public KeyGenerator keyGenerator() {
        return new CustomKeyGenerator();
    }

	public static String[] CLEAR_ALL_CACHE = { USER_BY_USERNAME_CACHE, USER_SCOPE_CACHE, USER_TARGET_OPERATION_CACHE,
			USER_VARIABLE_CACHE, GROUP_BY_GROUPID_CACHE, USER_ROLES_CACHE, USER_ATTRIBUTES_CACHE,
			USER_LIST_SCOPES_CACHE, CALENDAR_BY_CALENDARID_CACHE, CALENDAR_BY_ASSIGNED_STATUS_CACHE,
			CALENDAR_BY_LIST_CACHE, CALENDAR_BY_SEARCH_LIST_COUNT_CACHE, CALENDAR_BY_LIST_COUNT_CACHE,
			CALENDAR_BY_ASSIGNED_STATUS_COUNT_CACHE, CALENDAR_BY_SEARCH_LIST_CACHE, USER_BY_USERID_CACHE,
			ALL_USER_LIST_CACHE, ALL_ORG_LIST_CACHE, USER_AUTH_APPS_CACHE, ATTRIBUTES_DATA_CACHE,
			USER_TENANT_SCOPE_CACHE, ORG_HIERARCHY_GRID_VIEW_CACHE, ORG_HIERARCHY_D3_VIEW_CACHE,
			ORG_LIST_BY_TENANT_CACHE, USER_APP_PERMISSION_CACHE, USER_BY_USERNAME_LIKE_CACHE, ALL_USER_BY_USERNAME,
			USER_LIST_FOR_DISPATCH_CACHE, USER_COUNT, USER_SEARCH_LIST_CACHE, USER_CUSTOM_INFO_CACHE, ORG_BY_NAME_CACHE,
			ORG_CUSTOM_INFO_CACHE, ORG_SEARCH_BOX_DATA_CACHE };

	public static String[] CLEAR_ALL_USER_CACHE = { USER_BY_USERNAME_CACHE, USER_SCOPE_CACHE,
			USER_TARGET_OPERATION_CACHE, USER_VARIABLE_CACHE, USER_ROLES_CACHE, USER_ATTRIBUTES_CACHE,
			USER_LIST_SCOPES_CACHE, USER_BY_USERID_CACHE, ALL_USER_LIST_CACHE, USER_AUTH_APPS_CACHE,
			ATTRIBUTES_DATA_CACHE, USER_TENANT_SCOPE_CACHE, ORG_HIERARCHY_GRID_VIEW_CACHE, ORG_HIERARCHY_D3_VIEW_CACHE,
			USER_APP_PERMISSION_CACHE, USER_BY_USERNAME_LIKE_CACHE, ALL_USER_BY_USERNAME, USER_LIST_FOR_DISPATCH_CACHE,
			USER_COUNT, USER_SEARCH_LIST_CACHE, USER_CUSTOM_INFO_CACHE, ORG_SEARCH_BOX_DATA_CACHE,USERS_IN_GROUP_CACHE };
	public static String[] CLEAR_ALL_ORG_CACHE = { ALL_ORG_LIST_CACHE, ALL_USER_LIST_CACHE,
			ORG_HIERARCHY_GRID_VIEW_CACHE, ORG_HIERARCHY_D3_VIEW_CACHE, ORG_LIST_BY_TENANT_CACHE, ALL_USER_BY_USERNAME,
			USER_LIST_FOR_DISPATCH_CACHE, USER_COUNT, USER_SEARCH_LIST_CACHE, USER_CUSTOM_INFO_CACHE, ORG_BY_NAME_CACHE,
			ORG_CUSTOM_INFO_CACHE, ORG_SEARCH_BOX_DATA_CACHE };

	public static String[] CLEAR_GROUP_CACHE = { GROUP_BY_GROUPID_CACHE,GROUP_ID_NAMES_CACHE,GROUP_ID_NAMES_WITH_SCOPES_CACHE,USERS_IN_GROUP_CACHE};
	
	public static String[] CLEAR_TENANT_CACHE = { TENANT_BY_NAME_CACHE,TENANT_LIST_CACHE,TENANT_SEARCH_LIST_CACHE,TENANT_ID_LIST_CACHE,TENANT_BY_ID_CACHE,
			TENANT_ID_NAMES_LIST_CACHE,TENANT_ID_NAMES_SEARCH_LIST_CACHE,ALL_ORG_LIST_CACHE,ORG_HIERARCHY_GRID_VIEW_CACHE};
	
}
