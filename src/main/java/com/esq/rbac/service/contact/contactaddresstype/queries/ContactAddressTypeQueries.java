package com.esq.rbac.service.contact.contactaddresstype.queries;

public interface ContactAddressTypeQueries {

    String LIST_ROLES_NAME_ID =
            "SELECT c FROM ContactAddressType c ORDER BY c.id ASC";

    String FIND_ADDRESS_TYPE_ID_BY_NAME =
            "SELECT c.id FROM ContactAddressType c WHERE c.type = :name";
}
