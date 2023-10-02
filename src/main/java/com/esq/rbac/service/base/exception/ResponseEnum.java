package com.esq.rbac.service.base.exception;

public enum ResponseEnum {

    NO_CONTENT(204),
    NOT_MODIFIED(304),
    CLIENT_ERROR(400),
    NOT_FOUND(404),
    METHOD_NOT_ALLOWED(405),
    NOT_ACCEPTABLE(406),
    CONFLICT(409),
    PRECONDITION_FAILED(412),
    UNSUPPORTED_MEDIA_TYPE(415);


    private final int value;

    ResponseEnum(int value) {

        this.value= value;
    }

    public int getValue() {
        return value;
    }
}
