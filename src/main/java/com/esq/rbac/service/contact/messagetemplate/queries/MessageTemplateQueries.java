package com.esq.rbac.service.contact.messagetemplate.queries;

public interface MessageTemplateQueries {

    String TEMPLATE_NAME_SEARCH ="select count(1) from contact.template where name= ? ";
    String TEMPLATE_ADDRESS_TYPE_SEARCH ="select count(1) from contact.template where channelId= ? ";

    String TEMPLATE_SEARCH_WITH_TENANTNAME =
            "SELECT DISTINCT tp.*"
                    + " FROM contact.template tp"
                    + " LEFT OUTER JOIN rbac.tenant t ON (tp.tenant_id = t.tenantId)"
                    + " WHERE (LOWER(t.tenantName) LIKE ?"
                    + " OR LOWER(tp.name) LIKE ?"
                    + " OR LOWER(tp.template_type) LIKE ?)";

    String TEMPLATE_SORT_ON_TENANTNAME =
            "SELECT DISTINCT tp.*, tenantName"
                    + " FROM contact.template tp"
                    + " LEFT OUTER JOIN rbac.tenant t ON (tp.tenant_id = t.tenantId)";

    String QUERY_TEMPLATE =
            "select mt from MessageTemplate mt join ContactAddressType a " +
                    "on a.id = mt.addressTypeId " +
                    "where :addressType = a.type " +
                    "and :tenantId = mt.tenantId";

    String QUERY_TEMPLATE_WITH_CHANNEL =
            "select mt from MessageTemplate mt join Code channelCode " +
                    "on channelCode.codeId = mt.channelId " +
                    "where :channel = channelCode.name " +
                    "and mt.tenantId in :tenantId " +
                    "and mt.appKey = :appKey";
}
