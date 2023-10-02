/*
 * Copyright (c)2013,2014 ESQ Management Solutions Pvt Ltd. All Rights Reserved.
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

import org.apache.commons.lang.RandomStringUtils;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.util.encoders.Base64;

import javax.crypto.Cipher;
import java.math.BigInteger;
import java.security.*;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.RSAPublicKeySpec;
import java.security.spec.X509EncodedKeySpec;

public class JCryptionUtil {

	public static final Provider pro = new BouncyCastleProvider();

	private static final String charSet = "UTF-8";

	
	public static KeyPair generateKeyPair() throws Exception {
		KeyPairGenerator kpg = KeyPairGenerator.getInstance("RSA", pro);
		String generatedString = RandomStringUtils.random(10, true, false);
		kpg.initialize(1024, new SecureRandom(generatedString.getBytes()));
		KeyPair kp = kpg.generateKeyPair();

		return kp;
	}

	public static PublicKey getPublicRSAKey(String modulus, String exponent) throws Exception {
		RSAPublicKeySpec spec = new RSAPublicKeySpec(new BigInteger(modulus, 16), new BigInteger(exponent, 16));
		KeyFactory kf = KeyFactory.getInstance("RSA", pro);
		return kf.generatePublic(spec);
	}

	public static PublicKey getPublicRSAKey(String key) throws Exception {
		X509EncodedKeySpec x509 = new X509EncodedKeySpec(Base64.decode(key));
		KeyFactory kf = KeyFactory.getInstance("RSA", pro);
		return kf.generatePublic(x509);
	}

	public static PrivateKey getPrivateRSAKey(String key) throws Exception {
		PKCS8EncodedKeySpec pkgs8 = new PKCS8EncodedKeySpec(Base64.decode(key));
		KeyFactory kf = KeyFactory.getInstance("RSA", pro);
		return kf.generatePrivate(pkgs8);
	}

	public static byte[] encrypt(String input, PublicKey publicKey) throws Exception {
		Cipher cipher = Cipher.getInstance("RSA/None/PKCS1Padding", pro);
		cipher.init(Cipher.ENCRYPT_MODE, publicKey);
		byte[] re = cipher.doFinal(input.getBytes(charSet));
		return re;
	}

	public static byte[] decrypt(String encrypted, PrivateKey privateKey) throws Exception {
		byte [] encryptedBytes = Base64.decode(encrypted);
		
		Cipher cipher = Cipher.getInstance("RSA/None/PKCS1Padding", pro);
		cipher.init(Cipher.DECRYPT_MODE, privateKey);
		byte[] re = cipher.doFinal(encryptedBytes);
		return re;
	}
}