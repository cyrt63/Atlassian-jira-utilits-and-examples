package com.atlassian.pocketknife.api.ao.dao;

import com.atlassian.jira.util.NotNull;
import net.java.ao.Entity;

import java.util.List;

/**
 * A common interface for DAOs which handle an AO record {@link T} which has a relationship with parent record {@link P}.
 */
public interface RelatedEntityDao<PK, P extends Entity, T extends Entity, U> {
    /**
     * Return all the records for the specified parent.
     *
     * @param parent
     * @return the records
     */
    @NotNull
    public T[] getForParent(P parent);

    /**
     * Return all the records for the specified parent.
     *
     * @param primaryKey
     * @return the records
     */
    @NotNull
    public T[] getForParent(PK primaryKey);

    /**
     * Set the list of domain objects onto the parent record. This will create new or updated existing records as necessary.
     *
     * @param parent
     * @param models
     * @return the updated list of domain objects
     */
    @NotNull
    public List<T> updateForParent(P parent, List<U> models);

    /**
     * Delete all records for specified parent.
     *
     * @param parent
     */
    public void deleteForParent(P parent);
}
