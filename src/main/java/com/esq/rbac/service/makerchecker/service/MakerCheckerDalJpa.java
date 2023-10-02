package com.esq.rbac.service.makerchecker.service;

import java.io.IOException;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import com.esq.rbac.service.auditlog.service.AuditLogService;
import com.esq.rbac.service.basedal.BaseDalJpa;
import com.esq.rbac.service.exception.ErrorInfoException;
import com.esq.rbac.service.filters.domain.Filters;
import com.esq.rbac.service.lookup.Lookup;
import com.esq.rbac.service.makerchecker.domain.MakerChecker;
import com.esq.rbac.service.makerchecker.repository.MakerCheckerRepository;
import com.esq.rbac.service.makerchecker.makercheckerlog.domain.MakerCheckerLog;
import com.esq.rbac.service.makerchecker.makercheckerlog.repository.MakerCheckerLogRepository;
import com.esq.rbac.service.makerchecker.makercheckerlog.service.MakerCheckerLogDal;
import com.esq.rbac.service.user.domain.User;
import com.esq.rbac.service.user.service.UserDal;
import com.esq.rbac.service.user.vo.UserWithLogoutData;
import com.esq.rbac.service.util.RBACUtil;
import com.esq.rbac.service.util.SearchUtils;
import com.esq.rbac.service.util.dal.OptionFilter;
import com.esq.rbac.service.util.dal.Options;
import jakarta.persistence.*;

import jakarta.ws.rs.core.MultivaluedMap;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.UriInfo;
import lombok.extern.slf4j.Slf4j;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
@Service
@Slf4j
public class MakerCheckerDalJpa extends BaseDalJpa implements MakerCheckerDal {

    public static final String PENDING_STATUS = "0,4,3";
    public static final String REJECTED_STATUS = "2";

    public static final Integer PENDING_REQUEST = 0;
    public static final Integer APPROVE_REQUEST = 1;
    public static final Integer REJECT_REQUEST = 2;
    public static final Integer REQUEST_TO_DELETE = 3;
    public static final Integer REQUEST_RESUBMITTED = 4;
    public static final Integer DELETE_REJECTED = 5;
    public static final Integer DELETE_APPROVED = 6;
    public static final Integer CANCELLED_REQUEST = 7;

    private static final Map<String, String> SORT_COLUMNS;
    public static final String MKR_SEPARATOR = "-inactive";


    private AuditLogService auditLogDal;

    @Autowired
    public void setAuditLogDal(AuditLogService auditLogDal){
        this.auditLogDal = auditLogDal;
    }



    public UserDal userDal;

    @Autowired
    public void setUserDal(UserDal userDal){
        this.userDal = userDal;
    }


    private MakerCheckerLogDal makerCheckerLogDal;

    @Autowired
    public void setMakerCheckerLogDal(MakerCheckerLogDal makerCheckerLogDal){
        this.makerCheckerLogDal = makerCheckerLogDal;
    }


    private MakerCheckerLogRepository makerCheckerLogRepository;

    @Autowired
    public void setMakerCheckerLogRepository(MakerCheckerLogRepository makerCheckerLogRepository){
        this.makerCheckerLogRepository = makerCheckerLogRepository;
    }


    private MakerCheckerRepository makerCheckerRepository;

    @Autowired
    public void setMakerCheckerRepository(MakerCheckerRepository makerCheckerRepository){
        this.makerCheckerRepository = makerCheckerRepository;
    }

    static {
        SORT_COLUMNS = new TreeMap<String, String>();
        SORT_COLUMNS.put("isValid", "m.isValid");
    }


    @Override
    @Transactional(propagation = Propagation.SUPPORTS, readOnly = true)
    public List<Map<String, Object>> searchMakerCheckerInfo(Options options) {
        Filters filters = prepareFilters(options);
        filters.addCondition("("
                + "lower(m.entityJson) like :q or lower(m.entityName) like :q or lower(m.rejectReason) like :q or m.transactionBy IN ( select u.userId from User u where lower(u.userName) like :q ))");// RBAC-1997
        filters.addParameter(SearchUtils.SEARCH_PARAM,
                SearchUtils.wildcarded(SearchUtils.getSearchParam(options, SearchUtils.SEARCH_PARAM).toLowerCase()));
        List<Map<String, Object>> returnObj = getSearchResult(filters, options);
        return returnObj;
    }

    private Filters prepareFilters(Options options) {

        Filters result = new Filters();
        OptionFilter optionFilter = options == null ? null : options.getOption(OptionFilter.class);
        Map<String, String> filters = optionFilter == null ? null : optionFilter.getFilters();
        if (filters != null) {

            String entityName = filters.get("entityName");
            if (entityName != null && entityName.length() > 0) {
                result.addCondition("m.entityName = :entityName");
                result.addParameter("entityName", entityName);
            }

            String makerCheckerIdForAction = filters.get("makerCheckerIdForAction");
            if (makerCheckerIdForAction != null && makerCheckerIdForAction.length() > 0) {
                makerCheckerIdForAction = makerCheckerIdForAction.substring(1);
                log.debug("makerCheckerIdForAction {}", makerCheckerIdForAction);
                result.addCondition(" m.id IN (" + makerCheckerIdForAction + ")");
            }

//			String loggedInTenant = filters.get("loggedInTenant");
//			if (loggedInTenant != null && loggedInTenant.length() > 0) {
//				result.addCondition(" m.tenantId = :tenantId ");
//				result.addParameter("tenantId", Long.valueOf(loggedInTenant));
//			}

            String entityId = filters.get("entityId");
            if (entityId != null && entityId.length() > 0) {
                result.addCondition("m.entityId = :entityId");
                result.addParameter("entityId", Integer.valueOf(entityId));
            }

            result.addCondition(" m.isValid = 1");

            String entityToShow = filters.get("entityToShow");
            if (entityToShow != null && entityToShow.length() > 0) {
                result.addCondition(" m.entityName = :entityToShow ");
                result.addParameter("entityToShow", entityToShow);
            }

            String entityType = filters.get("type");
            if (entityType != null && entityType.length() > 0) {
                if(entityType.equalsIgnoreCase("createReq"))
                    result.addCondition(" m.entityStatus IN (" + PENDING_REQUEST + ","+REQUEST_RESUBMITTED+")");
                else if(entityType.equalsIgnoreCase("delReq"))
                    result.addCondition(" m.entityStatus IN (" + REQUEST_TO_DELETE + ")");
                else if(entityType.equalsIgnoreCase("createRej"))
                    result.addCondition(" m.entityStatus IN (" + REJECT_REQUEST + ")");

            }

            String loggedInUserName = filters.get("loggedInUserName");
            if (loggedInUserName != null && loggedInUserName.length() > 0) {
                String scope = RBACUtil.extractScopeForMakerChecker(
                        userDal.getUserScopes(loggedInUserName, RBACUtil.RBAC_UAM_APPLICATION_NAME, true), null, false);
                if (scope != null && !scope.isEmpty()) {
                    result.addCondition(" (" + scope + ") ");
                } else {
                    String loggedInTenant = filters.get("loggedInTenant");
                    if (loggedInTenant != null && loggedInTenant.length() > 0) {
                        result.addCondition(" m.tenantId = :tenantId ");
                        result.addParameter("tenantId", Long.valueOf(loggedInTenant));
                    }
                }

                String isApprover = filters.get("isApprover");
                if (isApprover != null && isApprover.length() > 0) {
                    if (isApprover.equalsIgnoreCase("false")) {
                        result.addCondition(" m.createdBy = " + Lookup.getUserId(loggedInUserName));
                    }
                }
            }
        }
        return result;
    }

