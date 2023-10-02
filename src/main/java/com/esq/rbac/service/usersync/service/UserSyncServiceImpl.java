package com.esq.rbac.service.usersync.service;

import com.esq.rbac.service.auditloginfo.domain.AuditLogInfo;
import com.esq.rbac.service.auditlog.service.AuditLogService;
import com.esq.rbac.service.basedal.BaseDalJpa;
import com.esq.rbac.service.filters.domain.Filters;
import com.esq.rbac.service.ldapuserservice.service.LdapUserServiceImpl;
import com.esq.rbac.service.user.domain.User;
import com.esq.rbac.service.user.service.UserDal;
import com.esq.rbac.service.userexternalrecord.domain.UserExternalRecord;
import com.esq.rbac.service.userexternalrecord.repository.UserExternalRecordRepository;
import com.esq.rbac.service.usersync.domain.UserSync;
import com.esq.rbac.service.usersync.repository.UserSyncRepository;
import com.esq.rbac.service.util.AuditLogHelperUtil;
import com.esq.rbac.service.util.SearchUtils;
import com.esq.rbac.service.util.dal.OptionFilter;
import com.esq.rbac.service.util.dal.Options;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.PersistenceContext;
import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import jakarta.persistence.EntityManager;

import java.util.List;
import java.util.Map;
import java.util.TreeMap;

@Service
public class UserSyncServiceImpl extends BaseDalJpa implements UserSyncService{


    private UserSyncRepository userSyncRepository;

    @Autowired
    public void setUserSyncRepository(UserSyncRepository userSyncRepository){
        this.userSyncRepository = userSyncRepository;
    }


    private UserDal userService;

    @Autowired
    public void setUserService(UserDal userService){
        this.userService = userService;
    }


    private UserExternalRecordRepository externalRecordRepository;

    @Autowired
    public void setExternalRecordRepository(UserExternalRecordRepository externalRecordRepository){
        this.externalRecordRepository = externalRecordRepository;
    }


    private AuditLogService auditLogService;

    @Autowired
    public void setAuditLogService(AuditLogService auditLogService){
        this.auditLogService = auditLogService;
    }





    private static Logger log = LoggerFactory.getLogger(UserSyncServiceImpl.class);
    private static final Map<String, String> SORT_COLUMNS;
    static {
        SORT_COLUMNS = new TreeMap<String, String>();
        SORT_COLUMNS.put("source", "us.source");
    }


    @PersistenceContext
    public void setEntityManager(EntityManager em) {
        log.trace("setEntityManager; {}", em);
        this.em = em;
        this.entityClass = UserSync.class;
    }


    @Override
    @Transactional(propagation = Propagation.REQUIRED)
    public UserSync create(UserSync userSync, AuditLogInfo auditLogInfo) {
        userSync.setCreatedBy(String.valueOf(auditLogInfo.getLoggedInUserId()));
        userSync.setCreatedOn(DateTime.now().toDate());
        userSyncRepository.save(userSync);
        auditLogService.createSyncLog(auditLogInfo.getLoggedInUserId(), userSync.getExternalRecordId(),
                auditLogInfo.getTarget(), auditLogInfo.getOperation(), getObjectChangeSetLocal(null,userSync));
        return userSync;
    }

    @Override
    public UserSync update(UserSync userSync, AuditLogInfo auditLogInfo) {
        UserSync dbUserSync = em.find(UserSync.class, userSync.getUserSyncId());
        UserSync oldUserSync = new UserSync();
        BeanUtils.copyProperties(dbUserSync, oldUserSync);
        dbUserSync.setSyncData(userSync.getSyncData());
        dbUserSync.setStatus(userSync.getStatus());
        dbUserSync.setUpdatedSyncData(userSync.getUpdatedSyncData());
        dbUserSync.setExternalRecordId(userSync.getExternalRecordId());
        dbUserSync.setUpdatedBy(String.valueOf(auditLogInfo.getLoggedInUserId().intValue()));
        dbUserSync.setUpdatedOn(DateTime.now().toDate());
        dbUserSync.setIsDeleted(false);
        dbUserSync.setUser(userSync.getUser());
        auditLogService.createSyncLog(auditLogInfo.getLoggedInUserId(), oldUserSync.getExternalRecordId(),
                auditLogInfo.getTarget(), auditLogInfo.getOperation(),
                getObjectChangeSetLocal(oldUserSync, dbUserSync));
        UserSync reUserSync = userSyncRepository.save(dbUserSync);
        return reUserSync;
    }

    @Override
    public UserSync getById(int userSyncId) {
        return userSyncRepository.findById(userSyncId).get();
    }

