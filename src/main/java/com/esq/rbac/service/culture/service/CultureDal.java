package com.esq.rbac.service.culture.service;

import com.esq.rbac.service.culture.domain.Culture;
import com.esq.rbac.service.culture.embedded.ApplicationCulture;
import com.esq.rbac.service.culture.embedded.ResourceStrings;
import org.springframework.http.HttpHeaders;


import java.util.List;

public interface CultureDal {

    List<Culture> getSupportedCultures();

    List<ApplicationCulture> getAllApplicationSupportedCulture();

    ApplicationCulture assingLanguageToApplication(ApplicationCulture applicationCulture) throws Exception;

    ApplicationCulture updateApplicationLanguage(ApplicationCulture applicationCulture) throws Exception;

    ApplicationCulture getByApplicationKey(String appKey) throws Exception;

    ApplicationCulture getByApplicationId(Integer appId) throws Exception;

    ResourceStrings getApplicationResourceStrings(HttpHeaders headers);

    String getOffsetFromTimeZone(String timeZone);
}
