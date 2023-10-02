package com.esq.rbac.service.ldapuserservice.rest;

import com.esq.rbac.service.ldapuserservice.service.LdapUserService;
import com.esq.rbac.service.user.domain.User;
import jakarta.ws.rs.FormParam;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/ldapData")
public class LdapDataRest {
    private LdapUserService ldapUserService;

    @Autowired
    public void setLdapUserService(LdapUserService ldapUserService) {
        log.trace("setLdapUserService; ldapUserService={}", ldapUserService);
        this.ldapUserService = ldapUserService;
    }

    @GetMapping(value = "/isUserImportEnabled", produces = MediaType.APPLICATION_JSON)
    public boolean isUserImportEnabled() {
        return ldapUserService.isUserImportEnabled();
    }

    @GetMapping(value = "/isBulkImportEnabled", produces = MediaType.APPLICATION_JSON)
    public boolean isBulkImportEnabled() {
        return ldapUserService.isBulkImportEnabled();
    }

    @GetMapping(value = "/isLdapDetailsSyncEnabled", produces = MediaType.APPLICATION_JSON)
    public boolean isLdapDetailsSyncEnabled() {
        return ldapUserService.isLdapDetailsSyncEnabled();
    }

    @GetMapping(value = "/isLdapEnabled", produces = MediaType.APPLICATION_JSON)
    public boolean isLdapEnabled() {
        return ldapUserService.isLdapEnabled();
    }


    @GetMapping(value = "/userDetails", produces = MediaType.APPLICATION_JSON)
    public User getUserDetails(@QueryParam("searchParam") String searchParam) throws Exception {
        return ldapUserService.getUserDetails(searchParam);
    }

    @GetMapping(value = "/allUserNames", produces = MediaType.APPLICATION_JSON)
    public List<String> getAllUserNames() throws Exception {
        return ldapUserService.getAllUserNames();
    }

    @GetMapping(value = "/testConnection", produces = MediaType.APPLICATION_JSON, consumes = MediaType.APPLICATION_FORM_URLENCODED)
    public Map<String, List<String>> testConnection(@RequestParam("url") String url, @RequestParam("userDn") String userDn, @RequestParam("password") String password, @RequestParam("base") String base) throws Exception {
        return ldapUserService.testConnection(url, userDn, password, base);
    }
}
