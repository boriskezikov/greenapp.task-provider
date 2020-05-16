package com.task.manager.exception;

import reactor.core.publisher.Mono;

public enum ValidationError {

    CANNOT_PARSE_VALUE(400, "Error parsing value"),
    NO_SUCH_PROPERTIES_ATTACHED(400, "No such properties attached"),
    NO_SUCH_PROPERTIES_EXIST(400, "No such properties exist"),
    UNCERTAIN_PROPERTY_VALUE_ERROR(400, "Uncertain property value"),
    CANNOT_PARSE_FIND_REQUEST(400, "Error parsing find request"),
    UNSUPPORTED_MEDIA_TYPE(415, "Unsupported media type"),
    CANNOT_PARSE_APPEND_REQUEST(400, "Error parsing append request");

    public final int status;
    public final String description;

    ValidationError(int status, String description) {
        this.status = status;
        this.description = description;
    }

    public ValidationErrorException exception(String body) {
        return new ValidationErrorException(this, body);
    }

    public ValidationErrorException exception() {
        return new ValidationErrorException(this);
    }

    public ValidationErrorException exception(Exception e, String body) {
        return new ValidationErrorException(e, this, body);
    }

    public <T> Mono<T> exceptionMono(String body) {
        return Mono.error(exception(body));
    }

    public <T> Mono<T> exceptionMono() {
        return Mono.error(exception());
    }

    public static class ValidationErrorException extends HttpCodeException {

        public ValidationErrorException(ValidationError error, String args) {
            super(error.status, error.description + ": ".concat(args));
        }

        public ValidationErrorException(ValidationError error) {
            super(error.status, error.description);
        }

        public ValidationErrorException(Exception e, ValidationError error, String args) {
            super(e, error.status, error.description + ": ".concat(args));
        }
    }
}
