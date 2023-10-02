package com.esq.rbac.service.contact.locationrole.queries;

public interface LocationRoleQueries {

    String LIST_LOCATION_ROLE_NAME_ID =
            "select c from LocationRole c order by c.id ASC";
}
