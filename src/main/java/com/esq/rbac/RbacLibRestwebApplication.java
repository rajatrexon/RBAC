package com.esq.rbac;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.web.reactive.config.EnableWebFlux;
import org.springframework.web.reactive.function.client.WebClient;

import static org.springframework.web.reactive.function.client.WebClient.*;

@SpringBootApplication
public class RbacLibRestwebApplication {


//	@Bean
//	public WebClient.Builder webClientBuilder() {
//		return WebClient.builder();
//	}
//

	public static void main(String[] args) {


		SpringApplication.run(RbacLibRestwebApplication.class, args);
	}

}
