package com.esq.rbac.service.culture.embedded;

import com.esq.rbac.service.culture.domain.Culture;
import jakarta.persistence.Embeddable;
import lombok.Data;

import java.util.List;

@Data
@Embeddable
public class ApplicationCulture {

    private Integer applicationId;
    private Integer applicationCode;
    private String name;
    private List<Culture> supportedCultures;
}
