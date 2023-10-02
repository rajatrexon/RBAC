package com.esq.rbac.service.util;

import com.esq.rbac.service.configuration.enc.EsqSymetricCipher;
import org.springframework.beans.factory.FactoryBean;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class EsqPasswordDecrypter implements FactoryBean {
    private static final Pattern passwordPattern = Pattern.compile("\\{(\\S+)\\}");
    private String output;

    public EsqPasswordDecrypter() {
    }

    public void setInput(String input) {
        StringBuffer sb = new StringBuffer();
        Matcher m = passwordPattern.matcher(input);

        while(m.find()) {
            String encryptedPassword = m.group(1);
            String decryptedPassword = EsqSymetricCipher.decryptPassword(encryptedPassword);
            m.appendReplacement(sb, Matcher.quoteReplacement(decryptedPassword));
        }

        m.appendTail(sb);
        this.output = sb.toString();
    }

    public Object getObject() throws Exception {
        return this.output;
    }

    public Class getObjectType() {
        return String.class;
    }

    public boolean isSingleton() {
        return false;
    }
}
