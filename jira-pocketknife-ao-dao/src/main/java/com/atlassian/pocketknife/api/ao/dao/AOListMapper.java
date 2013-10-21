package com.atlassian.pocketknife.api.ao.dao;

import net.java.ao.Entity;

import java.util.List;
import java.util.Map;

/**
 * To be implemented by mappers that map a list of values onto ActiveObjects.
 * 
 * AO's that implement Positionable will in addition get their position inside the List persisted
 * 
 * @param <T> The ActiveObject class
 * @param <U> The business object mapped by the ActiveObject class
 */
public interface AOListMapper<T extends Entity, U>
{
    /** Provide the db parameter for T to be created. */
    public void addCreateValues(U u, Map<String, Object> params);
    
    /** Updates an AO. Note that save should not be called. */
    public void setValues(T t, U u);
    
    /**
     * Called on the updated AO object (either once created or updated/saved).
     * Gives the mapper a chance to do further work, e.g. update child objects.
     */
    public void postCreateUpdate(T t, U u);
    
    /**
     * Called before the element is deleted, gives the mapper a chance to clean up child objects
     */
    public void preDelete(T t);
    
    /** Provides all existing T. */
    public T[] getExisting();
    
    /** Get an existing T for given u. Returns null if not found. */
    public T findExisting(T[] ts, U u);
    
    /** Get the class of T */
    public Class<T> getActiveObjectClass();

    public List<U> fromAO(T[] ts);
}