    @Override
    public void deleteById(Integer userSyncId, AuditLogInfo auditLogInfo) {
        UserSync oldUserSync = userSyncRepository.findById(userSyncId).get();
        userSyncRepository.discardUserSyncById(userSyncId, LdapUserServiceImpl.LDAP_DISCARD, auditLogInfo.getLoggedInUserId(),DateTime.now().toDate());
        UserSync dbUserSync = userSyncRepository.findById(userSyncId).get();
        auditLogService.createSyncLog(auditLogInfo.getLoggedInUserId(), oldUserSync.getExternalRecordId(),
                auditLogInfo.getTarget(), auditLogInfo.getOperation(),
                getObjectChangeSetLocal(oldUserSync, dbUserSync));
    }

    @Override
    public void forceDeleteById(Integer userSyncId, AuditLogInfo auditLogInfo) {
        userSyncRepository.deleteById(userSyncId);
    }

    @Override
    public List<UserSync> list(Options options) {
        Filters filters = prepareFilters(options);
        // NEW com.acme.example.CustomerDetails(c.id, c.status, o.count)
        List<UserSync> userSyncList = filters.getList(em, UserSync.class, "select us from UserSync us",
                options, SORT_COLUMNS);
        return userSyncList;
    }

    @Override
    @Transactional(propagation = Propagation.SUPPORTS, readOnly = true)
    public int getCount(Options options) {
        Filters filters = prepareFilters(options);
        return filters.getCount(em, "select count(us) from UserSync us");
    }

    @Override
    public List<UserSync> searchList(Options options) {
        Filters filters = prepareFilters(options);
        filters.addCondition("(" + "lower(us.syncData) like :q "
                //+ "or lower(u.emailAddress) like :q "
                //+ " or u.groupId IN ( select g.groupId from Group g where lower(g.name) like :q )"
                //+ "or u.groupId IN ( select g.groupId from Group g where g.groupId IN ( select gr.groupId from GroupRole gr where gr.roleId IN ( select r.roleId from Role r where lower(r.name) like :q ) ) )  "
                + ")");

        filters.addParameter(SearchUtils.SEARCH_PARAM, SearchUtils.wildcarded(SearchUtils
                .getSearchParam(options, SearchUtils.SEARCH_PARAM)
                .toLowerCase()));

        List<UserSync> userSyncList = filters.getList(em, UserSync.class, "select distinct us from UserSync us ", options, SORT_COLUMNS);
        return userSyncList;
    }

    @Override
    public int getSearchCount(Options options) {
       Filters filters = prepareFilters(options);
        filters.addCondition("(" + "lower(us.syncData) like :q "
                //+ "or lower(u.emailAddress) like :q "
                //+ " or u.groupId IN ( select g.groupId from Group g where lower(g.name) like :q )"
                //+ "or u.groupId IN ( select g.groupId from Group g where g.groupId IN ( select gr.groupId from GroupRole gr where gr.roleId IN ( select r.roleId from Role r where lower(r.name) like :q ) ) )  "
                + ")");
        filters.addParameter(SearchUtils.SEARCH_PARAM, SearchUtils.wildcarded(SearchUtils
                .getSearchParam(options, SearchUtils.SEARCH_PARAM)
                .toLowerCase()));

        return filters.getCount(em, "select count(distinct us) from UserSync us ");
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRED)
    public void createUserExternalRecord(String externalRecordId,int userId) {
        UserExternalRecord userExtRecord = new UserExternalRecord();
        userExtRecord.setExternalRecordId(externalRecordId);
        userExtRecord.setUserId(userId);
        externalRecordRepository.save(userExtRecord);
    }

    @Override
    public Integer findUserIdByExternalRecordId(String externalRecordId) {
        return externalRecordRepository.findByExternalRecordId(externalRecordId).getUserId();
    }

    @Override
    public UserSync getByExternalRecordId(String externalRecordId) {
        return userSyncRepository.findByExternalRecordId(externalRecordId);
    }

    @Override
    public UserExternalRecord findExternalRecordByUserId(Integer userId) {
        return externalRecordRepository.findById(userId).orElse(null);
    }

    @Override
    public UserSync getByUserId(Integer userId) {
        return userSyncRepository.findById(userId).orElse(null);
    }

    @Override
    public void deleteUserFromUserSync(Integer userId, Integer loggedInUserId, String clientIp, Long makerCheckerId, Integer userStatus) {
    UserSync userSyncById = getByUserId(userId);
        if(userSyncById != null)
        {
            UserSync userSyncNew = getByUserId(userId);
            userSyncNew.setIsDeleted(false);
            userSyncNew.setStatus(LdapUserServiceImpl.LDAP_PENDING);
            userSyncNew.setUpdatedBy(String.valueOf(loggedInUserId));
            userSyncNew.setUpdatedOn(DateTime.now().toDate());
            // forceDeleteByUserExternalRecordId(userSyncById.getExternalrecordId());
            // forceDeleteUserExternalRecordById(userId); //RBAC-2730
            if (makerCheckerId != null && userStatus == 0) {
                // add the user again to sync
                update(userSyncNew, new AuditLogInfo(loggedInUserId, clientIp, userSyncNew.getExternalRecordId(),
                        "UserSync", "Create"));

            } else {
                // change the status to new
                update(userSyncNew, new AuditLogInfo(loggedInUserId, clientIp, userSyncNew.getExternalRecordId(),
                        "UserSync", "Update"));
            }
        }

    }

