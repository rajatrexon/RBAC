package com.esq.rbac.service.ivrpasswordhistory.domain;

import com.esq.rbac.service.util.UtcDateConverter;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "ivrPasswordHistory", schema = "rbac")
public class IVRPasswordHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ivrPasswordHistoryId")
    private Integer ivrPasswordHistoryId;

    @Column(name = "userId", nullable = false)
    private Integer userId;

    @Column(name = "setTime", nullable = false)
    @Convert(converter = UtcDateConverter.class)
    private Date setTime;

    @Column(name = "ivrPasswordSalt", length = 200)
    private String ivrPasswordSalt;

    @Column(name = "ivrPasswordHash", length = 200)
    private String ivrPasswordHash;
}
