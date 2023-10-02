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
import io.netty.channel.ChannelOption;
import io.netty.handler.timeout.ReadTimeoutHandler;
import org.apache.http.HttpHeaders;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.netty.http.client.HttpClient;
import reactor.netty.resources.ConnectionProvider;

@Component
public class RestClient {
        private static final Logger log = LoggerFactory.getLogger(RestClient.class);
    //Todo need to be put in seperate configuration properties file    private final int defaultConnectTimeoutMs = 5000;
   //     private final int defaultReadTimeoutMs = 5000;
        WebClient.Builder client;

        @Value("${webclient.connectTimeoutMsKey}")
        private String connectTimeoutMsKey;

        @Value("${webclient.readTimeoutMsKey}")
        private String readTimeoutMsKey;

        @Value("${webclient.username}")
        private String username;

        @Value("${webclient.password}")
        private String password;

    @   Value("${webclient.baseUrlKey}")
        private String baseUrlKey;

        private String appKeyHeader;
        private String tag;

        public String getAppKeyHeader() {
            return appKeyHeader;
        }

        public void setAppKeyHeader(String appKeyHeader) {
            this.appKeyHeader = appKeyHeader;
        }

        public String getTag() {
            return tag;
        }

        public void setTag(String tag) {
            this.tag = tag;
        }


        public void setConfigKey(String configKey) {
            log.debug("setConfigKey; {}", configKey);
            this.baseUrlKey = configKey + baseUrlKey;
            this.connectTimeoutMsKey = configKey + connectTimeoutMsKey;
            this.readTimeoutMsKey = configKey + readTimeoutMsKey;
            this.username = configKey + username;
            this.password = configKey + password;
        }

    private static String basicAuth(String username, String password) {
        String credentials = username + ":" + password;
        return "Basic " + java.util.Base64.getEncoder().encodeToString(credentials.getBytes());
    }

        private void setConnectTimeout() {

            HttpClient httpClient = HttpClient.create().option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 20000);

            client = WebClient.builder().clientConnector(new ReactorClientHttpConnector(httpClient));
            log.debug("setConnectTimeout; {} ms", 20000);
        }

        private void setReadTimeout() {

            // Create a new HttpClient with a read timeout handler
            HttpClient httpClient = HttpClient.create().doOnConnected(conn -> conn.addHandlerLast(new ReadTimeoutHandler(20000)));

            // Update the existing WebClient instance with the new HttpClient
            client = WebClient.builder().clientConnector(new ReactorClientHttpConnector(httpClient));

            log.debug("setReadTimeout; {} ms", 20000);
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
            if (baseUrlKey == null || baseUrlKey.isEmpty()) {
                log.error("getUrl; required property - {} null or empty ", baseUrlKey);
            }
            sb.append(baseUrlKey);
            for (String p : parts) {
                sb.append("/");
                sb.append(p);
            }
            log.debug("getUrl; {}", sb);
            return sb.toString();
        }

        public synchronized WebClient.Builder resource(String... parts) {
            setConnectTimeout();
            setReadTimeout();
            System.out.println("URL Called->"+ getUrl(parts));
            return WebClient.builder().baseUrl(getUrl(parts))
                    .defaultHeader(HttpHeaders.AUTHORIZATION, basicAuth(username, password));

        }

        public synchronized WebClient.Builder resource(int readTimeout, String... parts) {
            setConnectTimeout();
            if (readTimeout > 0) {
                HttpClient httpClient = HttpClient.create().doOnConnected(conn -> conn.addHandlerLast(new ReadTimeoutHandler(readTimeout)));

                // Update the existing WebClient instance with the new HttpClient
                client = WebClient.builder().clientConnector(new ReactorClientHttpConnector(httpClient));
            } else {
                setReadTimeout();
            }
            return WebClient.builder().baseUrl(getUrl(parts));
        }
    }