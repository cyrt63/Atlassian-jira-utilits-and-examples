package com.atlassian.pocketknife.api.ao.util;

import com.atlassian.activeobjects.external.ActiveObjects;
import net.java.ao.Entity;
import net.java.ao.EntityStreamCallback;
import net.java.ao.Query;

/**
 * A helper class for common migration techniques.  It uses the {@link com.atlassian.activeobjects.external.ActiveObjects#stream(Class, net.java.ao.Query, net.java.ao.EntityStreamCallback)} mechanism to
 * read all the rows in the query and return them to you one at a time so you can decide on how you want to change them.
 * <p/>
 * Remember each returned entity is READ ONLY and you need to make a call to {@link #getWriteableEntity(net.java.ao.Entity)} to get one that can be written to
 */
public abstract class AoStreamingMigrator<E extends Entity>
{
    private final Class<E> classOfEntity;

    private ActiveObjects ao;
    private Query query;
    private long read;
    private long written;

    /**
     * Usage: Create a new AoStreamingMigrator anonymous class, implement required methods and call
     * migrate(ao) on it.
     */
    public AoStreamingMigrator(Class<E> classOfEntity)
    {
        this.classOfEntity = classOfEntity;
    }

    public final void migrate(ActiveObjects ao) {
        this.ao = ao;

        Query query = buildQuery();
        ao.stream(classOfEntity, query, new EntityStreamCallback<E, Integer>()
        {
            @Override
            public void onRowRead(E readOnlyE)
            {
                onRowReadImpl(readOnlyE);
            }
        });
        onEnd();

        this.ao = null;
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
     * @param readOnlyE a streamed READ ONLY version of the entity.  You MUST get a writeable
     */
    protected abstract void onRowRead(E readOnlyE);

    /**
     * You need to implement this method
     *
     * @return the query to run. Originally this defaulted to Query.select("*"), but this isn't supported anymore,
     *         so you will have to spell out each individual field you want to query. For more infos see
     *         https://ecosystem.atlassian.net/browse/AO-552
     */
    protected abstract Query buildQuery();

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
