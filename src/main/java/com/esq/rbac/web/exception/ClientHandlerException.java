package com.esq.rbac.web.exception;

public class ClientHandlerException extends RuntimeException {
        public ClientHandlerException() {
            super();
        }

        public ClientHandlerException(String message) {
            super(message);
        }

        public ClientHandlerException(String message, Throwable cause) {
            super(message, cause);
        }

        public ClientHandlerException(Throwable cause) {
            super(cause);
        }
    }
