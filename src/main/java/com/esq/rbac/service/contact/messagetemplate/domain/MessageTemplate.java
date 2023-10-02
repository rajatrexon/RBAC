package com.esq.rbac.service.contact.messagetemplate.domain;

import com.esq.rbac.service.validation.annotation.Length;
import com.esq.rbac.service.validation.annotation.Mandatory;
import jakarta.persistence.*;
import jakarta.xml.bind.annotation.XmlRootElement;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "template", schema = "contact")
@XmlRootElement
public class MessageTemplate {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "name", nullable = false, length = 256)
    @Mandatory
    @Length(min = 0, max = 50)
    private String name;

    @Column(name = "description", nullable = true, length = 1024)
    private String description;

    @Column(name = "template_type", nullable = false)
    private String templateType;

    @Column(name = "json_definition", nullable = false)
    private String jsonDefinition;

    @Column(name = "tenant_id")
    private Long tenantId;

    @Column(name = "channelId", nullable = false)
    @Mandatory
    private Long channelId;

    @Column(name = "appKey", nullable = false)
    private String appKey;

    public static MessageTemplate copyOf(MessageTemplate c){
        MessageTemplate output = new MessageTemplate();
        output.id = c.id;
        output.name = c.name;
        output.description = c.description;
        output.templateType = c.templateType;
        output.jsonDefinition = c.jsonDefinition;
        output.tenantId = c.tenantId;
        output.channelId = c.channelId;
        output.appKey = c.appKey;
        return output;
    }

}
