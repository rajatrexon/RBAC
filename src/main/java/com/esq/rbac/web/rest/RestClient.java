///*
// * Copyright (c)2013 ESQ Management Solutions Pvt Ltd. All Rights Reserved.
// *
// * Permission to use, copy, modify, and distribute this software requires
// * a signed licensing agreement.
// *
// * IN NO EVENT SHALL ESQ BE LIABLE TO ANY PARTY FOR DIRECT, INDIRECT, SPECIAL,
// * INCIDENTAL, OR CONSEQUENTIAL DAMAGES, INCLUDING LOST PROFITS, ARISING OUT OF
// * THE USE OF THIS SOFTWARE AND ITS DOCUMENTATION, EVEN IF ESQ HAS BEEN ADVISED
// * OF THE POSSIBILITY OF SUCH DAMAGE. ESQ SPECIFICALLY DISCLAIMS ANY WARRANTIES,
// * INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND
// * FITNESS FOR A PARTICULAR PURPOSE.
// */
//package com.esq.rbac.web.rest;
//
//import io.netty.channel.ChannelOption;
//import io.netty.handler.timeout.ReadTimeoutHandler;
//import org.apache.commons.configuration.Configuration;
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;
//import org.springframework.http.client.reactive.ReactorClientHttpConnector;
//import org.springframework.stereotype.Component;
//import org.springframework.web.reactive.function.client.WebClient;
//import reactor.netty.http.client.HttpClient;
//import reactor.netty.resources.ConnectionProvider;
//
//import java.util.Arrays;
//
//@Component
//public class RestClient {
//        private static final Logger log = LoggerFactory.getLogger(RestClient.class);
//
//        // private final Client client;
//        private final int defaultConnectTimeoutMs = 5000;
//        private final int defaultReadTimeoutMs = 5000;
//        WebClient.Builder client;
//        Configuration configuration;
//        WebClient.Builder authFilter;
//        private String connectTimeoutMsKey = ".20000";
//        private String readTimeoutMsKey = ".20000";
//        private String usernameKey = ".username";
//        private String passwordKey = ".password";
//        private String baseUrlKey = ".http://localhost:8002/rbac/restapp";
//        private String appKeyHeader;
//        private String tag;
//
//        public String getAppKeyHeader() {
//            return appKeyHeader;
//        }
//
//        public void setAppKeyHeader(String appKeyHeader) {
//            this.appKeyHeader = appKeyHeader;
//        }
//
//        public String getTag() {
//            return tag;
//        }
//
//        public void setTag(String tag) {
//            this.tag = tag;
//        }
//
//        public void setConfigKey(String configKey) {
//            log.debug("setConfigKey; {}", configKey);
//            this.baseUrlKey = configKey + ".http://localhost:8002/rbac/restapp";
//            this.connectTimeoutMsKey = configKey + ".20000";
//            this.readTimeoutMsKey = configKey + ".20000";
//            this.usernameKey = configKey + ".username";
//            this.passwordKey = configKey + ".password";
//        }
//
//        private void setConnectTimeout() {
//            //    Integer connectTimeout = configuration.getInteger(connectTimeoutMsKey, defaultConnectTimeoutMs);
//            HttpClient httpClient = HttpClient.create().option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 20000);
//            //   client.setConnectTimeout(connectTimeout);
//            client = WebClient.builder().clientConnector(new ReactorClientHttpConnector(httpClient));
//            log.debug("setConnectTimeout; {} ms", 20000);
//        }
//
//        private void setReadTimeout() {
//            //      Integer readTimeout = configuration.getInteger(readTimeoutMsKey, defaultReadTimeoutMs);
//
//            // Create a new HttpClient with a read timeout handler
//            HttpClient httpClient = HttpClient.create().doOnConnected(conn -> conn.addHandlerLast(new ReadTimeoutHandler(20000)));
//
//            // Update the existing WebClient instance with the new HttpClient
//            client = WebClient.builder().clientConnector(new ReactorClientHttpConnector(httpClient));
//
//            log.debug("setReadTimeout; {} ms", 20000);
//        }
//        public void setMaxConnectionsPerHost(int maxConnectionsPerHost) {
//            HttpClient httpClient = HttpClient.create(ConnectionProvider.builder("customConnectionProvider").maxConnections(maxConnectionsPerHost).build());
//            client = WebClient.builder().clientConnector(new ReactorClientHttpConnector(httpClient));
//        }
//
//        public void setMaxTotalConnections(int maxTotalConnections) {
//            HttpClient httpClient = HttpClient.create(ConnectionProvider.builder("customConnectionProvider").maxConnections(maxTotalConnections).build());
//            client = WebClient.builder().clientConnector(new ReactorClientHttpConnector(httpClient));
//        }
//
//        private String getUrl(String... parts) {
//            StringBuilder sb = new StringBuilder();
//            //   String baseUrl = configuration.getString(baseUrlKey);
//            String baseUrl = "http://localhost:8002/rbac/restapp";
//            if (baseUrl == null || baseUrl.isEmpty()) {
//                log.error("getUrl; required property - {} null or empty ", baseUrlKey);
//            }
//            sb.append(baseUrl);
//            for (String p : parts) {
//                sb.append("/");
//                sb.append(p);
//            }
//            log.debug("getUrl; {}", sb);
//            return sb.toString();
//        }
//
//        public synchronized WebClient.Builder resource(String... parts) {
//            setConnectTimeout();
//            setReadTimeout();
//            return WebClient.builder().baseUrl(Arrays.toString(parts));
//
//        }
//
//        public synchronized WebClient.Builder resource(int readTimeout, String... parts) {
//            setConnectTimeout();
//            if (readTimeout > 0) {
//                HttpClient httpClient = HttpClient.create().doOnConnected(conn -> conn.addHandlerLast(new ReadTimeoutHandler(readTimeout)));
//
//                // Update the existing WebClient instance with the new HttpClient
//                client = WebClient.builder().clientConnector(new ReactorClientHttpConnector(httpClient));
//            } else {
//                setReadTimeout();
//            }
//            return WebClient.builder().baseUrl(getUrl(parts));
//        }
//    }