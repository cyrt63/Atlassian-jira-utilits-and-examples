package com.atlassian.pocketknife.api.ao.dao;

import com.atlassian.jira.util.NotNull;
import net.java.ao.Entity;

import java.util.Map;

/**
 * Common interface for mapping between AO records of type {@link T} to domain objects of type {@link U}.
 */
public interface AOMapper<T extends Entity, U>
{
    /**
     * Return a mapping of AO column names to values which will represent the domain object.
     * <p/>
     * The keys of the map must be the exact names of the columns as they will appear in AO. E.g. for a column <tt>fieldId</tt>
     * the column name would be <tt>FIELD_ID</tt>.
     * <p/>
     * Note: the <tt>ID</tt> (primary key) column does not need to be included. This is only used when persisting new
     * domain objects to AO.
     *
     * @param model the new domain object
     * @return a map containing the columns and values
     */
    @NotNull
    public Map<String, Object> toAO(U model);

    /**
     * Constructs a (usually immutable) domain object from the AO record.
     * @param record the record to use
     * @return the domain object
     */
    @NotNull
    public U toModel(T record);

    /**
     * Copies fields from a domain object to the AO record representation. Note that the save() method must not be called.
     * @param source the domain object
     * @param target the corresponding AO record
     */
    public void update(U source, T target);
}
