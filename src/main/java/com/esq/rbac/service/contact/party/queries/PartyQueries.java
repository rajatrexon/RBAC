package com.esq.rbac.service.contact.party.queries;

public interface PartyQueries {

    String LIST_QUERY =
            "select p from Party p"//, PartyType t"
                    + " left join p.department d"
                    + " left join p.securityRole s"
                    + " left join p.type pt";
    // + " where t.id = p.type.id";
    String FULL_TEXT_SEARCH =
            "select p from Party p"//, PartyType t"
                    + " left join p.department d"
                    + " left join p.securityRole s"
                    + " left join p.type pt"
                    //+ " where t.id = p.type.id"
                    + " where (lower(p.name) like :q"
                    + " or lower(p.code) like :q"
                    + " or lower(d.name) like :q"
                    + " or lower(s.name) like :q"
                    + " or lower(pt.name) like :q"
                    + " or (select count(l) from p.locations l where lower(l.name) like :q)>0 ) ";

    String FULL_TEXT_COUNT =
            "select count(p) from Party p"//, PartyType t"
                    + " left join p.department d"
                    + " left join p.securityRole s"
                    + " left join p.type pt"
                    // + " where t.id = p.type.id"
                    + " where (lower(p.name) like :q"
                    + " or lower(p.code) like :q"
                    + " or lower(d.name) like :q"
                    + " or lower(s.name) like :q"
                    + " or lower(pt.name) like :q"
                    + " or (select count(l) from p.locations l where lower(l.name) like :q)>0 ) ";

    String PART_CONTACT_SEARCH =
            "select count(1) from contact.objectrole_contact where contact_id in (select id from contact.contact where party_id = ?) ";

    String PARTY_NAME_SEARCH ="select count(1) from contact.party where name= ? ";
}
