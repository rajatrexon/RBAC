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

import java.util.LinkedList;
import java.util.List;

public class OptionSort {

    private final List<String> sortProperties;

    public OptionSort(final List<String> properties) {
        sortProperties = properties;
    }

    public OptionSort(MultivaluedMap<String, String> queryParams) {
        sortProperties = new LinkedList<String>();
        List<String> tmpList = queryParams.get("sort");
        if (tmpList != null) {
            for (String item : tmpList) {
                String[] entries = item.split(",");
                for (String entry : entries) {
                    sortProperties.add(entry.trim());
                }
            }
        }

        String asc = queryParams.getFirst("asc");
        if (asc != null && !asc.isEmpty()) {
            sortProperties.add(asc);
        }

        String desc = queryParams.getFirst("desc");
        if (desc != null && desc.isEmpty() == false) {
            sortProperties.add("-" + desc);
        }
    }

    public List<String> getSortProperties() {
        return sortProperties;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("OptionSort").append(sortProperties);
        return sb.toString();
    }
}
