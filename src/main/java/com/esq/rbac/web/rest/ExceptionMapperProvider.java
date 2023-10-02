/*
 * Copyright (c)2013,2014 ESQ Management Solutions Pvt Ltd. All Rights Reserved.
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

import com.esq.rbac.web.exception.ErrorInfo;
import com.esq.rbac.web.exception.ErrorInfoException;
import com.fasterxml.jackson.databind.JsonMappingException;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Provider
public class ExceptionMapperProvider implements ExceptionMapper<Exception> {

    private static final Logger log = LoggerFactory.getLogger(ExceptionMapperProvider.class);

    @Override
    public Response toResponse(Exception e) {
        if (e instanceof ErrorInfoException) {
            ErrorInfo errorInfo = new ErrorInfo((ErrorInfoException) e);
            return Response.status(Response.Status.CONFLICT).entity(errorInfo).type(MediaType.APPLICATION_JSON).build();
        }
// Todo discuss       if (e instanceof UniformInterfaceException) {
//            log.debug("toResponse; {}", e);
//            UniformInterfaceException uie = (UniformInterfaceException) e;
//            return Mapper.toResponse(uie.getResponse());
//        }

        //log.error("toResponse; exception={}", e.toString());
        if (e instanceof JsonMappingException) {
            if (e.getMessage() != null && e.getMessage().toLowerCase().contains(ErrorInfo.XSS_ERROR_MESSAGE.toLowerCase())) {
                JsonMappingException jme = (JsonMappingException) e;
                String fieldName = (jme.getPath() != null && !jme.getPath().isEmpty()) ? jme.getPath().get(0).getFieldName() : null;
                log.error("toResponse; JsonMappingException; fieldName={}; exception={}", fieldName, e);
                ErrorInfoException errorInfoException = new ErrorInfoException(ErrorInfo.XSS_ERROR_CODE, ErrorInfo.XSS_ERROR_MESSAGE);
                errorInfoException.getParameters().put("fieldName", fieldName);
                ErrorInfo errorInfo = new ErrorInfo(errorInfoException);
                return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(errorInfo).type(MediaType.APPLICATION_JSON).build();
            }
        }
        log.error("toResponse; exception={}", e);

        ErrorInfo errorInfo = new ErrorInfo(ErrorInfo.SERVER_ERROR);
        errorInfo.setExceptionMessage("Invalid Input");
        return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(errorInfo).type(MediaType.APPLICATION_JSON).build();
    }
}
