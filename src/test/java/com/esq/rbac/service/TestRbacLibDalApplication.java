package com.esq.rbac.service;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.context.annotation.Bean;
import org.testcontainers.containers.MSSQLServerContainer;

@TestConfiguration(proxyBeanMethods = false)
public class TestRbacLibDalApplication {

	@Bean
	@ServiceConnection
	MSSQLServerContainer<?> sqlServerContainer() {
		return new MSSQLServerContainer<>("mcr.microsoft.com/mssql/server:latest");
	}

	public static void main(String[] args) {
		SpringApplication.from(RbacLibDalApplication::main).with(TestRbacLibDalApplication.class).run(args);
	}

}
