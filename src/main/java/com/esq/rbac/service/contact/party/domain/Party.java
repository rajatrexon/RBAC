package com.esq.rbac.service.contact.party.domain;

import com.esq.rbac.service.contact.domain.Contact;
import com.esq.rbac.service.contact.location.domain.Location;
import com.esq.rbac.service.contact.party.partytype.domain.PartyType;
import com.esq.rbac.service.contact.partydepartment.domain.PartyDepartment;
import com.esq.rbac.service.contact.securityrole.domain.SecurityRole;
import com.esq.rbac.service.contact.util.JpaDateConvertor;
import com.esq.rbac.service.validation.annotation.Length;
import com.esq.rbac.service.validation.annotation.Mandatory;
import jakarta.persistence.*;
import jakarta.xml.bind.annotation.XmlRootElement;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(schema = "contact", name = "party")
@XmlRootElement
public class Party {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @ManyToOne
    @JoinColumn(name = "department_id")
    private PartyDepartment department;

    @ManyToOne
    @JoinColumn(name = "security_role_id")
    private SecurityRole securityRole;

    @ManyToOne
    @JoinColumn(name = "type")
    private PartyType type;

    @Column(name = "is_RBAC_Group", nullable = false)
    private boolean isRBACGroup;

    @Column(name = "code", length = 50)
    @Mandatory
    @Length(min = 0, max = 20)
    private String code;

    @Column(name = "name", nullable = false, length = 100)
    @Mandatory
    @Length(min = 0, max = 100)
    private String name;


    @Column(name = "created_time", nullable = false, updatable = false)
    @Convert(converter = JpaDateConvertor.class)
    private Date createdTime;

    @Column(name = "updated_time", nullable = false)
    @Convert(converter = JpaDateConvertor.class)
    private Date updatedTime;

    @Column(name = "tenant_id")
    private Long tenantId;

    @OneToMany(mappedBy = "party", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    private List<Location> locations;

    @OneToMany(mappedBy = "partyInternal", fetch = FetchType.EAGER, cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Contact> contacts;

    public Boolean getIsRBACGroup() {
        return isRBACGroup;
    }
}