    @Override
    @Transactional(propagation = Propagation.SUPPORTS, readOnly = true)
    public List<Map<String, Object>> getMakerCheckerInfo(Options options) {
        Filters filters = prepareFilters(options);
        List<Map<String, Object>> returnObj = getSearchResult(filters, options);
        return returnObj;
    }

    private List<Map<String, Object>> getSearchResult(Filters filters, Options options) {
        List<Map<String, Object>> returnObj = new LinkedList<Map<String, Object>>();
        List<Object[]> result = filters.getList(em, Object[].class, "select  m.id " + // 0
                "      ,m.entityType" + // 1
                "      ,m.entityName" + // 2
                "      ,m.entityJson" + // 3
                "      ,m.entityId" + // 4
                "      ,m.organizationId" + // 5
                "      ,m.createdBy" + // 6
                "      ,m.createdOn" + // 7
                "      ,m.transactionBy" + // 8
                "      ,m.transactionOn" + // 9
                "      ,m.rejectReason" + // 10
                "      ,m.entityStatus" + // 11
                "      ,m.isValid" + // 12
                "      ,m.tenantId" + // 13
                " from MakerChecker m", options, SORT_COLUMNS);
        if (result != null && !result.isEmpty()) {
            for (Object[] obj : result) {
                Map<String, Object> temp = new HashMap<String, Object>();
                int entityStatus = (int) obj[11];
                Object createdById = obj[6];
                Object createdOn = obj[7];
                Object transactionById = obj[8];
                Object transactionOn = obj[9];
                Object rejectReason = obj[10];
                temp.put("id", obj[0]);
                temp.put("entityType", obj[1]);
                temp.put("entityName", obj[2]);
                temp.put("entitySummary", getEntitySummary(obj[3], obj[1], obj[2], obj[4]));
                temp.put("entityId", obj[4]);
                temp.put("organizationId", obj[5]);
                temp.put("createdBy", createdById);
                temp.put("createdOn", new DateTime(createdOn, DateTimeZone.UTC).toString("yyyy-MM-dd'T'HH:mm:ss'Z"));
                temp.put("transactionOn", new DateTime(transactionOn, DateTimeZone.UTC).toString("yyyy-MM-dd'T'HH:mm:ss'Z")); // RBAC-1996
                temp.put("rejectReason", rejectReason);
                temp.put("entityStatus", entityStatus);
                temp.put("isValid", obj[12]);
                temp.put("tenantId", obj[13]);
                String actionDetails = "";
                String actionBy = "";
                if (transactionById != null) {
                    try {
                        User transactionByUser = userDal.getById((int) transactionById);
                        actionBy = transactionByUser.getUserName();
                    } catch (Exception ex) {
                        ErrorInfoException e = new ErrorInfoException("genError");
                        e.getParameters().put("value", "User does not exists with " + transactionById + " user id");
                        throw e;
                    }
                }

                if (entityStatus == 0) {// Status Pending
                    actionDetails = "Request to create a new " + obj[2];
                } else if (entityStatus == APPROVE_REQUEST) {// Status Approved
                    actionDetails = "Approved " + obj[2];
                } else if (entityStatus == REJECT_REQUEST) {// Status Rejected
                    actionDetails = "Request rejected to create/delete" + obj[2];
                } else if (entityStatus == REQUEST_TO_DELETE) { // Deleted Entry
                    actionDetails = "Request to delete " + obj[2];
                } else if (entityStatus == REQUEST_RESUBMITTED) { // Updated Entity
                    actionDetails = "Updated " + obj[2] + " and re-submitted for approval";
                } else if (entityStatus == DELETE_REJECTED) { // Rejected plea of deleted users
                    actionDetails = "Rejected request to delete " + obj[2];
                }
                temp.put("actionDetails", actionDetails);
                temp.put("remarks", rejectReason != null ? rejectReason : "");
                temp.put("actionBy", actionBy);
                temp.put("actionOn", getCurrentTimeZoneTime(transactionOn));
                returnObj.add(temp);
            }
        }
        return returnObj;
    }

    private Object getEntitySummary(Object json, Object entityType, Object entityName, Object entityId) {
        Object entSummary = null;
        if (entityName.toString().equalsIgnoreCase("User")) {

            ObjectMapper objectMapper = new ObjectMapper();
            objectMapper.setVisibility(objectMapper.getSerializationConfig().getDefaultVisibilityChecker()
                    .withFieldVisibility(Visibility.ANY).withSetterVisibility(Visibility.ANY)
                    .withGetterVisibility(Visibility.ANY));

            try {
                // User user = userDal.getById(Integer.parseInt(entityId+""));
                // //objectMapper.readValue((String) json, User.class);
                entSummary = getUserReadableDetails(entityId);
                if (entSummary == null)
                    entSummary = (objectMapper.readValue((String) json, User.class)).getUserName()
                            + " is deleted from the system";
            } catch (Exception ex) {
                ErrorInfoException e = new ErrorInfoException("genError");
                e.getParameters().put("value", ex.getMessage());
                ex.printStackTrace();
                throw e;

            }

        }

        return entSummary;
    }

    private String getUserReadableDetails(Object entityId) {
        TypedQuery<Object[]> query = em.createNamedQuery("getUserDetailsInMakerChecker", Object[].class);
        query.setParameter(1, entityId);
        try {
            Object[] list = query.getSingleResult();
            StringBuilder result = new StringBuilder();

            result.append("Username : " + extractUserNameFromMKR(list[1].toString()));
            result.append("<br/>Orgainization : " + list[7].toString());
            /*
             * if(list[2] != null && !list[2].toString().isEmpty())
             * result.append("<br/>Email : "+list[2].toString()); if(list[3] != null &&
             * !list[3].toString().isEmpty())
             * result.append("<br/>Phone : "+list[3].toString()); if(list[4] != null &&
             * !list[4].toString().isEmpty())
             * result.append("<br/>Login Enabled : "+list[4].toString()); if(list[6] != null
             * && !list[6].toString().isEmpty())
             * result.append("<br/>Is shared? : "+list[6].toString());
             */
            return result.toString();
        } catch (Exception e) {
            log.trace("no user found in user table");
            return null;
        }
    }

