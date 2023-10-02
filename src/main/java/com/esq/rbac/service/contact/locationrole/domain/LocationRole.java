/*
 * Copyright ©2012 ESQ Management Solutions Pvt Ltd. All Rights Reserved.
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
package com.esq.rbac.service.contact.locationrole.domain;

import com.esq.rbac.service.contact.embedded.BaseEntity;
import com.esq.rbac.service.contact.util.JpaDateConvertor;
import com.esq.rbac.service.validation.annotation.Length;
import com.esq.rbac.service.validation.annotation.Mandatory;
import jakarta.persistence.*;
import jakarta.xml.bind.annotation.XmlRootElement;

import java.time.LocalDateTime;
import java.util.Date;

@XmlRootElement
@Entity
@Table(name = "location_role", schema = "contact")
public class LocationRole extends BaseEntity {

    @Mandatory
    @Length(min = 0, max = 500)
    @Column(name = "role_name", nullable = false, length = 500)
    private String roleName;

    @Convert(converter = JpaDateConvertor.class)
    @Column(name = "created_time", nullable = false, updatable = false)
    private LocalDateTime createdTime;

    @Convert(converter = JpaDateConvertor.class)
    @Column(name = "updated_time", nullable = false)
    private LocalDateTime updatedTime;

    public String getRoleName() {
        return roleName;
    }

    public void setRoleName(String roleName) {
        this.roleName = roleName;
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
        StringBuilder sb = new StringBuilder("LocationRole");
        sb.append("id:").append(getId());
        sb.append("name: ").append(roleName);
        sb.append("createdDateTime: ").append(getCreatedTime());
        sb.append("updatedDateTime: ").append(getUpdatedTime());

        return sb.toString();
    }
}
