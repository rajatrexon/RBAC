/*
 * Copyright (c)2016 ESQ Management Solutions Pvt Ltd. All Rights Reserved.
 *
 * Permission to use, copy, modify, and distribute this software requires
 * a signed licensing agreement.
 *
 * IN NO EVENT SHALL ESQ BE LIABLE TO ANY PARTY FOR DIRECT, INDIRECT, SPECIAL,
 * INCIDENTAL, OR CONSEQUENTIAL DAMAGES, INCLUDING LOST PROFITS, ARISING OUT OF
 * THE USE OF THIS SOFTWARE AND ITS DOCUMENTATION, EVEN IF ESQ HAS BEEN ADVISED
 * OF THE POSSIBILITY OF SUCH DAMAGE. ESQ SPECIFICALLY DISCLAIMS ANY WARRANTIES,
 * INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND
 * FITNESS FOR A PARTICULAR PURPOSE.
 */
package com.esq.rbac.web.vo;

import com.fasterxml.jackson.annotation.JsonIgnore;

import java.util.Date;
import java.util.Set;
import java.util.TreeSet;

public class Tenant {
    private Long tenantId;

    private String tenantName;

    private Code tenantType;

    private Code tenantSubType;
    private Date startDate;
    private Date billDate;
    private String accountManager;
    private Long createdBy;
    private Date createdOn;
    private Long updatedBy;
    private Date updatedOn;

    private String tenantURL;

    private String remarks;
    private boolean isDeleted;
    /* Start
     * Added By Fazia 19-Dec-2018
     * This Flag was added to enable or disable the maker checker feature for the tenant*/
    private Boolean makerCheckerEnabled;
    /*END*/
    private Set<TenantIdentifier> identifiers;

    private Boolean twoFactorAuthEnabled;//RBAC-1562

    public Boolean isTwoFactorAuthEnabled() {
        return twoFactorAuthEnabled;
    }

    public void setTwoFactorAuthEnabled(Boolean twoFactorAuthEnabled) {
        this.twoFactorAuthEnabled = twoFactorAuthEnabled;
    }

    public Boolean isMakerCheckerEnabled() {
        return makerCheckerEnabled;
    }

    public void setMakerCheckerEnabled(Boolean makerCheckerEnabled) {
        this.makerCheckerEnabled = makerCheckerEnabled;
    }

    public String getTenantURL() {
        return tenantURL;
    }

    public void setTenantURL(String tenantURL) {
        this.tenantURL = tenantURL;
    }

    public String getRemarks() {
        return remarks;
    }

    public void setRemarks(String remarks) {
        this.remarks = remarks;
    }

    public Long getTenantId() {
        return tenantId;
    }

    public void setTenantId(Long tenantId) {
        this.tenantId = tenantId;
    }

    public String getTenantName() {
        return tenantName;
    }

    public void setTenantName(String tenantName) {
        this.tenantName = tenantName;
    }

    public Code getTenantType() {
        return tenantType;
    }

    public void setTenantType(Code tenantType) {
        this.tenantType = tenantType;
    }

    public Code getTenantSubType() {
        return tenantSubType;
    }

    public void setTenantSubType(Code tenantSubType) {
        this.tenantSubType = tenantSubType;
    }

    public Date getStartDate() {
        return startDate;
    }

    public void setStartDate(Date startDate) {
        this.startDate = startDate;
    }

    public Date getBillDate() {
        return billDate;
    }

    public void setBillDate(Date billDate) {
        this.billDate = billDate;
    }

    public String getAccountManager() {
        return accountManager;
    }

    public void setAccountManager(String accountManager) {
        this.accountManager = accountManager;
    }

    public Long getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(Long createdBy) {
        this.createdBy = createdBy;
    }

    public Date getCreatedOn() {
        return createdOn;
    }

    public void setCreatedOn(Date createdOn) {
        this.createdOn = createdOn;
    }

    public Long getUpdatedBy() {
        return updatedBy;
    }

    public void setUpdatedBy(Long updatedBy) {
        this.updatedBy = updatedBy;
    }

    public Date getUpdatedOn() {
        return updatedOn;
    }

    public void setUpdatedOn(Date updatedOn) {
        this.updatedOn = updatedOn;
    }

    @JsonIgnore
    public boolean isDeleted() {
        return isDeleted;
    }

    public void setDeleted(boolean isDeleted) {
        this.isDeleted = isDeleted;
    }

    public Set<TenantIdentifier> getIdentifiers() {
        if (identifiers != null && !(identifiers instanceof TreeSet)) {
            identifiers = new TreeSet<TenantIdentifier>(identifiers);
        }
        return identifiers;
    }

    public void setIdentifiers(Set<TenantIdentifier> identifiers) {
        this.identifiers = identifiers;
    }
}
