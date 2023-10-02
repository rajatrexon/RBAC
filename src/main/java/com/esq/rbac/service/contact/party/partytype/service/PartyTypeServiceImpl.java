package com.esq.rbac.service.contact.party.partytype.service;

import com.esq.rbac.service.contact.party.partytype.repository.PartyTypeRepository;
import com.esq.rbac.service.contact.party.partytype.domain.PartyType;
import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Slf4j
public class PartyTypeServiceImpl implements PartyTypeService {

    private static final String LIST_ROLES_NAME_ID = "listPartyTypes";
    private static final String SQL_WILDCARD = "%";

    private PartyTypeRepository partyTypeRepository;

    EntityManager entityManager;

    @Override
    @Autowired
    public void setPartyTypeRepository(PartyTypeRepository partyTypeRepository) {
        this.partyTypeRepository = partyTypeRepository;
    }

    @Autowired
    public void setEntityManager(EntityManager entityManager) {
        this.entityManager = entityManager;
    }

    @Override
    @Transactional(propagation = Propagation.SUPPORTS, readOnly = true)
    public PartyType readById(long id) {
        PartyType result = partyTypeRepository.findById(id).get();
        log.debug("readById; result={}", result);
        return result;
    }

    private String wildcarded(String q) {
        StringBuilder sb = new StringBuilder();
        if (q.startsWith(SQL_WILDCARD) == false) {
            sb.append(SQL_WILDCARD);
        }
        sb.append(q.toLowerCase());
        if (q.endsWith(SQL_WILDCARD) == false) {
            sb.append(SQL_WILDCARD);
        }
        return sb.toString();
    }

    @Override
    @Transactional(propagation = Propagation.SUPPORTS, readOnly = true)
    public List<PartyType> getPartyTypes(String q) {
        TypedQuery<PartyType> query = entityManager.createNamedQuery(
                "listPartyTypes", PartyType.class);
        if (q == null) {
            q = "";
        }
        query.setParameter("q", wildcarded(q));
        return query.getResultList();
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRED)
    public PartyType createFromName(String name) {
        PartyType partyType = new PartyType();
        partyType.setName(name);
        partyType.preCreate();
        partyType.preUpdate();
//        entityManager.getTransaction().begin();
//        entityManager.persist(partyType);
//        entityManager.getTransaction().commit();
//        entityManager.refresh(partyType);
        partyTypeRepository.save(partyType);
        return partyType;
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRED)
    public void deleteById(long id) {
//        PartyType partyTypes = readById(id);
//        entityManager.remove(partyTypes);
        partyTypeRepository.delete(partyTypeRepository.findById(id).get());
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRED)
    public PartyType update(long id, PartyType partyTypes) {
        partyTypes = partyTypeRepository.findById(id).get();
        partyTypes.setId(id);
        return partyTypeRepository.save(partyTypes);
    }
}
