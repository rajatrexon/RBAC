package com.esq.rbac.service.sessiondata.domain;

import com.esq.rbac.service.util.UtcDateConverter;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;


@AllArgsConstructor
@NoArgsConstructor
@Data
@Entity
@Table(name = "sessionData", schema = "rbac")
public class Session {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "sessionLogId")
    private Integer sessionLogId;

    @Column(name = "sessionHash", nullable = false)
    private String sessionHash;

    @Column(name = "deviceId")
    private String deviceId;

    @Column(name = "deviceType")
    private String deviceType;

    @Column(name = "headerInfo", nullable = false)
    private String headerInfo;

    @Convert(converter = UtcDateConverter.class)
    @Column(name = "createdTime", nullable = false)
    private Date createdTime;

    @Column(name = "userId")
    private Integer userId;

    @Column(name = "ticket")
    private String ticket;

    @Column(name = "serviceUrl")
    private String serviceUrl;

    @Column(name = "childApplicationId")
    private Integer childApplicationId;

    @Column(name = "clientIp")
    private String clientIp;

    @Column(name = "userName")
    private String userName;

    @Column(name = "childApplicationName")
    private String childApplicationName;

    @Column(name = "appType")
    private Integer appType;

    @Convert(converter = UtcDateConverter.class)
    @Column(name = "lastActivityTime")
    private Date lastActivityTime;

    @Column(name = "appKey")
    private String appKey;

    @Column(name = "loginType")
    private String loginType;

    @Column(name = "identityId")
    private String identityId;

    @Column(name = "additionalAttributes")
    private String additionalAttributes;

    private Integer appUrlId;

}

