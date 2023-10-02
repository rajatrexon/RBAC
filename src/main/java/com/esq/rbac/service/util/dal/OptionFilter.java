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
package com.esq.rbac.service.util.dal;

import jakarta.ws.rs.core.MultivaluedMap;

import java.util.Map;
import java.util.TreeMap;

public class OptionFilter {

    private static final String DEFAULT_PREFIX = "_";
    private final Map<String, String> filters;

    public OptionFilter() {
        this(null, DEFAULT_PREFIX);
    }

    public OptionFilter(MultivaluedMap<String, String> queryParams) {
        this(queryParams, DEFAULT_PREFIX);
    }

    public OptionFilter(MultivaluedMap<String, String> queryParams, String prefix) {
        filters = new TreeMap<String, String>();
        if (queryParams != null) {
            for (String key : queryParams.keySet()) {
                String value = queryParams.getFirst(key);
                String property = key;
                if (!prefix.isEmpty() && key.length() > prefix.length() && key.startsWith(prefix)) {
                    property = key.substring(prefix.length());
                }
                if (!property.isEmpty() && value != null && !value.isEmpty()) {
                    filters.put(property, value);
                }
            }
        }
    }

    public Map<String, String> getFilters() {
        return filters;
    }

    public void addFilter(String name, String value) {
        filters.put(name, value);
    }

    public String getFilter(String name) {
        return filters.get(name);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("OptionFilter").append(filters);
        return sb.toString();
    }
}
