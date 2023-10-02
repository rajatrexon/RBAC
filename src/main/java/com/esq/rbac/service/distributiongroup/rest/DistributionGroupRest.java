package com.esq.rbac.service.distributiongroup.rest;

import com.esq.rbac.service.distributiongroup.distusergroup.domain.DistUserMap;
import com.esq.rbac.service.distributiongroup.distusergroup.service.DistUserMapDal;
import com.esq.rbac.service.distributiongroup.domain.DistributionGroup;
import com.esq.rbac.service.distributiongroup.service.DistributionGroupDal;
import com.esq.rbac.service.exception.ErrorInfoException;
import com.esq.rbac.service.user.domain.User;
import com.esq.rbac.service.util.SearchUtils;
import com.esq.rbac.service.util.dal.OptionFilter;
import com.esq.rbac.service.util.dal.OptionPage;
import com.esq.rbac.service.util.dal.OptionSort;
import com.esq.rbac.service.util.dal.Options;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Parameters;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.MultivaluedHashMap;
import jakarta.ws.rs.core.MultivaluedMap;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;


@RestController
@RequestMapping("/distGroup")
@Tag(name = "/distGroup", description = "Distribution Groups of Users")
@Slf4j
public class DistributionGroupRest {

    private final Validator validator;
    private final DistributionGroupDal distributionGroupDal;
    private final DistUserMapDal distUserMapDal;

    public DistributionGroupRest(Validator validator, DistributionGroupDal distributionGroupDal, DistUserMapDal distUserMapDal) {
        this.distributionGroupDal = distributionGroupDal;
        this.distUserMapDal = distUserMapDal;
        this.validator = validator;

    }

    @PostMapping
    @Parameters({@Parameter(name = "userId", description = "loggedInUserId", required = true, schema = @Schema(type = "string"), in = ParameterIn.HEADER),})
    public ResponseEntity<DistributionGroup> create(@RequestHeader org.springframework.http.HttpHeaders headers, @RequestBody DistributionGroup distributionGroup) throws Exception {
        log.debug("create; distributionGroup={}", distributionGroup);
        validate(distributionGroup);
        Integer loggedInUserId = distributionGroup.getCreatedBy();
        try {
            loggedInUserId = Integer.parseInt(headers.get("userId").get(0));
        } catch (Exception e) {
        }
        return ResponseEntity.ok().body(distributionGroupDal.create(distributionGroup, loggedInUserId));

    }

    @PutMapping
    @Parameters({@Parameter(name = "userId", description = "loggedInUserId", required = true, schema = @Schema(type = "string"), in = ParameterIn.HEADER),})
    public ResponseEntity<DistributionGroup> update(@RequestHeader org.springframework.http.HttpHeaders headers, @RequestBody DistributionGroup distributionGroup) {
        log.debug("update; distributionGroup={}", distributionGroup);
        validate(distributionGroup);
        Integer loggedInUserId = distributionGroup.getCreatedBy();
        try {
            loggedInUserId = Integer.parseInt(headers.get("userId").get(0));
        } catch (Exception e) {

        }
        return ResponseEntity.ok().body(distributionGroupDal.update(distributionGroup, loggedInUserId));
    }

    @GetMapping(value = "/{distId}", produces = MediaType.APPLICATION_JSON)
    public ResponseEntity<DistributionGroup> getDistributionGroupById(@PathVariable("distId") int distId) throws Exception {
        log.debug("getDistributionGroupById; distId{}", distId);

        return ResponseEntity.ok(distributionGroupDal.getDistributionGroupByDistId(distId));
    }

