package com.esq.rbac.service.contact.partydepartment.domain;

import com.esq.rbac.service.contact.embedded.BaseEntity;
import com.esq.rbac.service.validation.annotation.Mandatory;
import jakarta.persistence.*;
import jakarta.xml.bind.annotation.XmlAttribute;
import jakarta.xml.bind.annotation.XmlRootElement;
import org.hibernate.validator.constraints.Length;

@XmlRootElement
@Entity
@Table(schema = "contact", name = "party_department")
@NamedQueries({
        @NamedQuery(name = "listPartyDepartments", query = "SELECT d FROM PartyDepartment d WHERE lower(d.name) LIKE lower(:q) ORDER BY d.id ASC")
})
public class PartyDepartment extends BaseEntity {

    @Mandatory
    @Length(min = 0, max = 50)
    @Column(name = "name", nullable = true, length = 150)
    private String name;

    @XmlAttribute
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("{PartyDepartment: {id:").append(getId());
        sb.append(", name:'").append(name);
        sb.append("'}}");
        return sb.toString();
    }
}

