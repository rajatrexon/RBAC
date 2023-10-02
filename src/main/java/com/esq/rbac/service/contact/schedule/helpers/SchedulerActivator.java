//package com.esq.rbac.service.user.contact.schedule.helpers;
//
//import lombok.extern.slf4j.Slf4j;
//import org.springframework.context.annotation.Condition;
//import org.springframework.context.annotation.ConditionContext;
//import org.springframework.context.annotation.Configuration;
//import org.springframework.core.type.AnnotatedTypeMetadata;
//import java.io.File;
//import java.io.FileInputStream;
//import java.io.IOException;
//import java.util.Properties;
//
//@Configuration
//@Slf4j
//// Todo @PropertySource("file:${esq.conf.dir}/init.properties")
//public class SchedulerActivator implements Condition {
//
//    private String status="false";
//    private Properties properties = new Properties();
//    public SchedulerActivator(){
//
//        String propsUrl = System.getProperty("defaultProps");
//        try {
//            properties.load(new FileInputStream(new File(propsUrl)));
//            String rbacHealthEnabled = properties.getProperty("deploymentUtil.activaterbachealth");
//            String healthSchedulerTimeInterval = properties.getProperty("deploymentUtil.rbachealthschedulertimeinterval");
//            if(rbacHealthEnabled != null && !rbacHealthEnabled.isEmpty() && healthSchedulerTimeInterval != null && !healthSchedulerTimeInterval.isEmpty()) {
//                if(rbacHealthEnabled.equalsIgnoreCase("true")) {
//                    status = "true";
//                }
//            }
//        } catch (IOException e) {
//            log.error("Error while reading file for RBAC health scheduler activator: {}", e.getMessage());
//            status="false";
//
//        }
//
//    }
//
//    @Override
//    public boolean matches(ConditionContext context, AnnotatedTypeMetadata metadata) {
//        if("true".equals(status)) {
//            return true;
//        }
//        return false;
//    }
//}
