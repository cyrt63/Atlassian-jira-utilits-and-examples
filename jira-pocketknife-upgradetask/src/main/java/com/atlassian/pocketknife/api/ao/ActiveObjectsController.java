package com.atlassian.pocketknife.api.ao;

import com.atlassian.activeobjects.external.ActiveObjects;

/**
 * This stateful class can initialise and detect if ActiveObjects was able to be brought up successfully and will prevent you from using ActiveObjects
 * if it has previously failed to start
 */
public interface ActiveObjectsController {
    /**
     * @return true if {@link #initialise()} has been successfully called before
     */
    boolean isInitialised();

    /**
     * Call this to initialise ActiveObjects, which will cause AO upgrade tasks and migration to happen
     *
     * @throws ActiveObjectInitialisationException if it can initialise
     */
    void initialise() throws ActiveObjectInitialisationException;

    /**
     * This will return the underlying initialised ActiveObjects object or throw an IllegalStateException if it previously failed to initialise.
     *
     * @return the AO object
     * @throws IllegalStateException if it previously failed to initialise
     */
    ActiveObjects getAO() throws IllegalStateException;

}
