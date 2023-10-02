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
import com.esq.rbac.web.vo.TimeZoneMaster;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping(TimeZoneRest.RESOURCE_PATH)
public class TimeZoneRest {

    //private static final Logger log = LoggerFactory.getLogger(TimeZoneRest.class);
    private RestClient restClient;
    public static final String RESOURCE_PATH = "timeZones";
    
    @Autowired
    public void setRestClient(RestClient restClient) {
        log.debug("setRestClient");
        this.restClient = restClient;
    }

    @GetMapping(path = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<TimeZoneMaster[]> getAvailableTimeZones() {
        try {
      	 restClient.resource("culture", "timezones")
                .build().get().accept(MediaType.APPLICATION_JSON)
                .retrieve().bodyToMono(TimeZoneMaster[].class)
                .map(timeZoneMasters -> ResponseEntity.ok(timeZoneMasters));
          
      } catch (Exception e) {
          log.error("getAvailableTimeZones; exception={}", e);
      }
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
  }
}
