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
package com.esq.rbac.service.contact.contactaddresstype.domain;
import com.esq.rbac.service.contact.util.JpaDateConvertor;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(schema = "contact", name = "contact_address_type")
@NamedQueries({
        @NamedQuery(name = "listContactAddressTypes", query = "SELECT c FROM ContactAddressType c ORDER BY c.id ASC"),
        @NamedQuery(name = "findAddressTypeIdByName", query = "SELECT c.id FROM ContactAddressType c WHERE c.type = :name")
})
public class ContactAddressType{
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "type", nullable = false, length = 50)
    private String type;

    @Column(name = "user_mapping")
    private String userMapping;

    @Convert(converter = JpaDateConvertor.class)
    @Column(name = "created_time", nullable = false, updatable = false)
    private Date createdTime;

    @Convert(converter = JpaDateConvertor.class)
    @Column(name = "updated_time", nullable = false)
    private Date updatedTime;


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
}
