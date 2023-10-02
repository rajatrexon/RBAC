package com.esq.rbac.service.label.service;
import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;
import jakarta.persistence.TypedQuery;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

@Service
@Slf4j
public class LabelDalJpa implements LabelDal {

   // LabelRepository labelRepository;

    private final String getAllLabelNames=
            "select distinct l.labelName from rbac.label l order by l.labelName";

    @Autowired
    EntityManager entityManager;

//    @Autowired
//    public void setLabelRepository(LabelRepository labelRepository) {
//        this.labelRepository = labelRepository;
//    }

    @Override
    @Transactional(propagation = Propagation.SUPPORTS, readOnly = true)
    public List<String> getAllLabelNames() {
//        Query query = entityManager.createNativeQuery(getAllLabelNames);
        Query query= entityManager.createNativeQuery(getAllLabelNames);
        List<String> resultList = query.getResultList();
        return  resultList;
    }
}

