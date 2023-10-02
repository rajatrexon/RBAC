package com.esq.rbac.service.application.applicationmaintenance.service;

import com.esq.rbac.service.application.applicationmaintenance.domain.ApplicationMaintenance;

import com.esq.rbac.service.basedal.BaseDal;
import com.esq.rbac.service.util.dal.Options;
import org.joda.time.DateTime;

import java.util.Date;
import java.util.List;


public interface ApplicationMaintenanceDal extends BaseDal {


    ApplicationMaintenance create(ApplicationMaintenance applicationMaintenance, int userId);

    ApplicationMaintenance update(ApplicationMaintenance applicationMaintenance, int userId);

    void deleteById(int maintenanceId);

    List<ApplicationMaintenance> getList(Options options);

    int getCount(Options options);

    ApplicationMaintenance getById(int maintenanceId);

    List<ApplicationMaintenance> getAppsUnderMaintenance();

    Date getMinimumRefreshTime();

    Date getNextRefreshTime(Date currentTime);

    void updateExpiredFlag();

    void refreshAppMaintenanceCache(DateTime nextRefreshTime);

    void deleteByChildApplicationId(int childApplicationId);
}
