package com.esq.rbac.service.contact.contactrole.queries;

public interface ContactRoleQueries {

    String LIST_CONTACT_ROLES =
            "SELECT c FROM ContactRole c ORDER BY c.id ASC";

    String LIST_CONTACT_ROLES_WITH_TENANT_ID =
            "SELECT c FROM ContactRole c WHERE c.tenantId = :tenantId ORDER BY c.id ASC";

    String LIST_CONTACT_ROLES_WITH_ID_TENANT_ID_LIST =
            "SELECT c FROM ContactRole c WHERE c.tenantId IN :tenantId ORDER BY c.id ASC";

    String QUERY_CONTACT_ROLE_NAME =
            "SELECT DISTINCT c.name FROM ObjectRole o, ContactRole c " +
                    "WHERE c.id = o.contactRoleId AND :objectId = o.objectId";

    String QUERY_CONTACT_ROLE_NAME_BY_TENANT =
            "SELECT DISTINCT c.name FROM ObjectRole o, ContactRole c " +
                    "WHERE c.id = o.contactRoleId AND :objectId = o.objectId AND o.tenantId = c.tenantId";

    String FIND_CONTACT_ROLE_ID_BY_NAME =
            "SELECT c.id FROM ContactRole c WHERE c.name = :name";

    String CONTACT_ROLE_BY_NAME =
            "SELECT c FROM ContactRole c WHERE c.name = :name";

    String CONTACT_ROLE_BY_NAME_AND_TENANT =
            "SELECT c FROM ContactRole c WHERE c.name = :name AND c.tenantId = :tenantId";
}
