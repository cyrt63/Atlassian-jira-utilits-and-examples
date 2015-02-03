package com.atlassian.pocketknife.api.ao.util;

import com.atlassian.activeobjects.external.ActiveObjects;
import net.java.ao.Entity;
import net.java.ao.Query;

/**
 * A helper class for common migration techniques.
 *
 * It reads all the rows in the query and returns them to you one at a time so you can decide on how you want to change them.
 * <p/>
 * Contrary to AoStreamingMigrator it does not rely on {@link com.atlassian.activeobjects.external.ActiveObjects#stream(Class, net.java.ao.Query, net.java.ao.EntityStreamCallback)} to
 * load the rows, so the entity you get is a fully writable AO object.
 */
public abstract class AoFindMigrator<E extends Entity>
{
    private final Class<E> classOfEntity;
    private final ActiveObjects ao;

    private long read;
    private long written;

    public AoFindMigrator(Class<E> classOfEntity, ActiveObjects ao)
    {
        this.classOfEntity = classOfEntity;
        this.ao = ao;
        
        final E[] entities = ao.find(classOfEntity);
        for (E entity: entities)
        {
            onRowReadImpl(entity);
        }
        onEnd();
    }

    public AoFindMigrator(Class<E> classOfEntity, Query query, ActiveObjects ao)
    {
        this.classOfEntity = classOfEntity;
        this.ao = ao;

        final E[] entities = ao.find(classOfEntity, query);
        for (E entity: entities)
        {
            onRowReadImpl(entity);
        }
        onEnd();
    }


    /**
     * Gets a writeable version of the read only entity by re-reading it off the database
     *
     * @param readOnlyE the readonly entity that was streamed in
     * @return a writeable version of this entity ready for you to call entity.save() on!
     */
    protected E getWriteableEntity(E readOnlyE)
    {
        E[] entityList = ao.find(classOfEntity, "ID = ?", readOnlyE.getID());
        if (entityList.length != 1)
        {
            throw new IllegalStateException("getWriteableEntity assumes very strongly that you have a single entity per ID and that it exists on the database");
        }
        written++;
        return entityList[0];
    }

    private void onRowReadImpl(E readOnlyE)
    {
        read++;
        onRowRead(readOnlyE);
    }

    /**
     * You need to implement this template method
     *
     * @param readOnlyE a WRITABLE version of the entity.
     */
    protected abstract void onRowRead(E readOnlyE);

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

    /**
     * @return how many times {@link #getWriteableEntity(net.java.ao.Entity)} has been called
     */
    public long getWritten()
    {
        return written;
    }
}
