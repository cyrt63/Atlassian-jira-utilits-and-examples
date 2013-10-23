package com.atlassian.pocketknife.api.rest;

import org.codehaus.jackson.annotate.JsonAutoDetect;

import java.util.Map;

@JsonAutoDetect
public class RestfulTableErrorResponse
{
    private String reasonKey;
    private Map<String, String> errors;

    public RestfulTableErrorResponse(String reasonKey, Map<String, String> errors)
    {
        this.reasonKey = reasonKey;
        this.errors = errors;
    }

    public Map<String, String> getErrors()
    {
        return errors;
    }

    public void setErrors(Map<String, String> errors)
    {
        this.errors = errors;
    }

    public String getReasonKey()
    {
        return reasonKey;
    }

    public void setReasonKey(String reasonKey)
    {
        this.reasonKey = reasonKey;
    }
}