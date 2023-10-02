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
import com.esq.rbac.web.exception.ErrorInfoException;
import com.esq.rbac.web.util.DeploymentUtil;
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
import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;

@RestController
@RequestMapping(UserPhotoRest.RESOURCE_PATH)
public class UserPhotoRest {

    private static final Logger log = LoggerFactory.getLogger(UserPhotoRest.class);
    public static final String RESOURCE_PATH = "userPhoto";
    private RestClient restClient;
    private UserDetailsService userDetailsService;
    private DeploymentUtil deploymentUtil;

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
    
    @Autowired
    public void setDeploymentUtil(DeploymentUtil deploymentUtil) {
        log.trace("setDeploymentUtil; {};", deploymentUtil);
        this.deploymentUtil = deploymentUtil;
    }

    @PostMapping(value = "/{userId}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ClientResponse> upload(@PathVariable("userId") Integer userId, @RequestPart("File") MultipartFile multiPart, HttpServletResponse httpResponse) throws Exception {

        log.trace("upload; userId={}", userId);
        userDetailsService.verifyPermission("User.Update");
        String contentType = multiPart.getContentType();
        String fileName = multiPart.getOriginalFilename();
        log.trace("upload; contentType={}; fileName={}", contentType, fileName);

        if(deploymentUtil.isValidateUserImage()){
	        BufferedImage buffImage = ImageIO.read(new ByteArrayInputStream(multiPart.getBytes()));
	        if(buffImage==null){
	        	throw new ErrorInfoException("invalidImage", "invalidImage");
	        }
        }
        ClientResponse clientResponse = restClient
        		.resource(RESOURCE_PATH, Integer.toString(userId))
                .build().post()
                .header("userId", String.valueOf(100))
                .contentType(MediaType.MULTIPART_FORM_DATA)
                .header("userId", String.valueOf(userDetailsService.getCurrentUserDetails().getUserInfo().getUserId())).bodyValue(multiPart).retrieve()
                .bodyToMono(ClientResponse.class).block();

        return ResponseEntity.status(clientResponse.statusCode().value()).build();
    }

    @GetMapping("/{userId}")
    public ResponseEntity<ClientResponse> download(@PathVariable("userId") Integer userId, HttpServletResponse httpResponse) {
        log.trace("download; userId={}", userId);
        userDetailsService.verifyPermission("User.View");

        ClientResponse clientResponse = restClient
                .resource(RESOURCE_PATH, Integer.toString(userId))
                .build().get()
                .retrieve().bodyToMono(ClientResponse.class).block();
        return ResponseEntity.status(clientResponse.statusCode().value()).build();
    }
}
