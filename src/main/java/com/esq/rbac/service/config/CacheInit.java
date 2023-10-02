package com.esq.rbac.service.config;


import com.esq.rbac.service.organization.organizationmaintenance.service.OrganizationMaintenanceDal;
import com.esq.rbac.service.user.service.UserDal;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.CacheManager;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.stereotype.Component;

@Component
public class CacheInit implements ApplicationListener<ContextRefreshedEvent>{

	private static final Logger log = LoggerFactory.getLogger(CacheInit.class);

	UserDal userDal;

	OrganizationMaintenanceDal orgMaintenanceDal;

	@Autowired
	private void setUserDal(UserDal userDal) {
		this.userDal = userDal;
	}

	@Autowired
	private void setOrgMaintenanceDal(OrganizationMaintenanceDal orgMaintenanceDal) {
		this.orgMaintenanceDal = orgMaintenanceDal;
	}

	CacheManager cacheManager;

	@Autowired
	private void setCacheManager(CacheManager cacheManager) {
		this.cacheManager = cacheManager;
	}

	@Override
	public void onApplicationEvent(ContextRefreshedEvent event) {
//		// TODO Auto-generated method stub
//		log.info("Initiated to load UsersList, OrganizationList data");
//
//    	//taking the default values to populate Options for getList, so that it can be populated in the cache while service starts
//
//    	//created a var just for init
//        //Todo thought -> to use either hashmap or copy the implementation
//    //Todo
//        MultivaluedMapImpl userParam = new MultivaluedMapImpl();
//
//    	OptionPage optionPage = new OptionPage(0, Integer.MAX_VALUE);
//        OptionSort optionSort = new OptionSort(userParam);
//        OptionFilter optionFilter = new OptionFilter(userParam);
//        Options options = new Options(optionPage, optionSort, optionFilter);
//    	userDal.getList(options);
//    	orgMaintenanceDal.getList(options);
	}

}
