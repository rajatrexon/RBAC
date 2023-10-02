package com.esq.rbac.service.codes.domain;


import com.esq.rbac.service.util.UtcDateConverter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.Where;

import java.util.Date;



@Where(clause = "isActive = true")
@Entity
@Table(schema = "rbac", name = "codes")
@Data
public class Code {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "codeId")
    private Long codeId;

    @Column(name = "displayOrder")
    private Integer displayOrder;

    @Column(name = "isActive")
    private Boolean isActive;

    @Column(name = "name")
    private String name;

    @Column(name = "parentType")
    private String parentType;

    @Column(name = "codeType")
    private String codeType;

    @Column(name = "validationRegex")
    private String validationRegex;

    @Column(name = "parentCodeValue")
    private String parentCodeValue;

    @Column(name = "codeValue")
    private String codeValue;

    @Column(name = "createdBy")
    private Integer createdBy;

    @Column(name = "createdOn")
    @Convert(converter = UtcDateConverter.class)
    private Date createdOn;

    @Column(name = "updatedBy")
    private Integer updatedBy;

    @Column(name = "updatedOn")
    @Convert(converter = UtcDateConverter.class)
    private Date updatedOn;

    @Basic(fetch = FetchType.LAZY)
    @Column(name = "scopeData")
    private String scopeData;

    @Column(name = "remarks")
    private String remarks;

    // Constructors, getters, and setters

    public Code() {
        // Default constructor
    }


    public Long getCodeId() {
        return codeId;
    }

    public void setCodeId(Long codeId) {
        this.codeId = codeId;
    }

    public Integer getDisplayOrder() {
        return displayOrder;
    }

    public void setDisplayOrder(Integer displayOrder) {
        this.displayOrder = displayOrder;
    }

    public Boolean getIsActive() {
        return isActive;
    }

    public void setIsActive(Boolean isActive) {
        this.isActive = isActive;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getParentType() {
        return parentType;
    }

    public void setParentType(String parentType) {
        this.parentType = parentType;
    }

    public String getCodeType() {
        return codeType;
    }

    public void setCodeType(String codeType) {
        this.codeType = codeType;
    }

    public String getValidationRegex() {
        return validationRegex;
    }

    public void setValidationRegex(String validationRegex) {
        this.validationRegex = validationRegex;
    }

    public String getParentCodeValue() {
        return parentCodeValue;
    }

    public void setParentCodeValue(String parentCodeValue) {
        this.parentCodeValue = parentCodeValue;
    }

    public String getCodeValue() {
        return codeValue;
    }

    public void setCodeValue(String codeValue) {
        this.codeValue = codeValue;
    }

    public Integer getCreatedBy() {
        return createdBy;
    }

    @JsonIgnore
    public void setCreatedBy(Integer createdBy) {
        this.createdBy = createdBy;
    }

    @JsonIgnore
    public Date getCreatedOn() {
        return createdOn;
    }

    public void setCreatedOn(Date createdOn) {
        this.createdOn = createdOn;
    }

    @JsonIgnore
    public Integer getUpdatedBy() {
        return updatedBy;
    }

    public void setUpdatedBy(Integer updatedBy) {
        this.updatedBy = updatedBy;
    }

    @JsonIgnore
    public Date getUpdatedOn() {
        return updatedOn;
    }

    public void setUpdatedOn(Date updatedOn) {
        this.updatedOn = updatedOn;
    }

    @JsonIgnore
    public String getScopeData() {
        return scopeData;
    }

    public void setScopeData(String scopeData) {
        this.scopeData = scopeData;
    }

    public String getRemarks() {
        return remarks;
    }

    public void setRemarks(String remarks) {
        this.remarks = remarks;
    }

    public static Code copyOf(Code c){
        Code output = new Code();
        output.codeId = c.codeId;
        output.displayOrder = c.displayOrder;
        output.isActive = c.isActive;
        output.name = c.name;
        output.parentType = c.parentType;
        output.codeType = c.codeType;
        output.validationRegex = c.validationRegex;
        output.parentCodeValue = c.parentCodeValue;
        output.codeValue = c.codeValue;
        output.createdBy = c.createdBy;
        output.createdOn = c.createdOn;
        output.updatedBy = c.updatedBy;
        output.updatedOn = c.updatedOn;
        output.scopeData = c.scopeData;
        return output;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("Code{codeId=").append(codeId);
        sb.append("; displayOrder=").append(displayOrder);
        sb.append("; isActive=").append(isActive);
        sb.append("; name=").append(name);
        sb.append("; parentType=").append(parentType);
        sb.append("; codeType=").append(codeType);
        sb.append("; validationRegex=").append(validationRegex);
        sb.append("; parentCodeValue=").append(parentCodeValue);
        sb.append("; codeValue=").append(codeValue);
        sb.append("; createdBy=").append(createdBy);
        sb.append("; createdOn=").append(createdOn);
        sb.append("; updatedBy=").append(updatedBy);
        sb.append("; updatedOn=").append(updatedOn);
        sb.append("; scopeData=").append(scopeData);
        sb.append("}");
        return sb.toString();
    }

}
