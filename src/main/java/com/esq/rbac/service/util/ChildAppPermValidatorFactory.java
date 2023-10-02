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

import com.fasterxml.jackson.databind.JsonNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.AutowireCapableBeanFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Service;


@Service
public class ChildAppPermValidatorFactory implements ApplicationContextAware {
	private static final Logger log = LoggerFactory
			.getLogger(ChildAppPermValidatorFactory.class);
	private ApplicationContext applicationContext;

	@Override
	public void setApplicationContext(ApplicationContext applicationContext)
			throws BeansException {
		log.trace("setApplicationContext");
		this.applicationContext = applicationContext;
	}

	@SuppressWarnings("unchecked")
	public <T> T newInstance(Class<T> clazz, String appKey,
			JsonNode validatorData) throws Exception {
		try {
			log.trace("newInstance; class={}", clazz.getName());
			AutowireCapableBeanFactory factory = applicationContext
					.getAutowireCapableBeanFactory();
			log.trace("autowire called");
			T validator = (T) factory.autowire(clazz,
					AutowireCapableBeanFactory.AUTOWIRE_BY_TYPE, false);
			ChildAppPermValidator newObj = (ChildAppPermValidator) validator;
			newObj.setAppKey(appKey);
			newObj.setValidatorData(validatorData);
			log.trace("initializeBean called");
			validator = (T) factory.initializeBean(validator, null);
			return validator;

		} catch (Exception e) {
			log.warn("newInstance; class={}; exception {}", clazz.getName(),
					e.toString());
			throw e;
		}
	}

}
