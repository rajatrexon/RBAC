package com.esq.rbac.service.codes.service;

import com.esq.rbac.service.basedal.BaseDalJpa;
import com.esq.rbac.service.codes.domain.Code;
import com.esq.rbac.service.codes.repository.CodeRepository;
import com.esq.rbac.service.config.CacheConfig;
import com.esq.rbac.service.filters.domain.Filters;
import com.esq.rbac.service.util.dal.OptionFilter;
import com.esq.rbac.service.util.dal.Options;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.PersistenceContextType;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.TreeMap;

@Service
@Slf4j
public class CodeDalJpa extends BaseDalJpa implements CodeDal {
    private static final Map<String, String> SORT_COLUMNS;
    static {
        SORT_COLUMNS = new TreeMap<String, String>();
        SORT_COLUMNS.put("codeType", "c.codeType");
        SORT_COLUMNS.put("parentType", "c.parentType");
        SORT_COLUMNS.put("displayOrder", "c.displayOrder");
    }
    private final CodeRepository codeRepository;
    public CodeDalJpa(CodeRepository codeRepository){
        this.codeRepository=codeRepository;
    }

    @PersistenceContext(type = PersistenceContextType.EXTENDED)
    public void setEntityManager(EntityManager em) {
        log.trace("setEntityManager; {}", em);
        this.em = em;
        this.entityClass = Code.class;
    }

    @Override
    public Code create(Code code) {
        return codeRepository.save(code);
    }

    @Override
    @Transactional(propagation = Propagation.SUPPORTS, readOnly = true)
    @Cacheable(value = CacheConfig.CODE_CACHE, keyGenerator = CacheConfig.CUSTOM_KEY_GENERATOR,unless="#result == null")
    public List<Code> list(Options options) {
        Filters filters = prepareFilters(options);
        return filters.getList(em, Code.class, "select c from Code c", options,
                SORT_COLUMNS);
    }

    private Filters prepareFilters(Options options) {

        Filters result = new Filters();
        OptionFilter optionFilter = options == null ? null : options
                .getOption(OptionFilter.class);
        Map<String, String> filters = optionFilter == null ? null
                : optionFilter.getFilters();
        if (filters != null) {
            String codeId = filters.get("codeId");
            if (codeId != null && codeId.length() > 0) {
                result.addCondition("c.codeId = :codeId");
                result.addParameter("codeId", Long.valueOf(codeId));
            }
            String codeType = filters.get("codeType");
            if (codeType != null && codeType.length() > 0) {
                result.addCondition("c.codeType = :codeType");
                result.addParameter("codeType", codeType);
            }
            String codeValue = filters.get("codeValue");
            if (codeValue != null && codeValue.length() > 0) {
                result.addCondition("c.codeValue = :codeValue");
                result.addParameter("codeValue", codeValue);
            }
            String parentType = filters.get("parentType");
            if (parentType != null && parentType.length() > 0) {
                result.addCondition("c.parentType = :parentType");
                result.addParameter("parentType", parentType);
            }
            String parentCodeValue = filters.get("parentCodeValue");
            if (parentCodeValue != null && parentCodeValue.length() > 0) {
                result.addCondition("c.parentCodeValue = :parentCodeValue");
                result.addParameter("parentCodeValue", parentCodeValue);
            }
            String parentCodeId = filters.get("parentCodeId");
            if (parentCodeId != null && parentCodeId.length() > 0) {
                result.addCondition("c.parentCodeValue = (select cd.codeValue from Code cd where cd.codeId = "+parentCodeId+ ")");
            }

        }
        return result;
    }

    @Override
    @Cacheable(value = CacheConfig.CODE_BY_APPLICATION_CACHE, keyGenerator = CacheConfig.CUSTOM_KEY_GENERATOR,unless="#result == null")
    public List<Code> getCodesByApplication(OptionFilter optionFilter) {
        Map<String, String> filters = optionFilter == null ? null : optionFilter.getFilters();
        if(filters==null)
            return null;
        String applicationName = filters.get("applicationName");
        if(applicationName == null)
            return null;
        String codeType = filters.get("codeType");
        if(codeType!=null)
            return codeRepository.getByCodeTypeAndApplication(codeType,applicationName);
        return codeRepository.getByApplication(applicationName);
    }
}
