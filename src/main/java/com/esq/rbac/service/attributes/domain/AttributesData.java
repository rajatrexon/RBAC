package com.esq.rbac.service.attributes.domain;


import com.esq.rbac.service.group.domain.Group;
import com.esq.rbac.service.user.domain.User;
import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import lombok.Data;
import org.springframework.cache.annotation.Cacheable;

import java.util.HashSet;

@Cacheable("attributesData")
@Entity
@Table(schema="rbac" , name = "attributes_data")
@Data
public class AttributesData implements Comparable<AttributesData>{
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "attributeDataId")
    private Integer attributeDataId;
    
    @ManyToOne
    @JoinColumn(name = "groupId")
    @JsonBackReference("AttributesDataGroup")
    private Group group;

    @ManyToOne
    @JoinColumn(name = "userId")
    @JsonBackReference("AttributesDataUser")
    private User user;
    
    @Column(name = "attributeId")
    private Integer attributeId;
    
    @Column(name = "valueReferenceId")
    private String valueReferenceId;
    
    @Column(name = "attributeDataValue")
    private String attributeDataValue;
    
    
    
    public Integer getAttributeDataId() {
        return attributeDataId;
    }

    public void setAttributeDataId(Integer attributeDataId) {
        this.attributeDataId = attributeDataId;
    }

    public Group getGroup() {
        return group;
    }

    public  void setGroup(Group group) {
        // set new group
        this.group = group;

        // add this group to newly set attribute data
        if (this.group != null) {
            if (this.group.getAttributesData() == null) {
                this.group.setAttributesData(new HashSet<AttributesData>());
            }
            if (!this.group.getAttributesData().contains(this)) {
                this.group.getAttributesData().add(this);
            }
        }
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        // set new group
        this.user = user;

        // add this group to newly set attribute data
        if (this.user != null) {
            if (this.user.getAttributesData() == null) {
                this.user.setAttributesData(new HashSet<AttributesData>());
            }
            if (!this.user.getAttributesData().contains(this)) {
                this.user.getAttributesData().add(this);
            }
        }
    }

    public Integer getAttributeId() {
        return attributeId;
    }

    public void setAttributeId(Integer attributeId) {
        this.attributeId = attributeId;
    }

    public String getValueReferenceId() {
        return valueReferenceId;
    }

    public void setValueReferenceId(String valueReferenceId) {
        this.valueReferenceId = valueReferenceId;
    }

    public String getAttributeDataValue() {
        return attributeDataValue;
    }

    public void setAttributeDataValue(String attributeDataValue) {
        this.attributeDataValue = attributeDataValue;
    }

    @Override
    public int compareTo( AttributesData o) {
        if(this.attributeId!=null)
            return this.attributeId.compareTo(o.attributeId);
        return 0;
    }
}