    @GetMapping
    @Parameters({@Parameter(name = "distId", description = "distributionGroupId", required = false, schema = @Schema(type = "string"), in = ParameterIn.QUERY), @Parameter(name = "distName", description = "distributionGroupName", required = false, schema = @Schema(type = "string"), in = ParameterIn.QUERY), @Parameter(name = "loggedInTenantId", description = "loggedInTenantId", required = false, schema = @Schema(type = "string"), in = ParameterIn.QUERY), @Parameter(name = "tenantId", description = "tenantId", required = false, schema = @Schema(type = "string"), in = ParameterIn.QUERY),})
    public ResponseEntity<List<DistributionGroup>> getDistributionGroupList(HttpServletRequest servletRequest) throws Exception {
        Map<String, String[]> parameterMap = servletRequest.getParameterMap();
        log.debug("retrive; distribution group list{}", servletRequest.getRequestURI());
        MultivaluedMap<String, String> uriInfo = new MultivaluedHashMap<>();
        parameterMap.forEach((key, values) -> uriInfo.addAll(key, Arrays.asList(values)));

        OptionPage optionPage = new OptionPage(uriInfo, 0, Integer.MAX_VALUE);
        OptionSort optionSort = new OptionSort(uriInfo);
        OptionFilter optionFilter = new OptionFilter(uriInfo);
        Options options = new Options(optionSort, optionFilter, optionPage);
        if (uriInfo.containsKey(SearchUtils.SEARCH_PARAM)) {
            return ResponseEntity.ok(distributionGroupDal.getSearchGroup(options));
        } else {
            return ResponseEntity.ok(distributionGroupDal.getDistributionGroupList(options));
        }
    }

    @GetMapping(value = "/count")
    @Parameters({@Parameter(name = "distId", description = "distributionGroupId", required = false, schema = @Schema(type = "string"), in = ParameterIn.QUERY), @Parameter(name = "distName", description = "distributionGroupName", required = false, schema = @Schema(type = "string"), in = ParameterIn.QUERY), @Parameter(name = "loggedInTenantId", description = "loggedInTenantId", required = false, schema = @Schema(type = "string"), in = ParameterIn.QUERY), @Parameter(name = "tenantId", description = "tenantId", required = false, schema = @Schema(type = "string"), in = ParameterIn.QUERY),})
    public ResponseEntity<Integer> count(HttpServletRequest servletRequest) {
        Map<String, String[]> parameterMap = servletRequest.getParameterMap();
        log.trace("count; requestUri={}", servletRequest.getRequestURI());
        MultivaluedMap<String, String> uriInfo = new MultivaluedHashMap<>();
        parameterMap.forEach((key, values) -> uriInfo.addAll(key, Arrays.asList(values)));


        OptionSort optionSort = new OptionSort(uriInfo);
        OptionFilter optionFilter = new OptionFilter(uriInfo);
        Options options = new Options(optionSort, optionFilter);
        if (uriInfo.containsKey(SearchUtils.SEARCH_PARAM)) {
            return ResponseEntity.ok().body(distributionGroupDal.getSearchCount(options));
        } else {
            return ResponseEntity.ok().body(distributionGroupDal.getCount(options));
        }
    }


    @Deprecated // Only used for API
    @GetMapping(value = "/users")
    public ResponseEntity<User[]> getUsers(HttpServletRequest servletRequest) throws Exception {
        return getUsersInDistributionGroup(servletRequest);
    }


    @GetMapping(value = "/assignedUserListInDistGroup")
    @Parameters({@Parameter(name = "distId", description = "distributionGroupId", required = false, schema = @Schema(type = "string"), in = ParameterIn.QUERY),})
    public ResponseEntity<User[]> getUsersInDistributionGroup(HttpServletRequest servletRequest) throws Exception {
        Map<String, String[]> parameterMap = servletRequest.getParameterMap();
        log.debug("retrive; All user in a distribution group={}", servletRequest.getRequestURI());
        MultivaluedMap<String, String> uriInfo = new MultivaluedHashMap<>();
        parameterMap.forEach((key, values) -> uriInfo.addAll(key, Arrays.asList(values)));

        OptionPage optionPage = new OptionPage(uriInfo, 0, Integer.MAX_VALUE);
        OptionSort optionSort = new OptionSort(uriInfo);
        OptionFilter optionFilter = new OptionFilter(uriInfo);
        Options options = new Options(optionSort, optionFilter, optionPage);
        List<User> userListInDistGroup = null;

        if (uriInfo.containsKey(SearchUtils.SEARCH_PARAM)) {
            userListInDistGroup = distUserMapDal.getAssignedUserListBySearch(options);
        } else {
            userListInDistGroup = distUserMapDal.getUserInDistributionGroup(options);
        }

        for (User userList : userListInDistGroup) {
            hidePasswordDetails(userList);
            hideIVRPinDetails(userList);
        }

        User[] userArray = new User[userListInDistGroup.size()];
        userListInDistGroup.toArray(userArray);

        return ResponseEntity.ok().body(userArray);
    }

