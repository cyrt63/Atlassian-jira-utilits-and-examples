package com.atlassian.pocketknife.api.rest;

import com.atlassian.annotations.tenancy.TenancyScope;
import com.atlassian.annotations.tenancy.TenantAware;
import org.codehaus.jackson.annotate.JsonAutoDetect;

import java.util.Map;

@JsonAutoDetect
public class RestfulTableErrorResponse {
    private String reasonKey;

    @TenantAware(value = TenancyScope.TENANTED)
    private Map<String, String> errors;

    public RestfulTableErrorResponse(String reasonKey, Map<String, String> errors) {
        this.reasonKey = reasonKey;
        this.errors = errors;
    }

    public Map<String, String> getErrors() {
        return errors;
    }

    public void setErrors(Map<String, String> errors) {
        this.errors = errors;
    }

    public String getReasonKey() {
        return reasonKey;
    }

    public void setReasonKey(String reasonKey) {
        this.reasonKey = reasonKey;
    }
}