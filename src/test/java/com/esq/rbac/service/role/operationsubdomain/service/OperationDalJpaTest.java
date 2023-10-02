package com.esq.rbac.service.role.operationsubdomain.service;



import com.esq.rbac.service.application.domain.Application;
import com.esq.rbac.service.filters.domain.Filters;
import com.esq.rbac.service.role.operationsubdomain.domain.Operation;
import com.esq.rbac.service.role.operationsubdomain.repository.OperationRepository;
import com.esq.rbac.service.role.targetsubdomain.domain.Target;
import com.esq.rbac.service.util.dal.OptionFilter;
import com.esq.rbac.service.util.dal.Options;
import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;


@SpringBootTest
@Slf4j
class OperationDalJpaTest {

    @Mock
    OperationRepository operationRepository;


    @Mock
    Filters filters;


    @Mock
    Options options;


    @Autowired
    protected EntityManager em ;



    @InjectMocks
    OperationDalJpa operationDalJpa;


    @BeforeEach
    public void setup() {
        MockitoAnnotations.openMocks(this);


    }




    @Test
    void test_create() {

        Application fdApp = new Application("FraudDECK");
        Target atmTarget = new Target("ATM", fdApp);
        Operation atmView = new Operation("View", atmTarget);

        when(operationRepository.save(atmView)).thenReturn(atmView);

     Operation actualoperation = operationDalJpa.create(atmView);

        Assertions.assertEquals(atmView,actualoperation);


    }

    @Test
    void test_update() {

        Application fdApp = new Application("FraudDECK");
        Target atmTarget = new Target("ATM", fdApp);
        Operation atmView = new Operation("View", atmTarget);



        when(operationRepository.save(atmView)).thenReturn(atmView);

        Operation actualoperation = operationDalJpa.update(atmView);

        Assertions.assertEquals(atmView,actualoperation);
    }

    @Test
    void test_getById_WithExistingOperation_ReturnsOperation() {
        // Arrange
        int operationId = 1;
        Application fdApp = new Application("FraudDECK");
        Target atmTarget = new Target("ATM", fdApp);
        Operation atmView = new Operation("View", atmTarget);
        atmView.setOperationId(operationId);
        when(operationRepository.findById(operationId)).thenReturn(Optional.of(atmView));

        // Act
        Operation result = operationDalJpa.getById(operationId);

        // Assert
        verify(operationRepository).findById(operationId);
        assertNotNull(result);
        Assertions.assertEquals(atmView, result);
    }

    @Test
    void test_getById_WithNonExistingOperation_ReturnsNull() {
        // Arrange
        int operationId = 1;
        when(operationRepository.findById(operationId)).thenReturn(Optional.empty());

        // Act
        Operation result = operationDalJpa.getById(operationId);

        // Assert
        verify(operationRepository).findById(operationId);
        assertNull(result);
    }

    @Test
    void test_deleteById_WithExistingOperation_DeletesOperation() {
        // Arrange
        int operationId = 1;
        doNothing().when(operationRepository).deleteOperationById(operationId);

        // Act
        operationDalJpa.deleteById(operationId);

        // Assert
        verify(operationRepository).deleteOperationById(operationId);
    }

    @Test
    void test_deleteById_WithNonExistingOperation_DoesNothing() {
        // Arrange
        int operationId = 1;
        doThrow(new RuntimeException()).when(operationRepository).deleteOperationById(operationId);

        // Act & Assert
        assertThrows(RuntimeException.class, () -> operationDalJpa.deleteById(operationId));
        verify(operationRepository).deleteOperationById(operationId);
    }

    //Todo
    @Test
    void test_getLists(){
        Application obApp = new Application("OB");
        obApp.setApplicationId(1);

        Target obAtmTarget = new Target("ATM", obApp);
        obAtmTarget.setTargetId(1);
        Operation obAtmQuery = new Operation("Query", obAtmTarget);
        obAtmQuery.setOperationId(1);
        Operation obAtmPing = new Operation("Ping", obAtmTarget);
        obAtmPing.setOperationId(2);
        Operation obAtmReboot = new Operation("Reboot", obAtmTarget);
        obAtmReboot.setOperationId(3);

        System.out.println(obAtmTarget.getOperations());

        List<Operation> expectedoperations = List.of(obAtmQuery,obAtmPing,obAtmReboot);

        OptionFilter optionFilter = new OptionFilter();



        optionFilter.addFilter("applicationId", obApp.getApplicationId().toString());
        options = new Options(optionFilter);
        log.info("test; getTargets with filter; options={}", options);

        operationDalJpa.setEntityManager(em);

        TypedQuery<Operation> mockedQuery = mock(TypedQuery.class);
        when(mockedQuery.getResultList()).thenReturn(expectedoperations);
        List<Operation> operations = operationDalJpa.getList(options);

        log.info("test; getTargets with filter; result={}", operations);

        Assertions.assertEquals(expectedoperations,operations);

    }

//
//    @Test
//    void testGetList() {
//        // Create sample options and filters
//        Map<String, String> filters = new HashMap<>();
//        filters.put("applicationId", "123");
//        filters.put("targetId", "456");
//        filters.put("name", "Operation 1");
//        filters.put("label", "Label 1");
//
//           Options options = new Options();
//
//        OptionFilter optionFilter = mock(OptionFilter.class);
//        when(options.getOption(OptionFilter.class)).thenReturn(optionFilter);
//        when(optionFilter.getFilters()).thenReturn(filters);
//
//
//
//        Filters expectedFilters = new Filters();
//        expectedFilters.addCondition("o.target.application.applicationId = :applicationId");
//        expectedFilters.addParameter("applicationId", 123);
//        expectedFilters.addCondition("o.target.targetId = :targetId");
//        expectedFilters.addParameter("targetId", 456);
//        expectedFilters.addCondition("o.name = :name");
//        expectedFilters.addParameter("name", "Operation 1");
//        expectedFilters.addCondition(":label member of o.labels");
//        expectedFilters.addParameter("label", "Label 1");
//
//
//
//        List<Operation> expectedList = new ArrayList<>(); // Replace with your expected list
//
//
//
//        when(operationDalJpa.prepareFilters(options)).thenReturn(expectedFilters);
//        when(expectedFilters.getList(em, Operation.class, "select o from Operation o", options)).thenReturn(expectedList);
//
//
//
//        // Call the method under test
//        List<Operation> actualList = operationDalJpa.getList(options);
//
//
//
//        // Verify the interactions and assertions
//        verify(operationDalJpa).prepareFilters(options);
//
//        assertEquals(expectedList, actualList);
//    }
}