package com.atlassian.pocketknife.internal.ao;

import com.atlassian.activeobjects.external.ActiveObjects;
import com.atlassian.pocketknife.api.ao.ActiveObjectsController;
import com.atlassian.pocketknife.api.ao.ActiveObjectInitialisationException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

/**
 */
@Service
public class ActiveObjectsControllerImpl implements ActiveObjectsController
{

    private final ActiveObjects activeObjects;

    private final AtomicBoolean initialsed = new AtomicBoolean(false);
    private final AtomicReference<Exception> failedInit = new AtomicReference<Exception>();

    @Autowired
    public ActiveObjectsControllerImpl(ActiveObjects activeObjects)
    {
        this.activeObjects = activeObjects;
    }

    @Override
    public boolean isInitialised()
    {
        return initialsed.get();
    }

    @Override
    public void initialise() throws ActiveObjectInitialisationException
    {
        if (isInitialised())
        {
            return;
        }
        try
        {
            // this will cause AO to initialise and hence run AO upgrade tasks and the like
            activeObjects.flushAll();
            initialsed.set(true);
        }
        catch (RuntimeException e)
        {
            ActiveObjectInitialisationException initialisationException = new ActiveObjectInitialisationException(e);
            failedInit.set(initialisationException);
            throw initialisationException;
        }
    }

    @Override
    public ActiveObjects getAO() throws IllegalStateException
    {
        if (failedInit.get() != null)
        {
            throw new IllegalStateException("ActiveObjects has previous failed initialisation", failedInit.get());
        }
        if (!isInitialised())
        {
            throw new IllegalStateException("This is a stateful service.  You must call initialise() before asking for the AO object ");
        }
        return activeObjects;
    }
}
