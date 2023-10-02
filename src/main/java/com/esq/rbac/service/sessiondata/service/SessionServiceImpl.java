package com.esq.rbac.service.sessiondata.service;

import com.esq.rbac.service.application.childapplication.domain.ChildApplication;
import com.esq.rbac.service.filters.domain.Filters;
import com.esq.rbac.service.loginlog.domain.LoginLog;
import com.esq.rbac.service.loginlog.service.LoginLogService;
import com.esq.rbac.service.sessiondata.domain.Session;
import com.esq.rbac.service.sessiondata.repository.SessionRepository;
import com.esq.rbac.service.util.RBACUtil;
import com.esq.rbac.service.util.SearchUtils;
import com.esq.rbac.service.util.dal.OptionFilter;
import com.esq.rbac.service.util.dal.OptionSort;
import com.esq.rbac.service.util.dal.Options;
import jakarta.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.NoResultException;
import java.util.*;

@Service
@Slf4j
public class SessionServiceImpl implements SessionService{

    private static final Map<String, String> SORT_COLUMNS;
    private static final Map<String, String> GROUP_COLUMNS;

    private EntityManager em;

    private SessionRepository sessionRepository;

    @Autowired
    public void setSessionRepository(SessionRepository sessionRepository){
        this.sessionRepository = sessionRepository;
    }


    private LoginLogService loginLogService;

    @Autowired
    public void setLoginLogService(LoginLogService loginLogService){
        this.loginLogService = loginLogService;
    }

    static {
        SORT_COLUMNS = new TreeMap<String, String>();
        SORT_COLUMNS.put("userName", "s.userName");
    }

    static {
        GROUP_COLUMNS = new TreeMap<String, String>();
        GROUP_COLUMNS.put("childApplicationName", "s.childApplicationName");
    }

    @PersistenceContext
    public void setEntityManager(EntityManager em) {
        log.trace("setEntityManager");
        this.em = em;
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRED)
    public void createSession(Session session) {
        sessionRepository.save(session);
    }

    @Override
    @Transactional(propagation = Propagation.SUPPORTS, readOnly = true)
    public List<Session> getList(Options options) {
        List<Integer> userListPaginated = getUserListPaginated(options);
        if(userListPaginated!=null && !userListPaginated.isEmpty()){
            Filters filters = prepareFiltersForSession(userListPaginated);
            Options optionsForSession = new Options(options.getOption(OptionSort.class));
            return filters.getList(em, Session.class, "select s from Session s", optionsForSession, SORT_COLUMNS);
        }
        return new ArrayList<Session>();
    }

    @Override
    @Transactional(propagation = Propagation.SUPPORTS, readOnly = true)
    public List<Session> searchList(Options options) {
        Filters filters = prepareFiltersForUserScope(options);
        filters.addCondition(" ( lower(s.userName) like :q or lower(s.childApplicationName) like :q)");
        filters.addParameter(
                SearchUtils.SEARCH_PARAM,
                SearchUtils.wildcarded(com.esq.rbac.service.util.SearchUtils.getSearchParam(options,
                        SearchUtils.SEARCH_PARAM).toLowerCase()));
        List<Integer> userIds = filters.getList(em, Integer.class, "select distinct(s.userId) from Session s", options, SORT_COLUMNS);
        if(userIds!=null && !userIds.isEmpty()){
            filters = prepareFiltersForSession(userIds);
            Options optionsForSession = new Options(options.getOption(OptionSort.class));
            return filters.getList(em, Session.class, "select s from Session s", optionsForSession, SORT_COLUMNS);
        }
        return new ArrayList<Session>();
    }

    @Override
    @Transactional(propagation = Propagation.SUPPORTS, readOnly = true)
    public int getUserNameCount(Options options) {
        Filters filters = prepareFiltersForUserScope(options);
        return filters.getCount(em, "select count(distinct s.userId) from Session s");
    }

    @Override
    @Transactional(propagation = Propagation.SUPPORTS, readOnly = true)
    public int getSearchCount(Options options) {
       Filters filters = prepareFiltersForUserScope(options);
        filters.addCondition(" ( lower(s.userName) like :q or lower(s.childApplicationName) like :q)");
        filters.addParameter(
                SearchUtils.SEARCH_PARAM,
                SearchUtils.wildcarded(com.esq.rbac.service.util.SearchUtils.getSearchParam(options,
                        SearchUtils.SEARCH_PARAM).toLowerCase()));
        return filters.getCount(em, "select count(distinct s.userId ) from Session s");
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRED)
    public void deleteBySessionHashAndTicket(String sessionHash, String ticket) {
        sessionRepository.deleteBySessionHashAndTicket(sessionHash, ticket);
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRED)
    public void deleteBySessionHash(String sessionHash) {
        sessionRepository.deleteBySessionHash(sessionHash);
    }