    /*
     * @Override
     *
     * @Transactional(propagation = Propagation.SUPPORTS, readOnly = true) public
     * List<MakerChecker> searchList(Options options) { Filters filters =
     * prepareFilters(options); filters.addCondition("(" +
     * "lower(m.entityJson) like :q or lower(m.entityName) like :q or m.createdBy IN ( select u.userId from User u where lower(u.userName) like :q ) and m.entityStatus IN (0,2))"
     * ); filters.addParameter(SearchUtils.SEARCH_PARAM,
     * SearchUtils.wildcarded(SearchUtils.getSearchParam(options,
     * SearchUtils.SEARCH_PARAM).toLowerCase())); return filters.getList(em,
     * MakerChecker.class, "select distinct m from MakerChecker m ", options,
     * SORT_COLUMNS);
     *
     * }
     *
     * @Override
     *
     * @Transactional(propagation = Propagation.SUPPORTS, readOnly = true) public
     * List<MakerChecker> getList(Options options) { Filters filters =
     * prepareFilters(options); return filters.getList(em, MakerChecker.class,
     * "select m from MakerChecker m", options, SORT_COLUMNS); }
     */

    @Override
    @Transactional(propagation = Propagation.SUPPORTS, readOnly = true)
    public int getSearchCount(Options options) {
        Filters filters = prepareFilters(options);

        filters.addCondition("("
                + "lower(m.entityJson) like :q or lower(m.entityName) like :q or m.transactionBy IN ( select u.userId from User u where lower(u.userName) like :q ))");
        filters.addParameter(SearchUtils.SEARCH_PARAM,
                SearchUtils.wildcarded(SearchUtils.getSearchParam(options, SearchUtils.SEARCH_PARAM).toLowerCase()));
        return filters.getCount(em, "select count(distinct m) from MakerChecker m ");
    }

    @Override
    @Transactional(propagation = Propagation.SUPPORTS, readOnly = true)
    public int getCount(Options options) {
        Filters filters = prepareFilters(options);
        // filters.addCondition(" m.entityStatus IN (0,2) ");
        return filters.getCount(em, "select count(m) from MakerChecker m ");
    }

    @Override
    public Object getEntityByMakerCheckerId(int makerCheckerId) {
        MakerChecker makerChecker = em.find(MakerChecker.class, makerCheckerId);
        Object entityObject = null;
        try {
            Class<?> entityClass = Class.forName(makerChecker.getEntityType());
            ObjectMapper objectMapper = new ObjectMapper();
            objectMapper.setVisibility(objectMapper.getSerializationConfig().getDefaultVisibilityChecker()
                    .withFieldVisibility(Visibility.ANY).withSetterVisibility(Visibility.ANY)
                    .withGetterVisibility(Visibility.ANY));
            String json = makerChecker.getEntityJson();
            try {
                entityObject = objectMapper.readValue(json, entityClass);
            } catch (JsonParseException ex) {
                log.error("getByMakerCheckerId; JsonParseException");
                ErrorInfoException e = new ErrorInfoException("genError");
                e.getParameters().put("value", ex.getMessage());
                ex.printStackTrace();
                throw e;
            } catch (JsonMappingException ex) {
                log.error("getByMakerCheckerId; JsonMappingException");
                ErrorInfoException e = new ErrorInfoException("genError");
                e.getParameters().put("value", ex.getMessage());
                ex.printStackTrace();
                throw e;
            } catch (IOException ex) {
                log.error("getByMakerCheckerId; IOException");
                ErrorInfoException e = new ErrorInfoException("genError");
                e.getParameters().put("value", ex.getMessage());
                ex.printStackTrace();
                throw e;
            }
        } catch (ClassNotFoundException ex) {
            log.error("getByMakerCheckerId; ClassNotFoundException");
            ErrorInfoException e = new ErrorInfoException("genError");
            e.getParameters().put("value", ex.getMessage());
            ex.printStackTrace();
            throw e;
        }

        return entityObject;
    }

    @Override
    @Transactional(propagation = Propagation.SUPPORTS, readOnly = true)
    public Boolean checkEntityPermission(int makerCheckerId, Options options) {
        Filters filters = prepareFilters(options);
        filters.addCondition(" m.id = " + makerCheckerId + " ");
        if (filters.getCount(em, "select count(m) from MakerChecker m") == 1) {
            return true;
        }
        return false;
    }

    @Override
    @Transactional(propagation = Propagation.SUPPORTS, readOnly = true)
    public Boolean checkEntityPermissionForEntity(Options options) {
        Filters filters = prepareFilters(options);
        if (filters.getCount(em, "select count(m) from MakerChecker m") == 1) {
            return true;
        }
        return false;
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRED)
    public ResponseEntity approveOrRejectMakerCheckerEntity(Options options, MakerChecker objMakerChecker,
                                                            Integer createdUserId, String clientIp, Long tenantId) {
//		ObjectMapper objectMapper = new ObjectMapper();
//		Map<String, Object> map = null;
//		try {
//			map = objectMapper.readValue(jsonPost, new TypeReference<Map<String, Object>>() {
//			});
//		} catch (IOException e) {
//			e.printStackTrace();
//		}
//		log.info("=================map======================");
//		log.info("{}", map);
        if (objMakerChecker != null) {
//			String makerCheckerIds = map.get("makerCheckerIds") + "";
//			Integer isApprove = Integer.parseInt(map.get("isApprove") + "");
//			String rejectReason = (String) map.get("rejectReason");
            log.info("makerCheckerIds = {}", objMakerChecker.getMakerCheckerIdsForAction());
            return approveOrRejectEntity(objMakerChecker, createdUserId, clientIp, tenantId);
        }
        return ResponseEntity.ok().body("Ok");
    }

