package com.esq.rbac.service.sessiondata.repository;

import com.esq.rbac.service.sessiondata.domain.Session;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Date;
import java.util.List;

@Repository
public interface SessionRepository extends JpaRepository<Session, Integer> {

    void deleteBySessionHash(String sessionHash);

    void deleteBySessionHashAndChildApplicationId(String sessionHash, Integer childApplicationId);

    void deleteBySessionHashAndTicket(String sessionHash, String ticket);

    List<Session> findByAppType(Integer appType);

    @Query(value = "delete from Session s where s.appType = :appType and s.createdTime < :cutOffDate", nativeQuery = true)
    public void removeAllNativeSessions(@Param("appType") Integer appType, @Param("cutOffDate") Date cutOffDate);

    @Query(value = "select count(1) from Session s where s.userName = :userName and s.sessionHash = :sessionHash and s.appType= :appType and s.childApplicationId=:childApplicationId", nativeQuery = true)
    public Integer isSSOSessionExistForSameSessionHash(@Param("userName") String userName, @Param("sessionHash") String sessionHash, @Param("appType") Integer appType, @Param("childApplicationId") Integer childApplicationId);

    @Query(value = "select count(1) from Session s where s.userName = :userName and s.sessionHash != :sessionHash and s.appType= :appType", nativeQuery = true)
    public boolean isSSOSessionExistForOtherSessionHash(@Param("userName") String userName, @Param("sessionHash") String sessionHash, @Param("appType") Integer appType);

    @Query(value = "select count(1) from Session s where s.userName = :userName and s.appType= :appType", nativeQuery = true)
    public boolean isSSOSessionExist(@Param("userName") String userName, @Param("appType") Integer appType);

    @Query(value = "select s from Session s where s.userName = :userName and s.appKey=:appKey", nativeQuery = true)
    public  List<Session> getExistingNonSSOSessionsByAppKey(@Param("userName") String userName, @Param("appKey") String appKey);

    @Query(value = "select s from Session s where s.userName = :userName and s.childApplicationName=:childApplicationName", nativeQuery = true)
    public  List<Session> getExistingNonSSOSessionsByAppName(@Param("userName") String userName, @Param("childApplicationName") Integer childApplicationName);

    @Query(value = "select s from Session s where s.appType =:appType and s.sessionHash = :sessionHash and s.loginType is not null", nativeQuery = true)
    public List<Session> getSsoUserData(@Param("appType") Integer appType, @Param("sessionHash") String sessionHash);

    @Query(value = "select count(distinct(s.ticket)) from Session s where s.appType =:appType and s.userName = :userName and s.sessionHash = :sessionHash and s.loginType is not null", nativeQuery = true)
    public Integer getSSOUserDataCountByUserName(@Param("appType") Integer appType,@Param("userName") String userName, @Param("sessionHash") String sessionHash);

    @Query(value = "select s from Session s where s.appType =:appType and s.sessionHash = :sessionHash and s.appKey =:appKey", nativeQuery = true)
    public List<Session> getNonSSOUserDataByAppKey(@Param("appType") Integer appType, @Param("sessionHash") String sessionHash, @Param("appKey") String appKey);

    @Query(value = "select count(distinct s.userName) from Session s where s.appType = :appType and s.sessionHash = :sessionHash and s.loginType is not null", nativeQuery = true)
    public List<Integer> checkDistinctUsernameBySessionHash(@Param("appType") Integer appType, @Param("sessionHash") String sessionHash);

    @Query(value = "select s from Session s where s.userName = :userName and s.appType = :appType", nativeQuery = true)
    List<Session> getSSOSessionsByUserName(@Param("userName") String userName, @Param("appType") Integer appType);

    List<Session> findAllByUserName(String userName);

    @Query(value = "select s from Session s where s.userName = :userName and s.appType = :appType  and s.sessionHash = :sessionHash", nativeQuery = true)
    List<Session> getSSOSessionsByUserNameWithSessionHash(@Param("userName") String userName, @Param("appType") Integer appType, @Param("sessionHash") String sessionHash);

