package com.esq.rbac.service.application.service;

import com.esq.rbac.service.application.applicationmaintenance.service.ApplicationMaintenanceDalJpa;
import com.esq.rbac.service.application.childapplication.domain.ChildApplication;
import com.esq.rbac.service.application.childapplication.repository.ChildApplicationRepository;
import com.esq.rbac.service.application.domain.Application;
import com.esq.rbac.service.application.repository.ApplicationRepo;
import com.esq.rbac.service.auditlog.service.AuditLogService;
import com.esq.rbac.service.auditloginfo.domain.AuditLogInfo;
import com.esq.rbac.service.config.CacheConfig;
import com.esq.rbac.service.filters.domain.Filters;
import com.esq.rbac.service.lookup.Lookup;
import com.esq.rbac.service.role.targetsubdomain.domain.Target;
import com.esq.rbac.service.util.CacheService;
import com.esq.rbac.service.util.DeploymentUtil;
import com.esq.rbac.service.util.dal.Options;
import com.esq.rbac.service.variable.domain.Variable;
import com.esq.rbac.service.variable.service.VariableDal;
import com.google.common.collect.Sets;
import jakarta.persistence.EntityManager;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.validator.routines.RegexValidator;
import org.apache.commons.validator.routines.UrlValidator;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.*;

import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@SpringBootTest
@Slf4j
class ApplicationDalJpaTest {



    @Mock
    private EntityManager entityManager;


    @Mock
    private UrlValidator urlValidator;



    @Mock
    private CacheService cacheService;

    @Mock
    private ApplicationRepo applicationRepo;

    @Mock
    private AuditLogService auditLogDal;


    @Mock
    private VariableDal variableDal;


    @Mock
    private ApplicationMaintenanceDalJpa applicationMaintenanceDal;

    @Mock
    private Application applicationmock;


    @Mock
    ChildApplicationRepository childApplicationRepository;

    @Mock
    Filters filters;

    @Autowired
    private DeploymentUtil deploymentUtil;





    @InjectMocks
    private ApplicationDalJpa applicationDal ;



    @BeforeEach
    public void setup() {
        MockitoAnnotations.openMocks(this);


    }



    @Test
    void test_createApplication() {

        log.debug("test_create");
        final String APP1_NAME = "TEST APP";
        final String APP1_DESC = "TEST APP application for test in RBAC";
        final String LABEL_COOL = "cool";
        final String LABEL_NEW = "new";
        final String LABEL_TEST_APP = "test app";

        Application app = new Application();

        app.setName(APP1_NAME);
        app.setDescription(APP1_DESC);
        app.setLabels(Sets.newHashSet(LABEL_COOL, LABEL_NEW, LABEL_TEST_APP));

        Variable var1 = new Variable();
        var1.setVariableName("test1");
        var1.setVariableValue("test1value");
        Variable var2 = new Variable();
        var2.setVariableName("test2");
        var2.setVariableValue("test2value");

        when(applicationRepo.save(app)).thenReturn(app);

        Application createdApp = applicationDal.create(app, new AuditLogInfo(1, null));
        createdApp.setApplicationId(100);
        log.debug("test1; create; app={}", createdApp);


        Assertions.assertNotNull(createdApp, "The created application should not be null");

        Assertions.assertNotNull(createdApp.getApplicationId(), "This should not be null");
        Assertions.assertTrue(createdApp.getApplicationId() > 0);
        Assertions.assertEquals(app.getName(), createdApp.getName());
        Assertions.assertEquals(app.getDescription(), createdApp.getDescription());

        app.setApplicationId(createdApp.getApplicationId());


        AuditLogInfo auditLogInfo = new AuditLogInfo();
        auditLogInfo.setLoggedInUserId(1);


//        Map<String,String> objectChanges = new HashMap<>();
//       // objectChanges.put("TEST APP","\"description:new\" = \"TEST APP application for test in RBAC\", \"labels:new\" = \"[new, test app, cool]\", \"name\" = \"TEST APP\"");
//         objectChanges.put(,anyString());

        // Verify the interactions and assertions
        verify(cacheService).clearCache(CacheConfig.CLEAR_ALL_USER_CACHE);
        verify(applicationRepo).save(app);

        verify(variableDal).cleanVariablesForApplicationChanges();

        // Assertions the result
        Assertions.assertEquals(app, createdApp);








    }


