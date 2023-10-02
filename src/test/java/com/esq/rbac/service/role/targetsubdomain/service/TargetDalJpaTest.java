package com.esq.rbac.service.role.targetsubdomain.service;

import com.esq.rbac.service.application.domain.Application;
import com.esq.rbac.service.role.targetsubdomain.domain.Target;
import com.esq.rbac.service.role.targetsubdomain.repository.TargetRepository;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.Optional;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@SpringBootTest
class TargetDalJpaTest {

    @Mock
    TargetRepository targetRepository;

    @InjectMocks
    TargetDalJpa targetDalJpa;

    @BeforeEach
    public void setup(){
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void test_create() {
        Application application = new Application("OB");

        Target target = new Target("ATM",application);
        when(targetRepository.save(target)).thenReturn(target);
       Target target1 = targetDalJpa.create(target);

        Assertions.assertEquals(target,target1);

    }

    @Test
    void test_update() {

        Application application = new Application("OB");

        Target target = new Target("ATM",application);
        when(targetRepository.save(target)).thenReturn(target);
        Target target1 = targetDalJpa.update(target);

        Assertions.assertEquals(target,target1);

    }

    @Test
    void test_getById() {
        Application application = new Application("OB");

        Target target = new Target("ATM",application);
        target.setTargetId(1);
        when(targetRepository.findById(1)).thenReturn(Optional.of(target));
        Optional<Target> target1 = Optional.ofNullable(targetDalJpa.getById(1));
        Assertions.assertEquals(target,target1.orElse(null));


    }

    @Test
    void test_deleteById() {
        // Prepare test data
        int targetId = 1;


        TargetDalJpa targetDalJpa = new TargetDalJpa(targetRepository);

        // Call the deleteById method
        targetDalJpa.deleteById(targetId);

        // Verify that targetRepository.deleteById is called with the correct targetId
        verify(targetRepository).deleteById(targetId);
    }


    @Test
    void test_getList() {
    }

    @Test
    void test_getCount() {
    }
}