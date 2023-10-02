package com.esq.rbac.service.contact.objectrole.domain;

import com.esq.rbac.service.contact.contactrole.domain.ContactRole;
import com.esq.rbac.service.contact.domain.Contact;
import com.esq.rbac.service.contact.mappingtype.domain.MappingType;
import com.esq.rbac.service.contact.schedule.domain.Schedule;
import com.esq.rbac.service.contact.sla.domain.SLA;
import com.esq.rbac.service.contact.util.JpaDateConvertor;
import com.esq.rbac.service.validation.annotation.Length;
import com.esq.rbac.service.validation.annotation.Mandatory;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.*;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.Cascade;

import java.util.Date;
import java.util.LinkedList;
import java.util.List;


@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "objectrole", schema = "contact")
@XmlRootElement
public class ObjectRole {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "object_id", nullable = false, length = 200)
    @Mandatory
    @Length(min = 0, max = 200)
    private String objectId;

    @Column(name = "contact_role_id")
    @Mandatory
    private long contactRoleId;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "contact_role_id", insertable = false, updatable = false)
    private ContactRole contactRole;

    @Column(name = "schedule_id")
    private Long scheduleId;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "schedule_id", insertable = false, updatable = false)
    private Schedule schedule;

    @ElementCollection
    @CollectionTable(schema = "contact", name = "objectrole_contact", joinColumns = @JoinColumn(name = "objectrole_id", nullable = false))
    @OrderColumn(name = "seq_num")
    @Column(name = "contact_id", nullable = false)
    private List<Long> contactIdList;

    @ManyToMany
    @JoinTable(schema = "contact", name = "objectrole_contact", joinColumns = @JoinColumn(name = "objectrole_id", referencedColumnName = "id", insertable = false, updatable = false), inverseJoinColumns = @JoinColumn(name = "contact_id", referencedColumnName = "id", insertable = false, updatable = false))
    @OrderColumn(name = "seq_num")
    private List<Contact> contactList;

    @OneToMany(fetch = FetchType.EAGER, mappedBy = "objectRoleInternal", orphanRemoval = true)
    @OrderColumn(name = "seq_num")
    @Cascade(org.hibernate.annotations.CascadeType.ALL)
    private List<Contact> contacts = new LinkedList<>();

    @ElementCollection
    @CollectionTable(schema = "contact", name = "objectrole_sla", joinColumns = @JoinColumn(name = "objectrole_id", nullable = false))
    @OrderColumn(name = "seq_num")
    @Column(name = "sla_id", nullable = false)
    private List<Long> slaIdList;

    @ManyToMany
    @JoinTable(schema = "contact", name = "objectrole_sla", joinColumns = @JoinColumn(name = "objectrole_id", referencedColumnName = "id", insertable = false, updatable = false), inverseJoinColumns = @JoinColumn(name = "sla_id", referencedColumnName = "id", insertable = false, updatable = false))
    @OrderColumn(name = "seq_num")
    private List<SLA> slaList;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "mapTypeId", insertable = false, updatable = false)
    private MappingType mappingType;

    @Column(name = "mapTypeId")
    private long mapTypeId;

    @Column(name = "object_key")
    private String objectKey;

    @Column(name = "tenant_id")
    private long tenantId;

    @Column(name = "isObjectContact")
    private boolean objectContact;

    @Column(name = "appKey")
    private String appKey;

    @Column(name = "created_time", nullable = false, updatable = false)
    @Convert(converter = JpaDateConvertor.class)
    private Date createdTime;

    @Column(name = "updated_time", nullable = false)
    @Convert(converter = JpaDateConvertor.class)
    private Date updatedTime;


    @XmlElement(name = "contactIds")
    @JsonProperty(value = "contactIds")
    public void setContactIdList(List<Long> contactIds) {
        this.contactIdList = contactIds;
    }

    @XmlElement(name = "contacts")
    @JsonProperty(value = "contacts")
    public void setContactList(List<Contact> contacts) {
        this.contactList = contacts;
    }

    @XmlElement(name = "slaIds")
    @JsonProperty(value = "slaIds")
    public void setSlaIdList(List<Long> slaIdList) {
        this.slaIdList = slaIdList;
    }

    @XmlElement(name = "slas")
    @JsonProperty(value = "slas")
    public void setSlaList(List<SLA> slaList) {
        this.slaList = slaList;
    }


    @PrePersist
    public void preCreate() {
        Date now = new Date();
        this.setCreatedTime(now);
        this.setUpdatedTime(now);
    }


    @PreUpdate
    public void preUpdate() {
        this.setUpdatedTime(new Date());
    }

    @XmlElement(name = "contact")
    @JsonProperty(value = "contact")
    public void setContacts(List<Contact> contacts) {
        this.contacts = contacts;
        if (this.contacts != null) {
            for (int i = 0; i < contacts.size(); i++) {
                contacts.get(i).setObjectRoleInternal(this);
                contacts.get(i).setSeqNum(i);
            }
        }
    }

    // Constructors, getters, setters, and other methods...
}

