package com.task.manager.exception;

import reactor.core.publisher.Mono;

public enum ApplicationError {

    DOCUMENT_NOT_FOUND_BY_ID(400, "Document not found error"),
    PROPERTY_DOESNT_EXIST(400, "Property doesn't exist"),
    DOCUMENT_TYPE_DOESNT_EXIST(400, "Document type doesn't exist"),
    DOCUMENT_CONTENT_DOESNT_EXIST(400, "Document content doesn't exist"),
    BAD_REQUEST(400, "Bad request"),
    LINK_DOESNT_EXIST(400, "Requested link to document doesn't exist"),
    INTERNAL_SERVER_ERROR(500, "Internal server error");

    public final int status;
    public final String description;

    ApplicationError(int status, String description) {
        this.status = status;
        this.description = description;
    }

    public ApplicationErrorException exception(String body) {
        return new ApplicationErrorException(this, body);
    }

    public ApplicationErrorException exception(Exception e, String body) {
        return new ApplicationErrorException(e, this, body);
    }

    public ApplicationErrorException exception(Exception e) {
        return new ApplicationErrorException(e, this);
    }

    public ApplicationErrorException exception() {
        return new ApplicationErrorException(this);
    }

    public <T> Mono<T> exceptionMono(String body) {
        return Mono.error(exception(body));
    }

    public <T> Mono<T> exceptionMono(Exception e) {
        return Mono.error(exception(e));
    }

    public <T> Mono<T> exceptionMono() {
        return Mono.error(exception());
    }

    public static class ApplicationErrorException extends HttpCodeException {

        public ApplicationErrorException(ApplicationError error, String args) {
            super(error.status, error.description + ": ".concat(args));
        }

        public ApplicationErrorException(Exception e, ApplicationError error, String args) {
            super(e, error.status, error.description + ": ".concat(args));
        }

        public ApplicationErrorException(Exception e, ApplicationError error) {
            super(e, error.status, error.description);
        }

        public ApplicationErrorException(ApplicationError error) {
            super(error.status, error.description);
        }
    }
}
