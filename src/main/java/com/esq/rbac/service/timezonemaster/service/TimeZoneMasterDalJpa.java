package com.esq.rbac.service.timezonemaster.service;

import com.esq.rbac.service.basedal.BaseDalJpa;
import com.esq.rbac.service.lookup.Lookup;
import com.esq.rbac.service.timezonemaster.domain.TimeZoneMaster;
import com.esq.rbac.service.timezonemaster.repository.TimeZoneMasterRepository;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.event.ApplicationStartedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

@Service
public class TimeZoneMasterDalJpa extends BaseDalJpa implements TimeZoneMasterDal {
	
	private static final Logger log = LoggerFactory.getLogger(TimeZoneMasterDalJpa.class);
	
//	@PersistenceContext
//    public void setEntityManager(EntityManager em) {
//        log.trace("setEntityManager");
//        this.em = em;
//        this.entityClass = TimeZoneMaster.class;
//        TimeZoneMasterLookup.fillTimeZoneMasterLookupTable(getTimeZones());
//    }


	@Autowired
	TimeZoneMasterRepository timeZoneMasterRepository;

//	@Autowired
//	public TimeZoneMasterDalJpa(){
//		log.info("fillTimeZoneMasterLookupTable");
//		TimeZoneMasterLookup.fillTimeZoneMasterLookupTable(getTimeZones());
//	}


	@EventListener
	public void initTimeZoneLookupTable(ApplicationStartedEvent event) {
		log.info("fillTimeZoneMasterLookupTable");
		Lookup.fillTimeZoneMasterLookupTable(getTimeZones());
	}





	@Override
	@Transactional
	public List<TimeZoneMaster> getTimeZones() {
		log.trace("getTimeZOneDisplayNameList;");
		List<TimeZoneMaster> result = Lookup.getTimeZoneMaster();
		if(result.isEmpty()) {
//			TypedQuery<TimeZoneMaster> query = em.createNamedQuery("getTimeZones", TimeZoneMaster.class);
//			result =  query.getResultList();
			result = timeZoneMasterRepository.getTimeZones();
		}
		result.sort((r1, r2) -> r1.getTimeOffsetMinute() - r2.getTimeOffsetMinute());
		return result;
	}


	
}
