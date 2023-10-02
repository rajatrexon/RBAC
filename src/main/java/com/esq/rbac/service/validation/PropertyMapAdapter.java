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

package com.esq.rbac.service.validation;

import jakarta.xml.bind.annotation.adapters.XmlAdapter;

import java.util.HashMap;
import java.util.Map;

public class PropertyMapAdapter extends XmlAdapter<PropertyList, Map<String, String>> {

    @Override
    public PropertyList marshal(Map<String, String> value) {
        PropertyList ret = new PropertyList();
        for (String key : value.keySet()) {
            Property property = new Property();
            property.setKey(key);
            property.setValue(value.get(key));
            ret.getProperties().add(property);
        }
        return ret;
    }

    @Override
    public Map<String, String> unmarshal(PropertyList value) {
        Map<String, String> r = new HashMap<String, String>();
        for (Property c : value.getProperties()) {
            r.put(c.getKey(), c.getValue());
        }
        return r;
    }
}