    @Test
    void test_updateApplication() {

        log.debug("test_update");

        final Integer USER_ID = 1;
        final Integer APPLICATION_ID = 100;
        final String APP_NAME = "TEST APP";
        final String APP_DESC = "TEST APP application for test in RBAC";
        final String LABEL_COOL = "cool";
        final String LABEL_NEW = "new";
        final String LABEL_TEST_APP = "test app";

        Application existingApp = new Application();
        existingApp.setApplicationId(APPLICATION_ID);
        existingApp.setName(APP_NAME);
        existingApp.setDescription(APP_DESC);
        existingApp.setLabels(Sets.newHashSet(LABEL_COOL, LABEL_NEW, LABEL_TEST_APP));

        Application updatedApp = new Application();
        updatedApp.setApplicationId(APPLICATION_ID);
        updatedApp.setName("Updated App");
        updatedApp.setDescription("Updated description");
        updatedApp.setLabels(Sets.newHashSet("updatedLabel1", "updatedLabel2"));

        AuditLogInfo auditLogInfo = new AuditLogInfo();
        auditLogInfo.setLoggedInUserId(USER_ID);

        Map<String, String> objectChanges = new HashMap<>();
        objectChanges.put("name", "Updated App");
        objectChanges.put("description", "Updated description");
        objectChanges.put("labels", "[updatedLabel1, updatedLabel2]");

        // Mock the dependencies
        when(applicationRepo.findById(APPLICATION_ID)).thenReturn(Optional.of(existingApp));
        when(applicationRepo.save(existingApp)).thenReturn(existingApp);

        // Call the method under test
        Application updatedApplication = applicationDal.update(updatedApp, auditLogInfo);

        // Verify the interactions and assertions
//        verify(cacheManager).getCache(Arrays.toString(CacheConfig.CLEAR_ALL_USER_CACHE));
//        verify(cacheManager.getCache(Arrays.toString(CacheConfig.CLEAR_ALL_USER_CACHE))).clear();
        verify(applicationRepo).findById(APPLICATION_ID);
        verify(applicationRepo).save(existingApp);
//        verify(auditLogDal).createSyncLog(eq(USER_ID), eq(APP_NAME),
//                eq("Application"), eq("Update"), eq(objectChanges));
        verify(variableDal).cleanVariablesForApplicationChanges();

        // Assert the result
        Assertions.assertEquals(existingApp, updatedApplication);
        Assertions.assertEquals("Updated App", updatedApplication.getName());
        Assertions.assertEquals("Updated description", updatedApplication.getDescription());
        Assertions.assertEquals(2, updatedApplication.getLabels().size());
        Assertions.assertTrue(updatedApplication.getLabels().contains("updatedLabel1"));
        Assertions.assertTrue(updatedApplication.getLabels().contains("updatedLabel2"));
    }


    @Test
    void testUpdate_WithInvalidApplicationId() {


        // Prepare test data
        Application application = new Application();
        application.setApplicationId(null);

        AuditLogInfo auditLogInfo = new AuditLogInfo();
        auditLogInfo.setLoggedInUserId(1);

        // Verify and assert the exception
        IllegalArgumentException exception = Assertions.assertThrows(IllegalArgumentException.class, () ->
                applicationDal.update(application, auditLogInfo));
        Assertions.assertEquals("applicationId missing", exception.getMessage());


    }

