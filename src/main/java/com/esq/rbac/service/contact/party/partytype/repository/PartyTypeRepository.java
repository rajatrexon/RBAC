package com.esq.rbac.service.contact.party.partytype.repository;


import com.esq.rbac.service.contact.party.partytype.domain.PartyType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PartyTypeRepository extends JpaRepository<PartyType, Long> {

    @Query("select c from PartyType c where lower(c.name) like %:q% order by c.id ASC")
    List<PartyType> listPartyTypes(@Param("q") String query);

    // Other custom query methods can be added here if needed

}