    private ResponseEntity<Integer> approveOrRejectEntity(MakerChecker objMakerChecker, Integer createdUserId, String clientIp,
                                           Long tenantId) {

        User loggedInUserDetails = userDal.getByUserName(objMakerChecker.getTransactionByName());
        Integer loggedInUserId = loggedInUserDetails.getUserId();
        Integer actionFlag = objMakerChecker.getIsApproveFlag();
        String rejectReason = objMakerChecker.getRejectReason();

        /*
         * 1 = Approve 2 = Reject 3 = Delete 4 = Resave Rejected and again submit for
         * approval 5 = Rejected plea of deleted user
         */
        Integer oldStatusOfRequest = null;
        if (objMakerChecker != null && objMakerChecker.getMakerCheckerIdsForAction() != null) {
            String arrIds[] = objMakerChecker.getMakerCheckerIdsForAction().split(",");
            for (int i = 1; i < arrIds.length; i++) {
                // Integer makerId = Integer.parseInt(arrIds[i]);
                Long makerId = Long.parseLong(arrIds[i]);
                MakerChecker mkrckr = em.find(MakerChecker.class, makerId);
                isSameTenantUser(tenantId, mkrckr, objMakerChecker.getTransactionByName());
                oldStatusOfRequest = mkrckr.getEntityStatus();

                checkUserCheckerPermission(mkrckr, actionFlag, loggedInUserId, tenantId);
                mkrckr.setRejectReason(rejectReason);
                if (actionFlag == APPROVE_REQUEST) {
                    // Approval can be of creation or deletion
                    log.info("in approve======");
                    if (oldStatusOfRequest == REQUEST_TO_DELETE) // get original status of the entity, if delete
                        // was the status
                        // then delete the entity from the system else update
                        invokeDeleteMethodsByEntityName(mkrckr.getEntityName(), mkrckr.getEntityId(),mkrckr.getEntityValue(), loggedInUserId,
                                clientIp, APPROVE_REQUEST, tenantId);
                    else
                        invokeUpdateMethodsByEntityName(mkrckr.getEntityName(), mkrckr.getEntityId(), loggedInUserId,
                                clientIp, actionFlag);
                } else if (actionFlag == REJECT_REQUEST) { // reject
                    log.info("in reject======");
                    // invokeDeleteMethodsByEntityName(mkrckr.getEntityName(), mkrckr.getEntityId(),
                    // loggedInUserId,clientIp);
                } else if (actionFlag == REQUEST_TO_DELETE) { // delete
                    log.info("in REQUEST_TO_DELETE======");
                    if (oldStatusOfRequest != REQUEST_TO_DELETE) // get original status of the entity, if delete
                        // was the original status then do nothing else
                        // delete the entity
                        invokeDeleteMethodsByEntityName(mkrckr.getEntityName(), mkrckr.getEntityId(), mkrckr.getEntityValue(),loggedInUserId,
                                clientIp, 0, tenantId);
                } else if (actionFlag == REQUEST_RESUBMITTED) { // resubmit rejected entries after changes
                    log.info("in REQUEST_RESUBMITTED======");
                    invokeUpdateMethodsByEntityName(mkrckr.getEntityName(), mkrckr.getEntityId(), loggedInUserId,
                            clientIp, actionFlag);
                }

                else if (actionFlag == CANCELLED_REQUEST) { // cancelRequest for deletion or creation

                    if(oldStatusOfRequest != REQUEST_TO_DELETE)
                        invokeDeleteMethodsByEntityName(mkrckr.getEntityName(), mkrckr.getEntityId(),  mkrckr.getEntityValue(),loggedInUserId,
                                clientIp, APPROVE_REQUEST, tenantId);
                }
                updateMakerChecker(mkrckr, loggedInUserId, actionFlag);
            }
        }
        return ResponseEntity.ok().body(oldStatusOfRequest);
    }

    private void isSameTenantUser(Long tenantId, MakerChecker mkrckr, String transactionByName) {

        /* check user id corresponding to makerId */
        try {
            OptionFilter optionFilter = new OptionFilter();
            optionFilter.addFilter(RBACUtil.CHECK_ENTITY_PERM_IDENTIFIER, Integer.toString(mkrckr.getEntityId()));
            optionFilter.addFilter("entityName", mkrckr.getEntityName());
            optionFilter.addFilter("loggedInUserName", transactionByName);
            Options options = new Options(optionFilter);
            if(!checkEntityPermissionForEntity(options))
            {
                String entityValue = mkrckr.getEntityValue();
                StringBuilder sb = new StringBuilder();
                sb.append(MKR_OPERATION_NOT_ALLOWED).append("; ");
                sb.append(MKR_OPERATION_NOT_ALLOWED_NAME).append("=").append(entityValue);
                log.info("{}", sb.toString());
                ErrorInfoException errorInfo = new ErrorInfoException(MKR_OPERATION_NOT_ALLOWED, sb.toString());
                errorInfo.getParameters().put(MKR_OPERATION_NOT_ALLOWED_NAME, entityValue);
                throw errorInfo;
            }
            // add scope here

        } catch (Exception e) {
            throw e;

        }
        /* check the tenant id for user id */

        /*
         * if logged in tenant id and user tenant id is same return true else return
         * error message
         */

    }

    @Override
    @Transactional(propagation = Propagation.REQUIRED)
    public MakerChecker createEntry(Object objectToBeAdded, Class<?> entity, int createUserId, String target,
                                    String operation, Integer entityId, Long loggedInTenantId) {

        if (objectToBeAdded == null) {

            throw new IllegalArgumentException();
        }
        User user = null;

        try {
            if (objectToBeAdded != null)
                user = (User) objectToBeAdded;

        } catch (Exception ex) {

            log.info("Error in converting Object(objectToBeAdded) to user ={}", entity);
            ErrorInfoException e = new ErrorInfoException("genError");
            e.getParameters().put("value", ex.getMessage());
            ex.printStackTrace();
            throw e;
        }
        ObjectMapper objectMapper = new ObjectMapper().setVisibility(PropertyAccessor.FIELD, Visibility.ANY);
        objectMapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
        String entityJson = null;
        try {
            entityJson = objectMapper.writeValueAsString(objectToBeAdded);
        } catch (IOException ex) {
            log.info("Error in converting Object to JSON ={}", entity);
            ErrorInfoException e = new ErrorInfoException("genError");
            e.getParameters().put("value", ex.getMessage());
            ex.printStackTrace();
            throw e;
        }
        MakerChecker mkck = new MakerChecker();

        mkck.setTransactionBy(createUserId);
        Date createdDate = DateTime.now().toDate();
        mkck.setTransactionOn(createdDate);
        mkck.setCreatedBy(createUserId);
        mkck.setCreatedOn(createdDate);
        mkck.setIsValid(true);
        mkck.setEntityType(entity.getName());
        mkck.setEntityName(entity.getSimpleName());
        mkck.setEntityJson(entityJson.toString());
        if (operation.equalsIgnoreCase("delete")) {
            mkck.setEntityStatus(REQUEST_TO_DELETE); // delete status
            mkck.setRejectReason("");
        } else {
            operation = "Create";
            mkck.setEntityStatus(PENDING_REQUEST); // pending status
        }
        if (user != null) {
            mkck.setOrganizationId(user.getOrganizationId());
            mkck.setEntityValue(user.getUserName());
        }
        mkck.setTenantId(loggedInTenantId);
        mkck.setEntityId(entityId);
        try {
            makerCheckerRepository.saveAndFlush(mkck);
            makerCheckerLogDal.createEntry(mkck);
            Map<String, String> objectChanges = new TreeMap<String, String>();
            if (operation.equalsIgnoreCase("delete"))
                objectChanges.put("", "Deleted "+entity.getSimpleName()+" - "+mkck.getEntityValue()+" and sent for approval");
            else
                objectChanges.put("", "Added " + entity.getSimpleName() +" - "+mkck.getEntityValue()+" and sent for approval");
            auditLogDal.createSyncLog(createUserId, operation+" Entity", target, operation, objectChanges);
        } catch (Exception ex) {
            ErrorInfoException e = new ErrorInfoException("genError");
            log.info(ex.getMessage());
            String message = (ex.getCause() != null) ? ex.getCause().getMessage() : ex.getMessage();
            if (ex.getMessage().indexOf("duplicate key") > -1)
                message = "Duplicate value found in " + mkck.getEntityName() + " entity with value "+mkck.getEntityValue();
            e.getParameters().put("value", message);
            throw e;
        }
        return mkck;
    }

