/*
 * Copyright (c)2014 ESQ Management Solutions Pvt Ltd. All Rights Reserved.
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
package com.esq.rbac.web.exception;

import java.util.Map;
import java.util.TreeMap;

public class ErrorInfoException extends RuntimeException {

    private final String code;
    private final Map<String, String> parameters;

    public ErrorInfoException(String code) {
        this(code, null, null, new TreeMap<String, String>());
    }

    public ErrorInfoException(String code, String message) {
        this(code, message, null, new TreeMap<String, String>());
    }

    public ErrorInfoException(String code, String message, Throwable cause) {
        this(code, message, cause, new TreeMap<String, String>());
    }

    public ErrorInfoException(String code, String message, Throwable cause, Map<String, String> parameters) {
        super(message, cause);
        this.code = code;
        this.parameters = parameters;
    }

    public String getCode() {
        return code;
    }

    public Map<String, String> getParameters() {
        return parameters;
    }
    
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("ErrorInfoException{code=").append(code);
        sb.append("; message=").append(getMessage());
        sb.append("; parameters=").append(parameters);
        sb.append("}");
        return sb.toString();
    }
}
