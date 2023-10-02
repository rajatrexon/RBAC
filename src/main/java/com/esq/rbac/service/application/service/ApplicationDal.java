/*
 * Copyright (c)2013 ESQ Management Solutions Pvt Ltd. All Rights Reserved.
 *
 * Permission to use, copy, modify, and distribute this software requires
 * a signed licensing agreement.
 *
 * IN NO EVENT SHALL ESQ BE LIABLE TO ANY PARTY FOR DIRECT, INDIRECT, SPECIAL,
 * INCIDENTAL, OR CONSEQUENTIAL DAMAGES, INCLUDING LOST PROFITS, ARISING OUT OF
 * THE USE OF THIS SOFTWARE AND ITS DOCUMENTATION, EVEN IF ESQ HAS BEEN ADVISED
 * OF THE POSSIBILITY OF SUCH DAMAGE. ESQ SPECIFICALLY DISCLAIMS ANY WARRANTIES,
 * INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND
 * FITNESS FOR A PARTICULAR PURPOSE.
 */
package com.esq.rbac.service.application.service;

import com.esq.rbac.service.application.childapplication.domain.ChildApplication;
import com.esq.rbac.service.application.domain.Application;
import com.esq.rbac.service.auditloginfo.domain.AuditLogInfo;
import com.esq.rbac.service.basedal.BaseDal;
import com.esq.rbac.service.rolesinapplicationjson.RolesInApplicationJson;
import com.esq.rbac.service.util.dal.Options;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public interface ApplicationDal extends BaseDal {

    Application create(Application application, AuditLogInfo auditLogInfo);

    Application update(Application application, AuditLogInfo auditLogInfo);

    Application updateTargetOperations(Application application);

    Application getById(int applicationId);

    Application getByName(String name);

    void deleteById(int applicationId, AuditLogInfo auditLogInfo);

    void deleteByName(String name, AuditLogInfo auditLogInfo);

    List<String> getAllNames();

    List<Application> getList(Options options);

    int getCount(Options options);

    List<Application> searchList(Options options);

	int getSearchCount(Options options);

    List<Application> getUserAuthorizedApps(String userName);

    List<Map<String,Object>> getApplicationIdNames(Options options);

    String getRBACContextName();

    Date getStatus();

    ChildApplication getAppDashboardChildApplication();

	List<RolesInApplicationJson> getRolesInApplicationsData(Map<String, String> scopeMap);

	List<Map<String,Object>> getChildApplicationNamesForScheduleMaintenence(Options options);

	Map<String,Object> getLicenseByAppKey(String appKey);

	String getApplicationNameByAppKey(String appKey);

	List<Integer> getRevokedChildApplicationIds(Options options);

	List<Map<String, Object>> getUserAuthorizedApplicationIdNames(String userName, Options options);

    List<Integer> getAllIds();

    List<Map<String,Object>> getApplicationIdNamesForLoggedInUser(String loggedInUserName);


    public void validate(Application application);
}
