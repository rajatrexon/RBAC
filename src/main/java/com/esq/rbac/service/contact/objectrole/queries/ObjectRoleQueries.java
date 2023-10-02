package com.esq.rbac.service.contact.objectrole.queries;

public interface ObjectRoleQueries {

    String LIST_QUERY_NATIVE =
            "SELECT DISTINCT o.*"
                    + " FROM contact.objectrole o"
                    + " LEFT OUTER JOIN contact.objectrole_contact oc  ON (oc.objectrole_id = o.ID)"
                    + " LEFT OUTER JOIN contact.contact c              ON (c.ID = oc.contact_id)"
                    + " LEFT OUTER JOIN contact.contact_address_type a ON (a.ID = c.address_type)"
                    + " JOIN contact.contact_role r                    ON (r.ID = o.contact_role_id)";
    String LIST_QUERY_NATIVE_SPECIAL =
            "SELECT DISTINCT o.*, t.tenantName"
                    + " FROM contact.objectrole o"
                    + " LEFT OUTER JOIN contact.objectrole_contact oc  ON (oc.objectrole_id = o.ID)"
                    + " LEFT OUTER JOIN contact.contact c              ON (c.ID = oc.contact_id)"
                    + " LEFT OUTER JOIN contact.contact_address_type a ON (a.ID = c.address_type)"
                    + " LEFT OUTER JOIN rbac.tenant t  				   ON (o.tenant_id = t.tenantId)"
                    + " JOIN contact.contact_role r                    ON (r.ID = o.contact_role_id)";

    String LIST_QUERY_NATIVE_APPKEY =
            "SELECT DISTINCT o.*"
                    + " FROM contact.objectrole o"
                    + " LEFT OUTER JOIN contact.objectrole_contact oc  ON (oc.objectrole_id = o.ID)"
                    + " LEFT OUTER JOIN contact.contact c              ON (c.ID = oc.contact_id)"
                    + " LEFT OUTER JOIN contact.contact_address_type a ON (a.ID = c.address_type)"
                    + " JOIN contact.contact_role r                    ON (r.ID = o.contact_role_id)"
                    + " JOIN rbac.application ra                    ON (ra.applicationId = o.appKey)";
    String LIST_QUERY_NATIVE_SPECIAL_APPKEY =
            "SELECT DISTINCT o.*, t.tenantName"
                    + " FROM contact.objectrole o"
                    + " LEFT OUTER JOIN contact.objectrole_contact oc  ON (oc.objectrole_id = o.ID)"
                    + " LEFT OUTER JOIN contact.contact c              ON (c.ID = oc.contact_id)"
                    + " LEFT OUTER JOIN contact.contact_address_type a ON (a.ID = c.address_type)"
                    + " LEFT OUTER JOIN rbac.tenant t  				   ON (o.tenant_id = t.tenantId)"
                    + " JOIN contact.contact_role r                    ON (r.ID = o.contact_role_id)"
                    + "	JOIN rbac.application ra                    ON (ra.applicationId = o.appKey)";

    String FULL_TEXT_SEARCH_NATIVE =
            "SELECT DISTINCT o.*"
                    + " FROM contact.objectrole o"
                    + " LEFT OUTER JOIN contact.objectrole_contact oc  ON (oc.objectrole_id = o.ID)"
                    + " LEFT OUTER JOIN contact.contact c              ON (c.ID = oc.contact_id)"
                    + " LEFT OUTER JOIN contact.contact_address_type a ON (a.ID = c.address_type)"
                    + " JOIN contact.contact_role r                    ON (r.ID = o.contact_role_id)"
                    + " LEFT OUTER JOIN contact.schedule s             ON (s.ID = o.schedule_id)"
                    + " WHERE (LOWER(o.object_id) LIKE ?"
                    + " OR LOWER(r.name) LIKE ?"
                    + " OR LOWER(c.address) LIKE ?"
                    + " OR LOWER(a.type) LIKE ?"
                    + " OR LOWER(s.name) LIKE ? ) ";
    String FULL_TEXT_SEARCH_NATIVE_SPECIAL =
            "SELECT DISTINCT o.*"
                    + " FROM contact.objectrole o"
                    + " LEFT OUTER JOIN contact.objectrole_contact oc  ON (oc.objectrole_id = o.ID)"
                    + " LEFT OUTER JOIN contact.contact c              ON (c.ID = oc.contact_id)"
                    + " LEFT OUTER JOIN contact.contact_address_type a ON (a.ID = c.address_type)"
                    + " JOIN contact.contact_role r                    ON (r.ID = o.contact_role_id)"
                    + " LEFT OUTER JOIN contact.schedule s             ON (s.ID = o.schedule_id)"
                    + " LEFT OUTER JOIN rbac.tenant t 				   ON (o.tenant_id = t.tenantId)"
                    + " WHERE (LOWER(o.object_id) LIKE ?"
                    + " OR LOWER(r.name) LIKE ?"
                    + " OR LOWER(c.address) LIKE ?"
                    + " OR LOWER(a.type) LIKE ?"
                    + " OR LOWER(s.name) LIKE ? "
                    + " OR LOWER(t.tenantName) LIKE ?  ) ";
    int FULL_TEXT_SEARCH_NATIVE_PARAMETERS = 6;
    String FULL_TEXT_COUNT_NATIVE =
            "SELECT COUNT(DISTINCT o.id)"
                    + " FROM contact.objectrole o"
                    + " LEFT OUTER JOIN contact.objectrole_contact oc  ON (oc.objectrole_id = o.ID)"
                    + " LEFT OUTER JOIN contact.contact c              ON (c.ID = oc.contact_id)"
                    + " LEFT OUTER JOIN contact.contact_address_type a ON (a.ID = c.address_type)"
                    + " JOIN contact.contact_role r                    ON (r.ID = o.contact_role_id)"
                    + " LEFT OUTER JOIN contact.schedule s             ON (s.ID = o.schedule_id)"
                    + " WHERE (LOWER(o.object_id) LIKE ?"
                    + " OR LOWER(r.name) LIKE ?"
                    + " OR LOWER(c.address) LIKE ?"
                    + " OR LOWER(a.type) LIKE ?"
                    + " OR LOWER(s.name) LIKE ?) ";


    String OBJECT_NAME_SEARCH ="select count(1) from contact.objectrole where object_id= ?";
    String READ_OBJECTROLE=  "select o from ObjectRole o where o.objectKey = :objectKey and o.tenantId= :tenantId";
    String READ_ACTIONRULE = "SELECT DISTINCT o.object_id FROM contact.objectrole o";
}
