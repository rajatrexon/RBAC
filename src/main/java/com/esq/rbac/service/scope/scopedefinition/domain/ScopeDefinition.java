package com.esq.rbac.service.scope.scopedefinition.domain;

import com.esq.rbac.service.group.domain.Group;
import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.deser.std.StringDeserializer;
import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "scopeDefinition", schema = "rbac")
@IdClass(ScopeDefinitionPK.class)
@Data
//This class uses a special de-serializer for some fields (RBAC-1187)
public class ScopeDefinition implements Comparable<ScopeDefinition> {

    @Id
    @Column(name = "scopeId")
    private Integer scopeId;

    @Id
    @Column(name = "groupId")
    private Integer groupId;

    @ManyToOne
    @JoinColumn(name = "groupId", updatable = false, insertable = false)
    @JsonBackReference("scopeDefinitions")
    private Group group;

    @Column(name = "definition")
    @JsonDeserialize(using = StringDeserializer.class)
    private String scopeDefinition;

    @Column(name = "additionalData")
    @JsonDeserialize(using = StringDeserializer.class)
    private String scopeAdditionalData;




    public ScopeDefinition() {
    }

    public ScopeDefinition(String scopeDefinition, String scopeAdditionalData, Integer scopeId, Integer groupId){
        this.scopeAdditionalData = scopeAdditionalData;
        this.scopeDefinition = scopeDefinition;
        this.scopeId = scopeId;
        this.groupId = groupId;
    }


    @Override
    public int compareTo(ScopeDefinition sd) {
        if(groupId == null || scopeId == null)
            return 0;
        int res = this.groupId.compareTo(sd.groupId);
        if (res != 0) {
            return res;
        }
        res = this.scopeId.compareTo(sd.scopeId);
        if (res != 0) {
            return res;
        }
        return 0;
    }
}
