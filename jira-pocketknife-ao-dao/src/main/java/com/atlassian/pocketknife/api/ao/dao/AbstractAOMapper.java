package com.atlassian.pocketknife.api.ao.dao;

import com.atlassian.jira.util.NotNull;
import net.java.ao.Entity;

import java.util.ArrayList;
import java.util.List;

/**
 * Abstract {@link AOMapper} implementation that other mappers can inherit from.
 */
public abstract class AbstractAOMapper<T extends Entity, U extends AbstractModel> implements AOMapper<T, U>
{
    /**
     * Constructs a list of (usually immutable) domain objects from the AO record.
     * @param records the records to use
     * @return the domain objects
     */
    @NotNull
    public List<U> toModel(List<T> records)
    {
        List<U> result = new ArrayList<U>();
        for (T record : records)
        {
            result.add(toModel(record));
        }
        return result;
    }
}
