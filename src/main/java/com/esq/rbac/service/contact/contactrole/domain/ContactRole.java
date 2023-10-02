package com.esq.rbac.service.contact.contactrole.domain;

import com.esq.rbac.service.contact.util.JpaDateConvertor;
import com.esq.rbac.service.validation.annotation.Length;
import com.esq.rbac.service.validation.annotation.Mandatory;
import jakarta.persistence.*;
import jakarta.xml.bind.annotation.XmlRootElement;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(schema = "contact", name = "contact_role")
@XmlRootElement
public class ContactRole {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "name", nullable = false)
    @Mandatory
    @Length(min = 0, max = 450)
    private String name;

    @Column(name = "created_time", nullable = false, updatable = false)
    @Convert(converter = JpaDateConvertor.class)
    private Date createdTime;

    @Column(name = "updated_time", nullable = false)
    @Convert(converter = JpaDateConvertor.class)
    private Date updatedTime;

    @Column(name = "tenant_id")
    private Long tenantId;

    public void preCreate() {
        Date now = new Date();
        this.setCreatedTime(now);
        this.setUpdatedTime(now);
    }

    public void preUpdate() {
        this.setUpdatedTime(new Date());
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("ContactRole [id=").append(getId());
        sb.append("; name=").append(name);
        sb.append("', tenantId:'").append(tenantId);
        sb.append("; createdTime=").append(getCreatedTime());
        sb.append("; updatedTime=").append(getUpdatedTime());
        sb.append("]");
        return sb.toString();
    }
}