    @Test
    void testUpdate_WithInvalidExistingApplication() {


        // Prepare test data
        Application application = new Application();
        application.setApplicationId(1);

        AuditLogInfo auditLogInfo = new AuditLogInfo();
        auditLogInfo.setLoggedInUserId(1);

        when(applicationRepo.findById(application.getApplicationId())).thenReturn(Optional.empty());

        // Verify and assert the exception
        NoSuchElementException exception = Assertions.assertThrows(NoSuchElementException.class, () ->
                applicationDal.update(application, auditLogInfo));
        Assertions.assertEquals("applicationId invalid", exception.getMessage());


    }



    @Test
    void test_getById() {

        log.debug("test_getById");

        int applicationId = 100;
        Application application = new Application();
        application.setApplicationId(applicationId);
        when(applicationRepo.findById(applicationId)).thenReturn(Optional.of(application));


        Application result = applicationDal.getById(applicationId);

        Assertions.assertEquals(applicationId, result.getApplicationId());
    }

    @Test
    void test_getByName() {
        // Prepare test data
        String applicationName = "TEST APP";
        Application application = new Application();
        application.setName(applicationName);
        when(applicationRepo.getApplicationByName(applicationName)).thenReturn(application);

        // Call the method under test
        Application result = applicationDal.getByName(applicationName);

        // Verify the interactions and assertions
        Assertions.assertNotNull(result);
        Assertions.assertEquals(applicationName, result.getName());
        // Add more assertions based on the properties of the returned Application object
    }

    @Test
    void test_getByName_NotFound() {
        // Prepare test data
        String applicationName = "NON_EXISTENT_APP";
        when(applicationRepo.getApplicationByName(applicationName)).thenReturn(null);

        // Call the method under test
        Application result = applicationDal.getByName(applicationName);

        // Verify the interactions and assertions
        Assertions.assertNull(result);
    }





    @Test
    void test_deleteByName() {
        // Prepare test data
        String appName = "Test App";
        int userId = 1;
        AuditLogInfo auditLogInfo = new AuditLogInfo(userId, null);

        Application application = new Application();
        application.setName(appName);

        // Set up the mock behaviors
        when(applicationRepo.findById(Lookup.getApplicationId(appName))).thenReturn(Optional.of(application));

        // Call the method under test
        applicationDal.deleteByName(appName, auditLogInfo);

        // Verify the interactions
        verify(cacheService).clearCache(CacheConfig.CLEAR_ALL_USER_CACHE);
        verify(applicationRepo).delete(application);

    }
    @Test
    void test_getAllNames() {
        // Prepare test data
        List<String> expectedNames = Arrays.asList("App 1", "App 2", "App 3");

        // Set up the mock behavior
        when(applicationRepo.getAllApplicationNames()).thenReturn(expectedNames);

        // Call the method under test
        List<String> actualNames = applicationDal.getAllNames();

        // Verify the result
        Assertions.assertEquals(expectedNames, actualNames);

        // Verify the interaction
        verify(applicationRepo).getAllApplicationNames();
    }





    @Test
    void test_deleteById() {
        // Prepare test data
        int applicationId = 100;
        int userId = 1;
        AuditLogInfo auditLogInfo = new AuditLogInfo(userId, null);

        Application application = new Application();
        application.setApplicationId(applicationId);
        application.setName("Test App");

        ChildApplication childApplication1 = ChildApplication.builder().childApplicationId(1).childApplicationName("Child App 1").build();
        ChildApplication childApplication2 = ChildApplication.builder().childApplicationId(2).childApplicationName("Child App 2").build();
        Set<ChildApplication> childApplications = Set.of(
                childApplication1,
                childApplication2
        );

//        TreeSet<ChildApplication> childApplications = new TreeSet<>(Comparator.comparing(ChildApplication::getChildApplicationId));
//        childApplications.add(childApplication1);
//        childApplications.add(childApplication2);

        // Set up the mock behaviors
        when(applicationRepo.findById(applicationId)).thenReturn(Optional.of(application));

        when(applicationmock.getChildApplications()).thenReturn(childApplications);

        // Call the method under test
        applicationDal.deleteById(applicationId, auditLogInfo);

        // Verify the interactions
        verify(cacheService).clearCache(CacheConfig.CLEAR_ALL_USER_CACHE);
        verify(variableDal).deleteForCascade(null, null, applicationId);


        verify(applicationRepo).delete(application);
    }


