package com.esq.rbac.service.contact.mappingtype.domain;

import jakarta.persistence.*;
import jakarta.xml.bind.annotation.XmlRootElement;
import lombok.Data;
import org.hibernate.validator.constraints.Length;

@XmlRootElement
@Entity
@Table(name = "maptype", schema = "contact")
@Data
//public class MappingType extends BaseEntity {
public class MappingType{
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "mapTypeId")
//    @Length(min = 0, max = 450)
    private Integer id;

    public Integer getMapTypeId() {
        return id;
    }

    public void setMapTypeId(Integer id) {
        this.id = id;
    }

    @Length(min = 0, max = 450)
    @Column(name = "mapType", nullable = false)
    private String mapType;

    @Length(min = 0, max = 450)
    @Column(name = "mapFrom", nullable = false)
    private String mapFrom;


    @Length(min = 0, max = 450)
    @Column(name = "mapTo", nullable = false)
    private String mapTo;

    @Length(min = 0, max = 450)
    @Column(name = "fromFlag", nullable = false)
    private String fromFlag;

    @Length(min = 0, max = 450)
    @Column(name = "toFlag", nullable = false)
    private String toFlag;



    public String getMapType() {
        return mapType;
    }

    public void setMapType(String mapType) {
        this.mapType = mapType;
    }

    public String getMapFrom() {
        return mapFrom;
    }

    public void setMapFrom(String mapFrom) {
        this.mapFrom = mapFrom;
    }

    public String getMapTo() {
        return mapTo;
    }

    public void setMapTo(String mapTo) {
        this.mapTo = mapTo;
    }

    public String getFromFlag() {
        return fromFlag;
    }

    public void setFromFlag(String fromFlag) {
        this.fromFlag = fromFlag;
    }

    public String getToFlag() {
        return toFlag;
    }

    public void setToFlag(String toFlag) {
        this.toFlag = toFlag;
    }



    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("mapTypeTd[id=").append(id);
        sb.append("; mapType=").append(mapType);
        sb.append("; mapFrom=").append(mapFrom);
        sb.append("; mapTo=").append(mapTo);
        sb.append("; fromFlag=").append(fromFlag);
        sb.append("; toFlag=").append(toFlag);

        sb.append("]");
        return sb.toString();
    }


}