    /*
     * private Response approveOrRejectEntity(String makerCheckerIds, int status,
     * int loggedInUserId, String clientIp, String rejectReason, Long tenantId) {
     *
     * User user=em.find(User.class,loggedInUserId); // Long
     * loggedInUserTenantId=Lookup.getTenantIdByOrganizationId(user.
     * getOrganizationId());
     *
     *
     * 1 = Approve 2 = Reject 3 = Delete 4 = Resave Rejected and again submit for
     * approval 5 = Rejected plea of deleted user
     *
     * Integer makerCheckerEntityStatus=null; if (makerCheckerIds != null) { String
     * arrIds[] = makerCheckerIds.split(","); for (int i = 1; i < arrIds.length;
     * i++) { // Integer makerId = Integer.parseInt(arrIds[i]); Long makerId =
     * Long.parseLong(arrIds[i]); MakerChecker mkrckr = em.find(MakerChecker.class,
     * makerId);
     * isSameTenantUser(tenantId,makerId,user.getUserName(),mkrckr.getEntityName());
     * makerCheckerEntityStatus=mkrckr.getEntityStatus();
     * checkUserCheckerPermission(mkrckr, status, loggedInUserId, tenantId);
     * mkrckr.setRejectReason(rejectReason); if (status == APPROVED_REQUEST) {//
     * approve log.info("in approve======"); if (mkrckr.getEntityStatus() ==
     * REQUEST_TO_DELETE) // get original status of the entity, if delete was the
     * status // then delete the entity from the system else update
     * invokeDeleteMethodsByEntityName(mkrckr.getEntityName(), mkrckr.getEntityId(),
     * loggedInUserId, clientIp, APPROVED_REQUEST, tenantId); else
     * invokeUpdateMethodsByEntityName(mkrckr.getEntityName(), mkrckr.getEntityId(),
     * loggedInUserId, clientIp, status); } else if (status ==REJECTED_REQUEST) { //
     * reject log.info("in reject======"); //
     * invokeDeleteMethodsByEntityName(mkrckr.getEntityName(), mkrckr.getEntityId(),
     * // loggedInUserId,clientIp); } else if (status == REQUEST_TO_DELETE) { //
     * delete log.info("in reject======"); if (mkrckr.getEntityStatus() !=
     * REQUEST_TO_DELETE) // get original status of the entity, if delete was the
     * original status then do nothing else delete the entity
     * invokeDeleteMethodsByEntityName(mkrckr.getEntityName(), mkrckr.getEntityId(),
     * loggedInUserId, clientIp, 0, tenantId); } else if (status ==
     * REQUEST_RESUBMITTED) { // resubmit rejected entries after changes
     * log.info("in reject======");
     * invokeUpdateMethodsByEntityName(mkrckr.getEntityName(), mkrckr.getEntityId(),
     * loggedInUserId, clientIp, status); } updateMakerChecker(mkrckr,
     * loggedInUserId, status); } } return
     * Response.ok().entity(makerCheckerEntityStatus).expires(new Date()).build(); }
     */

    private void checkUserCheckerPermission(MakerChecker mkrckr, int status, int loggedInUserId, Long tenantId) {
        /*
         * 1 = Approve 2 = Reject 3 = Delete 4 = Resave Rejected and again submit for
         * approval 5 = Rejected plea of deleted user
         */
        String entityValue = mkrckr.getEntityValue();
        if (status == APPROVE_REQUEST || status == REJECT_REQUEST || status == DELETE_REJECTED) // Approve/Reject
        {
            if (mkrckr.getCreatedBy().compareTo(loggedInUserId) == 0
                    || mkrckr.getTransactionBy().compareTo(loggedInUserId) == 0) {
                StringBuilder sb = new StringBuilder();
                sb.append(MKR_APPROVE_REJECT_NOT_ALLOWED).append("; ");
                sb.append(MKR_APPROVE_REJECT_NOT_ALLOWED_NAME).append("=").append(entityValue);
                log.info("{}", sb.toString());
                ErrorInfoException errorInfo = new ErrorInfoException(MKR_APPROVE_REJECT_NOT_ALLOWED, sb.toString());
                errorInfo.getParameters().put(MKR_APPROVE_REJECT_NOT_ALLOWED_NAME, entityValue);
                throw errorInfo;
            }
        } else if (status == REQUEST_TO_DELETE) // Delete
        {
            if (mkrckr.getCreatedBy() != loggedInUserId) {
                StringBuilder sb = new StringBuilder();
                sb.append(MKR_DELETE_NOT_ALLOWED).append("; ");
                sb.append(MKR_DELETE_NOT_ALLOWED_NAME).append("=").append(entityValue);
                log.info("{}", sb.toString());
                ErrorInfoException errorInfo = new ErrorInfoException(MKR_DELETE_NOT_ALLOWED, sb.toString());
                errorInfo.getParameters().put(MKR_DELETE_NOT_ALLOWED_NAME, entityValue);
                throw errorInfo;
            }
        } else if (status == REQUEST_RESUBMITTED) {
            if (mkrckr.getCreatedBy() != loggedInUserId) {
                StringBuilder sb = new StringBuilder();
                sb.append(MKR_UPDATE_NOT_ALLOWED).append("; ");
                sb.append(MKR_UPDATE_NOT_ALLOWED_NAME).append("=").append(entityValue);
                log.info("{}", sb.toString());
                ErrorInfoException errorInfo = new ErrorInfoException(MKR_UPDATE_NOT_ALLOWED, sb.toString());
                errorInfo.getParameters().put(MKR_UPDATE_NOT_ALLOWED_NAME, entityValue);
                throw errorInfo;
            }
        }else if (status == CANCELLED_REQUEST) {
            if (mkrckr.getCreatedBy() != loggedInUserId) {
                StringBuilder sb = new StringBuilder();
                sb.append(MKR_OPERATION_NOT_ALLOWED).append("; ");
                sb.append(MKR_OPERATION_NOT_ALLOWED_NAME).append("=").append(entityValue);
                log.info("{}", sb.toString());
                ErrorInfoException errorInfo = new ErrorInfoException(MKR_OPERATION_NOT_ALLOWED, sb.toString());
                errorInfo.getParameters().put(MKR_OPERATION_NOT_ALLOWED_NAME, entityValue);
                throw errorInfo;
            }
        }
    }

