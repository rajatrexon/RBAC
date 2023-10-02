/*
 * Copyright (c)2013 ESQ Management Solutions Pvt Ltd. All Rights Reserved.
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
package com.esq.rbac.web.client;

import com.fasterxml.jackson.databind.DeserializationFeature;
import io.netty.channel.ChannelOption;
import io.netty.handler.timeout.ReadTimeoutHandler;
import org.apache.http.HttpHeaders;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.netty.http.client.HttpClient;
import reactor.netty.resources.ConnectionProvider;

import java.util.Map;


@Component
public class ExternalServiceRestClient {

    private static final Logger log = LoggerFactory.getLogger(ExternalServiceRestClient.class);
    WebClient.Builder client;
    private String connectTimeoutMs;
    private String readTimeoutMs;
    private String userName;
    private String password;
    private String baseUrl;
    private int defaultConnectTimeoutMs = 5000;
    private int defaultReadTimeoutMs = 5000;

    private Map<String, String> additionalProperties;



    public void setConnectTimeoutMs(String connectTimeoutMs) {
        HttpClient httpClient = HttpClient.create().option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 20000);

        client = WebClient.builder().clientConnector(new ReactorClientHttpConnector(httpClient));
        log.debug("setConnectTimeout; {} ms", 20000);
	}

	public void setReadTimeoutMs(String readTimeoutMs) {
        // Create a new HttpClient with a read timeout handler
        HttpClient httpClient = HttpClient.create().doOnConnected(conn -> conn.addHandlerLast(new ReadTimeoutHandler(20000)));

        // Update the existing WebClient instance with the new HttpClient
        client = WebClient.builder().clientConnector(new ReactorClientHttpConnector(httpClient));

        log.debug("setReadTimeout; {} ms", 20000);
	}

	public void setBaseUrl(String baseUrl) {
		this.baseUrl = baseUrl;
	}

	public void setUserName(String userName) {
		this.userName = userName;
	}

	public void setPassword(String password) {
		this.password = password;
	}

    private static String basicAuth(String username, String password) {
        String credentials = username + ":" + password;
        return "Basic " + java.util.Base64.getEncoder().encodeToString(credentials.getBytes());
    }

    public void setMaxConnectionsPerHost(int maxConnectionsPerHost) {
        HttpClient httpClient = HttpClient.create(ConnectionProvider.builder("customConnectionProvider").maxConnections(maxConnectionsPerHost).build());
        client = WebClient.builder().clientConnector(new ReactorClientHttpConnector(httpClient));
    }

    public void setMaxTotalConnections(int maxTotalConnections) {
        HttpClient httpClient = HttpClient.create(ConnectionProvider.builder("customConnectionProvider").maxConnections(maxTotalConnections).build());
        client = WebClient.builder().clientConnector(new ReactorClientHttpConnector(httpClient));
    }


    private String getUrl(String... parts) {
        StringBuilder sb = new StringBuilder();
        String baseUrl = "http://localhost:8002/rbac/restapp";
        if (baseUrl == null || baseUrl.isEmpty()) {
            log.error("getUrl; required property - {} null or empty ", baseUrl);
        }
        sb.append(baseUrl);
        for (String p : parts) {
            sb.append("/");
            sb.append(p);
        }
        log.debug("getUrl; {}", sb);
        return sb.toString();
    }

    public synchronized WebClient.Builder resource(String... parts) {

        return WebClient.builder().baseUrl(getUrl(parts))
                .defaultHeader(HttpHeaders.AUTHORIZATION, basicAuth(userName, password));
    }

	public String getBaseUrl() {
		return baseUrl;
	}

	public Map<String, String> getAdditionalProperties() {
		return additionalProperties;
	}

	public void setAdditionalProperties(Map<String, String> additionalProperties) {
		this.additionalProperties = additionalProperties;
	}

}
