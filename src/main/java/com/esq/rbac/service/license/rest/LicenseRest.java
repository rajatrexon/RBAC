package com.esq.rbac.service.license.rest;

import com.esq.rbac.service.application.service.ApplicationDal;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import java.util.Date;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/license")
public class LicenseRest {

    private ApplicationDal applicationDal;

    @Autowired
    public void setApplicationDal(ApplicationDal applicationDal) {
        log.trace("setApplicationDal");
        this.applicationDal = applicationDal;
    }

    @GetMapping(value ="/{appKey}", produces = MediaType.APPLICATION_JSON)
    public ResponseEntity< Map<String, Object>> getChildApplicationLicenseByAppKey(@PathVariable("appKey") String appKey) {
        log.trace("getChildApplicationLicenseByAppKey; appKey={}", appKey);
        Map<String, Object> result = applicationDal.getLicenseByAppKey(appKey);
        return ResponseEntity.ok(result);
    }

}
