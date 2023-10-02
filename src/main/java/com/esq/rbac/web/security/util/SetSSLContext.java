/*
 * Copyright (c)2014 ESQ Management Solutions Pvt Ltd. All Rights Reserved.
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
package com.esq.rbac.web.security.util;

import org.apache.commons.lang.ArrayUtils;

import javax.net.ssl.*;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.security.KeyStore;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;

public class SetSSLContext {

	private String defaultProtocol;
	
	private String keyStorePath;
	
	private String keyStorePassword;
	
	private String keyManagerPassword;
	
	public String getDefaultProtocol() {
		return defaultProtocol;
	}

	public void setDefaultProtocol(String defaultProtocol) {
		this.defaultProtocol = defaultProtocol;
	}

	public void setKeyStorePath(String keyStorePath) {
		this.keyStorePath = keyStorePath;
	}

	public void setKeyStorePassword(String keyStorePassword) {
		this.keyStorePassword = keyStorePassword;
	}

	public void setKeyManagerPassword(String keyManagerPassword) {
		this.keyManagerPassword = keyManagerPassword;
	}

	public void init() throws Exception {
		SSLContext context = SSLContext.getInstance(defaultProtocol);
		context.init(null, null, null);
		SSLContext.setDefault(context);
	}
	
	public void initWithKeyStore() throws Exception {
		
		SSLContext context = SSLContext.getInstance("TLSv1.2");
		if(defaultProtocol!=null && !defaultProtocol.isEmpty()){
			context = SSLContext.getInstance(defaultProtocol);
		}
		KeyStore keyStore = KeyStore.getInstance("JKS");
		InputStream is = new FileInputStream(new File(keyStorePath));
		keyStore.load(is, keyStorePassword!=null?keyStorePassword.toCharArray():null);
		is.close();

		KeyManagerFactory keyManagerFactory = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
		keyManagerFactory.init(keyStore, keyManagerPassword!=null?keyManagerPassword.toCharArray():
								(keyStorePassword!=null?keyStorePassword.toCharArray():null));
		
		//trust this certificate as well along with default
		TrustManagerFactory tmf = TrustManagerFactory
			    .getInstance(TrustManagerFactory.getDefaultAlgorithm());
		// Using null here initialises the TMF with the default trust store.
		tmf.init((KeyStore) null);

		// Get hold of the default trust manager
		X509TrustManager defaultTm = null;
		for (TrustManager tm : tmf.getTrustManagers()) {
			if (tm instanceof X509TrustManager) {
				defaultTm = (X509TrustManager) tm;
				break;
			}
		}

		FileInputStream myKeys = new FileInputStream(new File(keyStorePath));

		// Do the same with your trust store this time
		// Adapt how you load the keystore to your needs
		KeyStore myTrustStore = KeyStore.getInstance(KeyStore.getDefaultType());
		myTrustStore.load(myKeys, null);

		myKeys.close();

		tmf = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
		tmf.init(myTrustStore);

		// Get hold of the default trust manager
		X509TrustManager myTm = null;
		for (TrustManager tm : tmf.getTrustManagers()) {
			if (tm instanceof X509TrustManager) {
				myTm = (X509TrustManager) tm;
				break;
			}
		}

		// Wrap it in your own class.
		final X509TrustManager finalDefaultTm = defaultTm;
		final X509TrustManager finalMyTm = myTm;
		X509Certificate[] finalCerts = (X509Certificate[])ArrayUtils.addAll(finalDefaultTm.getAcceptedIssuers(), finalMyTm.getAcceptedIssuers());
		X509TrustManager customTm = new X509TrustManager() {
			@Override
			public X509Certificate[] getAcceptedIssuers() {
				// If you're planning to use client-cert auth,
				// merge results from "defaultTm" and "myTm".
				return finalCerts;
			}

			@Override
			public void checkServerTrusted(X509Certificate[] chain, String authType) throws CertificateException {
				try {
					finalMyTm.checkServerTrusted(chain, authType);
				} catch (CertificateException e) {
					// This will throw another CertificateException if this
					// fails too.
					finalDefaultTm.checkServerTrusted(chain, authType);
				}
			}

			@Override
			public void checkClientTrusted(X509Certificate[] chain, String authType) throws CertificateException {
				// If you're planning to use client-cert auth,
				// do the same as checking the server.
				try {
					finalDefaultTm.checkClientTrusted(chain, authType);
				} catch (CertificateException e) {
					// This will throw another CertificateException if this
					// fails too.
					finalMyTm.checkClientTrusted(chain, authType);
				}
			}
		};

			
		context.init(keyManagerFactory.getKeyManagers(), new TrustManager[] { customTm }, null);
		SSLContext.setDefault(context);
	}
}
