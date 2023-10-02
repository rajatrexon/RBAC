package com.esq.rbac.service.contact.party.partytype.domain;


import com.esq.rbac.service.contact.util.JpaDateConvertor;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(schema = "contact", name = "party_type")
@NamedQuery(name = "listPartyTypes", query = "select c from PartyType c where lower(c.name) like :q order by c.id ASC")
public class PartyType {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "name", nullable = true, length = 50)
    private String name;

    @Convert(converter = JpaDateConvertor.class)
    @Column(name = "created_time", nullable = false, updatable = false)
    private Date createdTime;

    @Convert(converter = JpaDateConvertor.class)
    @Column(name = "updated_time", nullable = false)
    private Date updatedTime;

    public void preCreate() {
        Date now = new Date();
        this.setCreatedTime(now);
        this.setUpdatedTime(now);
    }

    public void preUpdate() {
        this.setUpdatedTime(new Date());
    }


    // Constructors, getters, setters, and other methods as needed

    // You may need to define the JpaDateConverter class, if not already present
}