    @Test
    void test_getUserAuthorizedApps() {
        // Prepare test data
        String userName = "john.doe";

        Application app = Application.builder().applicationId(1).name("App 1").build();

        List<Application> expectedList = Arrays.asList(
                app
        );






        deploymentUtil.setShowAppDashboardInSwitcher(true);



        applicationDal.setDeploymentUtil(deploymentUtil);



        // Set up the mock behavior for getById

        when(applicationDal.getById(deploymentUtil.getAppDashboardApplicationId())).thenReturn(app);
        when(applicationRepo.findById(1).get()).thenReturn(app);

        // Set up the mock behavior for getUserAuthorizedApps
        when(applicationRepo.getUserAuthorizedApps(userName)).thenReturn(expectedList);

        // Call the method under test
        List<Application> actualList = applicationDal.getUserAuthorizedApps(userName);

        // Verify the result
        List<Application> expectedFinalList = new LinkedList<>(expectedList);
        int expectedFinalListsize = expectedFinalList.size();
        expectedFinalList.add(app);
        for(int i =0;i<expectedFinalListsize;i++){
            Assertions.assertEquals(expectedFinalList.get(i),actualList.get(i));

        }


    }


    @Test
    void testGetTargetNames_NullSet() {


        Application application = Application.builder().applicationId(1).name("TestApp").build();

        Target buttonTarget = new Target("",application);
        String result =applicationDal.getTargetNames(Set.of(buttonTarget));

        Assertions.assertEquals("", result);
    }



    @Test
    void testGetTargetNames_NonEmptyNames() {
        Application application = Application.builder().applicationId(1).name("TestApp").build();

        Target buttonTarget = new Target("Button",application);
        String result =applicationDal.getTargetNames(Set.of(buttonTarget));

        Assertions.assertEquals("Button", result);
    }






    @Test
    void testGetList() {
        // Initialize mocks


        // Prepare test data
        Options options = new Options();
        String queryText = "select a from Application a";

        List<String> nullList = null;
        when(filters.getConditions()).thenReturn(nullList);


        when(filters.getList(eq(entityManager), eq(Application.class), eq(queryText), eq(options), anyMap())).thenReturn(List.of());

        // Call the method under test
        List<Application> applications = applicationDal.getList(options);


        // Verify the result
        Assertions.assertEquals(List.of(), applications);
    }






