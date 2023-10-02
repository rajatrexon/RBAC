package com.esq.rbac.service.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.interceptor.KeyGenerator;
import org.springframework.util.StringUtils;

import java.lang.reflect.Method;
import java.util.ArrayList;

public class CustomKeyGenerator implements KeyGenerator {
	
	private static final Logger log = LoggerFactory.getLogger(CustomKeyGenerator.class);
	
	@Override
	public Object generate(Object target, Method method, Object... params) {
		log.trace("Params before parsing:{}", params);
		if(params.length>0) {
			java.util.List<Object> newParamList = new ArrayList<Object>();
			for(int i=0;i<params.length;i++) {
				log.trace("{} iteration; param:{}", i, params[i]);
				if(params[i] != null && params[i].toString().contains("OptionFilter")) {
					//OptionFilter{_=1623844671032, tenantId=100}
					String optionFilter = params[i].toString().split("OptionFilter")[1]
							.replace("{", "").replace("}", "").replace("[", "").replace("]", "");
					log.trace("OptionFilter parsed string: {}", optionFilter);
					Object[] filters = optionFilter.split(",");
					for(int j=0;j<filters.length;j++) {
						if(!filters[j].toString().contains("_=")) {
							newParamList.add(filters[j].toString().trim());	
						}
					}
					
				}
			}
			
			log.trace("newParamsList:{}", newParamList);
			if(!newParamList.isEmpty()) {
				 params = new Object[newParamList.size()];
			      newParamList.toArray(params);
//				params = newParamList.toArray();
			}
		}
		
		log.trace("Parsed params: {}", params);
		
		return target.getClass().getSimpleName() + "_"
        + method.getName() + "_"
        + StringUtils.arrayToDelimitedString(params, "_");
	}
	 
}