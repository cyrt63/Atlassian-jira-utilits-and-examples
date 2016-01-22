package com.atlassian.pocketknife.api.ao.dao;

import com.atlassian.jira.util.NotNull;
import net.java.ao.Entity;

import java.util.Map;

/**
 * A specialisation of {@link AOMapper} for AO records {@link T} which have a relationship with a parent record {@link P}.
 */
public interface RelatedAOMapper<P, T extends Entity, U> extends AOMapper<T, U> {
    /**
     * Returns a map representation of the domain object, similar to {@link AOMapper#toAO(Object)} but also incorporating
     * the parent record information.
     *
     * @param parent
     * @param model
     * @return
     */
    @NotNull
    public Map<String, Object> toAO(P parent, U model);
}
