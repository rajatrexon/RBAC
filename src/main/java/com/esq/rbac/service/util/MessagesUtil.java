package com.esq.rbac.service.util;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.support.ReloadableResourceBundleMessageSource;
import org.springframework.stereotype.Component;

import javax.inject.Inject;
import java.time.LocalDate;
import java.util.Locale;
@Component
public class MessagesUtil {

    private ReloadableResourceBundleMessageSource resourceBundle;


    @Autowired
    public void setResourceBundle(
            ReloadableResourceBundleMessageSource resourceBundle) {
        this.resourceBundle = resourceBundle;
    }

    public String getMessage(String key, Object[] args, Locale locale,
                             String defaultMessage) {
        if (locale == null) {
            locale = RBACUtil.getLocaleFromString(RBACUtil.DEFAULT_LOCALE_STRING);
        }
        if (defaultMessage == null) {
            defaultMessage = key;
        }
        return resourceBundle.getMessage(key, args, defaultMessage, locale);
    }

    public String getCurrentYear() {
        return LocalDate.now().getYear()+"";
    }

}
