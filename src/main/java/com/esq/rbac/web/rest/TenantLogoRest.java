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
package com.esq.rbac.web.rest;
import com.esq.rbac.web.client.RestClient;
import com.esq.rbac.web.client.UserDetailsService;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.ws.rs.PathParam;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.reactive.function.client.ClientResponse;

@RestController
@RequestMapping(TenantLogoRest.RESOURCE_PATH)
public class TenantLogoRest {

    private static final Logger log = LoggerFactory.getLogger(TenantLogoRest.class);
    public static final String RESOURCE_PATH = "tenantLogo";
    private RestClient restClient;
    private UserDetailsService userDetailsService;

    @Autowired
    public void setRestClient(RestClient restClient) {
        log.debug("setRestClient");
        this.restClient = restClient;
    }

    @Autowired
    public void setUserDetailsService(UserDetailsService userDetailsService) {
        log.debug("setUserDetailsService; {}", userDetailsService);
        this.userDetailsService = userDetailsService;
    }

    @PostMapping(value = "/{tenantId}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ClientResponse> upload(@PathParam("tenantId") long tenantId, @RequestPart("File") MultipartFile multiPart, HttpServletResponse httpResponse) throws Exception {

        log.trace("upload; tenantId={}", tenantId);
        userDetailsService.verifyPermission("Tenant.Update");

//        BodyPart bodyPart = multiPart.getBodyParts().get(0);
//        MediaType contentType = bodyPart.getMediaType();
//        String fileName = bodyPart.getContentDisposition().getFileName();
//        FormDataMultiPart form = new FormDataMultiPart();
//        form.bodyPart(bodyPart);

        String contentType = multiPart.getContentType();
        String fileName = multiPart.getOriginalFilename();
        log.trace("upload; contentType={}; fileName={}", contentType, fileName);

        ClientResponse clientResponse = restClient
                .resource(RESOURCE_PATH, Long.toString(tenantId))
                .build().post()
                .header("userId", String.valueOf(100))
                .contentType(MediaType.MULTIPART_FORM_DATA)
                .header("userId", String.valueOf(userDetailsService.getCurrentUserDetails().getUserInfo().getUserId()))                .bodyValue(multiPart).retrieve()
                .bodyToMono(ClientResponse.class).block();
        return ResponseEntity.status(clientResponse.statusCode().value()).build();
    }

    @GetMapping("/{tenantId}")
    public ResponseEntity<ClientResponse> download(@PathParam("tenantId") long tenantId, HttpServletResponse httpResponse) {
        log.trace("download; tenantId={}", tenantId);
        userDetailsService.verifyPermission("Tenant.View");

        ClientResponse clientResponse = restClient
                .resource(RESOURCE_PATH, Long.toString(tenantId))
                .build().get()
                .retrieve().bodyToMono(ClientResponse.class).block();
        return ResponseEntity.status(clientResponse.statusCode().value()).build();
    }
}
