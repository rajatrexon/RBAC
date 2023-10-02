package com.esq.rbac.service.application.repository;

import com.esq.rbac.service.application.domain.Application;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Date;
import java.util.List;

@Repository
public interface ApplicationRepo extends JpaRepository<Application,Integer> {
        @Query("SELECT a FROM Application a WHERE a.name =?1")
        Application getApplicationByName( String name);

        @Query("DELETE FROM Application a WHERE a.applicationId =?1")
        void deleteApplicationById(Long applicationId);

        @Query("DELETE FROM Application a WHERE a.name = ?1")
        void deleteApplicationByName(String name);



        @Query("SELECT a.applicationId FROM Application a")
        List<Integer> getAllApplicationIds();

        @Query(nativeQuery = true, value = "SELECT DISTINCT a.* FROM rbac.userTable u " +
                "JOIN rbac.groupRole gr ON gr.groupId = u.groupId " +
                "JOIN rbac.role r ON r.roleId = gr.roleId " +
                "JOIN rbac.application a ON a.applicationId = r.applicationId " +
                "WHERE u.userName = ?1")
        List<Application> getUserAuthorizedApps(String userName);

        @Query(nativeQuery = true,value = "select sd.groupId from rbac.scopeDefinition sd join rbac.scope s on (sd.scopeId=s.scopeId) where s.applicationId = ?1")
        List<Integer> getDefinedScopeIdsFromApplication(int applicationId);


        @Query("select a.name from Application a")
    List<String> getAllApplicationNames();



        @Query("select GETUTCDATE()")
        Date getDatabaseStatus();


}
