package com.atlassian.pocketknife.rest;

import java.util.Map.Entry;
import java.util.concurrent.Callable;

import javax.ws.rs.core.CacheControl;
import javax.ws.rs.core.Response;

import org.apache.log4j.Logger;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.security.JiraAuthenticationContext;

/**
 * A base class to make REST resources more structured and supportable
 * 
 */
public class AbstractRestResource
{

    protected final Logger log;
    protected final JiraAuthenticationContext jiraAuthenticationContext;

    protected AbstractRestResource(Class className, JiraAuthenticationContext jiraAuthenticationContext)
    {
        this.log = Logger.getLogger(className);
        this.jiraAuthenticationContext = jiraAuthenticationContext;
    }

    /*******************************************************
     * REST Helper methods
     *******************************************************/

    /**
     * Invokes the callable and returns the response but with extra logging support.
     * 
     * @param responseCallable the callable to invoke
     * 
     * @return the Response
     */
    protected Response response(Callable<Response> responseCallable)
    {
        return new RestCall(log).response(responseCallable);
    }

    /**
     * @return true if the current user is anonymous
     */
    protected boolean isAnonymousUser()
    {
        return jiraAuthenticationContext.getLoggedInUser() == null;
    }

    /**
     * @return the currently logged in user or null if they are anonymous
     */
    protected User getLoggedInUser()
    {
        return jiraAuthenticationContext.getLoggedInUser();
    }

    /**
     * i18n's text
     */
    protected String getText(String key, Object... params)
    {
        return jiraAuthenticationContext.getI18nHelper().getText(key, params);
    }

    /*******************************************************
     * REST Success Responses
     *******************************************************/

    /**
     * @return a No Content response
     */
    protected Response noContent()
    {
        return Response.noContent().build();
    }

    /**
     * @param result the result object to send back
     * 
     * @return a 200 OK response
     */
    protected Response ok(Object result)
    {
        return Response.ok(result).cacheControl(never()).build();
    }

    /**
     * @return a 201 Created response
     */
    protected Response created(Object result)
    {
        return Response.status(Response.Status.CREATED).entity(result).cacheControl(never()).build();
    }

    /*******************************************************
     * REST Error Responses
     *******************************************************/

    /**
     * @return Returns a NOT_FOUND response
     */
    protected Response notFoundRequest(String reasonKey)
    {
        return createErrorResponse(Response.Status.NOT_FOUND, reasonKey);
    }

    /**
     * @return the NOT_FOUND response
     */
    protected Response notFoundRequest(String reasonKey, String errorMessage)
    {
        return createErrorResponse(Response.Status.NOT_FOUND, reasonKey, errorMessage);
    }

    /**
     * @return Returns a NOT_FOUND response with errors from JIRA
     */
    protected Response notFoundRequest(String reasonKey, com.atlassian.jira.util.ErrorCollection errorCollection)
    {
        return createErrorResponse(Response.Status.NOT_FOUND, reasonKey, errorCollection);
    }

    /**
     * @return Returns a FORBIDDEN response
     * 
     */
    protected Response forbiddenRequest(String reasonKey)
    {
        return createErrorResponse(Response.Status.FORBIDDEN, reasonKey);
    }

    /**
     * @return the FORBIDDEN response
     */
    protected Response forbiddenRequest(String reasonKey, String errorMessage)
    {
        return createErrorResponse(Response.Status.FORBIDDEN, reasonKey, errorMessage);
    }

    /**
     * @return the FORBIDDEN response with error messages from JIRA
     */
    protected Response forbiddenRequest(String reasonKey, com.atlassian.jira.util.ErrorCollection errorCollection)
    {
        return createErrorResponse(Response.Status.FORBIDDEN, reasonKey, errorCollection);
    }

    /**
     * @return an UNAUTHORIZED response
     */
    protected Response unauthorizedRequest(String reasonKey)
    {
        return createErrorResponse(Response.Status.UNAUTHORIZED, reasonKey);
    }

    /**
     * @return the UNAUTHORIZED response
     */
    protected Response unauthorizedRequest(String reasonKey, String errorMessage)
    {
        return createErrorResponse(Response.Status.UNAUTHORIZED, reasonKey, errorMessage);
    }

    /**
     * @return the UNAUTHORIZED response with error messages from JIRA
     */
    protected Response unauthorizedRequest(String reasonKey, com.atlassian.jira.util.ErrorCollection errorCollection)
    {
        return createErrorResponse(Response.Status.UNAUTHORIZED, reasonKey, errorCollection);
    }

    /**
     * @return the BAD_REQUEST response
     */
    protected Response badRequest(String reasonKey)
    {
        return createErrorResponse(Response.Status.BAD_REQUEST, reasonKey);
    }

    /**
     * @return the BAD_REQUEST response
     */
    protected Response badRequest(String reasonKey, String errorMessage)
    {
        return createErrorResponse(Response.Status.BAD_REQUEST, reasonKey, errorMessage);
    }

    /**
     * @return the BAD_REQUEST response with error messages from JIRA
     */
    protected Response badRequest(String reasonKey, com.atlassian.jira.util.ErrorCollection errorCollection)
    {
        return createErrorResponse(Response.Status.BAD_REQUEST, reasonKey, errorCollection);
    }

    /*******************************************************
     * Private methods
     *******************************************************/

    private CacheControl never()
    {
        CacheControl cacheNever = new CacheControl();
        cacheNever.setNoStore(true);
        cacheNever.setNoCache(true);
        return cacheNever;
    }

    private Response createErrorResponse(Response.Status status, String reasonKey)
    {
        RestErrorResponse error = new RestErrorResponse(reasonKey, String.valueOf(status.getStatusCode()));
        return Response.status(status).entity(error).build();
    }

    private Response createErrorResponse(Response.Status status, String reasonKey, String errorMessage)
    {
        RestErrorResponse error = new RestErrorResponse(reasonKey, String.valueOf(status.getStatusCode()));
        error.addError(errorMessage);
        return Response.status(status).entity(error).build();
    }

    private Response createErrorResponse(Response.Status status, String reasonKey, com.atlassian.jira.util.ErrorCollection errorCollection)
    {
        RestErrorResponse error = new RestErrorResponse(reasonKey, String.valueOf(status.getStatusCode()));
        for (String s : errorCollection.getErrorMessages())
        {
            error.addError(s);
        }
        for (Entry<String, String> entry : errorCollection.getErrors().entrySet())
        {
            error.addError(entry.getValue(), entry.getKey());
        }
        return Response.status(status).entity(error).build();
    }
}
