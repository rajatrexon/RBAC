package com.esq.rbac.service.util;

import com.esq.rbac.service.auditlog.domain.AuditLog;
import com.esq.rbac.service.auditloginfo.domain.AuditLogInfo;
import com.esq.rbac.service.contact.domain.Contact;
import com.esq.rbac.service.contact.messagetemplate.domain.MessageTemplate;
import com.esq.rbac.service.contact.objectrole.domain.ObjectRole;
import com.esq.rbac.service.contact.sla.domain.SLA;
import com.esq.rbac.service.group.domain.Group;
import com.esq.rbac.service.group.service.GroupDal;
import com.esq.rbac.service.jointables.grouprole.domain.GroupRole;
import com.esq.rbac.service.lookup.Lookup;
import com.esq.rbac.service.organization.domain.Organization;
import com.esq.rbac.service.organization.organizationcalendar.domain.OrganizationCalendar;
import com.esq.rbac.service.organization.organizationcalendar.service.OrganizationCalendarDal;
import com.esq.rbac.service.organization.organizationlogo.domain.OrganizationLogo;
import com.esq.rbac.service.organization.organizationmaintenance.service.OrganizationMaintenanceDal;
import com.esq.rbac.service.scope.scopedefinition.domain.ScopeDefinition;
import com.esq.rbac.service.tenant.domain.Tenant;
import com.esq.rbac.service.tenant.tenantlog.domain.TenantLogo;
import com.esq.rbac.service.user.domain.User;
import com.esq.rbac.service.user.service.UserDal;
import com.esq.rbac.service.util.dal.OptionFilter;
import com.esq.rbac.service.util.dal.Options;
import com.google.gson.Gson;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.Query;
import jakarta.ws.rs.core.MultivaluedHashMap;
import jakarta.ws.rs.core.MultivaluedMap;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Component
public class CacheManagerUtil {

    private UserDal userDal;
    private GroupDal groupDal;
    private OrganizationMaintenanceDal organizationDal;
    private OrganizationCalendarDal organizationCalendarDal;

    @PersistenceContext
    private EntityManager em;

    @Autowired
    public void setUserDal(UserDal userDal) {
        this.userDal = userDal;
    }

    @Autowired
    public void setGroupDal(GroupDal groupDal) {
        this.groupDal = groupDal;
    }

    @Autowired
    public void setOrganizationMaintenanceDal(OrganizationMaintenanceDal organizationDal) {
        this.organizationDal = organizationDal;
    }

    @Autowired
    public void setOrganizationCalendarDal(OrganizationCalendarDal organizationCalendarDal) {
        this.organizationCalendarDal = organizationCalendarDal;
    }

