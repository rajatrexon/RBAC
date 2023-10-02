/*
 * Copyright (C)2013 ESQ Management Solutions Pvt Ltd. All Rights Reserved.
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


import com.esq.rbac.web.security.util.VariableInfoDeserializer;
import com.esq.rbac.web.security.util.XSSSafeStringDeserializer;
import com.esq.rbac.web.vo.VariableInfo;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.Version;
import com.fasterxml.jackson.databind.AnnotationIntrospector;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.introspect.JacksonAnnotationIntrospector;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.databind.util.StdDateFormat;
import jakarta.ws.rs.ext.ContextResolver;
import jakarta.ws.rs.ext.Provider;


@Provider
public class JacksonObjectMapperProvider implements ContextResolver<ObjectMapper> {

    private final static ObjectMapper objectMapper;

    static {
        objectMapper = new ObjectMapper();
        objectMapper.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);
        objectMapper.setDateFormat(new StdDateFormat());
        objectMapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);

        AnnotationIntrospector introspector = new JacksonAnnotationIntrospector();
        objectMapper.getSerializationConfig().withAppendedAnnotationIntrospector(introspector);
        objectMapper.getDeserializationConfig().withAppendedAnnotationIntrospector(introspector);

        SimpleModule module = new SimpleModule("HTML XSS DeSerializer", new Version(1, 0, 0, "FINAL", null, null));
        module.addDeserializer(String.class, new XSSSafeStringDeserializer());
        module.addDeserializer(VariableInfo.class, new VariableInfoDeserializer());
        objectMapper.registerModule(module);
    }

    @Override
    public ObjectMapper getContext(Class<?> type) {
        return objectMapper;
    }
}