    @GetMapping(value = "/countAssigned")
    @Parameters({@Parameter(name = "distId", description = "distributionGroupId", required = false, schema = @Schema(type = "string"), in = ParameterIn.QUERY),})
    public ResponseEntity<Integer> countAssigned(HttpServletRequest servletRequest) {
        Map<String, String[]> parameterMap = servletRequest.getParameterMap();
        log.trace("countAssigned; requestUri={}", servletRequest.getRequestURI());
        MultivaluedMap<String, String> uriInfo = new MultivaluedHashMap<>();
        parameterMap.forEach((key, values) -> uriInfo.addAll(key, Arrays.asList(values)));

        OptionPage optionPage = new OptionPage(uriInfo, 0, Integer.MAX_VALUE);
        OptionSort optionSort = new OptionSort(uriInfo);
        OptionFilter optionFilter = new OptionFilter(uriInfo);
        Options options = new Options(optionSort, optionFilter, optionPage);
        if (uriInfo.containsKey(SearchUtils.SEARCH_PARAM)) {
            return ResponseEntity.ok().body(distUserMapDal.getAssignedSearchCount(options));
        } else {
            return ResponseEntity.ok().body(distUserMapDal.getAssignedCount(options));
        }
    }

    @GetMapping(value = "/unassignedUserListInDistGroup")
    @Parameters({@Parameter(name = "distId", description = "distributionGroupId", required = false, schema = @Schema(type = "string"), in = ParameterIn.QUERY),})
    public ResponseEntity<User[]> unassignedUserListInDistGroup(HttpServletRequest servletRequest) throws Exception {

        Map<String, String[]> parameterMap = servletRequest.getParameterMap();
        log.debug("retrive; All user in a distribution group={}", servletRequest.getRequestURI());
        MultivaluedMap<String, String> uriInfo = new MultivaluedHashMap<>();
        parameterMap.forEach((key, values) -> uriInfo.addAll(key, Arrays.asList(values)));

        OptionPage optionPage = new OptionPage(uriInfo, 0, Integer.MAX_VALUE);
        OptionSort optionSort = new OptionSort(uriInfo);
        OptionFilter optionFilter = new OptionFilter(uriInfo);
        Options options = new Options(optionSort, optionFilter, optionPage);
        List<User> userListInDistGroup = null;

        if (uriInfo.containsKey(SearchUtils.SEARCH_PARAM)) {
            userListInDistGroup = distUserMapDal.getUnAssignedUserListBySearch(options);
        } else {
            userListInDistGroup = distUserMapDal.getUserNotInDistributionGroup(options);
        }

        for (User userList : userListInDistGroup) {
            hidePasswordDetails(userList);
            hideIVRPinDetails(userList);
        }

        User[] userArray = new User[userListInDistGroup.size()];
        userListInDistGroup.toArray(userArray);

        return ResponseEntity.ok().body(userArray);
    }

    @GetMapping(value = "/countUnAssigned")
    @Parameters({@Parameter(name = "distId", description = "distributionGroupId", required = false, schema = @Schema(type = "string"), in = ParameterIn.QUERY),})
    public ResponseEntity<Integer> countUnAssigned(HttpServletRequest servletRequest) {
        Map<String, String[]> parameterMap = servletRequest.getParameterMap();
        log.trace("countAssigned; requestUri={}", servletRequest.getRequestURI());
        MultivaluedMap<String, String> uriInfo = new MultivaluedHashMap<>();
        parameterMap.forEach((key, values) -> uriInfo.addAll(key, Arrays.asList(values)));

        OptionPage optionPage = new OptionPage(uriInfo, 0, Integer.MAX_VALUE);
        OptionSort optionSort = new OptionSort(uriInfo);
        OptionFilter optionFilter = new OptionFilter(uriInfo);
        Options options = new Options(optionSort, optionFilter, optionPage);
        if (uriInfo.containsKey(SearchUtils.SEARCH_PARAM)) {
            return ResponseEntity.ok().body(distUserMapDal.getUnAssignedSearchCount(options));
        } else {
            return ResponseEntity.ok().body(distUserMapDal.getUnAssignedCount(options));
        }
    }

    private User hidePasswordDetails(User user) {
        user.setPasswordSalt(null);
        user.setPasswordHash(null);
        // user.setPasswordSetTime(null);
        return user;
    }

    private User hideIVRPinDetails(User user) {
        user.setIvrPinSalt(null);
        user.setIvrPinHash(null);
        return user;
    }

