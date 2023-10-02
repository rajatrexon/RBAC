///*
// * Copyright (c)2014 ESQ Management Solutions Pvt Ltd. All Rights Reserved.
// *
// * Permission to use, copy, modify, and distribute this software requires
// * a signed licensing agreement.
// *
// * IN NO EVENT SHALL ESQ BE LIABLE TO ANY PARTY FOR DIRECT, INDIRECT, SPECIAL,
// * INCIDENTAL, OR CONSEQUENTIAL DAMAGES, INCLUDING LOST PROFITS, ARISING OUT OF
// * THE USE OF THIS SOFTWARE AND ITS DOCUMENTATION, EVEN IF ESQ HAS BEEN ADVISED
// * OF THE POSSIBILITY OF SUCH DAMAGE. ESQ SPECIFICALLY DISCLAIMS ANY WARRANTIES,
// * INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND
// * FITNESS FOR A PARTICULAR PURPOSE.
// */
//package com.esq.rbac.web.rest;
//
//import com.sun.jersey.api.client.ClientResponse;
//import jakarta.servlet.http.HttpServletResponse;
//import jakarta.ws.rs.core.Response;
//import org.springframework.web.reactive.function.client.ClientResponse;
//
//import javax.servlet.http.HttpServletResponse;
//import javax.ws.rs.core.Response;
//import java.util.*;
//
//public class Mapper {
//
//    public static Response toResponse(ClientResponse r) {
//        // copy the status code
//        Response.ResponseBuilder rb = Response.status(r.statusCode().value());
//        // copy all the headers, except
//        Set<String> skipHeaders = new TreeSet<String>(Arrays.asList("Date"));
//        for (Map.Entry<String, List<String>> entry : r.headers().asHttpHeaders().entrySet()) {
//            if (skipHeaders.contains(entry.getKey())) {
//                continue;
//            }
//            for (String value : entry.getValue()) {
//                rb.header(entry.getKey(), value);
//            }
//        }
//        // copy the entity
//        rb.entity(r.);
//        // return the response
//        return rb.build();
//    }
//
//    public static Response toResponse(ClientResponse r, HttpServletResponse httpResponse) {
//        // copy the status code
//        Response.ResponseBuilder rb = Response.status(r.getStatus());
//        // copy all the headers, except
//        Set<String> skipHeaders = new TreeSet<String>(Arrays.asList("Date"));
//        for (Map.Entry<String, List<String>> entry : r.getHeaders().entrySet()) {
//        	if (skipHeaders.contains(entry.getKey()) || httpResponse.containsHeader(entry.getKey())) {
//				continue;
//			}
//            for (String value : entry.getValue()) {
//                rb.header(entry.getKey(), value);
//            }
//        }
//        // copy the entity
//        rb.entity(r.getEntityInputStream());
//        // return the response
//        return rb.build();
//    }
//}
