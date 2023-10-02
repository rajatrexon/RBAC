package com.esq.rbac.service.contact.partydepartment.queries;

public interface PartyDepartmentQueries {

    String LIST_PARTY_DEPARTMENTS =
            "select d from PartyDepartment d where lower(d.name) like :q order by d.id ASC";
}
