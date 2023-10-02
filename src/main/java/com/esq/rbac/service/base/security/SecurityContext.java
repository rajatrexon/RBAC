package com.esq.rbac.service.base.security;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

public class SecurityContext {

    private static final Logger log = LoggerFactory.getLogger(SecurityContext.class);

    public static UserInfo getCurrentUserInfo() {
        org.springframework.security.core.context.SecurityContext securityContext = SecurityContextHolder.getContext();
        if (securityContext == null) {
            log.warn("getUserInfo; SecurityContextHolder.getContext() returned null");
            return null;
        }
        Authentication auth = securityContext.getAuthentication();
        if (auth == null) {
            log.warn("getUserInfo; Null Authentication in security context");
            return null;
        }
        if (auth.isAuthenticated() == false) {
            log.warn("getUserInfo; Authentication.isAuthenticated=false");
            return null;
        }

        Object principal = auth.getPrincipal();
        if (principal == null) {
            log.warn("");
            return null;
        }
        if (!(principal instanceof UserInfo)) {
            log.warn("");
            return null;
        }

        return (UserInfo) principal;
    }

    public static void verifyPermission(String permission) throws Exception {
        UserInfo currentUserInfo = getCurrentUserInfo();
        if (currentUserInfo.hasPermission(permission) == false) {
            throw new AccessDeniedException(permission);
        }
    }
}

