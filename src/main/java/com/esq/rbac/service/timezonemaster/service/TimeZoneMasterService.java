package com.esq.rbac.service.timezonemaster.service;

import com.esq.rbac.service.timezonemaster.domain.TimeZoneMaster;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface TimeZoneMasterService {

    public List<String> getTimeZoneDisplayNames();

    public List<TimeZoneMaster> getTimeZones();

    public String getOffsetOfTimeZone(String timeZone);
}
