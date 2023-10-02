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
package com.esq.rbac.service.util.json;

import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.databind.AnnotationIntrospector;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.introspect.JacksonAnnotationIntrospector;
import com.fasterxml.jackson.databind.util.StdDateFormat;


public class JacksonFactory {

    private static final ObjectMapper objectMapper;

    static {
        objectMapper = new ObjectMapper();
        objectMapper.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);
        objectMapper.setDateFormat(new StdDateFormat());
        objectMapper.setSerializationInclusion(Include.NON_NULL);

        AnnotationIntrospector introspector = new JacksonAnnotationIntrospector();
        objectMapper.getSerializationConfig().withAppendedAnnotationIntrospector(introspector);
        objectMapper.getDeserializationConfig().withAppendedAnnotationIntrospector(introspector);
    }

    public static ObjectMapper getObjectMapper() {
        return objectMapper;
    }
}
