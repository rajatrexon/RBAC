package com.esq.rbac.service.contact.mappingtype.repository;


import com.esq.rbac.service.contact.mappingtype.domain.MappingType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface MappingTypeRepository extends JpaRepository<MappingType, Integer> {

    @Query(value = "select m.id from MappingType m where m.mapFrom = :mapFrom and m.mapTo = :mapTo")
    Long findMapIdByMapFromMapTo(@Param("mapFrom") String mapFrom, @Param("mapTo") String mapTo);

}