    @GetMapping(value = "/userDistGroup")
    @Parameters({@Parameter(name = "userId", description = "userId", required = false, schema = @Schema(type = "string"), in = ParameterIn.QUERY), @Parameter(name = "userName", description = "userName", required = false, schema = @Schema(type = "string"), in = ParameterIn.QUERY), @Parameter(name = "firstName", description = "firstName", required = false, schema = @Schema(type = "string"), in = ParameterIn.QUERY),})
    public ResponseEntity<List<DistributionGroup>> getDistributionGroup(HttpServletRequest servletRequest) throws Exception {
        Map<String, String[]> parameterMap = servletRequest.getParameterMap();
        log.debug("retrive; All Distribution group with which a user is associated={}", servletRequest.getRequestURI());
        MultivaluedMap<String, String> uriInfo = new MultivaluedHashMap<>();
        parameterMap.forEach((key, values) -> uriInfo.addAll(key, Arrays.asList(values)));

        OptionPage optionPage = new OptionPage(uriInfo, 0, Integer.MAX_VALUE);
        OptionSort optionSort = new OptionSort(uriInfo);
        OptionFilter optionFilter = new OptionFilter(uriInfo);
        Options options = new Options(optionSort, optionFilter, optionPage);
        return ResponseEntity.ok().body(distUserMapDal.getDistributionGroup(options));
    }

    //    @DELETE
    @DeleteMapping
    @Parameters({@Parameter(name = "userId", description = "loggedInUserId", required = true, schema = @Schema(type = "string"), in = ParameterIn.QUERY), @Parameter(name = "distGroups", description = "Enter the distGrpId by comma separated... ", required = true, schema = @Schema(type = "string"), in = ParameterIn.QUERY, allowEmptyValue = true),})
    public void deleteDistributionGroups(HttpServletRequest servletRequest) throws Exception {
        Map<String, String[]> parameterMap = servletRequest.getParameterMap();
        log.debug("deleteDistributionGroups; distId={}", servletRequest.getRequestURI());
        MultivaluedMap<String, String> uriInfo = new MultivaluedHashMap<>();
        parameterMap.forEach((key, values) -> uriInfo.addAll(key, Arrays.asList(values)));

        distributionGroupDal.deleteDistributionGroups(uriInfo);
    }

    // FOR API
    @DeleteMapping("/{distId}")
    @Parameters({@Parameter(name = "userId", description = "loggedInUserId", required = true, schema = @Schema(type = "string"), in = ParameterIn.QUERY), @Parameter(name = "distGroups", description = "Enter the distGrpId by comma separated... ", required = true, schema = @Schema(type = "string"), in = ParameterIn.QUERY, allowEmptyValue = true),})
    public void deleteDistributionGroupById(HttpServletRequest servletRequest, @PathVariable("distId") Integer distId) throws Exception {
        Map<String, String[]> parameterMap = servletRequest.getParameterMap();
        log.trace("deleteDistributionGroupsById; uri={}", servletRequest.getRequestURI());
        MultivaluedMap<String, String> uriInfo = new MultivaluedHashMap<>();
        parameterMap.forEach((key, values) -> uriInfo.addAll(key, Arrays.asList(values)));
        log.debug("deleteDistributionGroupById; distId={}", distId);
        MultivaluedMap<String, String> map = uriInfo;
        map.add("distGroups", distId + "");
        distributionGroupDal.deleteDistributionGroups(map);
    }

