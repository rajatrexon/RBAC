package com.esq.rbac.service.util;

import com.esq.rbac.service.user.vo.SSOLogoutData;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.NameValuePair;
import org.apache.commons.httpclient.methods.PostMethod;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.security.SecureRandom;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class LogoutRunnerUtil {

    private static final Logger log = LoggerFactory
            .getLogger(LogoutRunnerUtil.class);
    private static final ExecutorService logoutExecutorService = Executors
            .newCachedThreadPool();
    private final static Map<String, List<String>> logoutRunner = new ConcurrentHashMap<String, List<String>>();

    public static void executeLogoutCall(final SSOLogoutData ssoLogoutData,
                                         final String requestId, final int ssoLogoutConnTimeoutMs,
                                         final int ssoLogoutSoTimeoutMs, String replaceByApplication) {
        if (logoutRunner.get(requestId) == null) {
            synchronized (logoutRunner) {
                if(logoutRunner.get(requestId) == null){
                    logoutRunner.put(requestId, new LinkedList<String>());
                }
            }
        }
        logoutRunner.get(requestId).add(ssoLogoutData.getService());
        logoutExecutorService.execute(getNewSingleSignOutRunnable(
                ssoLogoutData.getService(), ssoLogoutData.getTicket(),
                requestId, ssoLogoutData.getUrls(), ssoLogoutConnTimeoutMs,
                ssoLogoutSoTimeoutMs,replaceByApplication));
    }

    public static boolean isLogoutRequestDone(String requestId) {
        if (logoutRunner.get(requestId) == null) {
            return true;
        }
        if (logoutRunner.get(requestId).isEmpty()) {
            logoutRunner.remove(requestId);

            return true;
        }
        return false;
    }

    public static int getPendingSize(String requestId) {
        if (logoutRunner.get(requestId) == null) {
            return 0;
        }
        if (logoutRunner.get(requestId).isEmpty()) {
            logoutRunner.remove(requestId);
            return 0;
        }
        return logoutRunner.get(requestId).size();
    }

    private static Runnable getNewSingleSignOutRunnable(
            final String serviceUrl, final String casTicket,
            final String requestId, final List<String> urls,
            final int ssoLogoutConnTimeoutMs, final int ssoLogoutSoTimeoutMs,
            String replaceByApplication) {
        return new Runnable() {

            public void run() {
                PostMethod post = null;
                try {
                    String ticket = casTicket;
                    log.debug("singleSignOut; urls={}; service={}", urls,
                            serviceUrl);

                    String xmlDoc = getSingleSignOutDocument(ticket);
                    log.debug("singleSignOut; xmlDoc={}", xmlDoc);
                    for (String url : urls) {
                        //LTWOSUPP-2214 Start

                        log.debug("Post url before {}",url);
                        Boolean shouldReplace = false;

                        String tofindHost = "";
                        String replaceWithHost = "";
                        if(replaceByApplication != null && !replaceByApplication.isEmpty())
                        {
                            String ctxtName ="";
                            String[] arrApplContext = replaceByApplication.split(",");
                            for (String appContext : arrApplContext) {
                                String[] arrAppDet = appContext.split("@");
                                ctxtName = arrAppDet[0];
                                if (url.contains(ctxtName)) {
                                    tofindHost = arrAppDet[1];
                                    replaceWithHost = arrAppDet[2];
                                    break;
                                }
                            }
                            if(tofindHost!= null && !tofindHost.isEmpty() && replaceWithHost !=null && !replaceWithHost.isEmpty()
                                    && !tofindHost.equalsIgnoreCase(replaceWithHost) && url.contains(tofindHost)) {
                                url = url.replace(tofindHost, replaceWithHost);
                                shouldReplace = true;
                            }
                        }

                        log.debug("Post url after {}",url);
                        //LTWOSUPP-2214 End
                        post = new PostMethod(url);
                        NameValuePair logoutRequest = new NameValuePair(
                                "logoutRequest", xmlDoc);
                        post.setRequestBody(new NameValuePair[] { logoutRequest });
                        HttpClient httpclient = new HttpClient();
                        httpclient.getHttpConnectionManager().getParams()
                                .setConnectionTimeout(ssoLogoutConnTimeoutMs);
                        httpclient.getHttpConnectionManager().getParams()
                                .setSoTimeout(ssoLogoutSoTimeoutMs);
                        // fire and forget
                        try {
                            int statusCode = httpclient.executeMethod(post);
                            String responseBody = post
                                    .getResponseBodyAsString();
                            //LTWOSUPP-2214 Start
                            if(shouldReplace) {
                                log.debug("Post url before revert {}",url);
                                if(url.contains(replaceWithHost)) {
                                    url = url.replace(replaceWithHost, tofindHost);
                                    shouldReplace = false;
                                    log.debug("Post url after revert {}",url);
                                }
                            }
                            //LTWOSUPP-2214 End
                            log.info(
                                    "singleSignOut; serviceTicket={}; service={}; url={}; statusCode={}; responseBody={}",
                                    ticket, serviceUrl, url, statusCode,
                                    responseBody);
                        } catch (Exception e) {
                            // log single sign out error
                            log.info(
                                    "singleSignOut; serviceTicket={}; service={}; url={}; exception={};",
                                    ticket, serviceUrl, url, e);
                        }
                    }
                } catch (Exception e) {
                    // log and ignore
                    log.error(
                            "singleSignOut; service={}; urls={}; exception={};",
                            serviceUrl, urls, e);
                } finally {
                    if (post != null) {
                        post.releaseConnection();
                    }
                }
                logoutRunner.get(requestId).remove(serviceUrl);
            }
        };
    }

    /**
     * Create XML document to be used for single sign out requests.
     *
     * See: http://www.jasig.org/cas/protocol See: http://saml.xml.org/
     *
     * @param serviceTicket
     * @return
     */
    private static String getSingleSignOutDocument(String serviceTicket) {
        String randomId = generateId("ID", 20);
        String currentDateTime = new Date().toString();

        StringBuilder sb = new StringBuilder();
        sb.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
        sb.append("<samlp:LogoutRequest");
        sb.append(" xmlns:samlp=\"urn:oasis:names:tc:SAML:2.0:protocol\"");
        sb.append(" xmlns:saml=\"urn:oasis:names:tc:SAML:2.0:assertion\"");
        sb.append(" ID=\"");
        sb.append(randomId);
        sb.append("\"");
        sb.append(" Version=\"2.0\"");
        sb.append(" IssueInstant=\"");
        sb.append(currentDateTime);
        sb.append("\">");
        sb.append("<saml:NameID>@NOT_USED@</saml:NameID>");
        sb.append("<samlp:SessionIndex>");
        sb.append(serviceTicket);
        sb.append("</samlp:SessionIndex>");
        sb.append("</samlp:LogoutRequest>");
        return sb.toString();
    }

    private static String generateId(String prefix, int length) {
        final char[] AVAILABLE_CHARS = "0123456789abcdefghijklmnoqrstyvwxyzABCDEFGHIJKLMNOQRSTYVWXYZ"
                .toCharArray();

        SecureRandom random = new SecureRandom();
        StringBuilder sb = new StringBuilder();
        sb.append(prefix);
        for (int i = 0; i < length; i++) {
            sb.append(AVAILABLE_CHARS[random.nextInt(AVAILABLE_CHARS.length)]);
        }
        return sb.toString();
    }
}
