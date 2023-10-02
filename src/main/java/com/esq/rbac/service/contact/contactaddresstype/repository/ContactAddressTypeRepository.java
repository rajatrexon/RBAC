package com.esq.rbac.service.contact.contactaddresstype.repository;

import com.esq.rbac.service.base.repository.Repository;
import com.esq.rbac.service.contact.contactaddresstype.queries.ContactAddressTypeQueries;
import com.esq.rbac.service.contact.contactaddresstype.domain.ContactAddressType;
import jakarta.persistence.TypedQuery;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.ArrayUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import java.util.Arrays;
import java.util.List;

@Slf4j
@Service("ContactAddressTypeRepository")
public class ContactAddressTypeRepository extends Repository<ContactAddressType> {

    private static final String LIST_ROLES_NAME_ID_WITH_SCOPE = "listContactAddressTypesWithScope";
    private static final String PARAM_OBJECT_TENANT = "tenantId";

    public ContactAddressTypeRepository() {
        super(ContactAddressType.class);
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRED)
    public void deleteById(long id) {
        throw new UnsupportedOperationException();
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRED)
    public ContactAddressType create(ContactAddressType instance) {
        throw new UnsupportedOperationException();
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRED)
    public ContactAddressType update(long id, ContactAddressType instance) {
        throw new UnsupportedOperationException();
    }

    @Transactional(propagation = Propagation.SUPPORTS, readOnly = true)
    public List<ContactAddressType> getRoles(long...tenantScope) {
        StringBuilder sb = new StringBuilder();
        if(tenantScope.length==0){
            sb.append(ContactAddressTypeQueries.LIST_ROLES_NAME_ID);
        }else{
            sb.append(LIST_ROLES_NAME_ID_WITH_SCOPE);
        }

        TypedQuery<ContactAddressType> query = entityManager.createQuery(sb.toString(), ContactAddressType.class);
        if(tenantScope.length>0){
            Long[] longs = ArrayUtils.toObject(tenantScope);
            List<Long> tenantScopeList = Arrays.asList(longs);
            query.setParameter(PARAM_OBJECT_TENANT, tenantScopeList);
        }
        return query.getResultList();
    }
}

