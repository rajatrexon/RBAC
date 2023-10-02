package com.esq.rbac.service.restriction.service;

import com.esq.rbac.service.restriction.domain.Restriction;
import com.esq.rbac.service.restriction.repository.RestrictionRepository;
import jakarta.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class RestrictionDalJpa implements RestrictionDal{


    private RestrictionRepository restrictionRepository;

    @Autowired
    public  void setRestrictionRepository(RestrictionRepository restrictionRepository) {
        this.restrictionRepository = restrictionRepository;
    }
    @Override
    public Restriction create(Restriction restriction) {
        return restrictionRepository.save(restriction);
    }

    @Override
    public Restriction update(Restriction restriction) {
        Restriction restric=restrictionRepository.findById(restriction.getRestrictionId()).get();
       return restrictionRepository.save(restric);
    }
    @Override
    public Restriction getById(int restrictionId) {
        return restrictionRepository.findById(restrictionId).get();
    }

    @Override
    public void deleteById(int restrictionId) {
         restrictionRepository.deleteById(restrictionId);
    }
}
