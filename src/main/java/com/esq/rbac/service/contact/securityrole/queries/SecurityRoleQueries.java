package com.esq.rbac.service.contact.securityrole.queries;

public interface SecurityRoleQueries {

    String LIST_SECURITY_ROLES =
            "select r from SecurityRole r where lower(r.name) like :q order by r.id ASC";
}
