/*
 * Copyright (c)2014 ESQ Management Solutions Pvt Ltd. All Rights Reserved.
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
package com.esq.rbac.service.util;


import com.esq.rbac.service.application.childapplication.domain.ChildApplication;
import com.esq.rbac.service.application.domain.Application;
import com.esq.rbac.service.application.service.ApplicationDal;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.map.CaseInsensitiveMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;


import java.util.List;
import java.util.Map;



@Component
@Slf4j
public class ChildAppPermValidatorUtil {



	private ApplicationDal applicationDal;


	private ChildAppPermValidatorFactory childAppPermValidatorFactory;



	@SuppressWarnings("unchecked")
	private Map<String, ChildAppPermValidator> validatorMap = new CaseInsensitiveMap();


//	TODO INITIALIZERVALIDATOR
	@Autowired
	public void dependencies(@Lazy ApplicationDal applicationDal, @Lazy ChildAppPermValidatorFactory childAppPermValidatorFactory) {
		this.applicationDal = applicationDal;
		this.childAppPermValidatorFactory = childAppPermValidatorFactory;
//		initializeValidators(this.applicationDal.getList(null));
	}


	@Transactional
	public synchronized void initializeValidators(List<Application> appList) {
		validatorMap.clear();
		if (appList != null && !appList.isEmpty()) {
			for (Application app : appList) {
				if (app.getChildApplications() != null
						&& !app.getChildApplications().isEmpty()) {
					for (ChildApplication childApp : app.getChildApplications()) {
						if (childApp.getPermissionValidator() != null) {
							if (childApp.getPermissionValidator().equalsIgnoreCase(
									ChildAppPermValidator.SCOPECONSTRAINT_TYPE)) {
								try {
									JsonNode data = null;
									if (childApp.getPermissionValidatorData() != null
											&& !childApp
													.getPermissionValidatorData()
													.isEmpty()) {
										data = new ObjectMapper().readTree(childApp.getPermissionValidatorData());	
									}
									ChildAppPermValidatorSC validator = childAppPermValidatorFactory
											.newInstance(
													ChildAppPermValidatorSC.class,
													childApp.getAppKey(), data);
									validatorMap.put(childApp.getAppKey(),
											validator);
								} catch (Exception e) {
									log.error(
											"initializeValidators; Exception={};",
											e);
								}
							}
						}
					}
				}
			}
		}
	}

	public boolean validate(String appKey, String userName) {
		ChildAppPermValidator validator = validatorMap.get(appKey);
		if (validator != null) {
			return validator.validate(userName);
		}
		return true;
	}
}
