package com.esq.rbac.service.application.applicationmaintenance.util;

import com.esq.rbac.service.application.applicationmaintenance.domain.ApplicationMaintenance;
import com.esq.rbac.service.application.applicationmaintenance.service.ApplicationMaintenanceDal;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Component
public class ApplicationMaintenanceRefreshDal {
    @SuppressWarnings("unused")
    private static final Logger log = LoggerFactory
            .getLogger(ApplicationMaintenanceRefreshDal.class);

    @Autowired
    private ApplicationMaintenanceDal applicationMaintenanceDal;



    @Transactional
    public void updateExpiredFlag() {
        applicationMaintenanceDal.updateExpiredFlag();
    }

    @Transactional(propagation = Propagation.SUPPORTS, readOnly = true)
    public List<ApplicationMaintenance> getAppsUnderMaintenance() {
        return applicationMaintenanceDal.getAppsUnderMaintenance();
    }

}
