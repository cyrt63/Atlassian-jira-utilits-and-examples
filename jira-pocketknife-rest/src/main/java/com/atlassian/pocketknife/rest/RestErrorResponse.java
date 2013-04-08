package com.atlassian.pocketknife.rest;

import java.util.List;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import com.google.common.collect.Lists;

@XmlRootElement
public class RestErrorResponse
{
    @XmlElement
    private List<Error> errors;

    @XmlElement
    private final String reasonKey;

    @XmlElement
    private final String reasonCode;

    /**
     * REST errors must have a reasonKey and reasonCode
     * 
     * @param reasonKey
     * @param reasonCode
     */
    public RestErrorResponse(String reasonKey, String reasonCode)
    {
        this.reasonCode = reasonCode;
        this.reasonKey = reasonKey;
        errors = Lists.newArrayList();
    }

    /**
     * Add an error to the current list of errors
     * 
     * @param errorMessage
     */
    public void addError(String errorMessage)
    {
        errors.add(new Error(errorMessage));
    }

    /**
     * Add an error that maps to a field
     * 
     * @param errorMessage
     * @param field
     */
    public void addError(String errorMessage, String field)
    {
        errors.add(new Error(errorMessage, field));
    }

    @XmlRootElement
    private static class Error
    {
        @XmlElement
        private String errorMessage;

        @XmlElement
        private String field;

        public Error()
        {
        }

        public Error(String errorMessage)
        {
            this(errorMessage, null);
        }

        public Error(String errorMessage, String field)
        {
            this.errorMessage = errorMessage;
            this.field = field;
        }
    }
}