    @Override
    public List<Map<String, Object>> getAppWiseLoggedInCount(Options options) {
        List<Map<String, Object>> returnObj = new LinkedList<Map<String, Object>>();
        Filters filters = prepareFiltersForUserScope(options);
        List<Object[]> result = filters
                .getListWithGrouping(
                        em,
                        Object[].class,
                        "select s.childApplicationName, COUNT(distinct(s.ticket)) from Session s",
                        options, SORT_COLUMNS,
                        Arrays.asList(new String[] { "childApplicationName" }),
                        GROUP_COLUMNS);
        if (result != null && !result.isEmpty()) {
            for (Object[] obj : result) {
                Map<String, Object> temp = new HashMap<String, Object>();
                temp.put("name", obj[0].toString());
                temp.put("count", obj[1]);
                returnObj.add(temp);
            }
        }
        result = filters.getList(em, Object[].class,
                "select 'totalActiveSessions',count(distinct s.ticket) from Session s",
                options, SORT_COLUMNS);
        if (result != null && !result.isEmpty()) {
            for (Object[] obj : result) {
                Map<String, Object> temp = new HashMap<String, Object>();
                temp.put("name", obj[0].toString());
                temp.put("count", obj[1]);
                returnObj.add(temp);
            }
        }
        result = filters
                .getList(
                        em,
                        Object[].class,
                        "select  'totalDistinctUsers',count(distinct(s.userId)) from Session s",
                        options, SORT_COLUMNS);
        if (result != null && !result.isEmpty()) {
            for (Object[] obj : result) {
                Map<String, Object> temp = new HashMap<String, Object>();
                temp.put("name", obj[0].toString());
                temp.put("count", obj[1]);
                returnObj.add(temp);
            }
        }
        return returnObj;
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRED, readOnly = true)
    public Integer removeAllNativeSessions(String logType, String logBuffer, Date cutOffDate) {
        List<Session> result=sessionRepository.findByAppType(ChildApplication.APP_TYPE.NATIVE.getCode());
            for(Session session:result){
                loginLogService.create(LoginLog.createLoginLog(session.getUserName(), LoginLog.LOG_TYPE_LOGOUT+" ("+logType+") ", true, null, session.getServiceUrl(), logBuffer, session.getSessionHash(), null));
            }
            sessionRepository.removeAllNativeSessions(ChildApplication.APP_TYPE.NATIVE.getCode(), cutOffDate);
        return result==null?0:result.size();
    }

    @Override
    @Transactional(propagation = Propagation.SUPPORTS, readOnly = true)
    public boolean isSSOSessionExistForOtherSessionHash(String userName, String sessionHash, Integer appType) {
        return sessionRepository.isSSOSessionExistForOtherSessionHash(userName,sessionHash,appType);
    }

    @Override
    @Transactional(propagation = Propagation.SUPPORTS, readOnly = true)
    public boolean isSSOSessionExist(String userName, Integer appType) {
        return sessionRepository.isSSOSessionExist(userName,appType);
    }

    @Override
    @Transactional(propagation = Propagation.SUPPORTS, readOnly = true)
    public List<Session> getExistingNonSSOSessions(String userName, String appKey, String childApplicationName) {
        return sessionRepository.getExistingNonSSOSessionsByAppKey(userName,appKey);
    }

    @Override
    @Transactional(propagation = Propagation.SUPPORTS, readOnly = true)
    public Session getSsoUserData(String sessionHash, Integer appType) {
        List<Session> sessionList=sessionRepository.getSsoUserData(appType,sessionHash);
        try{
            if(sessionList!=null && !sessionList.isEmpty()){
                if(sessionList.size()==1){
                    log.debug("getSsoUserData sessionList {}",sessionList.get(0));
                    return sessionList.get(0);
                }
               List<Integer> querySessionCount = sessionRepository.checkDistinctUsernameBySessionHash(appType,sessionHash);
                log.debug("checkDistinctUsernameBySessionHash by sessionHash {} and appType {}",sessionHash,appType);
                if(querySessionCount.get(0).intValue()==1){
                    log.debug("size received as 1 and session {}",sessionList.get(0));
                    return sessionList.get(0);
                }
                else{
                    sessionList.get(0).setUserId(null);
                    sessionList.get(0).setUserName(null);
                    log.debug("checkDistinctUsernameBySessionHash sessionList {}",sessionList.get(0));
                    return sessionList.get(0);
                }
            }
        }
        catch(NoResultException nrex){
            return null;
        }
        return null;
    }

