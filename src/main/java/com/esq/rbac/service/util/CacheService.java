package com.esq.rbac.service.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.CacheManager;
import org.springframework.stereotype.Service;

@Service
public class CacheService {

    private static final Logger log = LoggerFactory.getLogger(CacheService.class);
	

	@Autowired
	CacheManager cacheManager;
	


	public void clearCache(String[] cacheToRemove){
	        for(String name:cacheToRemove){
	        	log.debug("CacheService; names {}",name);
	            cacheManager.getCache(name).clear();    
	            log.debug("CacheService; get {}", cacheManager.getCache(name));
	        }
	    }

}
