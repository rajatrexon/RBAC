package com.esq.rbac.service.roledaljpatest;

import com.esq.rbac.service.exception.ErrorInfoException;
import com.esq.rbac.service.role.domain.Role;
import com.esq.rbac.service.role.repository.RoleRepository;
import com.esq.rbac.service.role.service.RoleDalJpa;
import org.junit.Before;
import org.junit.Test;
import org.junit.jupiter.api.BeforeEach;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.Collections;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

//@DataJpaTest
@SpringBootTest
public class RoleDalJpaTest {

    @Mock
    private RoleRepository roleRepository;

    @InjectMocks
    private RoleDalJpa roleDalJpa;

    @Before
    public void setup() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    public void testCreate_ValidRole_SuccessfulCreation() {
        // Arrange
        Role role = new Role();
        role.setApplicationId(1);
        role.setName("Test Role");
        role.setRoleId(0);
        int userId = 123;

        when(roleRepository.findByNameAndApplicationId("Test Role", 1)).thenReturn(null);
        when(roleRepository.save(role)).thenReturn(role);

        // Act
        Role createdRole = roleDalJpa.create(role, userId);

        // Assert
        assertEquals(role, createdRole);
    }

    @Test
    public void testCreate_DuplicateRoleName_ThrowsException() {
        // Arrange
        Role role = new Role();
        role.setApplicationId(1);
        role.setName("Test Role");
        role.setRoleId(0);
        int userId = 123;

        when(roleRepository.findByNameAndApplicationId("Test Role", 1)).thenReturn(Collections.singletonList(role));

        // Act & Assert
        assertThrows(ErrorInfoException.class, () -> roleDalJpa.create(role, userId));
    }


}
