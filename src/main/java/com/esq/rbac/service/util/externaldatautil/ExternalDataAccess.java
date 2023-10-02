package com.esq.rbac.service.util.externaldatautil;

import com.esq.rbac.service.scope.scopeconstraint.domain.ScopeConstraint;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.ws.rs.core.UriInfo;


public interface ExternalDataAccess {


    String list(ScopeConstraint scopeConstraint, HttpServletRequest servletRequest, Integer userId);

    String update(ScopeConstraint scopeConstraint, String data, String contentType, Integer userId);
}
