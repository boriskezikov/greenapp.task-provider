package com.task.provider.exception;

import reactor.core.publisher.Mono;

public enum AuthenticationError {

    UNAUTHORIZED(401, "Unauthorized"),
    DOCUMENT_CANNOT_BE_SHARED(403, "Document type cannot be shared"),
    FORBIDDEN(403, "Forbidden");

    public final int status;
    public final String description;

    AuthenticationError(int status, String description) {
        this.status = status;
        this.description = description;
    }

    public AuthenticationErrorException exception(Exception e, String body) {
        return new AuthenticationErrorException(e, this, body);
    }

    public AuthenticationErrorException exception(String body) {
        return new AuthenticationErrorException(this, body);
    }

    public AuthenticationErrorException exception() {
        return new AuthenticationErrorException(this);
    }

    public <T> Mono<T> exceptionMono(String body) {
        return Mono.error(exception(body));
    }

    public <T> Mono<T> exceptionMono() {
        return Mono.error(exception());
    }

    public static class AuthenticationErrorException extends HttpCodeException {

        public AuthenticationErrorException(Exception e, AuthenticationError error, String args) {
            super(e, error.status, error.description + ": ".concat(args));
        }

        public AuthenticationErrorException(AuthenticationError error) {
            super(error.status, error.description);
        }

        public AuthenticationErrorException(AuthenticationError error, String args) {
            super(error.status, error.description + ": ".concat(args));
        }
    }
}
