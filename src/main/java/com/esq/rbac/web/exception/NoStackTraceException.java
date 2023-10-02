package com.esq.rbac.web.exception;

public class NoStackTraceException extends RuntimeException {

	private static final long serialVersionUID = 6779639591582527725L;

	public NoStackTraceException(String message, Object cause, boolean enableSuppression, boolean writableStackTrace) {
		super(message, (Throwable) cause, enableSuppression, writableStackTrace);
	}

}
