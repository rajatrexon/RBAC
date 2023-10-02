package com.esq.rbac.service.contact.domain;

import com.esq.rbac.service.codes.domain.Code;
import com.esq.rbac.service.contact.embedded.BaseEntity;
import com.esq.rbac.service.contact.sla.domain.SLA;
import com.esq.rbac.service.contact.customuserinfo.domain.CustomUserInfo;
import com.esq.rbac.service.contact.messagetemplate.domain.MessageTemplate;
import com.esq.rbac.service.contact.objectrole.domain.ObjectRole;
import com.esq.rbac.service.contact.party.domain.Party;
import com.esq.rbac.service.contact.util.JpaDateConvertor;
import com.esq.rbac.service.user.domain.User;
import com.esq.rbac.service.validation.annotation.Mandatory;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.*;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;
import jakarta.xml.bind.annotation.XmlTransient;

import java.util.Date;



@Entity
@Table(name = "contact", schema = "contact")
@XmlRootElement
public class Contact extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;


    @Column(name = "address_type", nullable = false)
    private Long addressTypeId;
    @Mandatory
    @Column(name = "address", nullable = false, length = 4000)
    private String address;

    @Transient
    private long partyId;

    @Column(name = "iscc")
    private boolean contactCC;


    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "party_id")
    private Party partyInternal;

    @Transient
    private Party party;
    @Transient
    private long objectRoleId;

    @Transient
    private ObjectRole objectRole;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "objectRoleId")
    private ObjectRole objectRoleInternal;
    @Mandatory
    @Column(name = "name", length = 500)
    private String name;

    @Column(name = "userId")
    private Integer userId;

    @Transient
    private CustomUserInfo customUserInfo;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "userId", insertable = false, updatable = false)
    private User user;

    @OneToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "durationId")
    private SLA duration;

    @Column(name = "templateId")
    private Long templateId;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "templateId", insertable = false, updatable = false)
    private MessageTemplate template;


    @Column(name = "lifecycleId")
    private Integer lifecycleId;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "lifecycleId", insertable = false, updatable = false)
    private Code lifecycle;

    @Column(name = "typeId")
    private Integer typeId;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "typeId", insertable = false, updatable = false)
    private Code type;
    @Column(name = "levelId")
    private Integer levelId;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "levelId", insertable = false, updatable = false)
    private Code level;
    @Column(name = "channelId")
    private Integer channelId;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "channelId", insertable = false, updatable = false)
    private Code channel;
    @Column(name = "atmScheduleId")
    private Integer atmScheduleId;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "atmScheduleId", insertable = false, updatable = false)
    private Code atmSchedule;

    @Column(name = "seq_num")
    private int seqNum;

    @Column(name = "waitForNextContact")
    private boolean waitForNextContact;
    @Column(name = "contactMapping")
    private String contactMapping;

    @Convert(converter = JpaDateConvertor.class)
    @Column(name = "created_time", nullable = false, updatable = false)
    private Date createdTime;

    @Convert(converter = JpaDateConvertor.class)
    @Column(name = "updated_time", nullable = false)
    private Date updatedTime;


    public Integer getLifecycleId() {
        return lifecycleId;
    }

    public void setLifecycleId(Integer lifecycleId) {
        this.lifecycleId = lifecycleId;
    }


    public Long getAddressTypeId() {
        return addressTypeId;
    }

    public void setAddressTypeId(Long addressTypeId) {
        this.addressTypeId = addressTypeId;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    @Transient
    public long getPartyId() {
        if (partyInternal != null) {
            return this.partyInternal.getId();
        }else{
            return this.partyId;
        }
    }

    public void setPartyId(long partyId) {
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

    public Party getParty() {
        Party party = new Party();
        if (partyInternal != null) {
            party.setId(partyInternal.getId());
            party.setName(partyInternal.getName());
            party.setCode(partyInternal.getCode());
            party.setDepartment(partyInternal.getDepartment());
            party.setLocations(partyInternal.getLocations());
            party.setSecurityRole(partyInternal.getSecurityRole());
            party.setType(partyInternal.getType());
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

    public Long getTemplateId() {
        return templateId;
    }

    public void setTemplateId(Long templateId) {
        this.templateId = templateId;
    }

    public MessageTemplate getTemplate() {
        return template;
    }

    public void setTemplate(MessageTemplate template) {
        this.template = template;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("Contact[id=").append(getId());
        sb.append("; partyId=").append(partyId);
        sb.append("; channel=").append(channel);
        sb.append("; objectRoleId=").append(objectRoleId);
        sb.append("; addressType=").append(addressTypeId);
        sb.append("; address=").append(address);
//        sb.append("; user=").append(user);
        sb.append("; user=").append(user);
        sb.append("; cc=").append(contactCC);
        sb.append("; template=").append(template);
        sb.append("; duration=").append(duration);
        sb.append("; lifecycle=").append(lifecycle);
        sb.append("; createdDateTime=").append(getCreatedTime());
        sb.append("; updatedDateTime=").append(getUpdatedTime());
        sb.append("]");
        return sb.toString();
    }

    public Code getLifecycle() {
        return lifecycle;
    }

    public boolean isContactCC() {
        return contactCC;
    }

    public void setContactCC(boolean contactCC) {
        this.contactCC = contactCC;
    }

    public void setLifecycle(Code lifecycle) {
        this.lifecycle = lifecycle;
    }

    public Integer getTypeId() {
        return typeId;
    }

    public void setTypeId(Integer typeId) {
        this.typeId = typeId;
    }

    public Code getType() {
        return type;
    }

    public void setType(Code type) {
        this.type = type;
    }

    public Code getLevel() {
        return level;
    }

    public void setLevel(Code level) {
        this.level = level;
    }

    public Integer getLevelId() {
        return levelId;
    }

    public void setLevelId(Integer levelId) {
        this.levelId = levelId;
    }

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

    public Code getAtmSchedule() {
        return atmSchedule;
    }

    public void setAtmSchedule(Code atmSchedule) {
        this.atmSchedule = atmSchedule;
    }

    public Integer getAtmScheduleId() {
        return atmScheduleId;
    }

    public void setAtmScheduleId(Integer atmScheduleId) {
        this.atmScheduleId = atmScheduleId;
    }

    @XmlTransient
    @JsonIgnore
    public ObjectRole getObjectRoleInternal() {
        return objectRoleInternal;
    }

    public void setObjectRoleInternal(ObjectRole objectRoleInternal) {
        this.objectRoleInternal = objectRoleInternal;
/*		if (this.objectRoleInternal != null) {
			if (this.objectRoleInternal.getContacts() == null) {
				this.objectRoleInternal
						.setContacts(new ArrayList<Contact>());
			}
			if (this.objectRoleInternal.getContacts().contains(this) == false) {
				this.objectRoleInternal.getContacts().add(this);
			}
		}*/
    }

    @Transient
    public Long getObjectRoleId() {
        return this.objectRoleInternal==null?null:this.objectRoleInternal.getId();
    }

    public void setObjectRoleId(long objectRoleId) {
        this.objectRoleId = objectRoleId;
    }

    public void setObjectRole(ObjectRole objectRole){
        //ignore
    }

    public ObjectRole getObjectRole(){
        ObjectRole objectRole=new ObjectRole();
        if(objectRoleInternal!=null){
            objectRole.setObjectId(objectRoleInternal.getObjectId());
            objectRole.setContactRole(objectRoleInternal.getContactRole());
            objectRole.setCreatedTime(objectRoleInternal.getCreatedTime());
            objectRole.setId(objectRoleInternal.getId());
            objectRole.setMappingType(objectRoleInternal.getMappingType());
            objectRole.setObjectKey(objectRoleInternal.getObjectKey());
            objectRole.setSchedule(objectRoleInternal.getSchedule());
            objectRole.setTenantId(objectRoleInternal.getTenantId());
            objectRole.setUpdatedTime(objectRoleInternal.getUpdatedTime());

        }
        return objectRole;
    }

    public SLA getDuration() {
        return duration;
    }

    public void setDuration(SLA duration) {
        this.duration = duration;
    }

    public int getSeqNum() {
        return seqNum;
    }

    public void setSeqNum(int seqNum) {
        this.seqNum = seqNum;
    }

    public boolean isWaitForNextContact() {
        return waitForNextContact;
    }

    public void setWaitForNextContact(boolean waitForNextContact) {
        this.waitForNextContact = waitForNextContact;
    }

    public String getContactMapping() {
        return contactMapping;
    }

    public void setContactMapping(String contactMapping) {
        this.contactMapping = contactMapping;
    }

    /*
     * public boolean isCC() { return CC; }
     *
     * public void setCC(boolean CC) { this.CC = CC; }
     */

    @Transient
    public CustomUserInfo getCustomUserInfo() {
        return customUserInfo;
    }

    @XmlElement(name="user")
    @JsonProperty(value="user")
    public void setCustomUserInfo(CustomUserInfo customUserInfo) {
        this.customUserInfo = customUserInfo;
    }
}

