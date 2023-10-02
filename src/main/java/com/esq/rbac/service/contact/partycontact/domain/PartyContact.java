package com.esq.rbac.service.contact.partycontact.domain;


import com.esq.rbac.service.codes.domain.Code;
import com.esq.rbac.service.contact.customuserinfo.domain.CustomUserInfo;
import com.esq.rbac.service.contact.embedded.BaseEntity;
import com.esq.rbac.service.contact.party.domain.Party;
import com.esq.rbac.service.contact.util.JpaDateConvertor;
import com.esq.rbac.service.user.domain.User;
import com.esq.rbac.service.validation.annotation.Mandatory;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.*;
import jakarta.persistence.Transient;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;
import jakarta.xml.bind.annotation.XmlTransient;
import org.hibernate.validator.constraints.Length;

import java.util.Date;


@Entity
@Table(schema = "contact", name = "contact")
@XmlRootElement
public class PartyContact extends BaseEntity {

    @Mandatory
    @Length(min = 0, max = 4000)
    @Column(name = "address", nullable = false, length = 4000)
    private String address;
    @Mandatory
    @Column(name = "party_id", nullable = false)
    private Long partyId;


    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "party_id", insertable = false, updatable = false)
    private Party partyInternal;
    @Mandatory
    @Length(min = 0, max = 500)
    @Column(name = "name", nullable = true, length = 500)
    private String name;



    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "userId", insertable = false, updatable = false)
    private User user;


    @Column(name = "userId", nullable = true)
    private Integer userId;
    private CustomUserInfo customUserInfo;



    @Column(name = "channelId")
    private Integer channelId;


    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "channelId", insertable = false, updatable = false)
    private Code channel;


    @Column(name = "created_time", nullable = false, updatable = false)
    @Convert(converter = JpaDateConvertor.class)
    private Date createdTime;


    @Column(name = "updated_time", nullable = false)
    @Convert(converter = JpaDateConvertor.class)
    private Date updatedTime;

    public Code getChannel() {
        return channel;
    }

    public void setChannel(Code channel) {
        this.channel = channel;
    }

    public Integer getChannelId() {
        return channelId;
    }

    public void setChannelId(Integer channelId) {
        this.channelId = channelId;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public Long getPartyId() {
        return partyId;
    }

    public void setPartyId(Long partyId) {
        this.partyId = partyId;
    }

    @XmlTransient
    @JsonIgnore
    public Party getPartyInternal() {
        return partyInternal;
    }

    public void setPartyInternal(Party party) {
        this.partyInternal = party;
    }

    public Integer getUserId() {
        return userId;
    }

    public void setUserId(Integer userId) {
        this.userId = userId;
    }

    @JsonIgnore
    @XmlTransient
    public User getUser() {
        return user;
    }

    /*
     * To Reduce the data of User that is sent to the Dispatch Mapping screen
     */
    public void setUser(User user) {
        this.user = user;
        if(user!= null){
            setCustomUserInfo(CustomUserInfo.getUserInfo(user));
        }
    }

    @Transient
    public CustomUserInfo getCustomUserInfo() {
        return customUserInfo;
    }

    @XmlElement(name="user")
    @JsonProperty(value = "user")
    public void setCustomUserInfo(CustomUserInfo customUserInfo) {
        this.customUserInfo = customUserInfo;
    }

    public Party getParty() {
        Party party = new Party();
        if (partyInternal != null) {
            party.setId(partyInternal.getId());
            party.setName(partyInternal.getName());
            party.setCode(partyInternal.getCode());
            party.setCreatedTime(partyInternal.getCreatedTime());
            party.setUpdatedTime(partyInternal.getUpdatedTime());
        }
        return party;
    }

    public void setParty(Party party) {
        // ignore
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
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
        StringBuilder sb = new StringBuilder();
        sb.append("PartyContact[id=").append(getId());
        sb.append("; partyId=").append(partyId);
        sb.append("; partyName=").append(partyInternal.getName());
        sb.append("; address=").append(address);
        sb.append("; createdDateTime=").append(getCreatedTime());
        sb.append("; updatedDateTime=").append(getUpdatedTime());
        sb.append("]");
        return sb.toString();
    }
}