    @Transactional(propagation = Propagation.REQUIRED)
    private void invokeDeleteMethodsByEntityName(String entityName, Integer entityId, String entityValue, int loggedInUserId,
                                                 String clientIp, int approveDelete, Long tenantId) {

        if (entityName != null) {
            try {
                if (entityName.equalsIgnoreCase("User")) {

                    if(!userDal.isUserAssociatedinDispatchContact(entityId))
                        userDal.softDeleteById(entityId, loggedInUserId, clientIp, approveDelete, tenantId);
                    else
                    {
                        StringBuilder sb = new StringBuilder();
                        sb.append(MKR_ENTITY_DISPATCH).append("; ");
                        sb.append(MKR_ENTITY_DISPATCH_NAME).append("=").append(entityValue);
                        log.info("delete User; {}", sb.toString());
                        ErrorInfoException errorInfo = new ErrorInfoException(MKR_ENTITY_DISPATCH, sb.toString());
                        errorInfo.getParameters().put(MKR_ENTITY_DISPATCH_NAME, entityValue);
                        log.info("delete User error={}", errorInfo);
                        throw errorInfo;
                    }
                }
            } catch (Exception ex) {
                throw ex;
            }
        } else {
            ErrorInfoException e = new ErrorInfoException(MKR_ENTITY_NOT_NULL);
            throw e;
        }
    }

    @Transactional(propagation = Propagation.REQUIRED)
    private void invokeUpdateMethodsByEntityName(String entityName, Integer entityId, int loggedInUserId,
                                                 String clientIp, int status) {
        if (entityName != null) {
            try {
                if (entityName.equalsIgnoreCase("User") && status != 4) {
                    User user = new User();
                    user.setUserId(entityId);
                    user.setIsStatus(status);// passing approve/reject
                    UserWithLogoutData userWithLogoutData = userDal.update(user, loggedInUserId, clientIp);
                    Lookup.updateUserLookupTable(userWithLogoutData.getUser());
                }
            } catch (Exception ex) {
                log.error("{}", ex);
                ErrorInfoException e = new ErrorInfoException("genError");
                e.getParameters().put("value", (ex.getMessage() != null) ? ex.getMessage() : "Internal Server Error");
                throw e;
            }
        } else {
            ErrorInfoException e = new ErrorInfoException(MKR_ENTITY_NOT_NULL);
            throw e;
        }
    }

    @Transactional(propagation = Propagation.REQUIRED)
    private void updateMakerChecker(MakerChecker mkrckr, int loggedInUserId, Integer status) {
        Date appDt = DateTime.now().toDate();
        int entityStatus = -1;
        String rejectReason = mkrckr.getRejectReason();
        String objMessage =  rejectReason != null && !rejectReason.isEmpty() ? (", Remarks: " + rejectReason) : "";
        String logName = "";
        String logOperation = "Approve";
        String errMessage = "Error in " + mkrckr.getEntityName()+" - "+mkrckr.getEntityValue();
        if (status == APPROVE_REQUEST) {// approve
            entityStatus = 1;
            errMessage += " approval";
            objMessage = "Action: Approved Request for "+mkrckr.getEntityValue() + objMessage;
            logName = "Approve Entity";
        } else if (status == REJECT_REQUEST) { // reject
            if (mkrckr.getRejectReason() != null && !mkrckr.getRejectReason().trim().isEmpty()) {
                errMessage += " rejection";
                objMessage = "Action: Rejected Request For "+mkrckr.getEntityValue() + objMessage;
                logName = "Reject Entity";
                int originalStatus = mkrckr.getEntityStatus();
                if (originalStatus == REQUEST_TO_DELETE) {// originally deleted but the delete request is rejected,
                    // therefore remove the
                    // maker checker entity only and do not delete the original entity
                    entityStatus = DELETE_REJECTED; // rejected the plea to remove entity
                } else {
                    entityStatus = REJECT_REQUEST;
                }
            } else {
                ErrorInfoException e = new ErrorInfoException(MKR_REASON_NOT_NULL);
                throw e;
            }
        } else if (status == REQUEST_TO_DELETE) { // delete
            entityStatus = DELETE_APPROVED;
            errMessage += " deletion";
            objMessage = "Action: Deleted "+mkrckr.getEntityName() +" - "+mkrckr.getEntityValue()+ objMessage;
            logName = "Delete Entity";

        } else if (status == REQUEST_RESUBMITTED) {// resubmit rejected entries after changes
            entityStatus = REQUEST_RESUBMITTED;
            errMessage += " resubmission";
            objMessage = "Action: Resubmitted "+mkrckr.getEntityName()+" - "+mkrckr.getEntityValue()+" for approval" + objMessage;
            logName = "Resubmitted Entity";
        }

        else if (status == CANCELLED_REQUEST) { // cancel the create or delete request
            entityStatus =  CANCELLED_REQUEST;
            errMessage += " cancelation";
            objMessage = "Action: Request removed from the system for "+mkrckr.getEntityName()+" "+mkrckr.getEntityValue() + objMessage;
            logName = "Request Removed";
        }

        try {
            if((mkrckr.getEntityStatus().compareTo(PENDING_REQUEST) == 0
                    || mkrckr.getEntityStatus().compareTo(REQUEST_RESUBMITTED) == 0
                    || mkrckr.getEntityStatus().compareTo(REJECT_REQUEST) == 0)
                    && status.compareTo(CANCELLED_REQUEST) == 0) {

            }else {
                makerCheckerRepository.updateMakerCheckerEntity(mkrckr.getId(),entityStatus,loggedInUserId,appDt,rejectReason);
                mkrckr.setEntityStatus(entityStatus);
                mkrckr.setRejectReason(rejectReason);
                mkrckr.setTransactionBy(loggedInUserId);
                mkrckr.setTransactionOn(appDt);
                makerCheckerLogDal.createEntry(mkrckr);
            }
        } catch (Exception ex) {
            ErrorInfoException e = new ErrorInfoException("genError");
            e.getParameters().put("value", errMessage);
            ex.printStackTrace();
            throw e;
        }
        Map<String, String> objectChanges = new TreeMap<String, String>();
        objectChanges.put("", objMessage);
        auditLogDal.createSyncLog(loggedInUserId, logName, mkrckr.getEntityName(), logOperation, objectChanges);

    }

