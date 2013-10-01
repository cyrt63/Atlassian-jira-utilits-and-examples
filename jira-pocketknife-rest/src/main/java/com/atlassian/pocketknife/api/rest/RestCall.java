package com.atlassian.pocketknife.api.rest;

import java.util.concurrent.Callable;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;

import org.apache.log4j.Logger;

public class RestCall
{
    private final Logger logger;

    public RestCall(Logger logger)
    {
        this.logger = logger;
    }

    /**
     * This will invoke the Callable and return the response to the callee. If an exception occurs it will log that into the appropriate logger.
     * 
     * @param callable the REST call code that will respond to the rest call
     * 
     * @return a REST {@link Response}
     */
    public Response response(Callable<Response> callable)
    {
        try
        {
            return callable.call();
        }
        catch (Exception e)
        {
            if (e instanceof WebApplicationException)
            {
                WebApplicationException webApplicationException = (WebApplicationException) e;
                if (webApplicationException.getResponse() != null)
                {
                    logger.info("Unable to complete REST method " + e.getMessage());

                    return webApplicationException.getResponse();
                }
            }

            logger.error("Unable to complete REST method ", e);
            if (e instanceof RuntimeException)
            {
                throw (RuntimeException) e;
            }
            else
            {
                throw new RuntimeException(e);
            }
        }
    }
}
