package com.atlassian.pocketknife.api.customfields.service;

public class CustomFieldException extends RuntimeException {
    public CustomFieldException() {
        super();
    }

    public CustomFieldException(String message, Throwable cause) {
        super(message, cause);
    }

    public CustomFieldException(String message) {
        super(message);
    }

    public CustomFieldException(Throwable cause) {
        super(cause);
    }
}