    @Override
    @Transactional(propagation = Propagation.REQUIRED)
    public MakerChecker getById(Long makerCheckerId) {
        try {
            MakerChecker makerChecker= makerCheckerRepository.findById(makerCheckerId).get();
            log.debug("id = {}", makerChecker.getId());
            return makerChecker;
        } catch (Exception ex) {
            StringBuilder sb = new StringBuilder();
            sb.append(MKR_NO_RECORDS).append("; ");
            sb.append(MKR_NO_RECORDS_ID).append("=").append(makerCheckerId);
            log.info("findById; {}", sb.toString());
            ErrorInfoException errorInfo = new ErrorInfoException(MKR_NO_RECORDS, sb.toString());
            errorInfo.getParameters().put(MKR_NO_RECORDS_ID, makerCheckerId + "");
            ex.printStackTrace();
            throw errorInfo;
        }
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRED)
    public void deleteById(Long long1) {
        MakerChecker mkrckr = makerCheckerRepository.findById(long1).get();
        mkrckr.setEntityStatus(DELETE_APPROVED); // deleted status
        makerCheckerRepository.save(mkrckr);
    }

    @Override
    public MakerChecker updateEntry(Object objectToBeUpdated, int loggedInUserId, Integer entityId, Integer actionStatus,
                                    Long makerCheckerId,Boolean updateDeleteStatus) {

        if (objectToBeUpdated == null) {

            throw new IllegalArgumentException();
        }
        ObjectMapper objectMapper = new ObjectMapper().setVisibility(PropertyAccessor.FIELD, Visibility.ANY);
        objectMapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
        String entityJson = null;
        String operation="";
        try {
            entityJson = objectMapper.writeValueAsString(objectToBeUpdated);
        } catch (IOException ex) {
            log.info("Error in converting Object to JSON ={}");
            ErrorInfoException e = new ErrorInfoException("genError");
            e.getParameters().put("value", ex.getMessage());
            ex.printStackTrace();
            throw e;
        }
        MakerChecker mkck = getById(makerCheckerId);
        if(actionStatus != null && actionStatus > -1)
            mkck.setEntityStatus(actionStatus);
        // User entityUpdated = userDal.getById(loggedInUserId);

        String getEntityValue = getEntityValueByObject(mkck,objectToBeUpdated);

        mkck.setTransactionBy(loggedInUserId);
        Date updDate = DateTime.now().toDate();
        mkck.setTransactionOn(updDate);
        mkck.setEntityJson(entityJson.toString());
        String msg = "Submitted " + mkck.getEntityName() + " - "+getEntityValue+ " for approval";
        if (mkck.getEntityStatus() == REQUEST_TO_DELETE || mkck.getEntityStatus() == DELETE_REJECTED
                || mkck.getEntityStatus() == DELETE_APPROVED || mkck.getEntityStatus().equals(APPROVE_REQUEST) || updateDeleteStatus) {
            operation = "Delete";
            msg = "Request to deleted " + mkck.getEntityName() + " - "+getEntityValue+" submitted for approval";
            mkck.setCreatedBy(loggedInUserId);
            mkck.setRejectReason("");
            mkck.setEntityStatus(REQUEST_TO_DELETE);
        } /* added by pankaj 06/12/2018 */
        else if(mkck.getEntityStatus() ==  REQUEST_RESUBMITTED) {
            operation = "Update";
            msg = "Request resubmitted for " + mkck.getEntityName() + " - "+getEntityValue;
            mkck.setEntityStatus(REQUEST_RESUBMITTED);
        }
        try {

            makerCheckerRepository.saveAndFlush(mkck);
            /* Added By Pankaj For maintaining log 06/12/2018 start */
            if(operation != "") {
                makerCheckerLogDal.createEntry(mkck);
                /* Added By Pankaj For maintaining log 06/12/2018 end */
                Map<String, String> objectChanges = new TreeMap<String, String>();
                objectChanges.put("", msg);
                auditLogDal.createSyncLog(loggedInUserId, operation+" Entity", mkck.getEntityName(), operation, objectChanges);
            }
        } catch (Exception ex) {
            ErrorInfoException e = new ErrorInfoException("genError");
            log.info(ex.getMessage());
            String message = (ex.getCause() != null) ? ex.getCause().getMessage() : ex.getMessage();
            if (ex.getMessage().indexOf("duplicate key") > -1)
                message = "Duplicate value found in " + mkck.getEntityName() + " entity with value "+getEntityValue;
            e.getParameters().put("value", message);
            throw e;
        }
        return mkck;

    }

    private String getEntityValueByObject(MakerChecker mkck, Object objectToBeUpdated) {


        String entityValue = "";
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.setVisibility(objectMapper.getSerializationConfig().getDefaultVisibilityChecker()
                .withFieldVisibility(Visibility.ANY).withSetterVisibility(Visibility.ANY)
                .withGetterVisibility(Visibility.ANY));

        try {

            if(mkck.getEntityName().equalsIgnoreCase("User")) {
                User entSummary = (objectMapper.readValue((String) objectToBeUpdated, User.class));
                entityValue = entSummary.getUserName();
            }
        }catch(Exception e) {
            log.error("Error in mapping object");
            entityValue = "";

        }
        return entityValue;
    }

    public static String getCurrentTimeZoneTime(Object dateObj) {
        if (dateObj == null) {
            return "";
        }
        DateTime currentTime = new DateTime(dateObj);
        return new DateTime(currentTime).toString("MM/dd/yyyy HH:mm:ss");
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRED)
    public List<MakerChecker> getByEntityIdAndEntity(Integer entityId, Class<?> entity) {
        List<MakerChecker> list = makerCheckerRepository.getMakerCheckerByEntityIdAndEntityName(entityId,entity.getSimpleName());

        if (list != null && list.size() > 0)
            return list;
        else
            return null;
    }