    @Deprecated // used only for API instead assignUsersToDistributionGroup
    @PostMapping(value = "/distUserMap")
    public ResponseEntity<User[]> assignMultipleUserToDistGroup(@RequestBody DistUserMap distUserMap, HttpServletRequest servletRequest) throws Exception {
        Map<String, String[]> parameterMap = servletRequest.getParameterMap();
        log.debug("create; distUserMapDal={}", distUserMap);
        MultivaluedMap<String, String> uriInfo = new MultivaluedHashMap<>();
        parameterMap.forEach((key, values) -> uriInfo.addAll(key, Arrays.asList(values)));

        MultivaluedMap<String, String> map = uriInfo;

        if (map.containsKey("distId")) {
            map.remove("distId", distUserMap.getDistId());
            map.putSingle("distId", Integer.toString(distUserMap.getDistId()));
        } else {
            map.putSingle("distId", Integer.toString(distUserMap.getDistId()));
        }
        Set<Integer> integerSet = new HashSet<>();
        integerSet.add(distUserMap.getUserId());
        distUserMap.setUserIdSet(integerSet);
        validateUserMap(distUserMap);
        OptionPage optionPage = new OptionPage(uriInfo, 0, Integer.MAX_VALUE);
        OptionSort optionSort = new OptionSort(uriInfo);
        OptionFilter optionFilter = new OptionFilter(map);
        Options options = new Options(optionSort, optionFilter, optionPage);

        List<User> assignMultipleUserToDistGroup = distUserMapDal.create(distUserMap, options, distUserMap.getCreatedBy());
        User[] array = new User[assignMultipleUserToDistGroup.size()];
        assignMultipleUserToDistGroup.toArray(array);
        if (assignMultipleUserToDistGroup != null && !assignMultipleUserToDistGroup.isEmpty()) {

            for (User user : assignMultipleUserToDistGroup) {
                hidePasswordDetails(user);
//				hideIVRPinDetails(user);
            }
        }
        return ResponseEntity.ok().body(array);
    }

    @PostMapping(value = "/assignUsers")
    @Parameters({@Parameter(name = "userId", description = "loggedInUserId", required = true, schema = @Schema(type = "string"), in = ParameterIn.HEADER),})
    public ResponseEntity<String> assignUsersToDistributionGroup(@RequestBody DistUserMap distUserMap, HttpServletRequest servletRequest, @RequestHeader org.springframework.http.HttpHeaders headers) throws Exception {
        log.debug("create; distUserMapDal={}", distUserMap);
        validateUserMap(distUserMap);
        Integer loggedInUserId = Integer.parseInt(headers.get("userId").get(0));
        distUserMapDal.assignUsers(distUserMap, loggedInUserId);
        return ResponseEntity.ok().body("ok");
    }

    @PostMapping(value = "/unassignUsers")
    @Parameters({@Parameter(name = "userId", description = "loggedInUserId", required = true, schema = @Schema(type = "string"), in = ParameterIn.HEADER),})
    public ResponseEntity<String> unassignUsersToDistributionGroup(@RequestBody DistUserMap distUserMap, HttpServletRequest servletRequest, @RequestHeader org.springframework.http.HttpHeaders headers) throws Exception {
        log.debug("create; distUserMapDal={}", distUserMap);
        validateUserMap(distUserMap);
        Integer loggedInUserId = Integer.parseInt(headers.get("userId").get(0));
        distUserMapDal.unassignUsers(distUserMap, loggedInUserId);
        return ResponseEntity.ok().body("ok");
    }

    @Deprecated
    @DeleteMapping(value = "/distUserMap")
    public void deleteUsersFromDistributionGroup(HttpServletRequest servletRequest) throws Exception {

        Map<String, String[]> parameterMap = servletRequest.getParameterMap();
        log.debug("deleteUsersFromDistGroup; distUserMap={};" + servletRequest.getRequestURI());
        MultivaluedMap<String, String> uriInfo = new MultivaluedHashMap<>();
        parameterMap.forEach((key, values) -> uriInfo.addAll(key, Arrays.asList(values)));
        MultivaluedMap<String, String> map = uriInfo;

        distUserMapDal.deleteUsersFromDistGroup(map);
    }

    private void validate(DistributionGroup distributionGroup) {
        Set<ConstraintViolation<DistributionGroup>> violations = validator.validate(distributionGroup);
        if (violations.size() > 0) {
            log.debug("DistributionGroupRest; distributionGroupRest={}", violations);

            ConstraintViolation<DistributionGroup> v = violations.iterator().next();
            ErrorInfoException e = new ErrorInfoException("validationError", v.getMessage());
            e.getParameters().put("value", v.getMessage() + " in " + v.getPropertyPath());
            throw e;
        }
    }

    private void validateUserMap(DistUserMap distUserMap) {
        Set<ConstraintViolation<DistUserMap>> violations = validator.validate(distUserMap);
        if (violations.size() > 0) {
            log.debug("DistributionGroupRest; distributionGroupRest={}", violations);
            ConstraintViolation<DistUserMap> v = violations.iterator().next();
            ErrorInfoException e = new ErrorInfoException("validationError", v.getMessage());
            e.getParameters().put("value", v.getMessage() + " in " + v.getPropertyPath());
            throw e;
        }

    }

}
