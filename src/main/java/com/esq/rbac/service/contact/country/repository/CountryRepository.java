package com.esq.rbac.service.contact.country.repository;


import com.esq.rbac.service.base.repository.Repository;
import com.esq.rbac.service.contact.country.domain.Country;
import com.esq.rbac.service.contact.country.queries.CountryQueries;
import jakarta.persistence.TypedQuery;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

@Slf4j
@Service("CountryRepository")
public class CountryRepository extends Repository<Country> {

    public CountryRepository() {
        super(Country.class);
    }

    public List<Country> getAllCountries() {
        log.debug("getAllCountries");
        return listCountries();
    }

    @Override
    public void deleteById(long id) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Country create(Country country) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Country update(long id, Country country) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Country readById(long id) {
        throw new UnsupportedOperationException();
    }

    @Transactional(propagation = Propagation.SUPPORTS, readOnly = true)
    private List<Country> listCountries() {
        TypedQuery<Country> query = entityManager.createQuery(CountryQueries.LIST_ALL_COUNTRIES_DATA, Country.class);
        return query.getResultList();
    }
}
