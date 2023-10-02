/*
 * Copyright ©2012 ESQ Management Solutions Pvt Ltd. All Rights Reserved.
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
package com.esq.rbac.service.timezonemaster.rest;

import com.esq.rbac.service.timezonemaster.domain.TimeZoneMaster;
import com.esq.rbac.service.timezonemaster.service.TimeZoneMasterDal;
import jakarta.inject.Singleton;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;

@RestController
@Singleton
@RequestMapping("/timeZone")
public class TimeZoneRest {

    private static final Logger log = LoggerFactory.getLogger(TimeZoneRest.class);
    
    TimeZoneMasterDal timeZoneMasterDal;
    
    @Autowired
    private void setTimeZoneMaster(TimeZoneMasterDal timeZoneMasterDal) {
    	this.timeZoneMasterDal = timeZoneMasterDal;
    }
    
    
//    @GET
//    @Produces(MediaType.APPLICATION_JSON)
    @GetMapping(produces = MediaType.APPLICATION_JSON)
    public Response getAvailableTimeZones() {
        try {
      	List<TimeZoneMaster> res = timeZoneMasterDal.getTimeZones();
      	List<String> timeZoneNames = new ArrayList<String>();
      	for(TimeZoneMaster tz: res) {
      		timeZoneNames.add(tz.getTimezoneValue());
      	}
      	String[] availableTimeZones = (timeZoneNames).toArray(new String[0]);
          return Response.ok().entity(availableTimeZones).build();
      } catch (Exception e) {
          log.error("getAvailableTimeZones; exception {}", e);            
      }
      return Response.serverError().build();
  }
}
