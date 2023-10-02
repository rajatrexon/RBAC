package com.esq.rbac.service.configuration.domain;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "configuration", schema = "rbac")
public class Configuration {

    @Id
    @Column(name = "confKey")
    private String confKey;

    @Column(name = "confValue")
    private String confValue;

    @Column(name = "confType")
    private String confType;

    @Column(name = "confOrder")
    private String confOrder;

    @Column(name = "confGroup")
    private String confGroup;

    @Column(name = "isVisible")
    private String isVisible;

    @Column(name = "subGroup")
    private String subGroup;

    public Configuration(String confKey, String confValue, String confType, String confOrder, String confGroup, String subGroup) {
        this.confKey = confKey;
        this.confValue = confValue;
        this.confType = confType;
        this.confOrder = confOrder;
        this.confGroup = confGroup;
        this.isVisible = "0";
        this.subGroup = subGroup;
    }
}