    @Override
    public List<Map<String, Object>> getHistoryByMakerCheckerId(int makerCheckerId) {


        List<MakerCheckerLog> logHist = makerCheckerLogDal.getByMakerCheckerId(makerCheckerId);
        List<Map<String, Object>> returnObj = new LinkedList<Map<String, Object>>();
        /*
         * 1 = Approve, 2 = Reject, 3 = Delete, 4 = Resave Rejected and again submit for
         * approval, 5 = Rejected plea of deleted user
         */
        if (logHist != null && !logHist.isEmpty()) {
            for (MakerCheckerLog obj : logHist) {
                Map<String, Object> hist = new HashMap<String, Object>();
                String actionDetails = "";
                String entityName = obj.getEntityName();
                User actionByUser = userDal.getById(obj.getTransactionBy());
                String actionBy = "";
                if(actionByUser != null)
                    actionBy = actionByUser.getUserName();
                int entityStatus = obj.getEntityStatus();
                if (entityStatus == PENDING_REQUEST) {// Status Pending
                    actionDetails = "Request to create a new " + entityName;
                } else if (entityStatus == APPROVE_REQUEST) {// Status Approved
                    actionDetails = "Approved " + entityName;
                } else if (entityStatus == REJECT_REQUEST) {// Status Rejected
                    actionDetails = "Request rejected to create/delete" + entityName;
                } else if (entityStatus == REQUEST_TO_DELETE) { // Deleted Entry
                    actionDetails = "Request to delete " + entityName;
                } else if (entityStatus == REQUEST_RESUBMITTED) { // Updated Entity
                    actionDetails = "Updated " + entityName + " and re-submitted for approval";
                } else if (entityStatus == DELETE_REJECTED) { // Rejected plea of deleted users
                    actionDetails = "Rejected request to delete " + entityName;
                } else if (entityStatus == CANCELLED_REQUEST) { // Request removed
                    actionDetails = "Cancelled request.";
                }
                hist.put("actionDetails", actionDetails);
                hist.put("remarks", obj.getRejectReason() != null && !obj.getRejectReason().isEmpty() ? obj.getRejectReason() : "");
                hist.put("entityValue",obj.getEntityValue());
                hist.put("actionBy", actionBy);
                hist.put("actionOn", getCurrentTimeZoneTime(obj.getTransactionOn()));
                returnObj.add(hist);
            }
        }
        return returnObj;
    }

    /*
     * Boolean isSameTenantUser(Long tenantId, Long makerCheckerId, String
     * loggerInUserName, String entityName) {
     *
     * check user id corresponding to makerId
     *
     * User user = null; try { user =
     * userDal.getUserByMakerCheckerId(makerCheckerId);
     *
     * OptionFilter optionFilter = new OptionFilter();
     * optionFilter.addFilter(RBACUtil.CHECK_ENTITY_PERM_IDENTIFIER,
     * Integer.toString(user.getUserId())); optionFilter.addFilter("entityName",
     * entityName); optionFilter.addFilter("loggedInUserName", loggerInUserName);
     * Options options = new Options(optionFilter);
     * checkEntityPermissionForEntity(options);
     *
     * return true;
     *
     * // add scope here
     *
     * } catch (Exception e) { e.printStackTrace(); ErrorInfoException e1 = new
     * ErrorInfoException(MKR_OPERATION_NOT_ALLOWED); throw e1;
     *
     * } check the tenant id for user id
     *
     *
     * if logged in tenant id and user tenant id is same return true else return
     * error message
     *
     *
     * ErrorInfoException e = new ErrorInfoException(MKR_OPERATION_NOT_ALLOWED);
     * throw e; }
     */

    @Override
    @Transactional(propagation = Propagation.REQUIRED)
    public void deleteEntryByEntityId(Integer entityId, Class<?> entity) {
        List<MakerChecker> alreadyExist = getByEntityIdAndEntity(entityId, entity);
        alreadyExist.forEach(entry ->{
            makerCheckerLogDal.deleteHistoryByMakerCheckerId(entry.getId());
        });
        makerCheckerRepository.deleteEntryByEntityId(entityId,entity.getSimpleName());
    }

    @Override
    public Boolean checkIfEntityIsEditable(UriInfo uriInfo) {
        Boolean isEditable = false;
        OptionFilter optionFilter = new OptionFilter(uriInfo.getQueryParameters());
        Options options = new Options(optionFilter);
        Boolean checkPerm = checkEntityPermissionForEntity(options);
        if (checkPerm) {
            String makerCheckerId = optionFilter.getFilter("makerCheckerId");
            String loggedInUserName = optionFilter.getFilter("loggedInUserName");
            User user = userDal.getByUserName(loggedInUserName);
            log.debug("user loggedin {}", user);

            if (user != null && makerCheckerId != null && makerCheckerId.length() > 0) {
                MakerChecker mkrckr = getById(Long.valueOf(makerCheckerId));
                log.debug("mkrckr entity {}", mkrckr);

                Integer status = mkrckr.getEntityStatus();

                if (status == APPROVE_REQUEST || status == REJECT_REQUEST || status == DELETE_REJECTED
                        || status == REQUEST_RESUBMITTED) // Approve/Reject
                {
                    if (mkrckr.getCreatedBy().compareTo(user.getUserId()) == 0)
                        isEditable = true;
                } else if (status == REQUEST_TO_DELETE) // Delete
                {
                    isEditable = false;

                }

            }
        }
        return isEditable;
    }

    @Override
    public Boolean checkIfEntityIsEditable1(MultivaluedMap<String,String> uriInfo){
        Boolean isEditable = false;
        OptionFilter optionFilter = new OptionFilter(uriInfo);
        Options options = new Options(optionFilter);
        Boolean checkPerm = checkEntityPermissionForEntity(options);
        if (checkPerm) {
            String makerCheckerId = optionFilter.getFilter("makerCheckerId");
            String loggedInUserName = optionFilter.getFilter("loggedInUserName");
            User user = userDal.getByUserName(loggedInUserName);
            log.debug("user loggedin {}", user);

            if (user != null && makerCheckerId != null && makerCheckerId.length() > 0) {
                MakerChecker mkrckr = getById(Long.valueOf(makerCheckerId));
                log.debug("mkrckr entity {}", mkrckr);

                Integer status = mkrckr.getEntityStatus();

                if (status == APPROVE_REQUEST || status == REJECT_REQUEST || status == DELETE_REJECTED
                        || status == REQUEST_RESUBMITTED) // Approve/Reject
                {
                    if (mkrckr.getCreatedBy().compareTo(user.getUserId()) == 0)
                        isEditable = true;
                } else if (status == REQUEST_TO_DELETE) // Delete
                {
                    isEditable = false;

                }

            }
        }
        return isEditable;
    }

    @Override
    public void removeUserFromMakerCheckerTransactions(int userId) {
        makerCheckerRepository.updateTransactionForUserInMaker(userId);
        makerCheckerRepository.updateTransactionForUserInChecker(userId);
    }



    public static String convertToMKRUserName(String userName, Long mkrId) {
        if (mkrId != null && mkrId > 0) {
            return userName + MakerCheckerDalJpa.MKR_SEPARATOR + mkrId;
        } else
            return userName + MakerCheckerDalJpa.MKR_SEPARATOR;

    }

    public static String extractUserNameFromMKR(String userNameTemp) {
        if (userNameTemp != null && !userNameTemp.isEmpty()) {
            return userNameTemp.split(MKR_SEPARATOR)[0];
        }
        return null;
    }

}