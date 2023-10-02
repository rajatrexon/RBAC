package com.esq.rbac.service.timezonemaster.service;

import com.esq.rbac.service.timezonemaster.domain.TimeZoneMaster;
import com.esq.rbac.service.timezonemaster.repository.TimeZoneMasterRepository;
import jakarta.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;

@Slf4j
@Service
public class TimeZoneMasterServiceImpl implements TimeZoneMasterService{


    private TimeZoneMasterRepository timeZoneMasterRepository;

    @Autowired
    public  void setTimeZoneMasterRepository(TimeZoneMasterRepository timeZoneMasterRepository) {
        this.timeZoneMasterRepository = timeZoneMasterRepository;
    }

    @Override
    public List<String> getTimeZoneDisplayNames() {
        return timeZoneMasterRepository.getTimeZoneDisplayNames();
    }

    @Override
    public List<TimeZoneMaster> getTimeZones() {
        List<TimeZoneMaster> result = timeZoneMasterRepository.getTimeZones();
        Collections.sort(result, (r1, r2) -> r1.getTimeOffsetMinute() - r2.getTimeOffsetMinute());
        return result;
    }

    @Override
    public String getOffsetOfTimeZone(String timeZone) {
        return timeZoneMasterRepository.getOffsetOfTimeZone(timeZone);
    }
}