    @Override
    @Transactional(propagation = Propagation.SUPPORTS, readOnly = true)
    public Session getNonSSOUserDataByAppKey(String sessionHash, Integer appType, String appKey) {
       Session session = null;
        try{
            session = sessionRepository.getNonSSOUserDataByAppKey(appType,sessionHash,appKey).get(0);
        }
        catch(NoResultException nrex){

        }
        if(session==null){
            log.debug("getNonSSOUserDataByAppKey; session is null");
            return null;
        }
        return session;
    }

    @Override
    @Transactional(propagation = Propagation.SUPPORTS, readOnly = true)
    public List<Session> getSessionsByUserName(String userName) {
        return sessionRepository.findAllByUserName(userName);
    }

    @Override
    @Transactional(propagation = Propagation.SUPPORTS, readOnly = true)
    public List<Session> getSSOSessionsByUserName(String userName, Integer appType, String sessionHash) {
         if(sessionHash!=null && !sessionHash.isEmpty()){
            if(userName != null && !userName.isEmpty()){
                return sessionRepository.getSSOSessionsByUserNameWithSessionHash(userName,appType,sessionHash);
            }
            else{
                return sessionRepository.getSSOSessionsBySessionHash(appType,sessionHash);
            }
        }
        else{
            return sessionRepository.getSSOSessionsByUserName(userName,appType);
        }
    }

    @Override
    @Transactional(propagation = Propagation.SUPPORTS, readOnly = true)
    public List<Session> getSSOSessionsByOnlySessionHash(String sessionHash) {
       return sessionRepository.getSSOSessionsByOnlySessionHash(sessionHash);
    }

    @Override
    @Transactional(propagation = Propagation.SUPPORTS, readOnly = true)
    public List<Session> getNonSSOSessionByUserNameAndAppKey(String userName, Integer appType, String appKey, String sessionHash) {
       // TypedQuery<com.esq.rbac.model.Session> typedQuery = null;
        if(sessionHash!=null && !sessionHash.isEmpty()){
            return sessionRepository.getNonSSOSessionByUserNameAndAppKeyWithSessionHash(userName,appType,appKey,sessionHash);
        }
        else{
            return sessionRepository.getNonSSOSessionByUserNameAndAppKey(userName,appType,appKey);
        }
    }

    @Override
    @Transactional(propagation = Propagation.SUPPORTS, readOnly = true)
    public Session getNativeSessionByAppKey(String sessionHash,
                                            Integer appType, String appKey) {
        Session session = null;
        try{
            session = sessionRepository.getNativeSessionByAppKey(appType,sessionHash,appKey).get(0);
        }
        catch(NoResultException nrex){

        }
        if(session==null){
            return null;
        }
        return session;
    }

    @Override
    public Session getSessionByTicket(String userName, String ticket) {
        Session session = null;
        try{
            session = sessionRepository.getSessionByTicket(userName, ticket).get(0);
        }
        catch(NoResultException nrex){

        }
        if(session==null){
            return null;
        }
        return session;
    }

    @Override
    public List<Session> getAllSessionsByAppKey(String appKey) {
        return sessionRepository.findAllByAppKey(appKey);
    }

    @Override
    public List<Session> getAllSessionsByAppKeyAndTag(String appKey, String tag) {
        return sessionRepository.getAllSessionsByAppKeyAndTag(appKey,tag);
    }

    @Override
    public boolean isAnySessionExistForSessionHash(String sessionHash) {
        List<Session> result=sessionRepository.isAnySessionExistForSessionHash(sessionHash);
        if (result.size() > 0) {
            return true;
        } else {
            return false;
        }
    }

    @Override
    public Integer getAppUrlIdByTicket(String ticket) {
        if(ticket!=null && !ticket.isEmpty()){
            try{
                return sessionRepository.findAppUrlIdByTicket(ticket);
            }
            catch(NoResultException nre){
                log.warn("getAppUrlIdByTicket; ticket={}; No record found", ticket);
                return null;
            }
        }
        return null;
    }

    @Override
    public Integer getAppUrlIdByUserNameAndAppKey(String userName, String appKey) {
        if(userName!=null && !userName.isEmpty() && appKey!=null && !appKey.isEmpty()){
            try{
                return sessionRepository.findAppUrlIdByUserNameAndAppKey(userName,appKey);
            }
            catch(NoResultException nre){
                log.warn("getAppUrlIdByUserNameAndAppKey; userName={}; appKey={}; No record found", userName, appKey);
                return null;
            }
        }
        return null;
    }

    @Override
    public List<Session> getAllWebSessions(Date cutOffDate) {
        return sessionRepository.getAllWebSessions(Arrays.asList(ChildApplication.APP_TYPE.SSO.getCode(), ChildApplication.APP_TYPE.NON_SSO.getCode()),cutOffDate);
    }

