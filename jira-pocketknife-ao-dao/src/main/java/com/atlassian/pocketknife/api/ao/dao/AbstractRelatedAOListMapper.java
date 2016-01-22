package com.atlassian.pocketknife.api.ao.dao;

import net.java.ao.Entity;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Abstract class of a {@link AOListMapper} for an entity {@link T} which has a relationship with parent entity {@link P}.
 *
 * @since v5.9.5
 */
public abstract class AbstractRelatedAOListMapper<P, T extends Entity, U extends AbstractModel> implements AOListMapper<T, U> {
    protected final P parentAO;
    protected final RelatedAOMapper<P, T, U> mapper;

    protected AbstractRelatedAOListMapper(P parentAO, RelatedAOMapper<P, T, U> mapper) {
        this.parentAO = parentAO;
        this.mapper = mapper;
    }

    @Override
    public void addCreateValues(U model, Map<String, Object> params) {
        params.putAll(mapper.toAO(parentAO, model));
    }

    @Override
    public void setValues(T recordAO, U model) {
        mapper.update(model, recordAO);
    }

    @Override
    public T findExisting(T[] recordAOs, U model) {
        if (model.getId() == null) {
            return null;
        }
        for (T record : recordAOs) {
            if (record.getID() == model.getId()) {
                return record;
            }
        }
        return null;
    }

    @Override
    public List<U> fromAO(T[] ts) {
        // if the AO type is Positionable, sort the array first
        if (Positionable.class.isAssignableFrom(getActiveObjectClass())) {
            AOUtil.sortPositionableArray((Positionable[]) ts);
        }

        // transform into business objects
        List<U> transformed = new ArrayList<U>();
        for (T t : ts) {
            transformed.add(mapper.toModel(t));
        }
        return transformed;
    }

    @Override
    public abstract Class<T> getActiveObjectClass();

    public abstract void postCreateUpdate(T recordAO, U model);

    public abstract void preDelete(T recordAO);

    public abstract T[] getExisting();

}
