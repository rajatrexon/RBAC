package com.esq.rbac.service.culture.embedded;

import jakarta.persistence.Embeddable;
import lombok.Data;
import java.util.Map;

@Data
@Embeddable
public class ResourceStrings {

    Integer cultureId;
    String cultureName;
    Map<String,String> stringResources;
}
