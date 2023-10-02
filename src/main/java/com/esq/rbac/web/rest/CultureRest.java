/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.esq.rbac.web.rest;

import com.esq.rbac.web.client.RestClient;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

import java.util.Date;

//Todo Review and disccussion
@RestController
@RequestMapping(CultureRest.RESOURCE_PATH)
public class CultureRest {
    public static final String RESOURCE_PATH = "culture";
    private static final Logger log = LoggerFactory.getLogger(CultureRest.class);
    private RestClient restClient;

    @Autowired
    public void setRestClient(RestClient restClient) {
        log.debug("setRestClient");
        this.restClient = restClient;
    }



	@GetMapping(value = "/supported", produces = MediaType.APPLICATION_JSON_VALUE)
	public Mono<ResponseEntity<String>> getSupportedCulture() throws Exception {
		log.trace("get supported cultures");

		// Make the GET request and retrieve the response body as a String
		Mono<String> responseBodyMono = restClient.resource(RESOURCE_PATH, "supported")
				.build()
				.get()
				.accept(MediaType.APPLICATION_JSON)
				.retrieve()
				.bodyToMono(String.class);

		return responseBodyMono.map(responseBody -> {
			if (!HttpStatus.OK.isSameCodeAs(ResponseEntity.ok(responseBody).getStatusCode())) {
				// If the status code is 200, return a ResponseEntity with the response body
				return ResponseEntity.ok(responseBody);
		}
			else {
				// If the status code is not 200, return a ResponseEntity with the appropriate status code
				return ResponseEntity.status(ResponseEntity.ok(responseBody).getStatusCode()).body(responseBody);
			}
		});
	}



	@GetMapping(path = "/application/applications",produces = MediaType.APPLICATION_JSON_VALUE)
    public  Mono<ResponseEntity<String>> getAllApplicationSupportedCulture() throws Exception {
        log.trace("get applications and their supported cultures");
		Mono<String> responseBodyMono = restClient.resource(RESOURCE_PATH, "application", "applications").build()
                .get()
				.accept(MediaType.APPLICATION_JSON)
                .retrieve().bodyToMono(String.class);


      return responseBodyMono.map(responseBody -> {
		  if (!HttpStatus.OK.isSameCodeAs(ResponseEntity.ok(responseBody).getStatusCode())) {
			  log.error("Error while getting applications and their supported cultures");
			  return ResponseEntity.status(ResponseEntity.ok(responseBody).getStatusCode()).body(responseBody);
		  }else {
			  return ResponseEntity.ok(responseBody);
		  }
	  });

    }



	@GetMapping(path = "/resources/strings",produces = MediaType.APPLICATION_JSON_VALUE)
	public Mono<ResponseEntity<String>> getApplicationResourceStrings(HttpServletRequest httpRequest) throws Exception {
		log.trace("get Resource Strings with Keys by Application and culture Name");
		if (httpRequest.getHeader("appName") == null || httpRequest.getHeader("appName").isEmpty())
		return Mono.just( ResponseEntity.status(HttpStatus.NOT_FOUND).body("Application Name is required"));


		if (httpRequest.getHeader("cultureName") == null || httpRequest.getHeader("cultureName").isEmpty())
			return Mono.just(ResponseEntity.status(HttpStatus.NOT_FOUND).body("Culture Name is required"));
		String category = null;
		if (httpRequest.getHeader("category") != null && !httpRequest.getHeader("category").isEmpty())
			category = httpRequest.getHeader("category");

		Mono<String> responseBodyMono = restClient.resource(RESOURCE_PATH, "resources", "strings").build()
				.get()
				.accept(MediaType.APPLICATION_JSON)
				.header("appName", httpRequest.getHeader("appName"))
				.header("cultureName", httpRequest.getHeader("cultureName"))
				.header("category", category)
				.retrieve()
				.bodyToMono(String.class);
		return responseBodyMono.map(responseBody -> {
			if (!HttpStatus.OK.isSameCodeAs(ResponseEntity.ok(responseBody).getStatusCode())) {
				log.error("Error Resource Strings with Keys by Application and culture Name");
				return ResponseEntity.status(ResponseEntity.ok(responseBody).getStatusCode()).body(responseBody);
			}

			return ResponseEntity.ok(responseBody);
		});
	}

	@GetMapping(path = "/timezones",produces = MediaType.APPLICATION_JSON_VALUE)
	public Mono<ResponseEntity<String>> getSupportedTimezones() throws Exception {
		log.trace("get supported timezones");
		Mono<String> responseBodyMono = restClient.resource(RESOURCE_PATH, "timezones").build()
                .get()
				.accept(MediaType.APPLICATION_JSON)
                .retrieve()
                .bodyToMono(String.class);


     return responseBodyMono.map(responseBody-> {
		 if (!HttpStatus.OK.isSameCodeAs(ResponseEntity.ok(responseBody).getStatusCode() )){
		 log.error("Error while getting supported timezones");
		 return ResponseEntity.status(ResponseEntity.ok(responseBody).getStatusCode()).body(responseBody);
	 }
		 return ResponseEntity.ok(responseBody);
	 });
	}
}
