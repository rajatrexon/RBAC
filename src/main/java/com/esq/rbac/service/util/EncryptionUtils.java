package com.esq.rbac.service.util;

import java.security.Key;
import java.util.Base64;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.stereotype.Component;

@Component
public class EncryptionUtils implements FactoryBean {

    private static Key key;
    private static final Pattern passwordPattern = Pattern.compile("\\{(\\S+)\\}");
    private String output;

    private static final Logger log = LoggerFactory
            .getLogger(EncryptionUtils.class);

    static {
        try {
            key = generateKey();
        } catch (Exception e) {
            log.error("EncryptionUtils; static block Exception={}", e);
        }
    }

    public static String encryptPassword(String password) throws Exception {

        if (password != null) {
            Cipher aes = Cipher.getInstance("AES/ECB/PKCS5Padding");
            aes.init(Cipher.ENCRYPT_MODE, key);
            byte[] ciphertext = aes.doFinal(password.getBytes("UTF-8"));
            return new String(Base64.getEncoder().encode(ciphertext));
        }
        return null;
    }

    private static Key generateKey() throws Exception {
        byte[] salt = "Aags$sd*dd%w##svbasbssbnnGSVBASVVCSVCSVVS".getBytes();
        String passphrase = "SOMNDFCBND%V@(HDBDBBDBDBDB";
        int iterations = 8888;
        SecretKeyFactory factory = SecretKeyFactory
                .getInstance("PBKDF2WithHmacSHA1");
        SecretKey tmp = factory.generateSecret(new PBEKeySpec(passphrase
                .toCharArray(), salt, iterations, 128));
        SecretKeySpec key = new SecretKeySpec(tmp.getEncoded(), "AES");
        return key;
    }

    public static String decryptPassword(String encryptedPassword)
            throws Exception {

        if (encryptedPassword != null) {
            byte[] ciphertext = Base64.getDecoder().decode(encryptedPassword
                    .getBytes("UTF-8"));
            Cipher aes = Cipher.getInstance("AES/ECB/PKCS5Padding");
            aes.init(Cipher.DECRYPT_MODE, key);
            return new String(aes.doFinal(ciphertext));
        }
        return null;
    }
    public void setInput(String input) throws Exception {
        StringBuffer sb = new StringBuffer();
        Matcher m = passwordPattern.matcher(input);
        while (m.find()) {
            String encryptedPassword = m.group(1);
            String decryptedPassword = decryptPassword(encryptedPassword);
            m.appendReplacement(sb, Matcher.quoteReplacement(decryptedPassword));
        }
        m.appendTail(sb);
        output = sb.toString();
    }

    @Override
    public Object getObject() throws Exception {
        return output;
    }
    @Override
    public Class getObjectType() {
        return String.class;
    }
    @Override
    public boolean isSingleton() {
        return false;
    }
}
