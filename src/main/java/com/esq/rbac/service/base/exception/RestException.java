package com.esq.rbac.service.base.exception;

import com.esq.rbac.service.base.error.RestError;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.Response;

import java.util.Arrays;
import java.util.List;

public class RestException extends WebApplicationException {


    //Todo  Responses.CONFLICT jersey
    public RestException(RestError response) {
        //this(Responses.CONFLICT, response);
        this(ResponseEnum.CONFLICT.getValue(), response);
    }

    public RestException(int statusCode, RestError response) {
        super(Response.status(statusCode).entity(response).build());

        //super(Response.status(statusCode).entity(response).type(MediaType.APPLICATION_JSON).build());
    }

    public RestException(String messageCode, String message, String... parameters) {
        //this(Responses.CONFLICT, buildResponse(messageCode, message, parameters));
        this(ResponseEnum.CONFLICT.getValue(), buildResponse(messageCode, message, parameters));
    }

    public RestException(String messageCode, String message, List<String> parameters) {
        //this(Responses.CONFLICT, buildResponse(messageCode, message, parameters));
        this(ResponseEnum.CONFLICT.getValue(), buildResponse(messageCode, message, parameters));
    }

    public RestException(int statusCode, String messageCode, String message, String... parameters) {
        this(statusCode, buildResponse(messageCode, message, parameters));
    }

    public RestException(int statusCode, String messageCode, String message, List<String> parameters) {
        this(statusCode, buildResponse(messageCode, message, parameters));
    }

    private static RestError buildResponse(String messageCode, String message, List<String> parameters) {
        RestError response = new RestError();
        response.setMessageCode(messageCode);
        response.setMessage(message);
        response.getParamaters().addAll(parameters);
        return response;
    }

    private static RestError buildResponse(String messageCode, String message, String[] parameters) {
        return buildResponse(messageCode, message, Arrays.asList(parameters));
    }
}
