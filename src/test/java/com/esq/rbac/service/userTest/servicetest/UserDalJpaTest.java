package com.esq.rbac.service.userTest.servicetest;

import com.esq.rbac.service.application.childapplication.domain.ChildApplication;
import com.esq.rbac.service.application.domain.Application;
import com.esq.rbac.service.application.service.ApplicationDal;
import com.esq.rbac.service.attributes.domain.AttributesData;
import com.esq.rbac.service.auditlog.service.AuditLogService;
import com.esq.rbac.service.calendar.service.CalendarDal;
import com.esq.rbac.service.config.CacheConfig;
import com.esq.rbac.service.exception.ErrorInfoException;
import com.esq.rbac.service.filters.domain.Filters;
import com.esq.rbac.service.group.domain.Group;
import com.esq.rbac.service.group.repository.GroupRepository;
import com.esq.rbac.service.group.service.GroupDal;
import com.esq.rbac.service.ivrpasswordhistory.domain.IVRPasswordHistory;
import com.esq.rbac.service.ivrpasswordhistory.repository.IVRPasswordHistoryRepository;
import com.esq.rbac.service.loginservice.email.EmailDal;
import com.esq.rbac.service.loginservice.embedded.LogoutResponse;
import com.esq.rbac.service.loginservice.embedded.SessionRegistryLogoutRequest;
import com.esq.rbac.service.loginservice.service.LoginService;
import com.esq.rbac.service.lookup.Lookup;
import com.esq.rbac.service.makerchecker.domain.MakerChecker;
import com.esq.rbac.service.makerchecker.service.MakerCheckerDal;
import com.esq.rbac.service.organization.domain.Organization;
import com.esq.rbac.service.organization.organizationmaintenance.service.OrganizationMaintenanceDal;
import com.esq.rbac.service.organization.reposotiry.OrganizationRepository;
import com.esq.rbac.service.password.ivrpasswordpolicy.IVRPasswordPolicy;
import com.esq.rbac.service.password.passwordpolicy.service.PasswordPolicy;
import com.esq.rbac.service.password.paswordHistory.domain.PasswordHistory;
import com.esq.rbac.service.password.paswordHistory.repository.PasswordHistoryRepository;
import com.esq.rbac.service.sessionregistry.registry.SessionRegistry;
import com.esq.rbac.service.tenant.repository.TenantRepository;
import com.esq.rbac.service.user.azurmanagementconfig.service.AzureManagementConfig;
import com.esq.rbac.service.user.domain.User;
import com.esq.rbac.service.user.embedded.UserIdentity;
import com.esq.rbac.service.user.repository.UserRepository;
import com.esq.rbac.service.user.service.UserDalJpa;
import com.esq.rbac.service.user.vo.SSOLogoutData;
import com.esq.rbac.service.user.vo.UserWithLogoutData;
import com.esq.rbac.service.usersync.service.UserSyncService;
import com.esq.rbac.service.util.*;
import com.esq.rbac.service.util.dal.OptionFilter;
import com.esq.rbac.service.util.dal.OptionPage;
import com.esq.rbac.service.util.dal.OptionSort;
import com.esq.rbac.service.util.dal.Options;
import com.esq.rbac.service.variable.domain.Variable;
import com.esq.rbac.service.variable.repository.VariableRepository;
import com.esq.rbac.service.variable.service.VariableDal;
import jakarta.persistence.*;
import jakarta.ws.rs.core.MultivaluedHashMap;
import jakarta.ws.rs.core.MultivaluedMap;
import org.apache.commons.configuration.Configuration;
import org.joda.time.DateTime;
import org.junit.After;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class UserDalJpaTest {

    @Mock
    private EntityManager em;

//    @Mock
//    private Lookup lookup;

    @Mock
    private DeploymentUtil deploymentUtil;

//    @Mock
//    private SearchUtils searchUtils;

    @Mock
    MockedStatic<SearchUtils> searchMok;

    @Mock
    private UserRepository userRepository;
    @Mock
    private PasswordPolicy passwordPolicy;

    @Mock
    private IVRPasswordPolicy ivrPasswordPolicy;

    @Mock
    private VariableDal variableDal;

    @Mock
    private GroupDal groupDal;
    @Mock
    private EmailDal emailDal;
    @Mock
    private ApplicationDal applicationDal;
    @Mock
    private ChildAppPermValidatorUtil childAppPermValidatorUtil;

    @Mock
    private AuditLogService auditLogDal;

    @Mock
    private LoginService loginService;

    @Mock
    private CalendarDal calendarDal;
    @Mock
    private ContactDispatcherUtil contactDispatcherUtil;
    @Mock
    private Configuration configuration;

    @Mock
    private MakerCheckerDal makerCheckerDal;

    @Mock
    private UserSyncService userSyncDal;

    @Mock
    private CacheService cacheService;

    @Mock
    private IVRPasswordHistoryRepository ivrPasswordHistoryRepository;

    @Mock
    private PasswordHistoryRepository passwordHistoryRepository;

    @Mock
    private AzureManagementConfig azureManagementConfig;

    @Mock
    private TenantRepository tenantRepository;

    @Mock
    private OrganizationMaintenanceDal organizationMaintenanceDal;

    @Mock
    private VariableRepository variableRepository;

    @Mock
    private Filters filters;

    @Mock
    private EntityManagerFactory entityManagerFactory;

    @Mock
    private Cache cache;
    @InjectMocks
    private UserDalJpa userDalJpa;

//    @BeforeEach
//    public void setUp() {
//        MockitoAnnotations.initMocks(this);
//    }

    @After
    public void tearDown() {
        Mockito.reset(SearchUtils.class);
    }

    @Test
    public void testGetListService(){

//        MultivaluedMap<String,String> uriInfo = new MultivaluedHashMap<>();
//        uriInfo.addAll("organizationId","100");
//        System.out.println("uriInfo :  :"+uriInfo.toString());
//        OptionPage optionPage = new OptionPage(uriInfo, 0, Integer.MAX_VALUE);
//        OptionSort optionSort = new OptionSort(uriInfo);
//        OptionFilter optionFilter = new OptionFilter(uriInfo);
//        Options options = new Options(optionPage, optionSort, optionFilter);

        Options options = mock(Options.class);
        List<User> expectedUsers = Collections.singletonList(new User());

        searchMok.when(() -> SearchUtils.getSearchParam(eq(options),any())).thenReturn("asdasda");

        TypedQuery<User> mockQuery = mock(TypedQuery.class);
        when(em.createQuery(anyString(), eq(User.class))).thenReturn(mockQuery);
        when(mockQuery.setParameter(anyString(), any())).thenReturn(mockQuery);
        when(mockQuery.getResultList()).thenReturn(expectedUsers);

        List<User> actualdata = userDalJpa.searchList(options);
        Assertions.assertEquals(expectedUsers,actualdata);
    }

    @Test
    public void testCreateUserWithInvalidUsername(){
        // Create a User object with an invalid username
        User user = new User();
        user.setUserName("deep.doe1121@esq.com");
        user.setGroupId(1);
        user.setUserId(1001);
        user.setIsEnabled(true);
        user.setFirstName("Dipak1");
        user.setLastName("vora1");
        user.setChangePasswordFlag(false);
        user.setIsLocked(false);
        user.setIsShared(false);
        user.setPreferredLanguage("en-US");
        user.setTimeZone("America/Santiago");
        List<UserIdentity> userIdentity = new ArrayList<>();
        UserIdentity windowsUser = new UserIdentity("windowsUser", "");
        UserIdentity johnDoe112311 = new UserIdentity("deepDoe112311", "");
        userIdentity.add(windowsUser);
        userIdentity.add(johnDoe112311);
        user.setIdentities(userIdentity);
        user.setExternalRecordId("asd");
        user.setOrganizationId(100L);

        when(userRepository.save(any(User.class))).thenReturn(user);
        when(azureManagementConfig.createUser(any(User.class))).thenReturn("uniqueId");
//        when(deploymentUtil.getUsernameValidationRegex()).thenReturn("valid regex");
        User createdUser = userDalJpa.create(user, 1, "User", "Create");
        assertNotNull(createdUser);
    }

    @Test
    public void testUpdateMethod() {
//        update user data
        User updateUser = User.builder()
                .userId(1012)
                .userName("dip.doe1121@esq.com")
                .changePasswordFlag(false)
                .preferredLanguage("sl-SI")
                .timeZone("Canada/Yukon")
                .organizationId(100L).build();
        int loggedInUserId=1;
        String clientIp = "111:111:111";

//        curent user data
        User actualUser = User.builder()
                .userId(1012)
                .userName("Jhon.doe1121@esq.com")
                .changePasswordFlag(false)
                .preferredLanguage("sl-SI")
                .timeZone("Canada/Yukon")
                .organizationId(100L)
                .isStatus(1)
                .isEnabled(true)
                .build();

        List<SSOLogoutData> ssoLogoutDataList = new ArrayList<>();

        UserWithLogoutData updateSuccessfully = UserWithLogoutData.builder()
                .user(updateUser)
                .ssoLogoutDataList(ssoLogoutDataList)
                .build();

        System.out.println("actual user name : : "+actualUser.getUserName());
        when(userRepository.findById(any())).thenReturn(Optional.of(actualUser));
        LogoutResponse logoutResponse = mock(LogoutResponse.class);
        when(loginService.sessionRegistryLogout(any(),any(),any())).thenReturn(logoutResponse);
        when(logoutResponse.getSsoLogoutDataList()).thenReturn(ssoLogoutDataList);
        when(userRepository.save(any())).thenReturn(actualUser);
        UserWithLogoutData update = userDalJpa.update(updateUser, loggedInUserId, clientIp);
        System.out.printf("updateUser Value : : %s , actualUser value : : %s , update : : %s ",updateUser.getUserName(),actualUser.getUserName(),update.getUser().getUserName());
//        System.out.printf("updateUser Value : : %s , actualUser value : : %s",updateUser.getUserName(),actualUser.getUserName());
        Assertions.assertEquals(actualUser.getUserName(),update.getUser().getUserName());
    }

    @Test
    public void testDelete(){

        Integer loggedInUserId = 1 ;
        String clientIp = "111:1111:1111";
        List<String> loggedInTenant = List.of("100");
        int userId = 121;

        User user = User.builder()
                .userId(121)
                .groupId(1)
                .isEnabled(true)
                .firstName("John")
                .lastName("Doe")
                .userName("john.doe1@esq.com")
                .changePasswordFlag(false)
                .isLocked(false)
                .isShared(false)
                .isDeleted(false)
                .isEnabled(true)
                .isStatus(1)
                .identities(List.of(new UserIdentity("windowsUser", "johnDoe1")))
                .organizationId(100L).build();


        when(userRepository.findById(userId)).thenReturn(Optional.of(user));

        MockedStatic<Lookup> lookupMockedStatic = mockStatic(Lookup.class);
        lookupMockedStatic.when(() -> Lookup.checkMakerCheckerEnabledInTenant(any())).thenReturn(true);
        lookupMockedStatic.close();
//        when(deploymentUtil.getIsMakercheckerActivated()).thenReturn(true);
//        when(makerCheckerDal.getByEntityIdAndEntity(userId,User.class)).thenReturn(null);
        MakerChecker makerChecker = new MakerChecker();
        makerChecker.setId(33L);
//        when(makerCheckerDal.createEntry(user, User.class, loggedInUserId, "User", "Delete", userId,Long.parseLong(loggedInTenant.get(0)))).thenReturn(makerChecker);
        when(deploymentUtil.isUserSoftDelete()).thenReturn(true);

        UserWithLogoutData userWithLogoutData = userDalJpa.softDeleteById(userId, loggedInUserId, clientIp, 1, Long.parseLong(loggedInTenant.get(0)));

        System.out.println("user Status : : "+userWithLogoutData.getUser().getIsStatus());
        Assertions.assertEquals(0,userWithLogoutData.getUser().getIsStatus());

    }

    @Test
    void getByIdTest(){
        User user = mock(User.class);
        Optional<User> optional = mock(Optional.class);
        when(userRepository.findById(anyInt())).thenReturn(optional);
        when(optional.orElse(null)).thenReturn(user);
        MockedStatic<Lookup> lookupMockedStatic = mockStatic(Lookup.class);
        lookupMockedStatic.when(() -> Lookup.getTenantIdByOrganizationId(anyLong())).thenReturn(null);

        lookupMockedStatic.close();
//        ConcurrentHashMap mock = mock(ConcurrentHashMap.class);
        User byId = userDalJpa.getById(101);
        Assertions.assertEquals(user,byId);

    }

    @Test
    void getUserByUserName(){
        User mockUser = mock(User.class);
        when(userRepository.findByUserName(anyString())).thenReturn(mockUser);
        User user = userDalJpa.getByUserName("getUserByUserName");
        Assertions.assertEquals(mockUser,user);
    }

    @Test
    void setPasswordByIdTest(){
        User user = mock(User.class);
        Optional<User> optional = mock(Optional.class);
        when(userRepository.findById(anyInt())).thenReturn(optional);
        when(optional.orElse(null)).thenReturn(user);
        MockedStatic<Lookup> lookupMockedStatic = mockStatic(Lookup.class);
        lookupMockedStatic.when(() -> Lookup.getTenantIdByOrganizationId(anyLong())).thenReturn(null);
        lookupMockedStatic.close();
        doNothing().when(passwordPolicy).checkNewPassword(any(User.class),anyString());
        assertAll(() -> userDalJpa.setPassword(1,"setPasswordById"));
    }

    @Test
    void setPasswordByUserName(){
        User mockUser = mock(User.class);
        when(userRepository.findByUserName(anyString())).thenReturn(mockUser);
        doNothing().when(passwordPolicy).checkNewPassword(any(User.class),anyString());
        assertAll(() -> userDalJpa.setPassword("userName","setPasswordByName"));
    }

    @Test
    void testEvictSecondLevelCacheById(){
        Integer userId = 123;
        when(em.getEntityManagerFactory()).thenReturn(entityManagerFactory);
        when(entityManagerFactory.getCache()).thenReturn(cache);
        userDalJpa.evictSecondLevelCacheById(userId);
        verify(cache).evict(User.class, userId);
    }

    @Test
    void overrideSHA1PasswordTest(){
        User user = new User();
        user.setUserId(1);
        String password = "newPassword";

        userDalJpa.overrideSHA1Password(user, password);

        Assertions.assertEquals(user.getPasswordSalt().length(),64);
        assertNotNull(user.getPasswordHash());
        assertNotNull(user.getPasswordSetTime());
        verify(passwordHistoryRepository).save(any(PasswordHistory.class));
    }

    @Test
    public void testOverrideSHA1Password_NullUser() {
        String password = "newPassword";
        assertThrows(IllegalArgumentException.class,
                () -> userDalJpa.overrideSHA1Password(null, password));
    }

    @Test
    public void testChangePassword_ValidUser() {
        User user = mock(User.class);
        when(userRepository.findByUserName(anyString())).thenReturn(user);
        when(user.checkPassword(anyString())).thenReturn(true);
        userDalJpa.changePassword("username", "oldPassword", "newPassword");
        Assertions.assertEquals(false,user.getChangePasswordFlag());
    }

    @Test
    public void testChangePassword_InvalidUser() {
        when(userRepository.findByUserName(anyString())).thenReturn(null);
        Assertions.assertThrows(RuntimeException.class,
                () -> userDalJpa.changePassword("username","oldPassword","newPassword"));
    }


    //    Test cases for ChangeIVRPassword ...
    @Test
    public void testChangeIVRPassword_ValidUserAndChange() {
        // Mock setup
        User user = mock(User.class);
        when(userRepository.findByUserName(anyString())).thenReturn(user);
        when(user.checkIVRPin("oldPassword",user.getIvrUserId())).thenReturn(true);
        userDalJpa.changeIVRPassword("username", "oldPassword", "newPassword");
        verify(ivrPasswordHistoryRepository).save(any(IVRPasswordHistory.class));
    }

    @Test
    public void testChangeIVRPassword_InvalidUser() {
        when(userRepository.findByUserName(anyString())).thenReturn(null);
        assertThrows(ErrorInfoException.class,
                () -> userDalJpa.changeIVRPassword("username", "oldPassword", "newPassword"));
    }

    @Test
    public void testChangeIVRPassword_InvalidOldPassword() {
        User user = mock(User.class);
        when(userRepository.findByUserName(anyString())).thenReturn(user);
        when(user.checkIVRPin(anyString(), anyString())).thenReturn(false);
        assertThrows(RuntimeException.class,
                () -> userDalJpa.changeIVRPassword("username", "oldPassword", "newPassword"));
    }

    //    Test cases For the updateConsecutiveIVRLoginFailures
    @Test
    public void updateConsecutiveIVRLoginFailuresTest(){
        User user = mock(User.class);
        when(userRepository.findById(anyInt())).thenReturn(Optional.of(user));
        userDalJpa.updateConsecutiveIVRLoginFailures(121,2);
        verify(userRepository).save(any(User.class));
    }

    @Test
    public void testLockIVRUser_Locked() {
        User user = new User();
        int consecutiveIVRLoginFailures = 3;
        when(configuration.containsKey("rbac.ivrLoginPolicy.failedAttempts")).thenReturn(true);
        when(configuration.getInt("rbac.ivrLoginPolicy.failedAttempts")).thenReturn(3);
        userDalJpa.lockIVRUser(user, consecutiveIVRLoginFailures);
        assertTrue(user.getIsIVRUserLocked());
    }

    @Test
    public void testLockIVRUser_NotLocked() {
        User user = new User();
        int consecutiveIVRLoginFailures = 0;
        when(configuration.containsKey("rbac.ivrLoginPolicy.failedAttempts")).thenReturn(true);
        when(configuration.getInt("rbac.ivrLoginPolicy.failedAttempts")).thenReturn(3);
        userDalJpa.lockIVRUser(user, consecutiveIVRLoginFailures);
        assertFalse(user.getIsIVRUserLocked());
    }


    //    Test cases for the getByIVRUserId
    @Test
    public void getByIVRUserIdValidTest(){
        User user = mock(User.class);
        when(userRepository.getUserByIVRUserId(anyString())).thenReturn(user);
        User userByIVRId = userDalJpa.getByIVRUserId("userByIVRId");
        Assertions.assertEquals(user,userByIVRId);
    }

    @Test
    public void getByIVRUserIdNotValidTest(){
        NoResultException ex = mock(NoResultException.class);
        when(userRepository.getUserByIVRUserId(anyString())).thenThrow(ex);
        userDalJpa.getByIVRUserId("userIvrId");
    }

//    Test cases for checkEntityPermission

    @Test
    public void checkEntityPermissionTestValid(){
        TypedQuery<Number> mockQuery = mock(TypedQuery.class);
        when(em.createQuery(anyString(), eq(Number.class))).thenReturn(mockQuery);
        when(mockQuery.getSingleResult()).thenReturn(1);
        boolean actual = userDalJpa.checkEntityPermission(121, mock(Options.class));
        assertTrue(actual);
    }

    @Test
    public void checkEntityPermissionTestInValid(){
        TypedQuery<Number> mockQuery = mock(TypedQuery.class);
        when(em.createQuery(anyString(), eq(Number.class))).thenReturn(mockQuery);
        when(mockQuery.getSingleResult()).thenReturn(0);
        boolean actual = userDalJpa.checkEntityPermission(121, mock(Options.class));
        assertFalse(actual);
    }

//    Test cases for getByIdentity

    @Test
    public void getByIdentityTestReturnUser(){
        List<User> list = mock(List.class);
        User user = mock(User.class);
        when(userRepository.getUsersByIdentity(anyString(),anyString())).thenReturn(list);
        when(list.size()).thenReturn(1);
        when(list.get(0)).thenReturn(user);
        User actualUser = userDalJpa.getByIdentity("windowUser", "dipak710@gmail.com");
        Assertions.assertEquals(user,actualUser);
    }

    @Test
    public void getByIdentityTestReturnNull(){
        List<User> list = mock(List.class);
        when(userRepository.getUsersByIdentity(anyString(),anyString())).thenReturn(list);
        when(list.size()).thenReturn(3);
        User actualUser = userDalJpa.getByIdentity("windowUser", "dipak710@gmail.com");
        assertNull(actualUser);
    }


//    Test cases for deleteByUserName

    @Test
    public void deleteByUserName(){
        String userName = "username";
        Integer loggedInUserId = 1;
        String clientIp = "1.2.3.4";
        when(deploymentUtil.isLogoutUserOnDeletion()).thenReturn(false);

        // Call the method to be tested
        List<SSOLogoutData> result = userDalJpa.deleteByUserName(userName, loggedInUserId, clientIp);

        // Verify the expected interactions and results
        verify(cacheService).clearCache(CacheConfig.CLEAR_ALL_CACHE);
        verify(userRepository).deleteUserByUserName(userName);
        verify(contactDispatcherUtil).updateContactsForUserDeletion(anyInt(), anyInt());

        assertNull(result);
    }

    @Test
    public void testSessionRegistryLogout() {
        // Mock setup

        String userName = "username";
        Integer loggedInUserId = 1;
        String clientIp = "1.2.3.4";
        when(deploymentUtil.isLogoutUserOnDeletion()).thenReturn(true);

        SessionRegistryLogoutRequest request = new SessionRegistryLogoutRequest();
        request.setUserName("username");
        request.setLogoutType("logoutType");
        request.setLogoutAction(RBACUtil.LOGOUT_ACTION.LOGOUT_ALL);
        request.setRequestId("requestId");
        request.setClientIp("1.2.3.4");

        List<SSOLogoutData> ssoList = mock(List.class);
        LogoutResponse logoutResponse1 = mock(LogoutResponse.class);
        when(loginService.sessionRegistryLogout(any(), any(), any())).thenReturn(logoutResponse1);
        when(logoutResponse1.getSsoLogoutDataList()).thenReturn(ssoList);
        List<SSOLogoutData> result = userDalJpa.deleteByUserName(userName, loggedInUserId, clientIp);
        assertEquals(ssoList, result);
    }

    // Test cases for getAllUserNames
    @Test
    public void testGetAllUserNames(){
        List<String> listOfusers = mock(List.class);
        when(userRepository.getAllUserNames()).thenReturn(listOfusers);
        List<String> allUserNames = userDalJpa.getAllUserNames();
        Assertions.assertEquals(listOfusers,allUserNames);
    }



    // Test cases for getList
    @Test
    public void testGetList(){
        Options options = mock(Options.class);
        List<User> list = Collections.singletonList(new User());
        TypedQuery<User> typedQueryMock = Mockito.mock(TypedQuery.class);
        Mockito.when(em.createQuery(Mockito.anyString(), Mockito.eq(User.class))).thenReturn(typedQueryMock);
        Mockito.when(typedQueryMock.getResultList()).thenReturn(list);
        List<User> list1 = userDalJpa.getList(options);
        Assertions.assertNotNull(list1);
    }

    // Test cases for getListForDispatch
    @Test
    public void testgetListForDispatch(){
        Options options = mock(Options.class);
        List<User> userList = Collections.singletonList(new User());
        TypedQuery<User> typedQueryMock = Mockito.mock(TypedQuery.class);
        Mockito.when(em.createQuery(Mockito.anyString(), Mockito.eq(User.class))).thenReturn(typedQueryMock);
        Mockito.when(typedQueryMock.getResultList()).thenReturn(userList);
        Map<String, String> additionalSortMap = Mockito.mock(Map.class);
        Map<String, String> SORT_COLUMNS = new TreeMap<String, String>();
        SORT_COLUMNS.put("isEnabled", "u.isEnabled");
        SORT_COLUMNS.put("userName", "u.userName");
        additionalSortMap.putAll(SORT_COLUMNS);
        when(deploymentUtil.getSortOrderForDispatch()).thenReturn("asd");
        additionalSortMap.put("variableValue", deploymentUtil.getSortOrderForDispatch());
        when(deploymentUtil.getQueryForDispatch()).thenReturn(" select u from User u left join Variable v on (v.userId = u.userId and v.variableName = :variableName)");
        List<User> listForDispatch = userDalJpa.getListForDispatch(options);
        Assertions.assertEquals(userList,listForDispatch);

    }


    //    Test case For getCount
    @Test
    public void testGetCount(){
        Options options = mock(Options.class);
        TypedQuery<Number> typedQueryMock = Mockito.mock(TypedQuery.class);
        Mockito.when(em.createQuery(Mockito.anyString(), Mockito.eq(Number.class))).thenReturn(typedQueryMock);
        Mockito.when(typedQueryMock.getSingleResult()).thenReturn(10L);
        int count = userDalJpa.getCount(options);
        Assertions.assertEquals(10,count);

    }

    //    Test case For getUserPermissions
    @Test
    public void testGetUserPermissions() {
        String userName = "testUser";
        String applicationName = "testApp";

        Object[] permissionData1 = {"permission1", "module1"};
        Object[] permissionData2 = {"permission2", "module2"};

        List<Object[]> expectedPermissions = Arrays.asList(permissionData1, permissionData2);
        when(userRepository.getUserPermissions(userName, applicationName))
                .thenReturn(expectedPermissions);
        List<String> result = userDalJpa.getUserPermissions(userName, applicationName);
        assertEquals(expectedPermissions.size(), result.size());
    }

    //   Test case For getUserTenantScope
    @Test
    public void testGetUserTenantScope(){
        String userName = "testUser";
        Mockito.when(userRepository.getUserTenantScope(userName, RBACUtil.SCOPE_KEY_TENANT))
                .thenReturn(Collections.emptyList());
        String result = userDalJpa.getUserTenantScope(userName);
        assertNull(result);
    }

    //    Test case for getUserScopes
    @Test
    public void testGetUserScopes(){
        String userName = "testUser";
        String applicationName = "testApp";
        List<Object[]> mockList = new ArrayList<>();
        when(userRepository.getUserScopes(userName, applicationName)).thenReturn(mockList);
        Map<String, String> result = userDalJpa.getUserScopes(userName, applicationName, false);
        assertNotNull(result);
    }

    @Test
    public void testReplaceRuntimeVariables() {
        User mockUser = new User();
        mockUser.setUserId(1);
        mockUser.setOrganizationId(10L);
        Group mockGroup = new Group();
        mockGroup.setGroupId(100);
        AttributesData mockAttributeData = new AttributesData();
        mockAttributeData.setAttributeId(1);
        mockAttributeData.setAttributeDataValue("AttributeValue");
        Set<AttributesData> attributeDataList = new HashSet<>();
        attributeDataList.add(mockAttributeData);
        mockUser.setAttributesData(attributeDataList);
        GroupRepository groupRepository = mock(GroupRepository.class);
        String scope = "$currentuser.AttributeName$ and $currentgroup.AttributeName$";
        TypedQuery<Variable> mockTypedQuery = mock(TypedQuery.class);
        List<Variable> mockVariables = new ArrayList<>();
        Variable mockVariable = new Variable();
        mockVariable.setVariableName("example");
        mockVariable.setVariableValue("replacement");
        mockVariables.add(mockVariable);
        when(em.createQuery(anyString(), eq(Variable.class))).thenReturn(mockTypedQuery);
        when(mockTypedQuery.setParameter(eq("userId"), anyInt())).thenReturn(mockTypedQuery);
        when(mockTypedQuery.getResultList()).thenReturn(mockVariables);
        scope = userDalJpa.replaceRuntimeVariables(scope, mockUser, mockGroup);
        assertNotNull(scope);
    }

    //    Test case for getUserInListScopesDetails

    @Test
    public void testGetUserInListScopesDetails() {
        List<Object[]> mockResultList = new ArrayList<>();
        mockResultList.add(new Object[]{"scope1", 1});
        mockResultList.add(new Object[]{"scope1", 2});
        mockResultList.add(new Object[]{"scope2", 3});
        when(userRepository.getUserInListScopesDetails(anyString(), anyString())).thenReturn(mockResultList);
        Map<String, List<Integer>> result = userDalJpa.getUserInListScopesDetails("username", "appname");
        Map<String, List<Integer>> expectedMap = new TreeMap<>();
        expectedMap.put("scope1", Arrays.asList(1, 2));
        expectedMap.put("scope2", Arrays.asList(3));
        assertEquals(expectedMap, result);
        verify(userRepository).getUserInListScopesDetails("username", "appname");
    }


    //    Test case for getUserRoles
    @Test
    public void testGetUserRoles(){
        List<String> mockRoles = Arrays.asList("role1", "role2");
        when(userRepository.getUserRoles(anyString(), anyString())).thenReturn(mockRoles);
        List<String> result = userDalJpa.getUserRoles("username", "appname");
        assertEquals(mockRoles, result);
        verify(userRepository).getUserRoles("username", "appname");
    }

    //    Test case for getPasswordHistory
    @Test
    public void getPasswordHistory1(){
        List<PasswordHistory> mockHistory = new ArrayList<>();
        PasswordHistory history1 = new PasswordHistory();
        PasswordHistory history2 = new PasswordHistory();
        mockHistory.add(history1);
        mockHistory.add(history2);

        Page<PasswordHistory> mockPage = new PageImpl<>(mockHistory);
        when(passwordHistoryRepository.getPasswordHistoryByUserName(anyString(), any(PageRequest.class))).thenReturn(mockPage);
        List<PasswordHistory> result = userDalJpa.getPasswordHistory("username", 5);
        assertEquals(mockHistory, result);
        verify(passwordHistoryRepository).getPasswordHistoryByUserName("username", PageRequest.of(0, 5));
    }

    @Test
    public void testGetPasswordHistory2(){
        List<PasswordHistory> mockHistory = new ArrayList<>();
        PasswordHistory history1 = new PasswordHistory();
        PasswordHistory history2 = new PasswordHistory();
        mockHistory.add(history1);
        mockHistory.add(history2);
        Page<PasswordHistory> mockPage = new PageImpl<>(mockHistory);
        when(passwordHistoryRepository.getPasswordHistoryByUserId(anyInt(), any(PageRequest.class))).thenReturn(mockPage);
        List<PasswordHistory> result = userDalJpa.getPasswordHistory(123, 5);
        assertEquals(mockHistory, result);
        verify(passwordHistoryRepository).getPasswordHistoryByUserId(123, PageRequest.of(0, 5));
    }

    //    Test : : getIVRPasswordHistory
    @Test
    public void testgetIVRPasswordHistory(){
        List<IVRPasswordHistory> mockHistory = new ArrayList<>();
        IVRPasswordHistory history1 = new IVRPasswordHistory();
        IVRPasswordHistory history2 = new IVRPasswordHistory();
        mockHistory.add(history1);
        mockHistory.add(history2);
        Page<IVRPasswordHistory> mockPage = new PageImpl<>(mockHistory);
        when(ivrPasswordHistoryRepository.getIVRPasswordHistoryByUserId(anyInt(), any(PageRequest.class))).thenReturn(mockPage);
        List<IVRPasswordHistory> result = userDalJpa.getIVRPasswordHistory(123, 5);
        assertEquals(mockHistory, result);
        verify(ivrPasswordHistoryRepository).getIVRPasswordHistoryByUserId(123, PageRequest.of(0, 5));
    }

    //    Test : : getUserTargetOperations
    @Test
    public void testgetUserTargetOperations(){
        List<Object[]> mockList = new ArrayList<>();
        Object[] entry1 = {"target1", "operation1"};
        Object[] entry2 = {"target1", "operation2"};
        Object[] entry3 = {"target2", "operation1"};
        mockList.add(entry1);
        mockList.add(entry2);
        mockList.add(entry3);
        when(userRepository.getUserTargetOperations(anyString(), anyString())).thenReturn(mockList);
        Map<String, List<String>> result = userDalJpa.getUserTargetOperations("username", "appname");
        Map<String, List<String>> expectedMap = new TreeMap<>();
        expectedMap.put("target1", Arrays.asList("operation1", "operation2"));
        expectedMap.put("target2", Arrays.asList("operation1"));
        assertEquals(expectedMap, result);
        verify(userRepository).getUserTargetOperations("username", "appname");
    }

    //    Test : : getUserVariables
    @Test
    public void testgetUserVariables(){
        List<Object[]> mockList = new ArrayList<>();
        Object[] entry1 = {"app1", "user1", "value1", null};
        Object[] entry2 = {"app1", "user1", "value2", null};
        Object[] entry3 = {"app1", "user2", "value3", null};
        Object[] entry4 = {"app2", "user2", "value4", "app2"};
        mockList.add(entry1);
        mockList.add(entry2);
        mockList.add(entry3);
        mockList.add(entry4);
        when(variableRepository.getUserVariables(anyString(), anyString())).thenReturn(mockList);
        Map<String, Map<String, String>> result = userDalJpa.getUserVariables("username", "appname");
        Map<String, Map<String, String>> expectedMap = new TreeMap<>();
        for (Object[] pair : mockList) {
            String appName = pair[0].toString();
            String userName = pair[1].toString();
            String variableValue = pair[2] != null ? pair[2].toString() : null;
            String appId = pair[3] != null ? pair[3].toString() : null;

            if (!expectedMap.containsKey(appName)) {
                expectedMap.put(appName, new HashMap<>());
            }

            if (appId == null || !expectedMap.get(appName).containsKey(userName)) {
                expectedMap.get(appName).put(userName, variableValue);
            }
        }
        verify(variableRepository).getUserVariables("appname", "username");
    }


    //    Test : : getUserAttributes
    @Test
    public void testgetUserAttributes(){
        List<Object[]> mockList = new ArrayList<>();
        Object[] entry1 = {"user1", "name1", "id1", "value1"};
        Object[] entry2 = {"user1", "name2", null, "value2"};
        Object[] entry3 = {"user2", "name3", "id3", null};
        mockList.add(entry1);
        mockList.add(entry2);
        mockList.add(entry3);


        when(userRepository.getUserAttributes(anyString())).thenReturn(mockList);
        Map<String, List<Map<String, String>>> result = userDalJpa.getUserAttributes("username");

        Map<String, List<Map<String, String>>> expectedMap = new TreeMap<>();

        for (Object[] pair : mockList) {
            String userName = pair[0].toString();
            String attributeName = pair[1].toString();
            String attributeId = pair[2] != null ? pair[2].toString() : null;
            String attributeValue = pair[3] != null ? pair[3].toString() : null;

            if (!expectedMap.containsKey(userName)) {
                expectedMap.put(userName, new LinkedList<>());
            }

            Map<String, String> attributeMap = new LinkedHashMap<>();
            attributeMap.put("name", attributeName);
            attributeMap.put("id", attributeId);
            attributeMap.put("value", attributeValue);
            expectedMap.get(userName).add(attributeMap);
        }

        assertEquals(expectedMap, result);
        verify(userRepository).getUserAttributes("username");
    }

    //    Test : : isUserNameDuplicate
    @Test
    public void testisUserNameDuplicate(){
        String existingUserName = "existingUser";
        User existingUser = new User();
        existingUser.setUserId(1);
        existingUser.setOrganizationId(11L);
        existingUser.setUserName(existingUserName);

        when(userRepository.findByUserName(existingUserName)).thenReturn(existingUser);
        Organization organization = mock(Organization.class);
        OrganizationRepository organizationRepository = mock(OrganizationRepository.class);
//        when(organizationRepository.findById(anyLong())).thenReturn(Optional.of(organization));
        when(userRepository.findByUserName("nonExistingUser")).thenReturn(null);

        // Test case 1: Duplicate user name with same user ID
        int result1 = userDalJpa.isUserNameDuplicate(1, existingUserName);
        assertEquals(0, result1);

        // Test case 2: Duplicate user name with different user ID
        int result2 = userDalJpa.isUserNameDuplicate(2, existingUserName);
        assertEquals(1, result2);

        // Test case 3: Non-duplicate user name
        int result3 = userDalJpa.isUserNameDuplicate(0, "nonExistingUser");
        assertEquals(0, result3);

        // Test case 4: Exception case
        when(userRepository.findByUserName("exceptionUser")).thenThrow(new RuntimeException("Something went wrong"));
        int result4 = userDalJpa.isUserNameDuplicate(0, "exceptionUser");
        assertEquals(-1, result4);
    }

    //    Test : : isIvrUserIdDuplicate
    @Test
    public void testisIvrUserIdDuplicate(){
        String existingIvrUserId = "existingIvrUser";
        User existingUser = new User();
        existingUser.setUserId(1);

        when(userRepository.getUserByIVRUserId(existingIvrUserId)).thenReturn(existingUser);
        when(userRepository.getUserByIVRUserId("nonExistingIvrUser")).thenThrow(NoResultException.class);

        int result1 = userDalJpa.isIvrUserIdDuplicate(1, existingIvrUserId);
        assertEquals(0, result1);

        int result2 = userDalJpa.isIvrUserIdDuplicate(2, existingIvrUserId);
        assertEquals(1, result2);

        int result3 = userDalJpa.isIvrUserIdDuplicate(0, "nonExistingIvrUser");
        assertEquals(0, result3);
    }


    //    Test : : getUsersNotAssignToGroup
    @Test
    public void testgetUsersNotAssignToGroup(){
        TypedQuery<Object[]> mockTypedQuery = Mockito.mock(TypedQuery.class);
        Filters mockFilters = Mockito.mock(Filters.class);
        Options mockOptions = Mockito.mock(Options.class);
        Map<String, String> SORT_COLUMNS = new TreeMap<String, String>();
        SORT_COLUMNS.put("isEnabled", "u.isEnabled");
        SORT_COLUMNS.put("userName", "u.userName");
        when(em.createQuery(anyString(), eq(Object[].class))).thenReturn(mockTypedQuery);
        when(mockTypedQuery.getResultList()).thenReturn(new ArrayList<>());
        userDalJpa.getUsersNotAssignToGroup(mockOptions);
        verify(em, times(1)).createQuery(anyString(), eq(Object[].class));
    }

    //    TEst : : getUsersOfAnotherGroup
    @Test
    public void testgetUsersOfAnotherGroup(){
        TypedQuery<Object[]> mockTypedQuery = Mockito.mock(TypedQuery.class);
        Options mockOptions = Mockito.mock(Options.class);
        when(em.createQuery(anyString(), eq(Object[].class))).thenReturn(mockTypedQuery);
        when(em.createQuery(anyString(), eq(Object[].class))).thenReturn(mockTypedQuery);
        when(mockTypedQuery.setParameter(anyString(), any())).thenReturn(mockTypedQuery);
        when(mockTypedQuery.setFirstResult(anyInt())).thenReturn(mockTypedQuery);
        when(mockTypedQuery.setMaxResults(anyInt())).thenReturn(mockTypedQuery);
        when(mockTypedQuery.getResultList()).thenReturn(Collections.emptyList());
        List<User> usersOfAnotherGroup = userDalJpa.getUsersOfAnotherGroup(mockOptions);
        Assertions.assertEquals(Collections.emptyList(),usersOfAnotherGroup);
    }

    //    Test : : getSearchCount
    @Test
    public void testgetSearchCount(){
        Options options = mock(Options.class);
        List<User> expectedUsers = Collections.singletonList(new User());
        searchMok.when(() -> SearchUtils.getSearchParam(eq(options),any())).thenReturn("asdasda");
        TypedQuery<Number> mockQuery = mock(TypedQuery.class);
        when(em.createQuery(anyString(), eq(Number.class))).thenReturn(mockQuery);
        when(mockQuery.setParameter(anyString(), any())).thenReturn(mockQuery);
        when(mockQuery.getSingleResult()).thenReturn(expectedUsers.size());
        int searchCount = userDalJpa.getSearchCount(options);
        Assertions.assertEquals(1,searchCount);
    }

    //    Test : : isUserIdentityAssociationValid
    @Test
    public void testisUserIdentityAssociationValid(){
        List<UserIdentity> identityList = mock(List.class);
        when(identityList.isEmpty()).thenReturn(true);
        boolean userIdentityAssociationValid = userDalJpa.isUserIdentityAssociationValid(identityList, 122);
        assertTrue(userIdentityAssociationValid);
        boolean userIdentityAssociationValid1 = userDalJpa.isUserIdentityAssociationValid(identityList, 122);
        assertTrue(userIdentityAssociationValid1);


    }


    //    Test Cases For : : isUserAuthorizedForApp
    @Test
    public void testisUserAuthorizedForApp(){
        when(deploymentUtil.getShowAppDashboardToAllUsers()).thenReturn(true);
        when(deploymentUtil.getAppDashboardApplicationId()).thenReturn(1);
        Application mockApp = new Application();
        mockApp.setName("TestApp");
        mockApp.setChildApplications(new HashSet<>());
        when(applicationDal.getById(anyInt())).thenReturn(mockApp);
        when(userRepository.isUserAuthorizedForApp(anyString(), anyString())).thenReturn(1);
        when(childAppPermValidatorUtil.validate(anyString(), anyString())).thenReturn(true);
        boolean isAuthorized = userDalJpa.isUserAuthorizedForApp("testUser", "TestApp", "appKey");
        assertTrue(isAuthorized);
    }

    //    Test Cases For : : getUserIdNames
    @Test
    public void testgetUserIdNames(){
        String queryText = "SELECT obj FROM Object obj";
        Options options = mock(Options.class);
        List<Object[]> actualList = new ArrayList<>();
        TypedQuery<Object[]> mockTypedQuery = mock(TypedQuery.class);
        when(em.createQuery(anyString(), eq(Object[].class))).thenReturn(mockTypedQuery);
        when(mockTypedQuery.getResultList()).thenReturn(actualList);
        List<Map<String,Object>> result = userDalJpa.getUserIdNames(options);
        assertTrue(result.isEmpty());
    }


    //    Test Cases For : : getUserIdNamesWithScope
    @Test
    public void testgetUserIdNamesWithScope(){
        Options options = mock(Options.class);
        List<Object[]> mockResult = new LinkedList<>();
        Object[] row1 = { 1L, "user1", 1L, 101L };
        Object[] row2 = { 2L, "user2", 2L, 102L };
        mockResult.add(row1);
        mockResult.add(row2);
        TypedQuery<Object[]> mockTypedQuery = mock(TypedQuery.class);
        when(em.createQuery(anyString(), eq(Object[].class))).thenReturn(mockTypedQuery);
        when(mockTypedQuery.getResultList()).thenReturn(mockResult);
        List<Map<String, Object>> result = userDalJpa.getUserIdNamesWithScope(options);
        assertFalse(result.isEmpty());
    }

    //    Test Cases For : : replaceRuntimeVariables
    @Test
    public void testreplaceRuntimeVariables(){
        User user = new User();
        user.setAttributesData(new HashSet<>());
        user.setUserId(100);
        Group group = new Group();
        group.setAttributesData(new HashSet<>());
        group.setGroupId(456);
        TypedQuery<Variable> mockQueryUserVariables = mock(TypedQuery.class);
        when(em.createQuery(anyString(), eq(Variable.class))).thenReturn(mockQueryUserVariables);
        when(mockQueryUserVariables.setParameter(eq("userId"), eq(user.getUserId()))).thenReturn(mockQueryUserVariables);
        String inputScope = "Hello $currentuser.attr1$ and $currentgroup.attr2$ and $currentuser.organization.organizationName$";
        String outputScope = userDalJpa.replaceRuntimeVariables(inputScope, user, group);
        Assertions.assertEquals(inputScope,outputScope);
    }

    //    Test Cases For : : getCustomUserInfo
    @Test
    public void testgetCustomUserInfo(){
        TypedQuery<Object[]> mockQuery = mock(TypedQuery.class);
        List<Object[]> mockResult = new ArrayList<>();
        // Add mock data to mockResult
        when(em.createQuery(anyString(), eq(Object[].class))).thenReturn(mockQuery);
        when(mockQuery.getResultList()).thenReturn(mockResult);
        Options options = new Options();
        List<Map<String, Object>> result = userDalJpa.getCustomUserInfo(options);
        assertTrue(result.isEmpty());
    }

    //    Test Cases For : : searchCustomUserInfo
    @Test
    public void testsearchCustomUserInfo(){
        TypedQuery<Object[]> mockQuery = mock(TypedQuery.class);
        List<Object[]> mockResult = new ArrayList<>();
        Options options = mock(Options.class);
        mockResult.add(new Object[]{1, true, "user1", "user1@example.com", new DateTime(), 1}); // groupId is present
        mockResult.add(new Object[]{2, true, "user2", "user2@example.com", null, null});       // groupId is null
        when(em.createQuery(anyString(), eq(Object[].class))).thenReturn(mockQuery);
        when(mockQuery.setParameter(anyString(), any())).thenReturn(mockQuery);
        when(mockQuery.getResultList()).thenReturn(mockResult);
        searchMok.when(() -> SearchUtils.getSearchParam(eq(options),any())).thenReturn("asdasda");
        List<Map<String, Object>> result = userDalJpa.searchCustomUserInfo(options);
        verify(em, times(1)).createQuery(anyString(), eq(Object[].class));
        verify(mockQuery, times(1)).setParameter(anyString(), any());
        assertEquals(1, result.get(0).get("groupId")); // Ensure groupId is set for the first result
        assertNull(result.get(1).get("groupId"));
    }


    //    Test Cases For : : checkTenantIdInOrgAndGroup
    @Test
    public void testcheckTenantIdInOrgAndGroup(){
        Integer mockResult = 1;
        when(tenantRepository.checkTenantIdInOrgAndGroup(anyLong(), anyLong())).thenReturn(mockResult);
        long organizationId = 1;
        long groupId = 2;
        boolean result = userDalJpa.checkTenantIdInOrgAndGroup(organizationId, groupId);
        assertTrue(result); // Expecting result to be true
        verify(tenantRepository, times(1)).checkTenantIdInOrgAndGroup(eq(organizationId), eq(groupId));
    }

    //    Test Cases For : : isUserAssociatedinDispatchContact
    @Test
    public void testisUserAssociatedinDispatchContact(){
        TypedQuery<Long> query = mock(TypedQuery.class);
        when(query.getSingleResult()).thenReturn(1L);
        when(em.createNamedQuery(eq("isUserAssociatedinDispatchContact"), eq(Long.class)))
                .thenReturn(query);
        Integer userId = 123;
        boolean result = userDalJpa.isUserAssociatedinDispatchContact(userId);
        assertTrue(result); // Expecting result to be true
        verify(query, times(1)).setParameter("userId", userId);
        verify(query, times(1)).getSingleResult();
    }

    //    Test Cases For : : updateAllUsersForOrganization
    @Test
    public void testupdateAllUsersForOrganization(){
        when(userRepository.updateAllUsersForOrganization(anyBoolean(), anyInt(), any(Date.class), anyLong()))
                .thenReturn(1);
        Date mockDate = new Date(); // Replace with a specific date
        doNothing().when(cacheService).clearCache(CacheConfig.CLEAR_ALL_USER_CACHE);
        Long organizationId = 123L;
        Boolean isShared = true;
        int userId = 456;
        int result = userDalJpa.updateAllUsersForOrganization(organizationId, isShared, userId);
        assertEquals(1, result); // Expecting result to be 1
        verify(cacheService, times(1)).clearCache(CacheConfig.CLEAR_ALL_USER_CACHE);
    }

    //    Test Cases For : : deleteUserOrgCalendarMapping
    @Test
    public void testdeleteUserOrgCalendarMapping(){
        Long calendarId = 123L;
        EntityManagerFactory emf = mock(EntityManagerFactory.class);
        Cache cachemock = mock(Cache.class);
        // Set up your mock UserRepository behavior
        List<Integer> userIdList = Arrays.asList(1, 2, 3); // Example user IDs
        when(userRepository.getUserIdsForOrgCalendarDeletion(calendarId))
                .thenReturn(userIdList);
        when(em.getEntityManagerFactory()).thenReturn(emf);
        when(emf.getCache()).thenReturn(cachemock);
        // Test the method
        userDalJpa.deleteUserOrgCalendarMapping(calendarId);
        // Verify interactions with mocks
        verify(userRepository, times(1)).getUserIdsForOrgCalendarDeletion(calendarId);
        verify(userRepository, times(1)).removeUserOrgCalendarMapping(userIdList);
        verify(cachemock, times(3)).evict(eq(User.class), anyInt());
    }

    //    Test Cases For : : searchGlobalCustomUserInfo
    @Test
    public void testsearchGlobalCustomUserInfo(){
        String searchText = "search text";
        String tenantList = "1,2,3";
        int userId = 123;
        Options options = mock(Options.class);

        List<Object[]> mockResult = new ArrayList<>();
        Object[] mockRow = new Object[7];
        mockRow[0] = 1; // userId
        mockRow[1] = true; // isEnabled
        mockRow[2] = "John Doe"; // userName
        mockRow[3] = "john@example.com"; // emailAddress
        mockRow[4] = new DateTime().toDate(); // loginTime
        mockRow[5] = 123; // groupId
        mockRow[6] = "Tenant A"; // tenantName
        mockResult.add(mockRow);
        TypedQuery<Object[]> typedQueryMock = mock(TypedQuery.class);
        when(em.createQuery(anyString(), eq(Object[].class))).thenReturn(typedQueryMock);
        when(typedQueryMock.getResultList()).thenReturn(mockResult);
        List<Map<String, Object>> result = userDalJpa.searchGlobalCustomUserInfo(searchText, tenantList, userId, options);
        verify(em, times(2)).createQuery(anyString(), any());
        verify(em, atLeastOnce()).createQuery(anyString(), eq(Object[].class));
        assertNotNull(result);
    }


    //    Test Cases For : : searchGlobalCustomUserInfoCount
    @Test
    public void testsearchGlobalCustomUserInfoCount(){
        Integer userId = 123;
        String searchText = "search text";
        String tenantList = "1,2,3";

        List<Integer> tenantsId = Arrays.asList(1, 2, 3);
        List<Object[]> mockResult = new ArrayList<>();
        Object[] row1 = {1, true, "user1", "email1@example.com", new Date(), 2}; // Modify this row as needed
        // ... add more rows if necessary
        mockResult.add(row1);

        TypedQuery<Object[]> queryMock = mock(TypedQuery.class);

        when(em.createQuery(anyString(), eq(Object[].class))).thenReturn(queryMock);
        when(queryMock.setParameter(anyString(), any())).thenReturn(queryMock);
        when(queryMock.getResultList()).thenReturn(mockResult);

        // Call the method
        List<Map<String, Object>> result = userDalJpa.searchGlobalCustomUserInfoCount(searchText, tenantList, userId);

        assertNotNull(result);
        assertEquals(1, result.size());
        Map<String, Object> resultMap = result.get(0);
        assertNotNull(resultMap);
        assertEquals(1, resultMap.get("Count"));

        verify(em, times(1)).createQuery(anyString(), eq(Object[].class));
    }

    //    Test Cases For : : noOfPasswordChanged
    @Test
    public void testnoOfPasswordChanged(){
        Integer userId = 123;
        Integer hour = 24;
        Integer mockResult = 5;
        when(userRepository.noOfPasswordChanged(userId, hour)).thenReturn(mockResult);
        Integer result = userDalJpa.noOfPasswordChanged(userId, hour);
        verify(userRepository, times(1)).noOfPasswordChanged(userId, hour);
        assertNotNull(result);
        assertEquals(mockResult, result);
    }

    //    Test Cases For : : getUserByMakerCheckerId
    @Test
    public void testgetUserByMakerCheckerId(){
        Long makerCheckerId = 123L;
        User mockUser = new User();
        when(userRepository.getUserMakerCheckerId(makerCheckerId)).thenReturn(mockUser);
        User result = userDalJpa.getUserByMakerCheckerId(makerCheckerId);
        verify(userRepository, times(1)).getUserMakerCheckerId(makerCheckerId);
        assertNotNull(result);
        assertEquals(mockUser, result);
    }

    @Test
    public void testGetUserByMakerCheckerIdNoResult() {
        Long makerCheckerId = 456L;
        when(userRepository.getUserMakerCheckerId(makerCheckerId)).thenThrow(NoResultException.class);
        User result = userDalJpa.getUserByMakerCheckerId(makerCheckerId);
        verify(userRepository, times(1)).getUserMakerCheckerId(makerCheckerId);
        assertNull(result);
    }


    //    Test Cases For : : getUsersByIdIn
    @Test
    public void testgetUsersByIdIn(){
        List<Integer> userIds = Arrays.asList(1, 2, 3);
        List<User> mockUsers = new ArrayList<>();
        when(userRepository.getUsersByIdIn(userIds)).thenReturn(mockUsers);
        List<User> result = userDalJpa.getUsersByIdIn(userIds);
        verify(userRepository, times(1)).getUsersByIdIn(userIds);
        assertNotNull(result);
        assertEquals(mockUsers, result);
    }

    //    Test Cases For : : getByUserNameLike
    @Test
    public void testgetByUserNameLike(){
        String userName = "exampleUsername";
        User mockUser = new User();
        when(userRepository.getUserByUserNameLike(userName)).thenReturn(mockUser);
        User result = userDalJpa.getByUserNameLike(userName);
        verify(userRepository, times(1)).getUserByUserNameLike(userName);
        assertNotNull(result);
        assertEquals(mockUser, result);
    }

    //    Test Cases For : : checkTwoFactorActiveForUserAndTenant
    @Test
    public void testcheckTwoFactorActiveForUserAndTenant(){
        Long tenantId = 123L;
        MockedStatic<Lookup> lookupMockedStatic = mockStatic(Lookup.class);
        lookupMockedStatic.when(()->Lookup.checkTwoFactorAuthEnabledInTenant(tenantId)).thenReturn(true);
        when(deploymentUtil.isEnableTwoFactorAuth()).thenReturn(true);
        Boolean result = userDalJpa.checkTwoFactorActiveForUserAndTenant(tenantId);
        verify(deploymentUtil, times(1)).isEnableTwoFactorAuth();
        assertTrue(result);
        lookupMockedStatic.close();
    }


    //    Test Cases For : : getByEmailAddress
    @Test
    public void testgetByEmailAddress(){
        String emailAddress = "test@example.com";
        User expectedUser = new User();
        when(userRepository.getByEmailAddress(emailAddress)).thenReturn(expectedUser);
        User result = userDalJpa.getByEmailAddress(emailAddress);
        verify(userRepository, times(1)).getByEmailAddress(emailAddress);
        assertSame(expectedUser, result);
    }
    @Test
    public void testGetByEmailAddress_NoResult() {
        String emailAddress = "test@example.com";
        when(userRepository.getByEmailAddress(emailAddress)).thenThrow(NoResultException.class);
        User result = userDalJpa.getByEmailAddress(emailAddress);
        verify(userRepository, times(1)).getByEmailAddress(emailAddress);
        assertNull(result);
    }

    //    Test Cases For : : isUserEmailIdDuplicate
    @Test
    public void testisUserEmailIdDuplicate(){
        String emailAddress = "test@example.com";
        String userName = "testuser";
        List<User> users = new ArrayList<>();
        User user = new User();
        users.add(user);
        when(userRepository.getUserByEmailAddressAndUserName(emailAddress, userName)).thenReturn(users);
        int result = userDalJpa.isUserEmailIdDuplicate(emailAddress, userName);
        verify(userRepository, times(1)).getUserByEmailAddressAndUserName(emailAddress, userName);
        assertEquals(1, result);
    }

    @Test
    public void testIsUserEmailIdDuplicate_NoDuplicate() {
        String emailAddress = "test@example.com";
        String userName = "testuser";
        when(userRepository.getUserByEmailAddressAndUserName(emailAddress, userName)).thenThrow(NoResultException.class);
        int result = userDalJpa.isUserEmailIdDuplicate(emailAddress, userName);
        verify(userRepository, times(1)).getUserByEmailAddressAndUserName(emailAddress, userName);
        assertEquals(0,result);
    }

    //    Test Cases For : : getByEmailAddress
    @Test
    public void testgetByEmailAddressUsername(){
        String emailAddress = "test@example.com";
        String userName = "testuser";
        List<User> users = new ArrayList<>();
        User user = new User();
        users.add(user);
        when(userRepository.getUserByEmailAddressAndUserName(emailAddress, userName)).thenReturn(users);
        List<User> result = userDalJpa.getByEmailAddress(emailAddress, userName);
        verify(userRepository, times(1)).getUserByEmailAddressAndUserName(emailAddress, userName);
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(user, result.get(0));
    }

    @Test
    public void testGetByEmailAddress_NoResult_userName() {
        String emailAddress = "test@example.com";
        String userName = "testuser";
        when(userRepository.getUserByEmailAddressAndUserName(emailAddress, userName)).thenThrow(NoResultException.class);
        List<User> result = userDalJpa.getByEmailAddress(emailAddress, userName);
        verify(userRepository, times(1)).getUserByEmailAddressAndUserName(emailAddress, userName);
        assertNull(result);
    }


    //    Test Cases For : : isAzureUserMgmtEnabled
    @Test
    public void testisAzureUserMgmtEnabled(){
        Boolean expectedValue = true;
        when(azureManagementConfig.isAzureUserMgmtEnabled()).thenReturn(expectedValue);
        Boolean result = userDalJpa.isAzureUserMgmtEnabled();
        verify(azureManagementConfig, times(1)).isAzureUserMgmtEnabled();
        assertNotNull(result);
        assertEquals(expectedValue, result);
    }

    //    Test Cases For : : isRevokedApplicationsForUserName
    @Test
    public void testisRevokedApplicationsForUserName(){
        String username = "testUser";
        ChildApplication childApplication = new ChildApplication();
        childApplication.setChildApplicationId(1);

        List<Object[]> userScopesList = new ArrayList<>();
//        Object[] scopeData = new Object[3];
//        scopeData[0] = "scopeKey";
//        scopeData[1] = "scopeValue";
//        scopeData[2] = "scopeValue2";
//        userScopesList.add(scopeData);
//        Group mockGroup = new Group();
//        mockGroup.setGroupId(123);
//        when(groupDal.getById(anyInt())).thenReturn(mockGroup);
        when(userRepository.getUserScopes(anyString(), anyString())).thenReturn(userScopesList);
//        when(applicationDal.getRevokedChildApplicationIds(any(Options.class))).thenReturn(Arrays.asList(1, 2));
        boolean result = userDalJpa.isRevokedApplicationsForUserName(username, childApplication);
//        verify(applicationDal, times(1)).getRevokedChildApplicationIds(any(Options.class));
//        verify(groupDal, times(1)).getById(anyInt());
        assertFalse(result);
    }

    //    Test Cases For : : getChannelTypesForTwoFactorAuth
    @Test
    public void testgetChannelTypesForTwoFactorAuth(){
        User mockUser = new User();
        mockUser.setTwoFactorAuthChannelType("1,2");
        mockUser.setIsChannelTypeSMS(false);
        mockUser.setIsChannelTypeEmail(false);
        MockedStatic<Lookup> lookupMockedStatic = mockStatic(Lookup.class);
        lookupMockedStatic.when(() -> Lookup.getCodeValueById(anyLong())).thenReturn("EMAIL");
        User result = userDalJpa.getChannelTypesForTwoFactorAuth(mockUser);
        assertTrue(result.getIsChannelTypeEmail());
        assertFalse(result.getIsChannelTypeSMS());
        lookupMockedStatic.close();
    }

    //    Test Cases For : : isAssertPasswordsEnabled
    @Test
    public void testisAssertPasswordsEnabled(){
        when(deploymentUtil.isAssertPasswords()).thenReturn(true);
        Boolean result = userDalJpa.isAssertPasswordsEnabled();
        verify(deploymentUtil).isAssertPasswords();
        assertTrue(result);
    }

}