    @Test
    void testUrlValidator() {
        UrlValidator urlValidatorNoLocal = new UrlValidator();
        UrlValidator urlValidatorLocal = new UrlValidator(UrlValidator.ALLOW_LOCAL_URLS);

//            when(deploymentUtil.getRegexForUrlAuhtorityValidation()).thenReturn("^([a-zA-Z0-9]+(?:(?:\\.|\\-)[a-zA-Z0-9]+)+(?:\\:\\d+)?(?:\\/[\\w\\-]+)*(?:\\/?|\\/\\w+\\.[a-zA-Z]{2,4}(?:\\?[\\w]+\\=[\\w\\-]+)?)?(?:\\&[\\w]+\\=[\\w\\-]+)*)$");

        // http
        Assertions.assertTrue(urlValidatorNoLocal.isValid("http://192.168.1.210:8001/rbac"));
        Assertions.assertTrue(urlValidatorNoLocal.isValid("http://127.0.0.1:8001/rbac"));
        Assertions.assertFalse(urlValidatorNoLocal.isValid("http://localhost:8001/rbac"));

        Assertions.assertTrue(urlValidatorLocal.isValid("http://192.168.1.210:8001/rbac"));
        Assertions.assertTrue(urlValidatorLocal.isValid("http://127.0.0.1:8001/rbac"));
        Assertions.assertTrue(urlValidatorLocal.isValid("http://localhost:8001/rbac"));

        // want this to work
        Assertions.assertFalse(urlValidatorNoLocal.isValid("http://vpc116.test.local:8001/rbac"));
        Assertions.assertFalse(urlValidatorLocal.isValid("http://vpc116.test.local:8001/rbac"));

        // https
        Assertions.assertTrue(urlValidatorNoLocal.isValid("https://192.168.1.210:8001/rbac"));
        Assertions.assertTrue(urlValidatorNoLocal.isValid("https://127.0.0.1:8001/rbac"));
        Assertions.assertFalse(urlValidatorNoLocal.isValid("https://localhost:8001/rbac"));

        Assertions.assertTrue(urlValidatorLocal.isValid("https://192.168.1.210:8001/rbac"));
        Assertions.assertTrue(urlValidatorLocal.isValid("https://127.0.0.1:8001/rbac"));
        Assertions.assertTrue(urlValidatorLocal.isValid("https://localhost:8001/rbac"));

        // want this to work
        Assertions.assertFalse(urlValidatorNoLocal.isValid("https://vpc116.test.local:8001/rbac"));
        Assertions.assertFalse(urlValidatorLocal.isValid("https://vpc116.test.local:8001/rbac"));

        UrlValidator regexValidator = new UrlValidator(new RegexValidator(deploymentUtil.getRegexForUrlAuhtorityValidation()), UrlValidator.ALLOW_LOCAL_URLS);
        // http
        Assertions.assertTrue(regexValidator.isValid("http://192.168.1.210:8001/rbac"));
        Assertions.assertTrue(regexValidator.isValid("http://127.0.0.1:8001/rbac"));
        Assertions.assertTrue(regexValidator.isValid("http://localhost:8001/rbac"));

        Assertions.assertTrue(regexValidator.isValid("http://192.168.1.210:8001/rbac"));
        Assertions.assertTrue(regexValidator.isValid("http://127.0.0.1:8001/rbac"));
        Assertions.assertTrue(regexValidator.isValid("http://localhost:8001/rbac"));

        // want this to work
        Assertions.assertTrue(regexValidator.isValid("http://vpc116.test.local:8001/rbac"));
        Assertions.assertTrue(regexValidator.isValid("http://vpc116.test.local:8001/rbac"));

        // https
        Assertions.assertTrue(regexValidator.isValid("https://192.168.1.210:8001/rbac"));
        Assertions.assertTrue(regexValidator.isValid("https://127.0.0.1:8001/rbac"));
        Assertions.assertTrue(regexValidator.isValid("https://localhost:8001/rbac"));

        Assertions.assertTrue(regexValidator.isValid("https://192.168.1.210:8001/rbac"));
        Assertions.assertTrue(regexValidator.isValid("https://127.0.0.1:8001/rbac"));
        Assertions.assertTrue(regexValidator.isValid("https://localhost:8001/rbac"));

        // want this to work
        Assertions.assertTrue(regexValidator.isValid("https://vpc116.test.local:8001/rbac"));
        Assertions.assertTrue(regexValidator.isValid("https://vpc116.test.local:8001/rbac"));
        Assertions.assertTrue(regexValidator.isValid("https://vpc116.test.local:8001/rbac?appKey=RBAC"));
        Assertions.assertTrue(regexValidator.isValid("https://vpc116.test.local/rbac?appKey=RBAC"));

        // want these to fail
        Assertions.assertFalse(regexValidator.isValid("rajah"));
        Assertions.assertFalse(regexValidator.isValid("htttps://vpc116.test.local/rbaca?dasdasdasd"));
        Assertions.assertFalse(regexValidator.isValid("http://vpc116.test.local:8001:8009/rbaca?dasdasdasd"));
    }











}