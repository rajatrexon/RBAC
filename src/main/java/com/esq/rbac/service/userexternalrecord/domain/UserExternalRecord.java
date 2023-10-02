package com.esq.rbac.service.userexternalrecord.domain;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.io.Serializable;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Table(name="userExternalRecord" , schema = "rbac")
@Entity
public class UserExternalRecord implements Serializable {

    @Id
    private Integer userId;

    private String externalRecordId;
}