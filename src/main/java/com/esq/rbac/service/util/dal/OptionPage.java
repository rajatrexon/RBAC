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
import org.apache.commons.lang.math.NumberUtils;

public class OptionPage {

    private final int firstResult;
    private final int maxResults;

    public OptionPage(final int first, final int max) {
        firstResult = first;
        maxResults = max;
    }

    public OptionPage(MultivaluedMap<String, String> queryParams, final int defaultFirst, final int defaultMax) {
        firstResult = NumberUtils.toInt(queryParams.getFirst("first"), defaultFirst);
        maxResults = NumberUtils.toInt(queryParams.getFirst("max"), defaultMax);
    }

    public int getFirstResult() {
        return firstResult;
    }

    public int getMaxResults() {
        return maxResults;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("OptionPage{first=").append(firstResult);
        sb.append(", max=").append(maxResults).append("}");
        return sb.toString();
    }
}
