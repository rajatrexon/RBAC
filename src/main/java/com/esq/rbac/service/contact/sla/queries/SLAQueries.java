package com.esq.rbac.service.contact.sla.queries;

public interface SLAQueries {

    String QUERY_SLAS =
            "select s " +
                    "from ObjectRole o, ContactRole r " +
                    "join o.slaList s " +
                    "where r.id = o.contactRoleId " +
                    "and :objectId = o.objectId " +
                    "and :contactRole = r.name " +
                    "order by index(s)";

    String QUERY_SLAS_BY_TENANT =
            "select s " +
                    "from ObjectRole o, ContactRole r " +
                    "join o.slaList s " +
                    "where r.id = o.contactRoleId " +
                    "and :objectId = o.objectId " +
                    "and :contactRole = r.name " +
                    "and :tenantId = o.tenantId " +
                    "order by index(s)";

    String QUERY_OBJECT_MAPPING_SLA_PATTERNS =
            "select distinct o.objectId, r.name " +
                    "from ObjectRole o, ContactRole r " +
                    "join o.slaList s " +
                    "where o.contactRoleId = r.id";

    String SLA_SEARCH =
            " SELECT count(csla.id)"
                    + " FROM contact.sla  csla"
                    + " LEFT OUTER JOIN contact.objectrole_sla cors ON (cors.sla_id=csla.id)"
                    + " WHERE (cors.sla_id=?) ";



    String SLA_NAME_SEARCH ="select count(1) from contact.sla where name= ? ";
}
