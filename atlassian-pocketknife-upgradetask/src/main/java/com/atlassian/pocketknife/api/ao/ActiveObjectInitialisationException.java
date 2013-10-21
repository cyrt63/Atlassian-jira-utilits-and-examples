package com.atlassian.pocketknife.api.ao;

/**
 */
public class ActiveObjectInitialisationException extends Exception
{
    public ActiveObjectInitialisationException(Throwable t)
    {
        super("ActiveObjects could not be initialised", t);
    }
}
