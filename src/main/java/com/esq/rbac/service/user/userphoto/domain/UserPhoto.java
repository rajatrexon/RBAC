package com.esq.rbac.service.user.userphoto.domain;

import com.esq.rbac.service.util.UtcDateConverter;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.Date;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(schema = "rbac", name = "userPhoto")
public class UserPhoto implements Serializable {

        @Id
        private Integer userId;

        @Column
        private String contentType;

        @Column
        @Lob
        private byte[] photo;

        @Column
        private Integer updatedBy;

        @Column
        @Convert(converter = UtcDateConverter.class)
        private Date updatedOn;

        // Constructors, getters, and setters...
}
