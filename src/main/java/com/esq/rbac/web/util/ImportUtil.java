/*
 * Copyright (c)2015 ESQ Management Solutions Pvt Ltd. All Rights Reserved.
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
package com.esq.rbac.web.util;

import com.esq.rbac.web.exception.ErrorInfoException;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.codec.digest.DigestUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.security.Key;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
@Component
//configuration for csv import user functionality
public class ImportUtil {
	private static final Logger log = LoggerFactory
			.getLogger(ImportUtil.class);
	
	private boolean storeImportedCSVFiles = true; //whether to keep the uploaded files in the system.
	private String importCSVLogFolder; //folder location
	public String defaultCSVStorageFolder = "csvImportLog"; //inside rbac folder
	private String csvCharSet = "utf8"; //charset for files
	private long maxFileSizeBytes = 2097152; //maximum file size allowed
	private String fileNameEncryptionKey = "7438sfjdfgt674eu23hk4jrnwdfr9e8243oijkr5nwtgd9843iotgnmsdfddopfn"; //used to hide the file name from the client
	private boolean validateFileName = true;
	private String fileNameValidationRegex = "(?i)^([a-zA-Z0-9\\s\\-_,]+\\.(csv)$)";
	
	private static final String ALGO = "AES";

	public boolean isStoreImportedCSVFiles() {
		return storeImportedCSVFiles;
	}

	public void setStoreImportedCSVFiles(boolean storeImportedCSVFiles) {
		this.storeImportedCSVFiles = storeImportedCSVFiles;
	}

	public String getImportCSVLogFolder() {
		return importCSVLogFolder;
	}

	public void setImportCSVLogFolder(String importCSVLogFolder) {
		this.importCSVLogFolder = importCSVLogFolder;
	}

	public String getDefaultCSVStorageFolder() {
		return defaultCSVStorageFolder;
	}

	public void setDefaultCSVStorageFolder(String defaultCSVStorageFolder) {
		this.defaultCSVStorageFolder = defaultCSVStorageFolder;
	}

	public String getCsvCharSet() {
		return csvCharSet;
	}

	public void setCsvCharSet(String csvCharSet) {
		this.csvCharSet = csvCharSet;
	}

	public long getMaxFileSizeBytes() {
		return maxFileSizeBytes;
	}

	public void setMaxFileSizeBytes(long maxFileSizeBytes) {
		this.maxFileSizeBytes = maxFileSizeBytes;
	}

	public void setFileNameEncryptionKey(String fileNameEncryptionKey) {
		this.fileNameEncryptionKey = fileNameEncryptionKey;
	}

	public boolean isValidateFileName() {
		return validateFileName;
	}

	public void setValidateFileName(boolean validateFileName) {
		this.validateFileName = validateFileName;
	}

	public String getFileNameValidationRegex() {
		return fileNameValidationRegex;
	}

	public void setFileNameValidationRegex(String fileNameValidationRegex) {
		this.fileNameValidationRegex = fileNameValidationRegex;
	}

	public void validateTypeOfData(byte[] data, String fileName) {
		Charset charSet = Charset.forName(getCsvCharSet());
		try {
			charSet.decode(ByteBuffer.wrap(data));
			log.error("validateTypeOfData; File invalid; fileName={};", fileName);
		} catch (Exception e) {
			throw new ErrorInfoException("csvImportFileTypeNotSupported",
					e.getMessage());
		}
	}
	
	public void validateFileName(String fileName) {
		try{
			Pattern p = Pattern.compile(fileNameValidationRegex);//. represents single character  
	        Matcher m = p.matcher(fileName);  
	        if(!m.matches()){
				log.error("validateFileName; File invalid; fileName={};", fileName);
				throw new ErrorInfoException("csvImportFileTypeNotSupported",
						"csvImportFileTypeNotSupported");
			} 
        } catch (Exception e) {
			log.error("validateFileName; File invalid; fileName={}; exception={};", fileName, e);
			throw new ErrorInfoException("csvImportFileTypeNotSupported",
					e.getMessage());
		}
	}

	public byte[] checkContentSizeAndReturnBytes(InputStream inputStream, String fileName)
			throws IOException {
		ByteArrayOutputStream osStream = new ByteArrayOutputStream();
		long nread = 0L;
		byte[] buf = new byte[1024];
		int read= -1;
		while ((read = inputStream.read(buf)) != -1) {
			osStream.write(buf, 0, read);
			nread += read;
			if (nread > getMaxFileSizeBytes()) {
				inputStream.close();
				osStream.close();
				log.error("checkContentSizeAndReturnBytes; File too large; fileName={};", fileName);
				throw new ErrorInfoException("csvImportFileTooLarge",
						"fileTooLarge:maxSize=" + getMaxFileSizeBytes());
			}
		}
		return osStream.toByteArray();
	}

	public String encryptFileName(String fileName) throws Exception {
		Key key = generateKey();
		Cipher c = Cipher.getInstance(ALGO);
		c.init(Cipher.ENCRYPT_MODE, key);
		byte[] encVal = c.doFinal(fileName.getBytes());
		String encryptedValue = Base64.encodeBase64URLSafeString(encVal);
		return encryptedValue;
	}

	public String decryptFileName(String encryptedFileName) throws Exception {
		Key key = generateKey();
		Cipher c = Cipher.getInstance(ALGO);
		c.init(Cipher.DECRYPT_MODE, key);
		byte[] decordedValue = Base64.decodeBase64(encryptedFileName);
		byte[] decValue = c.doFinal(decordedValue);
		String decryptedValue = new String(decValue);
		return decryptedValue;
	}

	private Key generateKey() throws Exception {
		Key key = new SecretKeySpec(DigestUtils.md5(fileNameEncryptionKey),
				ALGO);
		return key;
	}

}
