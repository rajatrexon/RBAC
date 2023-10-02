package com.esq.rbac.service.configuration.enc;

import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.Random;

@Slf4j
@NoArgsConstructor
public class EsqSymetricCipher {
    private static final int MAX_PASSWORD_LEN = 24;
    private static final int ENCRYPTED_PASSWORD_LEN = 56;

    public static String encryptPassword(String passwordString) {
        int i = 0;
        int j = 0;
        int k = 0;
        char[] tmpPassword = new char[64];
        char[] key = new char[10];
        char[] password = passwordString.toCharArray();
        if (password.length > 24) {
            log.warn("encryptPassword; exceeded max password length {}>{}", password.length, 24);
            return "";
        } else {
            Random generator = new Random();
            int randomNumber = Math.abs(generator.nextInt(10000));
            String tempKey = String.format("%d", randomNumber);
            System.arraycopy(tempKey.toCharArray(), 0, key, 0, tempKey.length());

            for(int len = (short)tempKey.length(); len < 8; key[len] = 0) {
                key[len++] = key[i++];
            }

            key[8] = 0;
            System.arraycopy(password, 0, tmpPassword, 0, password.length);

            for(i = password.length; i < 24; ++i) {
                tmpPassword[i] = ' ';
            }

            tmpPassword[24] = 0;
            char[] encryptedPwd = new char[56];
            k = 0;
            j = 0;

            for(i = 0; i < 24; ++i) {
                if (i % 3 == 0) {
                    encryptedPwd[j++] = key[k++];
                }

                int number = tmpPassword[i] + (key[k - 1] - 48);
                int reminder = number % 20;
                number /= 20;
                encryptedPwd[j++] = (char)(65 + number);
                encryptedPwd[j++] = (char)(65 + reminder);
            }

            return new String(encryptedPwd);
        }
    }

    public static String decryptPassword(String encryptedPassword) {
        char key = 'A';
        char[] password = new char[64];
        char[] cryptPwd = encryptedPassword.toCharArray();
        if (cryptPwd.length != 56) {
            log.warn("decryptPassword; encrypted password length must be {}, but is {}", 56, cryptPwd.length);
            return "";
        } else {
            short j = 0;
            short i = 0;

            while(i < cryptPwd.length) {
                if (i % 7 == 0) {
                    key = cryptPwd[i++];
                } else {
                    short number = (short)((cryptPwd[i++] - 65) * 20);
                    number = (short)(number + (cryptPwd[i++] - 65));
                    password[j++] = (char)(number - (key - 48));
                }
            }

            password[j] = 0;
            StringBuilder pwdSB = new StringBuilder();

            for(i = 0; i < 23 && password[i] != ' '; ++i) {
                pwdSB.append(password[i]);
            }

            return pwdSB.toString();
        }
    }
}
