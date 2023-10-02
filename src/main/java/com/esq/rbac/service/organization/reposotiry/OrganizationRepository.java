package com.esq.rbac.service.organization.reposotiry;

import com.esq.rbac.service.organization.domain.Organization;
import com.esq.rbac.service.organization.vo.OrganizationHierarchy;
import com.esq.rbac.service.user.domain.User;
import com.esq.rbac.service.user.embedded.OrganizationHierarchyUser;
import com.esq.rbac.service.user.embedded.OrganizationInfoUser;
import com.esq.rbac.service.util.dal.OptionFilter;
import jakarta.persistence.TypedQuery;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Date;
import java.util.List;
import java.util.Optional;

@Repository
public interface OrganizationRepository extends JpaRepository<Organization,Long> {

    @Query("SELECT o from Organization o where o.organizationName = :orgName and o.tenantId = :tenantId")
    Organization getOrganizationByTenant(@Param("orgName") String orgName, @Param("tenantId") Long tenantId);

    @Modifying
    @Query("update Organization org set org.isDeleted = true, org.updatedBy = :userId, org.updatedOn = :currDateTime where org.organizationId = :organizationId OR org.parentOrganizationId = :organizationId")
    void deleteOrganizationById(@Param("organizationId") Long organizationId, @Param("userId") Integer userId, @Param("currDateTime") Date currDateTime);

    @Query("SELECT new com.esq.rbac.service.organization.vo.OrganizationHierarchy(o) from Organization o where o.tenantId=:tenantId")
    List<OrganizationHierarchy> getOrganizationForGridView(@Param("tenantId") Long tenantId);

    @Query(value = "SELECT o.* FROM rbac.organization o where o.tenantId = ?1 and o.parentOrganizationId = ?2 and o.isDeleted = 0 ORDER BY o.organizationName ASC OFFSET ?3 ROWS FETCH NEXT ?4 ROWS ONLY", nativeQuery = true)
    List<Organization> getOrganizationInBatchForGridViewASC(Long tenantId, Long orgId, int offset, int limit);

    @Query(value = "SELECT o.* FROM rbac.organization o where o.tenantId = ?1 and o.parentOrganizationId = ?2 and o.isDeleted = 0 ORDER BY o.organizationName ASC", nativeQuery = true)
    List<Organization> getOrganizationAllForGridViewASC(Integer tenantId, Integer parentOrganizationId);

    @Query(value = "SELECT o.* FROM rbac.organization o where o.tenantId = ?1 and o.parentOrganizationId = ?2 and o.isDeleted = 0 ORDER BY o.organizationName DESC OFFSET ?3 ROWS FETCH NEXT ?4 ROWS ONLY", nativeQuery = true)
    List<Organization> getOrganizationInBatchForGridViewDESC(Long tenantId, Long parentOrganizationId, int offset, int limit);

    @Query(value = "SELECT o.* FROM rbac.organization o where o.tenantId = ?1 and o.parentOrganizationId = ?2 and o.isDeleted = 0 ORDER BY o.organizationName DESC", nativeQuery = true)
    List<Organization> getOrganizationAllForGridViewDESC(Integer tenantId, Integer parentOrganizationId);
    @Query("select count(1) from Organization o where o.tenantId = :tenantId")
    Long getOrganizationByTenantId(@Param("tenantId") Long tenantId);

    @Query("SELECT COUNT(u) FROM User u WHERE u.organizationId = :organizationId")
    long countByOrganizationId(@Param("organizationId") Long organizationId);

    @Modifying
    @Query("DELETE FROM OrganizationCalendar oc WHERE oc.organizationId = :organizationId")
    Integer deleteByOrganizationId(@Param("organizationId") Long organizationId);

    @Query("SELECT new com.esq.rbac.service.user.embedded.OrganizationHierarchyUser(u) FROM User u WHERE u.organizationId" +
            " IN (SELECT org.organizationId FROM Organization org WHERE org.tenantId = :tenantId)" +
            " AND u.isStatus = 1 ORDER BY u.userName")
    List<OrganizationHierarchyUser> findOrganizationHierarchyUsersByTenantId(@Param("tenantId") Long tenantId);

    @Query("SELECT COUNT(u) FROM User u WHERE u.organizationId IN (SELECT org.organizationId FROM Organization org WHERE org.tenantId = :tenantId) AND u.isStatus = 1")
    int getOrganizationHierarchyUsersByTenantCount(@Param("tenantId") long tenantId);

    @Query("SELECT new com.esq.rbac.service.user.embedded.OrganizationInfoUser(u) FROM User u WHERE u.organizationId" +
            " IN (SELECT org.organizationId FROM Organization org WHERE org.tenantId = :tenantId)" +
            " AND u.isStatus = 1 ORDER BY u.userName")
    List<OrganizationInfoUser> findOrganizationInfoUsersByTenantId(@Param("tenantId") Long tenantId);

    @Query(value = "SELECT u.* FROM rbac.userTable u WHERE u.organizationId IN" +
            " (SELECT o.organizationId FROM rbac.organization o WHERE o.tenantId = :tenantId AND" +
            " o.organizationId = :organizationId) AND u.isStatus = 1 ORDER BY u.userName ASC OFFSET :offset" +
            " ROWS FETCH NEXT :limit ROWS ONLY", nativeQuery = true)
    List<User> findUsersByOrganizationIdAndStatus(@Param("tenantId") Long tenantId, @Param("organizationId") Long organizationId,
                                                  @Param("offset") int offset,@Param("limit") int limit);

    @Query("SELECT COUNT(u) FROM User u WHERE u.organizationId IN " +
            "(SELECT org.organizationId FROM Organization org WHERE org.tenantId = :tenantId) AND u.isStatus = 1")
    Long countUsersByTenantId(@Param("tenantId") String tenantId);

}