    @Override
    public void updateExistingConflicts(Integer loggedInUserId, String clientIp) {
        int addToSync = 1;
        // log.info("performFirstSync; userDetailMap={};",
        // userDetailMap);
        try {
            OptionFilter optionFilter = new OptionFilter();
            optionFilter.addFilter("type", "newUsers");
            Options options = new Options(optionFilter);
            List<UserSync> listNewUsers = list(options);

            for(UserSync userSync : listNewUsers) {
                Map<String, String> existingSyncValue = (new ObjectMapper()).readValue(userSync.getSyncData(),new TypeReference<Map<String, String>>() {});
                String userName = existingSyncValue.get(LdapUserServiceImpl.LDAP_MAPPED_USERNAME);
                User userInRBAC = userService.getByUserName(userName);
                if(userInRBAC != null) {
                    userSync.setStatus(LdapUserServiceImpl.LDAP_CONFLICT);
                    userSync.setUser(userInRBAC);
                    update(userSync, new AuditLogInfo(loggedInUserId, clientIp,"UserSync","UserSync","Update"));
                }

            }

        } catch (Exception e) {
            log.error("performNextSync; Exception={};", e);

        }
    }

    private Map<String, String> getObjectChangeSetLocal(UserSync oldUserSync, UserSync newUserSync) {
        AuditLogHelperUtil logHelperUtil = new AuditLogHelperUtil();
        //UserSync userSync = new UserSync();
        logHelperUtil.putToObjectChangeSet(OBJECTNAME,
                newUserSync != null ? newUserSync.getExternalRecordId() : oldUserSync.getExternalRecordId());
        logHelperUtil.putToObjectChangeSet(OBJECTCHANGES_USERSYNCID,
                newUserSync != null ? newUserSync.getUserSyncId().toString(): oldUserSync.getUserSyncId().toString());
        logHelperUtil.checkPutToObjectChangeSet(OBJECTCHANGES_USERSYNCNAME,
                (newUserSync != null) ? newUserSync.getExternalRecordId() : null,
                (oldUserSync != null) ? oldUserSync.getExternalRecordId() : null, null, null);
        logHelperUtil.checkPutToObjectChangeSet(OBJECTCHANGES_USERSYNCSYNC,
                (newUserSync != null) ? newUserSync.getSyncData() : null,
                (oldUserSync != null) ? oldUserSync.getSyncData() : null, null, null);

        return logHelperUtil.getObjectChangeSet();
    }

    private Filters prepareFilters(Options options) {

        Filters result = new Filters();
        OptionFilter optionFilter = options == null ? null : options.getOption(OptionFilter.class);
        Map<String, String> filters = optionFilter == null ? null : optionFilter.getFilters();
        if (filters != null) {
            String source = filters.get("source");
            if (source != null && source.length() > 0) {
                result.addCondition("us.source = :source");
                result.addParameter("source", source);
            }
            String externalRecordId = filters.get("externalRecordId");
            if (externalRecordId != null && externalRecordId.length() > 0) {
                result.addCondition("us.externalRecordId = :externalRecordId");
                result.addParameter("externalRecordId", externalRecordId);
            }

            String filterType = filters.get("type");
            if (filterType != null && !filterType.isEmpty()) {

                if (filterType.equalsIgnoreCase("newUsers")) {
                    result.addCondition("us.status =:status");
                    result.addParameter("status", LdapUserServiceImpl.LDAP_PENDING);
                    result.addCondition(
                            "us.externalRecordId not in (select u.externalRecordId from  UserExternalRecord u )");

                } else if (filterType.equalsIgnoreCase("updatedUsers")) {
                    result.addCondition("us.status =:status");
                    result.addParameter("status",LdapUserServiceImpl.LDAP_UPDATED);
                    result.addCondition(
                            "us.externalRecordId  in (select u.externalRecordId from  UserExternalRecord u )");

                } else if (filterType.equalsIgnoreCase("deletedUsers")) {
                    result.addCondition("us.isDeleted = 1");


                } else if (filterType.equalsIgnoreCase("conflictedUsers")) {
                    result.addCondition("us.status =:status");
                    result.addParameter("status",LdapUserServiceImpl.LDAP_CONFLICT);

                }
            }

        }
        return result;
    }
}
