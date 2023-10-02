/*
 * Copyright (c)2016 ESQ Management Solutions Pvt Ltd. All Rights Reserved.
 *
 * Permission to use, copy, modify, and distribute this software requires
 * a signed licensing agreement.
 *
 * IN NO EVENT SHALL ESQ BE LIABLE TO ANY PARTY FOR DIRECT, INDIRECT, SPECIAL,
 * INCIDENTAL, OR CONSEQUENTIAL DAMAGES, INCLUDING LOST PROFITS, ARISING OUT OF
 * THE USE OF THIS SOFTWARE AND ITS DOCUMENTATION, EVEN IF ESQ HAS BEEN ADVISED
 * OF THE POSSIBILITY OF SUCH DAMAGE. ESQ SPECIFICALLY DISCLAIMS ANY WARRANTIES,
 * INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND
 * FITNESS FOR A PARTICULAR PURPOSE.
 */

package com.esq.rbac.service.util;

import com.esq.platform.licensing.WrapperAwareLicenseService;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.configuration.Configuration;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.SecretKeySpec;
import javax.sql.DataSource;
import java.io.*;
import java.nio.charset.Charset;
import java.security.*;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.HashMap;

@Component
public class EnvironmentUtil {
	private static final Logger log = LoggerFactory
			.getLogger(EnvironmentUtil.class);
	public static final String ENVIRONMENT_CONFIG = "environmentConfig";
	public static final String MULTI_TENANT_FLAG = "multiTenant";
	private static ObjectMapper objectMapper = new ObjectMapper();
	private static TypeReference<HashMap<String, Object>> typeRef = new TypeReference<HashMap<String, Object>>() {
	};

	private Configuration propertyConfiguration;
	private HashMap<String, Object> envConfig;

	@Autowired
	JdbcTemplate jt;


	@Autowired
	DataSource dataSource;

    public void setDataSource(){
    	Boolean isMultiTenant = Boolean.FALSE;
    	try{
//    		JdbcTemplate jt = new JdbcTemplate(dataSource);
			String sql = "select license from rbac.childApplicationLicense where childAppLicenseId = (select childAppLicenseId from rbac.childApplication where applicationId = 100 and appKey = 'RBAC') ";
			System.out.println("sq; --> "+sql);
//    		String licenseData = jt.queryForObject("select license from rbac.childApplicationLicense where childAppLicenseId"
//    				+ " = (select childAppLicenseId from rbac.childApplication where applicationId = 100 and appKey = 'RBAC') ", String.class);
			String licenseData = jt.queryForObject(sql , String.class);
	    	if(licenseData!=null && !licenseData.isEmpty()){
	    		WrapperAwareLicenseService licenseService = new WrapperAwareLicenseService();
	    		isMultiTenant = Boolean.valueOf(licenseService.decryptLicenseKey(decryptLicense(licenseData)).
	    					getAttributeAsString(MULTI_TENANT_FLAG));
	    	}
    	}
    	catch(Exception e){
    		log.warn("setApplicationDal; License not loaded; some features may be disabled, e={};", e);
    	}
    	if (envConfig == null) {
			envConfig = new HashMap<String, Object>();
		}
		envConfig.put(MULTI_TENANT_FLAG, isMultiTenant);
    }
    
	@Deprecated
	public void setPropertyConfiguration(Configuration propertyConfiguration) {
		log.trace("setPropertyConfiguration; propertyConfiguration={};",
				propertyConfiguration);
		this.propertyConfiguration = propertyConfiguration;
		reloadConfig();
	}

	@Deprecated
	public void reloadConfig() {
		try {
			if (propertyConfiguration.getString(ENVIRONMENT_CONFIG) != null) {
				PrivateKey privateKey = readPrivateKey(IOUtils.toByteArray(Thread.currentThread()
						.getContextClassLoader().getResourceAsStream("private.der")));
				byte[] decodedConfig = decrypt(privateKey,
						Base64.decodeBase64(propertyConfiguration
										.getString(ENVIRONMENT_CONFIG)));
				envConfig = objectMapper.readValue(decodedConfig, typeRef);
			}
		} catch (Exception e) {
			log.error("reloadConfig; Exception={};", e);
		}
		if (envConfig == null) {
			// assume default values
			envConfig = new HashMap<String, Object>();
			envConfig.put(MULTI_TENANT_FLAG, false);
		}
	}

