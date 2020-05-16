package com.task.manager.exception;

import io.netty.handler.codec.http.HttpResponseStatus;

public class HttpCodeException extends RuntimeException {

    public final HttpResponseStatus status;
    public final String body;

    public HttpCodeException(int status, String description) {
        this(HttpResponseStatus.valueOf(status), description);
    }

    public HttpCodeException(int status, String description, String body) {
        this(HttpResponseStatus.valueOf(status, description), body);
    }

    public HttpCodeException(HttpResponseStatus status) {
        this(status, "");
    }

    public HttpCodeException(HttpResponseStatus status, String body) {
        super(body);
        this.status = status;
        this.body = body;
    }

    public HttpCodeException(Throwable cause, HttpResponseStatus status, String body) {
        super(cause);
        this.status = status;
        this.body = body;
    }

    public HttpCodeException(Throwable cause, int status, String body) {
        super(cause);
        this.status = HttpResponseStatus.valueOf(status);
        this.body = body;
    }

    public String apiResponse() {
        return "{\n" +
            "   \"errorMessage\": \"" + body + "\"\n" +
            '}';
    }

    @Override
    public String toString() {
        return "'" + body + "'";
    }
}
