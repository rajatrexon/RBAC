package com.esq.rbac.service.contact.location.domain;

import com.esq.rbac.service.contact.embedded.BaseEntity;
import com.esq.rbac.service.contact.party.domain.Party;
import com.esq.rbac.service.contact.util.JpaDateConvertor;
import com.esq.rbac.service.validation.annotation.Mandatory;
import jakarta.persistence.*;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.xml.bind.annotation.XmlRootElement;
import jakarta.xml.bind.annotation.XmlTransient;
import org.hibernate.validator.constraints.Length;

import java.time.LocalDateTime;
import java.util.Date;

@XmlRootElement
@Entity
@Table(name = "location", schema = "contact")
public class Location extends BaseEntity {



    @Column(name = "party_id", nullable = false)
    private long partyId;

    @Column(name = "location_role_id")
    private Long locationRoleId;
    @Mandatory
    @Length(min = 0, max = 50)
    @Column(name = "name", nullable = false, length = 50)
    private String name;
    @Length(min = 0, max = 100)
    @Column(name = "address", length = 100)
    private String address;
    @Length(min = 0, max = 50)
    @Column(name = "city", length = 50)
    private String city;
    @Length(min = 0, max = 50)
    @Column(name = "state", nullable = false, length = 50)
    private String state;
    @Mandatory
    @Column(name = "country", nullable = false, length = 2)
    private String countryId;
    @Length(min = 0, max = 20)
    @Column(name = "zip", nullable = false, length = 20)
    private String zip;


    @Column(name = "latitude")
    private long latitude;


    @Column(name = "longitude")
    private long longitude;


    @Column(name = "notes", length = 100)
    private String notes;


    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "party_id", insertable = false, updatable = false)
    private Party party;



    @Convert(converter = JpaDateConvertor.class)
    @Column(name = "created_time", nullable = false, updatable = false)
    private LocalDateTime createdTime;

    @Convert(converter = JpaDateConvertor.class)
    @Column(name = "updated_time", nullable = false)
    private LocalDateTime updatedTime;

    @XmlTransient
    public Party getParty() {
        return party;
    }

    public void setParty(Party party) {
        this.party = party;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getCountryId() {
        return countryId;
    }

    public void setCountryId(String countryId) {
        this.countryId = countryId;
    }

    public long getLatitude() {
        return latitude;
    }

    public void setLatitude(long latitude) {
        this.latitude = latitude;
    }

    public Long getLocationRoleId() {
        return locationRoleId;
    }

    public void setLocationRoleId(Long locationRoleId) {
        this.locationRoleId = locationRoleId;
    }

    public long getLongitude() {
        return longitude;
    }

    public void setLongitude(long longitude) {
        this.longitude = longitude;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public long getPartyId() {
        return partyId;
    }

    public void setPartyId(long partyId) {
        this.partyId = partyId;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public String getZip() {
        return zip;
    }

    public void setZip(String zip) {
        this.zip = zip;
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

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("Location:");
        sb.append("id: ").append(getId());
        sb.append("partyId: ").append(partyId);
        sb.append("name: ").append(name);
        sb.append("locationRoleId: ").append(locationRoleId);
        sb.append("Address: ").append(address);
        sb.append("zip: ").append(zip);
        sb.append("City: ").append(city);
        sb.append("state: ").append(state);
        sb.append("Country: ").append(countryId);
        sb.append("latitude: ").append(latitude);
        sb.append("longitude: ").append(longitude);
        sb.append("notes: ").append(notes);
        sb.append("createdDateTime: ").append(getCreatedTime());
        sb.append("updatedDateTime: ").append(getUpdatedTime());
        return sb.toString();
    }
}