	@Deprecated
	public void setLicenseFile(String name){
		log.debug("setLicenseFile; name={};", name);
		WrapperAwareLicenseService licenseService = new WrapperAwareLicenseService();
		try {
			InputStream fileStream = null;
			File f = new File(name);
			  if (f.isFile()) {
				  fileStream =  new FileInputStream(f);
			  } else {
				  fileStream = Thread.currentThread().getContextClassLoader().getResourceAsStream(name);
			  }
			Boolean isMultiTenant = Boolean.valueOf(licenseService.decryptLicenseKey(IOUtils.toString(fileStream)).getAttributeAsString(MULTI_TENANT_FLAG));
			if (envConfig == null) {
				envConfig = new HashMap<String, Object>();
			}
			envConfig.put(MULTI_TENANT_FLAG, isMultiTenant);	
		} catch (Exception e) {
			log.warn("setLicenseFile; License not loaded, some features may be disabled, e={};", e);
		}
	}
	
	public boolean isMultiTenantEnvironment() {
		if (envConfig != null) {
			return (Boolean) envConfig.get(MULTI_TENANT_FLAG);
		}
		return false;
	}

	public static PublicKey readPublicKey(byte[] publicKey) throws IOException,
			NoSuchAlgorithmException, InvalidKeySpecException {
		X509EncodedKeySpec publicSpec = new X509EncodedKeySpec(publicKey);
		KeyFactory keyFactory = KeyFactory.getInstance("RSA");
		return keyFactory.generatePublic(publicSpec);
	}

	public static PrivateKey readPrivateKey(byte[] privateKey) throws IOException,
			NoSuchAlgorithmException, InvalidKeySpecException {
		PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(privateKey);
		KeyFactory keyFactory = KeyFactory.getInstance("RSA");
		return keyFactory.generatePrivate(keySpec);
	}

	public static final byte[] encrypt(PublicKey key, byte[] plaintext)
			throws NoSuchAlgorithmException, NoSuchPaddingException,
			InvalidKeyException, IllegalBlockSizeException, BadPaddingException {
		Cipher cipher = Cipher
				.getInstance("RSA/ECB/OAEPWithSHA1AndMGF1Padding");
		cipher.init(Cipher.ENCRYPT_MODE, key);
		return cipher.doFinal(plaintext);
	}

	private static byte[] decrypt(PrivateKey key, byte[] ciphertext)
			throws NoSuchAlgorithmException, NoSuchPaddingException,
			InvalidKeyException, IllegalBlockSizeException, BadPaddingException {
		Cipher cipher = Cipher
				.getInstance("RSA/ECB/OAEPWithSHA1AndMGF1Padding");
		cipher.init(Cipher.DECRYPT_MODE, key);
		return cipher.doFinal(ciphertext);
	}

	public static final String encryptLicense(String license) throws Exception {
		PrivateKey privateKey = EnvironmentUtil.readPrivateKey(IOUtils.toByteArray(new InputStreamReader(Thread.currentThread()
				.getContextClassLoader().getResourceAsStream("private.der"))));
		byte[] decodedKey = decrypt(privateKey,
				Base64.decodeBase64(IOUtils.toString(Thread.currentThread().getContextClassLoader().getResourceAsStream("encKey"))));
		SecretKeySpec newAesKey = new SecretKeySpec(decodedKey, "AES");
		Cipher aesCipher = Cipher.getInstance("AES");
		aesCipher.init(Cipher.ENCRYPT_MODE, newAesKey);
		return Base64.encodeBase64URLSafeString(
				aesCipher.doFinal(license.getBytes()));
	}

	public static final String decryptLicense(String encryptedLicense) throws Exception {
		PrivateKey privateKey = EnvironmentUtil.readPrivateKey(IOUtils.toByteArray(Thread.currentThread()
				.getContextClassLoader().getResourceAsStream("private.der")));
		byte[] decodedKey = decrypt(privateKey,
				Base64.decodeBase64(IOUtils.toString(Thread.currentThread().getContextClassLoader()
						.getResourceAsStream("encKey"),Charset.defaultCharset())));
		SecretKeySpec newAesKey = new SecretKeySpec(decodedKey, "AES");
		Cipher aesCipher = Cipher.getInstance("AES");
		aesCipher.init(Cipher.DECRYPT_MODE, newAesKey);
		return new String(aesCipher.doFinal(Base64.decodeBase64(encryptedLicense.getBytes())));
	}
   	
}
