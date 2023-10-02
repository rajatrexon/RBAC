package com.esq.rbac.service.contact.party.partytype.service;

import com.esq.rbac.service.contact.party.partytype.repository.PartyTypeRepository;
import com.esq.rbac.service.contact.party.partytype.domain.PartyType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

public interface PartyTypeService {
    @Autowired
    void setPartyTypeRepository(PartyTypeRepository partyTypeRepository);

    @Transactional(propagation = Propagation.SUPPORTS, readOnly = true)
    PartyType readById(long id);

    @Transactional(propagation = Propagation.SUPPORTS, readOnly = true)
    List<PartyType> getPartyTypes(String q);

    @Transactional(propagation = Propagation.REQUIRED)
    PartyType createFromName(String name);

    @Transactional(propagation = Propagation.REQUIRED)
    void deleteById(long id);

    @Transactional(propagation = Propagation.REQUIRED)
    PartyType update(long id, PartyType partyTypes);
}
