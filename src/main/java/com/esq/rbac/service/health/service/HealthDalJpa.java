package com.esq.rbac.service.health.service;

import com.esq.rbac.service.health.domain.RBACHealth;
import com.esq.rbac.service.health.repository.HealthRepository;
import jakarta.persistence.NoResultException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.List;
import java.util.Map;

@Service
@Slf4j
public class HealthDalJpa implements  HealthDal{


    private HealthRepository healthRepository;

    @Autowired
    public void setHealthRepository(HealthRepository healthRepository) {
        this.healthRepository = healthRepository;
    }

    @Override
    public RBACHealth createHealthInfo(RBACHealth rbacHealth) {
        return healthRepository.save(rbacHealth);
//        return healthRepository.createHealthInfo(rbacHealth.getComponentName(), rbacHealth.getHealthUpdateTime(),rbacHealth.getUpdateTime());
    }

    @Override
    public RBACHealth updateHealthInfo(RBACHealth rbacHealth) {
        return healthRepository.save(rbacHealth);
//        return healthRepository.updateHealthInfo(rbacHealth.getHealthUpdateTime(),rbacHealth.getUpdateTime(),rbacHealth.getComponentName());
    }

    @Override
    public RBACHealth getHealthInfo(String appName) {
        RBACHealth rbacHealth = null;
        List<RBACHealth> rbacHealthList = null;
        try {
            rbacHealthList=healthRepository.getRBACHealthByAppName(appName);
            if (rbacHealthList != null && rbacHealthList.size() > 0) {

                rbacHealth = rbacHealthList.get(0);
            }
        } catch (NoResultException e) {
            log.error(e.getMessage());
        } catch (Exception e) {
            log.error(e.getMessage());
        }

        return rbacHealth;
    }

    @Override
    public void healthManager(Map<String, Date> healthMap) {
        if (healthMap != null && !healthMap.isEmpty()) {
            try {
                healthMap.forEach((componentName, updateTime) -> {
                    RBACHealth rbacHealthInfo = getHealthInfo(componentName);
                    if (rbacHealthInfo == null) {
                        rbacHealthInfo = new RBACHealth();
                        rbacHealthInfo.setComponentName(componentName);
                        rbacHealthInfo.setHealthUpdateTime(updateTime);
                        ZonedDateTime zdt = ZonedDateTime.now(ZoneOffset.UTC);
                        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMdd HH:mm:ss");
                        DateFormat format = new SimpleDateFormat("yyyyMMdd HH:mm:ss");
                        String dateStr = zdt.format(formatter);
                        Date utcDate = null;
                        try {
                            utcDate = format.parse(dateStr);

                        } catch (Exception e) {}

                        rbacHealthInfo.setUpdateTime(utcDate);
                        createHealthInfo(rbacHealthInfo);

                    } else {
                        rbacHealthInfo.setHealthUpdateTime(updateTime);
                        ZonedDateTime zdt = ZonedDateTime.now(ZoneOffset.UTC);

                        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMdd HH:mm:ss");

                        DateFormat format = new SimpleDateFormat("yyyyMMdd HH:mm:ss");

                        String dateStr = zdt.format(formatter);

                        Date utcDate = null;

                        try {
                            utcDate = format.parse(dateStr);

                        } catch (Exception e) {}

                        rbacHealthInfo.setUpdateTime(utcDate);
                        updateHealthInfo(rbacHealthInfo);
                    }
                });
            }catch (Exception e) {
                log.error("Error in healthManager: {}", e.getMessage());
                e.printStackTrace();
            }
        }
    }

    @Override
    public RBACHealth[] getAllAppHealthInfo() {
        return new RBACHealth[0];
    }

    @Override
    public RBACHealth deleteHealthInfo(RBACHealth rbacHealth) {
        return null;
    }
}
