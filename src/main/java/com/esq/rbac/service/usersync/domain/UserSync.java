package com.esq.rbac.service.usersync.domain;

import com.esq.rbac.service.user.domain.User;
import com.esq.rbac.service.util.UtcDateConverter;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.Where;

import java.util.Date;


@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "userSync", schema = "rbac")
@Where(clause = "isDeleted = false")
public class UserSync {

    @Id
    @GeneratedValue(strategy = GenerationType.TABLE, generator = "userSyncIdGenerator")
    @TableGenerator(name = "userSyncIdGenerator", schema = "rbac", table = "idSequence", pkColumnName = "idName", valueColumnName = "idValue", pkColumnValue = "userSyncId", initialValue = 1, allocationSize = 1)
    @Column(name = "userSyncId")
    private Integer userSyncId;

    @Column(name = "externalRecordId")
    private String externalRecordId;

    @Column(name = "syncData", nullable = true)
    private String syncData;

    @Column(name = "updatedSyncData")
    private String updatedSyncData;

    @Column(name = "status")
    private Integer status;

    @Column(name = "createdBy")
    private String createdBy;

    @Column(name = "createdOn")
    @Convert(converter = UtcDateConverter.class)
    private Date createdOn;

    @Column(name = "updatedBy")
    private String updatedBy;

    @Column(name = "updatedOn")
    @Convert(converter = UtcDateConverter.class)
    private Date updatedOn;

    @Column(name = "isDeleted")
    private Boolean isDeleted;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "userId")
    private User user;

}
