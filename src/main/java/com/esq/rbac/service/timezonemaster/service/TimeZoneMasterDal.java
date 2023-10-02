package com.esq.rbac.service.timezonemaster.service;

import com.esq.rbac.service.timezonemaster.domain.TimeZoneMaster;

import java.util.List;

public interface TimeZoneMasterDal  {
	
	public List<TimeZoneMaster> getTimeZones();

}
