package com.esq.rbac.service.contact.queries;

public interface ContactQueries {

    String QUERY_CONTACTS =
            "SELECT c " +
                    "FROM ObjectRole o, ContactRole r, ContactAddressType a " +
                    "JOIN o.contactList c " +
                    "WHERE r.id = o.contactRoleId " +
                    "AND a.id = c.addressTypeId " +
                    "AND :objectId = o.objectId " +
                    "AND :contactRole = r.name " +
                    "AND :addressType = a.type " +
                    "ORDER BY index(c)";

    String QUERY_DISPATCH_MAP =
            "SELECT c " +
                    "FROM ObjectRole o, Code lifeCycleCode, Code atmSchedule " +
                    "JOIN o.contacts c " +
                    "WHERE lifeCycleCode.codeId = c.lifecycleId " +
                    "AND atmSchedule.codeId = c.atmScheduleId " +
                    "AND :objectId = o.objectId " +
                    "AND :tenantId = o.tenantId " +
                    "AND :lifecycle = lifeCycleCode.name " +
                    "AND :atmSchedule = atmSchedule.name " +
                    "ORDER BY c.contactMapping, c.seqNum";

    String QUERY_DISPATCH_MAP_WITHOUT_ATM_SCHED =
            "SELECT c " +
                    "FROM ObjectRole o, Code lifeCycleCode " +
                    "JOIN o.contacts c " +
                    "WHERE lifeCycleCode.codeId = c.lifecycleId " +
                    "AND :objectId = o.objectId " +
                    "AND :tenantId = o.tenantId " +
                    "AND :lifecycle = lifeCycleCode.name " +
                    "ORDER BY c.contactMapping, c.seqNum";

    String QUERY_DISPATCH_MAP_BY_SEQ =
            "SELECT c " +
                    "FROM ObjectRole o, Code lifeCycleCode, Code atmSchedule " +
                    "JOIN o.contacts c " +
                    "WHERE lifeCycleCode.codeId = c.lifecycleId " +
                    "AND atmSchedule.codeId = c.atmScheduleId " +
                    "AND :objectId = o.objectId " +
                    "AND :tenantId = o.tenantId " +
                    "AND :lifecycle = lifeCycleCode.name " +
                    "AND :atmSchedule = atmSchedule.name " +
                    "AND :iscc = c.contactCC " +
                    "ORDER BY c.seqNum";

    String QUERY_DISPATCH_MAP_WITH_CHANNEL_BY_SEQ =
            "SELECT c " +
                    "FROM ObjectRole o, Code lifeCycleCode, Code atmSchedule, Code channelCode " +
                    "JOIN o.contacts c " +
                    "WHERE lifeCycleCode.codeId = c.lifecycleId " +
                    "AND atmSchedule.codeId = c.atmScheduleId " +
                    "AND channelCode.codeId = c.channelId " +
                    "AND :objectId = o.objectId " +
                    "AND :channel = channelCode.name " +
                    "AND :tenantId = o.tenantId " +
                    "AND :lifecycle = lifeCycleCode.name " +
                    "AND :atmSchedule = atmSchedule.name " +
                    "AND :iscc = c.contactCC " +
                    "ORDER BY c.seqNum";

    String QUERY_DISPATCH_MAP_BY_SEQ_CC =
            "SELECT c " +
                    "FROM ObjectRole o, Code lifeCycleCode, Code atmSchedule " +
                    "JOIN o.contacts c " +
                    "WHERE lifeCycleCode.codeId = c.lifecycleId " +
                    "AND atmSchedule.codeId = c.atmScheduleId " +
                    "AND :objectId = o.objectId " +
                    "AND :tenantId = o.tenantId " +
                    "AND :lifecycle = lifeCycleCode.name " +
                    "AND :atmSchedule = atmSchedule.name " +
                    "ORDER BY c.seqNum";

    String QUERY_DISPATCH_MAP_WITH_CHANNEL_BY_SEQ_CC =
            "select c " +
                    "from ObjectRole o, Code lifeCycleCode, Code atmSchedule, Code channelCode " +
                    "join o.contacts c " +
                    "where lifeCycleCode.codeId=c.lifecycleId " +
                    "and atmSchedule.codeId=c.atmScheduleId " +
                    "and channelCode.codeId=c.channelId " +
                    "and :objectId = o.objectId " +
                    "and :channel = channelCode.name " +
                    "and :tenantId = o.tenantId " +
                    "and :lifecycle = lifeCycleCode.name " +
                    "and :atmSchedule = atmSchedule.name " +
                    "order by c.seqNum";

    String QUERY_DISPATCH_MAP_WITH_CHANNEL =
            "select c " +
                    "from ObjectRole o, Code lifeCycleCode, Code atmSchedule, Code channelCode " +
                    "join o.contacts c " +
                    "where lifeCycleCode.codeId=c.lifecycleId " +
                    "and atmSchedule.codeId=c.atmScheduleId " +
                    "and channelCode.codeId=c.channelId " +
                    "and :objectId = o.objectId " +
                    "and :channel = channelCode.name " +
                    "and :tenantId = o.tenantId " +
                    "and :lifecycle = lifeCycleCode.name " +
                    "and :atmSchedule = atmSchedule.name";

    String QUERY_ACTION_RULE_CONTACTS =
            "select c " +
                    "from ObjectRole o join o.contacts c " +
                    "where :tenantId = o.tenantId " +
                    "and o.objectId in :objectIdList";

    String DELETE_CONTACT_BY_ID =
            "delete from Contact c where c.id = :id";

    String UPDATE_CONTACT_ADDRESS_BY_ID =
            "update Contact c set c.address = :address where c.id = :id";

    String DELETE_CONTACT_BY_USER_ID =
            "delete from Contact c where c.userId = :userId";

    String IS_USER_ASSOCIATED_IN_DISPATCH_CONTACT =
            "select count(c) from Contact c where c.userId = :userId";

    String QUERY_CONTACTS_BY_TENANT =
            "select c " +
                    "from ObjectRole o, ContactRole r, ContactAddressType a " +
                    "join o.contactList c " +
                    "where r.id = o.contactRoleId " +
                    "and a.id = c.addressTypeId " +
                    "and :objectId = o.objectId " +
                    "and :contactRole = r.name " +
                    "and :addressType = a.type " +
                    "and :tenantId = o.tenantId " +
                    "order by index(c)";

    String QUERY_OBJECT_MAPPING_PATTERNS =
            "select distinct o.objectId, r.name, a.type " +
                    "from ObjectRole o, ContactRole r, ContactAddressType a " +
                    "join o.contactList c " +
                    "where r.id = o.contactRoleId " +
                    "and a.id = c.addressTypeId";

    String QUERY_OBJECT_IDS =
            "select distinct o.objectId " +
                    "from ObjectRole o";

    String IS_TEMPLATE_USED_IN_MAPPING =
            "select count(1) from Contact c where c.templateId = :templateId";

}
