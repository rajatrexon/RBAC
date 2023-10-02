package com.esq.rbac.service.sessiondata.service;

import com.esq.rbac.service.sessiondata.domain.Session;
import com.esq.rbac.service.util.dal.Options;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;

public interface SessionService {

    void createSession(Session session);

    List<Session> getList(Options options);

    List<Session> searchList(Options options);

    int getUserNameCount(Options options);

    int getSearchCount(Options options);

    void deleteBySessionHashAndTicket(String sessionHash, String ticket);

    void deleteBySessionHash(String sessionHash);

    List<Map<String, Object>> getAppWiseLoggedInCount(Options options);

    Integer removeAllNativeSessions(String logType, String logBuffer, Date cutOffDate);

    boolean isSSOSessionExistForOtherSessionHash(String userName, String sessionHash,
                                                 Integer appType);

    boolean isSSOSessionExist(String userName,
                              Integer appType);

    List<Session> getExistingNonSSOSessions(String userName, String appKey,
                                            String childApplicationName);

    Session getSsoUserData(String sessionHash, Integer appType);

    Session getNonSSOUserDataByAppKey(String sessionHash, Integer appType,
                                      String appKey);

    List<Session> getSessionsByUserName(String userName);

    List<Session> getSSOSessionsByUserName(String userName, Integer appType,
                                           String sessionHash);

    List<Session> getSSOSessionsByOnlySessionHash(String sessionHash);

    List<Session> getNonSSOSessionByUserNameAndAppKey(String userName,
                                                      Integer appType, String appKey, String sessionHash);
    Session getNativeSessionByAppKey(String sessionHash,
                                     Integer appType, String appKey);

    Session getSessionByTicket(String userName, String ticket);

    List<Session> getAllSessionsByAppKey(String appKey);

    List<Session> getAllSessionsByAppKeyAndTag(String appKey, String tag);

    boolean isAnySessionExistForSessionHash(String sessionHash);

    Integer getAppUrlIdByTicket(String ticket);

    Integer getAppUrlIdByUserNameAndAppKey(String userName, String appKey);

    List<Session> getAllWebSessions(Date cutOffDate);

    List<Session> getAllWebSessionsByTag(Date cutOffDate, String tag);

    Map<String, Set<String>> getSSOHashAppKey();

    void isSSOSessionExistForSameSessionHash(String userName, String sessionHash, Integer appType,Integer childApplicationId);

    void deleteBySessionHashAndChildAppId(String sessionHash, Integer childApplicationId);

    Boolean isChangePasswordReturnURLExits(String returnUrl);

    Integer getSsoUserDataCount(String ticket, String userName, Integer appType);
}
