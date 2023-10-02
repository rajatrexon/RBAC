package com.esq.rbac.service.makerchecker.service;

import com.esq.rbac.service.basedal.BaseDal;
import com.esq.rbac.service.makerchecker.domain.MakerChecker;
import com.esq.rbac.service.util.dal.Options;
import jakarta.ws.rs.core.MultivaluedMap;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.UriInfo;
import org.springframework.http.ResponseEntity;

import java.util.List;
import java.util.Map;

public interface MakerCheckerDal extends BaseDal {

    List<Map<String, Object>> searchMakerCheckerInfo(Options options);

    List<Map<String, Object>> getMakerCheckerInfo(Options options);

    /*List<MakerChecker> searchList(Options options);*/

    /*List<MakerChecker> getList(Options options);*/

    int getSearchCount(Options options);

    int getCount(Options options);

    Object getEntityByMakerCheckerId(int makerCheckerId);

    Boolean checkEntityPermission(int parseInt, Options options);

    MakerChecker createEntry(Object objectToBeAdded, Class<?> entity, int loggedInUserId, String target, String operation, Integer entityId, Long loggedInTenantId);

    ResponseEntity<Integer> approveOrRejectMakerCheckerEntity(Options options, MakerChecker objMakerChecker, Integer loggedInUserId, String clientIp, Long tenantId);

    MakerChecker getById(Long makerCheckerId);

    void deleteById(Long id);

    MakerChecker updateEntry(Object objectToBeUpdated, int loggedInUserId, Integer entityId, Integer actionStatus, Long makerCheckerId,Boolean updateDeleteStatus);

//	List<MakerChecker> getByEntityId(Integer userId);

    List<Map<String, Object>> getHistoryByMakerCheckerId(int makerCheckerId);

    void deleteEntryByEntityId(Integer userId, Class<?> entity);

    Boolean checkEntityPermissionForEntity(Options options);

    Boolean checkIfEntityIsEditable(UriInfo uriInfo);

    Boolean checkIfEntityIsEditable1(MultivaluedMap<String,String> uriInfo);

    List<MakerChecker> getByEntityIdAndEntity(Integer entityId, Class<?> entity);

    void removeUserFromMakerCheckerTransactions(int userId);
}
