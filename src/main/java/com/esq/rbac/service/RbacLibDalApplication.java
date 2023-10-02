package com.esq.rbac.service;

import com.esq.rbac.service.util.DeploymentUtil;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@SpringBootApplication
@EnableTransactionManagement
@EnableConfigurationProperties(DeploymentUtil.class)
//@EnableCaching
public class RbacLibDalApplication {

	public static void main(String[] args) {
		SpringApplication.run(RbacLibDalApplication.class, args);
	}
}
