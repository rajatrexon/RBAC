package com.esq.rbac.service.health.scheduler;

import com.esq.rbac.service.health.filter.RBACHealthFilter;
import com.esq.rbac.service.health.service.HealthDal;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@SpringBootApplication
@EnableScheduling
@Component
@Slf4j
public class HealthScheduler {

    @Autowired
    private HealthDal healthDal;

    public static void main(String[] args) {
        SpringApplication.run(HealthScheduler.class, args);
    }

    @Scheduled(fixedDelayString = "${deployement-util.rbachealthschedulertimeinterval}")
    public void run() {
        try {
            healthDal.healthManager(RBACHealthFilter.healthMap);
        } catch (Exception e) {
            log.error(e.getMessage());
        }
    }
}
