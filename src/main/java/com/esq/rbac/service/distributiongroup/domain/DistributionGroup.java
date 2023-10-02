package com.esq.rbac.service.distributiongroup.domain;

import com.esq.rbac.service.util.UtcDateConverter;
import jakarta.persistence.*;
import jakarta.validation.constraints.Pattern;
import lombok.Data;
import org.joda.time.DateTime;

import java.util.Date;


@Entity
@Table(schema = "rbac", name = "distributionGroup")
@Data
public class DistributionGroup {


    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "distId")
    private Integer distId;

    @Column(name = "distName", nullable = false, length = 32)
    @Pattern(regexp = "^([^<>=]*)$", message = "Name should not have <,> and =")
    private String distName;
    @Column(name = "description", length = 4000)
    @Pattern(regexp = "^([^<>=]*)$", message = "Description should not have <,> and =")
    private String description;

    @Column(name = "tenantId", nullable = false)
    private Integer tenantId;

    @Column(name = "updatedBy")
    private Integer updatedBy;

    @Column(name = "createdBy")
    private Integer createdBy;

    @Column(name = "createdOn")
    @Convert(converter = UtcDateConverter.class)
    private Date createdOn = DateTime.now().toDate();

    @Column(name = "updatedOn")
    @Convert(converter = UtcDateConverter.class)
    private Date updatedOn;

    @Column(name = "isActive")
    private Integer isActive = 1;


    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Integer getDistId() {
        return distId;
    }

    public void setDistId(Integer distId) {
        this.distId = distId;
    }

    public String getDistName() {
        return distName;
    }

    public void setDistName(String distName) {
        this.distName = distName;
    }

    public Integer getTenantId() {
        return tenantId;
    }

    public void setTenantId(Integer tenantId) {
        this.tenantId = tenantId;
    }

    public Integer getUpdatedBy() {
        return updatedBy;
    }

    public void setUpdatedBy(Integer updatedBy) {
        this.updatedBy = updatedBy;
    }

    public Integer getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(Integer createdBy) {
        this.createdBy = createdBy;
    }

    public Date getCreatedOn() {
        return createdOn;
    }

    public void setCreatedOn(Date createdOn) {
        this.createdOn = createdOn;
    }

    public Date getUpdatedOn() {
        return updatedOn;
    }

    public void setUpdatedOn(Date updatedOn) {
        this.updatedOn = updatedOn;
    }

    public Integer getIsActive() {
        return isActive;
    }

    public void setIsActive(Integer isActive) {
        this.isActive = isActive;
    }

}
