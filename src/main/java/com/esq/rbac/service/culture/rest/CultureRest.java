package com.esq.rbac.service.culture.rest;

import com.esq.rbac.service.culture.domain.Culture;
import com.esq.rbac.service.culture.embedded.ApplicationCulture;
import com.esq.rbac.service.culture.embedded.ResourceStrings;
import com.esq.rbac.service.culture.service.CultureDal;
import com.esq.rbac.service.timezonemaster.domain.TimeZoneMaster;
import com.esq.rbac.service.timezonemaster.service.TimeZoneMasterService;
import jakarta.ws.rs.core.MediaType;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/culture")
public class CultureRest {

    private TimeZoneMasterService timeZoneMasterDal;
    @Autowired
    public void setTimeZoneMasterDal(TimeZoneMasterService timeZoneMasterDal) {
        this.timeZoneMasterDal = timeZoneMasterDal;
    }

    private CultureDal cultureDal;
    @Autowired
    public void setCultureDal(CultureDal cultureDal) {
        this.cultureDal = cultureDal;
    }


    @GetMapping(value="/timezones", produces = MediaType.APPLICATION_JSON)
    public ResponseEntity<List<TimeZoneMaster>> getSupportedTimezones() throws Exception {
        return ResponseEntity.ok(timeZoneMasterDal.getTimeZones());
    }

    @GetMapping(value="/supported", produces = MediaType.APPLICATION_JSON)
    public ResponseEntity<List<Culture>> getSupportedCulture() throws Exception {
        return ResponseEntity.ok(cultureDal.getSupportedCultures());
    }


    @GetMapping(value="/application/applications", produces = MediaType.APPLICATION_JSON)
    public ResponseEntity<List<ApplicationCulture>> getAllApplicationSupportedCulture() throws Exception {
        return ResponseEntity.ok(cultureDal.getAllApplicationSupportedCulture());
    }


    @PostMapping(value="/application", produces = MediaType.APPLICATION_JSON)
    public ResponseEntity<ApplicationCulture> addApplicationSupportedLanguages(@RequestBody ApplicationCulture applicationCulture) throws Exception {
        return ResponseEntity.ok(cultureDal.assingLanguageToApplication(applicationCulture));
    }


    @PutMapping(value="/application", produces = MediaType.APPLICATION_JSON)
    public ResponseEntity<ApplicationCulture> updateApplicationSupportedLanguages(@RequestBody ApplicationCulture applicationCulture) throws Exception {
        return ResponseEntity.ok(cultureDal.updateApplicationLanguage(applicationCulture));
    }

    @GetMapping(value="/application/name/{appKey}", produces = MediaType.APPLICATION_JSON)
    public ResponseEntity<ApplicationCulture> getByApplicationKey(@PathVariable("appKey") String appKey) throws Exception {
        if(appKey != null && !appKey.isEmpty())
            return ResponseEntity.ok(cultureDal.getByApplicationKey(appKey));
        else
            throw new Exception("Application key is required");
    }

    @GetMapping(value="/application/id/{appId}", produces = MediaType.APPLICATION_JSON)
    public ResponseEntity<ApplicationCulture> getByApplicationId(@PathVariable("appId") Integer appId) throws Exception {
        if(appId != null && appId > 0)
            return ResponseEntity.ok(cultureDal.getByApplicationId(appId));
        else
            throw new Exception("Application Id is required");
    }

    @GetMapping(value="/resources/strings", produces = MediaType.APPLICATION_JSON)
    public ResponseEntity<ResourceStrings> getApplicationResourceStrings(@RequestHeader HttpHeaders headers) throws Exception {
        log.trace("In getApplicationResourceStrings");
        if(headers.get("appName") == null || headers.get("appName").get(0) == null)
            throw new Exception("Application Name is required to get Resource Strings");

        if(headers.get("cultureName") == null || headers.get("cultureName").get(0) == null)
            throw new Exception("Culture Name is required to get Resource Strings");

        return ResponseEntity.ok(cultureDal.getApplicationResourceStrings(headers));
    }

}