    @Transactional(propagation = Propagation.REQUIRED)
    public String deleteAndrefreshCacheForTenant(String tenantName, AuditLogInfo auditLogInfo) {
        Object result = null;
        log.info("refreshCacheForTenant; tenantName={}; starts;", tenantName);
        long startTime = System.currentTimeMillis();

        MultivaluedMap<String, String> queryParams = new MultivaluedHashMap<>();
        queryParams.add("tenantName", tenantName);
        Options options = new Options(new OptionFilter(queryParams));
        List<User> userList = userDal.getList(options);

        List<Group> groupList = groupDal.getList(options);

        List<Organization> orgList = organizationDal.getList(options);
        //select * from Organization o where o.parentId in (orgList.ids);

        // block all lookup change calls
        synchronized (Lookup.class) {
            Query query = em.createNativeQuery("DECLARE	@outputCodeProc int DECLARE	@outputMessageProc nvarchar(2000) "
                    + " EXECUTE rbac.deleteImportedTenant @name = ?, @userId = ?, @outputCode = @outputCodeProc OUTPUT, "
                    + " @outputMessage = @outputMessageProc OUTPUT "
                    + " SELECT	'outputCode' = @outputCodeProc, 'outputMessage' = @outputMessageProc");
            query.setParameter(1, tenantName);
            query.setParameter(2, auditLogInfo.getLoggedInUserId());
            result = query.getSingleResult();
            em.getEntityManagerFactory().getCache().evict(ScopeDefinition.class);
            em.getEntityManagerFactory().getCache().evict(GroupRole.class);
            em.getEntityManagerFactory().getCache().evict(OrganizationCalendar.class);
            em.getEntityManagerFactory().getCache().evict(OrganizationLogo.class);
            //TenantIdentifier is embeddable, can't be removed like this
            //em.getEntityManagerFactory().getCache().evict(TenantIdentifier.class);
            em.getEntityManagerFactory().getCache().evict(TenantLogo.class);
            em.getEntityManagerFactory().getCache().evict(AuditLog.class);

            //Lookup.fillUserLookupTable(userDal.getUserIdNames(null));
            if (userList != null && !userList.isEmpty()) {
                for (User user : userList) {
                    em.getEntityManagerFactory().getCache().evict(User.class, user.getUserId());
                    Lookup.deleteFromUserLookupTable(user.getUserId());
                }
            }
            //Lookup.fillGroupLookupTable(groupDal.getGroupIdNames(null));
            if (groupList != null && !groupList.isEmpty()) {
                for (Group group : groupList) {
                    em.getEntityManagerFactory().getCache().evict(Group.class, group.getGroupId());
                    Lookup.deleteFromGroupLookupTable(group.getGroupId());
                }
            }
            Lookup.fillOrganizationDefaultCalendars(organizationCalendarDal.getOrganizationDefaultWorkCalendar(),
                    organizationCalendarDal.getOrganizationDefaultHolidayCalendars());

            //Lookup.fillorganizations(organizationDal.getOrganizationIdNamesDetails(null));
            if (orgList != null && !orgList.isEmpty()) {
                for(Organization org: orgList){
                    em.getEntityManagerFactory().getCache().evict(Organization.class, org.getOrganizationId());
                }
                Lookup.deleteFromOrganizationtLookupTable(orgList);
            }
            //delete from tenantLookupTable
            em.getEntityManagerFactory().getCache().evict(Tenant.class, Lookup.getTenantIdByName(tenantName));
            Lookup.deleteFromTenantLookupTable(Lookup.getTenantIdByName(tenantName));

        }
        log.info("refreshCacheForTenant; tenantName={}; ends; timeTakenInMs={};", tenantName, System.currentTimeMillis()-startTime);
        if(result!=null && result instanceof Object[]){
            Map<String, Object> resultMap = new LinkedHashMap<String, Object>();
            resultMap.put("outputCode", ((Object[])(result))[0]);
            resultMap.put("outputMessage", ((Object[])(result))[1]);
            return new Gson().toJson(resultMap);
        }
        return new Gson().toJson(result);
    }

    @Transactional(propagation = Propagation.REQUIRED)
    public String deleteAndrefreshCacheForActionRuleMapping(String tenantName, AuditLogInfo auditLogInfo) {
        Object result = null;
        log.info("deleteAndrefreshCacheForActionRuleMapping; tenantName={}; starts;", tenantName);
        long startTime = System.currentTimeMillis();
        // block all lookup change calls
        synchronized (Lookup.class) {
            Query query = em.createNativeQuery("DECLARE	@outputCodeProc int DECLARE	@outputMessageProc nvarchar(2000) "
                    + " EXECUTE contact.deleteImportedActionRuleMapping @name = ?, @userId = ?, @outputCode = @outputCodeProc OUTPUT, "
                    + " @outputMessage = @outputMessageProc OUTPUT "
                    + " SELECT	'outputCode' = @outputCodeProc, 'outputMessage' = @outputMessageProc");
            query.setParameter(1, tenantName);
            query.setParameter(2, auditLogInfo.getLoggedInUserId());
            result = query.getSingleResult();
            em.getEntityManagerFactory().getCache().evict(Contact.class);
            em.getEntityManagerFactory().getCache().evict(ObjectRole.class);
            em.getEntityManagerFactory().getCache().evict(MessageTemplate.class);
            em.getEntityManagerFactory().getCache().evict(SLA.class);
        }
        log.info("deleteAndrefreshCacheForActionRuleMapping; tenantName={}; ends; timeTakenInMs={};", tenantName, System.currentTimeMillis()-startTime);
        if(result!=null && result instanceof Object[]){
            Map<String, Object> resultMap = new LinkedHashMap<String, Object>();
            resultMap.put("outputCode", ((Object[])(result))[0]);
            resultMap.put("outputMessage", ((Object[])(result))[1]);
            return new Gson().toJson(resultMap);
        }
        return new Gson().toJson(result);
    }
}
