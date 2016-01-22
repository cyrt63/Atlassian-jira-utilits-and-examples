package com.atlassian.pocketknife.api.rest;

import com.google.common.collect.Lists;
import org.codehaus.jackson.annotate.JsonProperty;

import java.util.List;

public class RestErrorResponse {
    @JsonProperty
    private List<Error> errors;

    @JsonProperty
    private final String reasonKey;

    @JsonProperty
    private final String reasonCode;

    public RestErrorResponse(String reasonKey, String reasonCode) {
        this.reasonCode = reasonCode;
        this.reasonKey = reasonKey;
        errors = Lists.newArrayList();
    }

    public void addError(String errorMessage) {
        errors.add(new Error(errorMessage));
    }

    public void addError(String errorMessage, String field) {
        errors.add(new Error(errorMessage, field));
    }

    private static class Error {
        @JsonProperty
        private String errorMessage;

        @JsonProperty
        private String field;

        public Error() {
        }

        public Error(String errorMessage) {
            this(errorMessage, null);
        }

        public Error(String errorMessage, String field) {
            this.errorMessage = errorMessage;
            this.field = field;
        }
    }
}
