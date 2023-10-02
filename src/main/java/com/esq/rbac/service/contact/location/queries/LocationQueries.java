package com.esq.rbac.service.contact.location.queries;

public interface LocationQueries {

        String LIST_LOCATIONS =
                "select l from Location l where lower(l.name) like :q order by l.name ASC";
}