    @Query(value = "select s from Session s where s.appType = :appType  and s.sessionHash = :sessionHash", nativeQuery = true)
    List<Session> getSSOSessionsBySessionHash(@Param("appType") Integer appType, @Param("sessionHash") String sessionHash);

    @Query(value = "select s from Session s where s.sessionHash = :sessionHash", nativeQuery = true)
    List<Session> getSSOSessionsByOnlySessionHash(@Param("sessionHash") String sessionHash);

    @Query(value = "select s from Session s where s.userName = :userName and s.appType = :appType and s.appKey =:appKey", nativeQuery = true)
    List<Session> getNonSSOSessionByUserNameAndAppKey(@Param("userName") String userName, @Param("appType") Integer appType, @Param("appKey") String appKey);

    @Query(value = "select s from Session s where s.userName = :userName and s.appType = :appType and s.appKey =:appKey and s.sessionHash = :sessionHash", nativeQuery = true)
    List<Session> getNonSSOSessionByUserNameAndAppKeyWithSessionHash(@Param("userName") String userName, @Param("appType") Integer appType, @Param("appKey") String appKey, @Param("sessionHash") String sessionHash);

    @Query(value = "select s from Session s where s.appType =:appType and s.sessionHash = :sessionHash and s.appKey =:appKey", nativeQuery = true)
    List<Session>  getNativeSessionByAppKey(@Param("appType") Integer appType,  @Param("sessionHash") String sessionHash, @Param("appKey") String appKey);

    @Query(value = "select s from Session s where s.userName =:userName and s.ticket = :ticket", nativeQuery = true)
    List<Session> getSessionByTicket(@Param("userName") String userName,  @Param("ticket") String ticket);

    List<Session> findAllByAppKey(String appKey);



    @Query("select s from Session s where lower(s.appKey) = lower(:appKey) and s.appUrlId IN (select ap.appUrlId from AppUrlData ap where lower(ap.tag) = lower(:tag) and ap.childApplication = (select ca from ChildApplication ca where lower(ca.appKey) = lower(:appKey)))")
    List<Session> getAllSessionsByAppKeyAndTag(String appKey, String tag);

    Integer findAppUrlIdByTicket(String ticket);

    @Query("SELECT s FROM Session s WHERE s.appType IN :appTypes AND s.createdTime < :cutOffDate")
    List<Session> getAllWebSessions(@Param("appTypes") List<Integer> appTypes, @Param("cutOffDate") Date cutOffDate);


    //    @Query("SELECT s FROM Session s LEFT JOIN AppUrlData ap WHERE s.createdTime < :cutOffDate AND s.appType IN :appTypes AND (ap.tag IS NULL OR LOWER(ap.tag) = LOWER(:tag))")
    @Query("SELECT s FROM Session s LEFT JOIN AppUrlData ap ON (ap.appUrlId = s.appUrlId) WHERE s.createdTime < :cutOffDate AND s.appType IN :appTypes AND (ap.tag IS NULL OR LOWER(ap.tag) = :tag)")
    List<Session> getAllWebSessionsByTag(@Param("appTypes") List<Integer> appTypes, @Param("cutOffDate") Date cutOffDate, @Param("tag") String tag);

    @Query("SELECT s FROM Session s WHERE s.sessionHash = :sessionHash")
    List<Session> isAnySessionExistForSessionHash(@Param("sessionHash") String sessionHash);

    Integer findAppUrlIdByUserNameAndAppKey(String userName, String appKey);

    @Query("SELECT s.sessionHash, s.appKey FROM Session s WHERE s.appType IN :appTypes")
    List<Object[]> getSSOHashAppKey(@Param("appTypes") List<Integer> appTypes);

    @Query("SELECT COUNT(b) FROM AppUrlData b WHERE LOWER(b.homeUrl) LIKE LOWER(:homeUrl)")
    Long isChangePasswordReturnURLExits(@Param("homeUrl") String homeUrl);

}
