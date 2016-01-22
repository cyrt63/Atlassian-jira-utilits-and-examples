package com.atlassian.pocketknife.api.ao.util;

import com.atlassian.activeobjects.external.ActiveObjects;
import net.java.ao.Entity;
import net.java.ao.EntityStreamCallback;
import net.java.ao.Query;

import javax.annotation.concurrent.NotThreadSafe;

/**
 * A helper class for common migration techniques.  It uses the {@link com.atlassian.activeobjects.external.ActiveObjects#stream(Class, net.java.ao.Query, net.java.ao.EntityStreamCallback)} mechanism to
 * read all the rows in the query and return them to you one at a time so you can decide on how you want to change them.
 * <p>
 * Remember each returned entity is READ ONLY and you need to make a call to {@link #getWriteableEntity(net.java.ao.Entity)} to get one that can be written to
 * <p>
 * Note: Do not use the read-only object to fetch foreign objects (methods returning another AO object). In practice this
 * currently works if you properly specify the XXX_ID column, but you really shouldn't rely on this as it isn't intended behaviour.
 * Use AoFindMigrator if you need to fetch linked objects, or load a writable object beforehand.
 */
@NotThreadSafe
public abstract class AoStreamingMigrator<E extends Entity> {
    private final Class<E> classOfEntity;
    private final ActiveObjects ao;

    private long read;
    private long written;

    public AoStreamingMigrator(Class<E> classOfEntity, Query query, ActiveObjects ao) {
        this.classOfEntity = classOfEntity;
        this.ao = ao;

        ao.stream(classOfEntity, query, new EntityStreamCallback<E, Integer>() {
            @Override
            public void onRowRead(E readOnlyE) {
                onRowReadImpl(readOnlyE);
            }
        });
        onEnd();
    }


    /**
     * Gets a writeable version of the read only entity by re-reading it off the database
     *
     * @param readOnlyE the readonly entity that was streamed in
     * @return a writeable version of this entity ready for you to call entity.save() on!
     */
    protected E getWriteableEntity(E readOnlyE) {
        E[] entityList = ao.find(classOfEntity, "ID = ?", readOnlyE.getID());
        if (entityList.length != 1) {
            throw new IllegalStateException("getWriteableEntity assumes very strongly that you have a single entity per ID and that it exists on the database");
        }
        written++;
        return entityList[0];
    }

    private void onRowReadImpl(E readOnlyE) {
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
     * A template method allowing you to do something once all rows have been migrated
     */
    protected void onEnd() {
    }

    /**
     * @return how many times {@link #onRowRead(net.java.ao.Entity)} has been called
     */
    public long getRead() {
        return read;
    }

    /**
     * @return how many times {@link #getWriteableEntity(net.java.ao.Entity)} has been called
     */
    public long getWritten() {
        return written;
    }
}
