package com.esq.rbac.service.scope.scopeBuilderdefault.repository;

import com.esq.rbac.service.scope.scopeBuilderdefault.domain.ScopeBuilderDefault;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ScopeBuildDefaultRepository extends JpaRepository<ScopeBuilderDefault,Integer> {
    @Query("select sb from ScopeBuilderDefault sb where sb.isEnabled = TRUE order by sb.scopeBuilderId")
    List<ScopeBuilderDefault> getScopeBuilderDefaults();
}
