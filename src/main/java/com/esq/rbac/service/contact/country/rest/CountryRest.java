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
package com.esq.rbac.service.contact.country.rest;

import com.esq.rbac.service.contact.country.domain.Country;
import com.esq.rbac.service.contact.country.repository.CountryRepository;
import com.esq.rbac.service.contact.helpers.CountryContainer;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("/country")
public class CountryRest {

    private CountryRepository countryRepo;

    @Autowired
    public void setCountryRepo(CountryRepository countryRepo) {
        this.countryRepo = countryRepo;
    }

    @GetMapping(produces = {MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    public Response countryList() {
        log.debug("countryList()");

        CountryContainer countryContainer = new CountryContainer();
        List<Country> country = countryRepo.getAllCountries();
        if (log.isDebugEnabled()){
            log.debug("Retrieved: " + country.size() + " countries.");
        }
        countryContainer.setCountries(country);

        return Response.ok().entity(countryContainer).build();
    }
}
