package com.atlassian.pocketknife.api.ao.util;

import com.atlassian.activeobjects.external.ActiveObjects;
import net.java.ao.Entity;
import net.java.ao.Query;

import javax.annotation.concurrent.NotThreadSafe;

/**
 * A helper class for common migration techniques.
 *
 * It reads all the rows in the query and returns them to you one at a time so you can decide on how you want to change them.
 * <p/>
 * Contrary to AoStreamingMigrator this class does not rely on {@link com.atlassian.activeobjects.external.ActiveObjects#stream(Class, net.java.ao.Query, net.java.ao.EntityStreamCallback)} to
 * load the rows, so the entity you get is a fully writable AO object with all its whistles and bells.
 */
@NotThreadSafe
public abstract class AoFindMigrator<E extends Entity>
{

    private long read;

    public AoFindMigrator(Class<E> classOfEntity, ActiveObjects ao)
    {
        final E[] entities = ao.find(classOfEntity);
        iterate(entities);
    }

    public AoFindMigrator(Class<E> classOfEntity, Query query, ActiveObjects ao)
    {
        final E[] entities = ao.find(classOfEntity, query);
        iterate(entities);
    }
    
    private void iterate(E[] entities)
    {
        for (E entity: entities)
        {
            onRowReadImpl(entity);
        }
        onEnd();
    }

    private void onRowReadImpl(E readOnlyE)
    {
        read++;
        onRowRead(readOnlyE);
    }

    /**
     * You need to implement this template method
     *
     * @param writableE a WRITABLE version of the entity.
     */
    protected abstract void onRowRead(E writableE);

    /**
     * A template method allowing you to do something once all rows have been migrated
     */
    protected void onEnd()
    {
    }

    /**
     * @return how many times {@link #onRowRead(net.java.ao.Entity)} has been called
     */
    public long getRead()
    {
        return read;
    }
}