    @Override
    public List<Session> getAllWebSessionsByTag(Date cutOffDate, String tag) {
        return  sessionRepository.getAllWebSessionsByTag(Arrays.asList(ChildApplication.APP_TYPE.SSO.getCode(), ChildApplication.APP_TYPE.NON_SSO.getCode()),cutOffDate,tag);
    }

    @Override
    public Map<String, Set<String>> getSSOHashAppKey() {
        Map<String, Set<String>> returnMap = new LinkedHashMap<String, Set<String>>();
        List<Object[]> result = sessionRepository.getSSOHashAppKey(Arrays.asList(ChildApplication.APP_TYPE.SSO.getCode()));
        if(result!=null && !result.isEmpty()){
            for(Object[] obj: result){
                try{
                    String sessionHashToLogin = obj[0].toString();
                    if(!returnMap.containsKey(sessionHashToLogin)){
                        returnMap.put(sessionHashToLogin, new HashSet<String>());
                    }
                    returnMap.get(sessionHashToLogin).add(obj[1].toString());
                }
                catch(Exception e){
                    //ignore all exceptions for now, it is for loginLog
                }
            }
        }
        return returnMap;
    }


    @Override
    @Transactional(propagation = Propagation.SUPPORTS, readOnly = true)
    public void isSSOSessionExistForSameSessionHash(String userName, String sessionHash, Integer appType, Integer childApplicationId) {
        Integer result=sessionRepository.isSSOSessionExistForSameSessionHash(userName,sessionHash,appType,childApplicationId);
        if (result > 0) {
            deleteBySessionHashAndChildAppId(sessionHash,childApplicationId);
        }
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRED)
    public void deleteBySessionHashAndChildAppId(String sessionHash, Integer childApplicationId) {
        sessionRepository.deleteBySessionHashAndChildApplicationId(sessionHash,childApplicationId);
    }

    @Override
    public Boolean isChangePasswordReturnURLExits(String returnUrl) {
        String[] urlSplit=returnUrl.split("/");
        StringBuilder builder=new StringBuilder();
        int countParts=0;
        for(String a:urlSplit){
            countParts++;
            if(countParts<=4){
                builder.append(a);
                builder.append("/");
            }
        }

        returnUrl=builder.toString();


        if(returnUrl!=null && !returnUrl.isEmpty() && ((returnUrl.charAt(returnUrl.length()-1))=='/' || (returnUrl.charAt(returnUrl.length()-1))=='\\')) {

            returnUrl=returnUrl.substring(0,returnUrl.length()-1);
        }
        if(returnUrl!=null && !returnUrl.isEmpty()){
            Long count=0L;
            try{
                count = sessionRepository.isChangePasswordReturnURLExits(returnUrl);
                if(count>0) {
                    return true;
                }else {
                    return false;
                }
            }
            catch(NoResultException nre){
                log.warn("isChangePasswordReturnURLExits; returnURL={}; No record found",returnUrl );
                return false;
            }
        }
        return false;
    }

    @Override
    public Integer getSsoUserDataCount(String ticket, String userName, Integer appType) {
        try {
            Session sessionByTicket = getSessionByTicket(userName, ticket);
            if (sessionByTicket != null) {
                Integer result = sessionRepository.getSSOUserDataCountByUserName(appType,userName,sessionByTicket.getSessionHash());
                log.trace("getSSOUserDataCountByUserName by sessionUsername {} and appType {} hash {}", userName,
                        appType, sessionByTicket.getSessionHash());
                log.trace("size received {}", result.intValue());
                return result.intValue();
            }
        } catch (Exception e) {
            return 0;
        }
        return 0;
    }

    @Transactional(propagation = Propagation.SUPPORTS, readOnly = true)
    public List<Integer> getUserListPaginated(Options options) {
        Filters filters = prepareFiltersForUserScope(options);
        return filters.getList(em, Integer.class, "select distinct(s.userId) from Session s", options, SORT_COLUMNS);
    }

    private Filters prepareFiltersForUserScope(Options options) {

        Filters result = new Filters();
        OptionFilter optionFilter = options == null ? null : options
                .getOption(OptionFilter.class);
        Map<String, String> filters = optionFilter == null ? null
                : optionFilter.getFilters();
        if (filters != null) {
            String scopeQuery = filters.get(RBACUtil.USER_SCOPE_QUERY);
            if (scopeQuery != null && scopeQuery.length() > 1) {
                result.addCondition("s.userId in (select u.userId from User u where "+"(" + scopeQuery + ") ) ");
            }
        }
        return result;
    }

    private Filters prepareFiltersForSession(List<Integer> userIds) {

        Filters result = new Filters();
        if(userIds!=null && !userIds.isEmpty()){
            result.addCondition("s.userId in :userIds");
            result.addParameter("userIds", userIds);
        }
        return result;
    }
}
