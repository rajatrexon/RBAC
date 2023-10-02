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
package com.esq.rbac.service.contact.location.rest;
import com.esq.rbac.service.base.rest.BaseRest;
import com.esq.rbac.service.contact.location.repository.LocationRepository;
import com.esq.rbac.service.contact.location.domain.Location;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.jmx.export.annotation.ManagedResource;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@RestController
@RequestMapping("/location")
//@ManagedResource(objectName = "com.esq.dispatcher.contacts:type=REST,name=LocationRest")
public class LocationRest extends BaseRest<Location> {

    private static final Set<String> EMPTY_SET = new HashSet<>();

    @Autowired
    public LocationRest(LocationRepository locationRepository) {
        super(Location.class, locationRepository);
    }

    @Override
    protected Set<String> getFilterColumns() {
        return EMPTY_SET;
    }

    @Override
    protected Set<String> getSearchColumns() {
        return EMPTY_SET;
    }

    @Override
    protected Set<String> getOrderColumns() {
        return EMPTY_SET;
    }

    @GetMapping(produces = {MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    public ResponseEntity<List<Location>> getLocations(@QueryParam("q") String q) {
        List<Location> locations = ((LocationRepository) repository).getLocations(q);
        return ResponseEntity.ok(locations);
    }
}

