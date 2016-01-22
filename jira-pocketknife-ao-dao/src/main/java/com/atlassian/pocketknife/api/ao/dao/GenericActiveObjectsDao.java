package com.atlassian.pocketknife.api.ao.dao;

import com.atlassian.activeobjects.external.ActiveObjects;
import com.atlassian.fugue.Either;
import com.atlassian.jira.util.ErrorCollection;
import com.atlassian.pocketknife.api.logging.Log;
import net.java.ao.RawEntity;
import org.springframework.beans.factory.annotation.Autowired;

import java.lang.reflect.ParameterizedType;
import java.util.Map;

import static com.atlassian.pocketknife.api.util.ServiceResult.error;
import static com.atlassian.pocketknife.api.util.ServiceResult.ok;

public abstract class GenericActiveObjectsDao<PK, E extends RawEntity<PK>> {
    protected final Log log = Log.with(getClass());

    @Autowired
    protected ActiveObjects ao;

    protected final Class<E> entityType;

    @SuppressWarnings("unchecked")
    public GenericActiveObjectsDao() {
        entityType = (Class<E>) ((ParameterizedType) getClass().getGenericSuperclass()).getActualTypeArguments()[1];
    }

    public Either<ErrorCollection, E> load(PK primaryKey) {
        E entity = ao.get(entityType, primaryKey);

        if (entity != null) {
            log.debug("loaded entity %s for id %s", entity, primaryKey);
            return ok(entity);
        } else {
            String message = "could not find entity of type " + entityType + " with key " + primaryKey;
            log.warn(message);
            return error(ErrorCollection.Reason.NOT_FOUND, message);
        }
    }

    public E create(Map<String, Object> fields) {
        log.debug("creating a new entity of type %s with fields %s", entityType.getName(), fields);

        return ao.create(entityType, fields);
    }

    public void save(E entity) {
        log.debug("saving entity of type %s", entityType);

        entity.save();
    }

    @SuppressWarnings("unchecked")
    public void delete(PK primaryKey) {
        log.debug("deleting entity of type %s with primary key = %s", entityType.getName(), primaryKey);

        E entity = ao.get(entityType, primaryKey);
        if (entity != null) {
            delete(entity);
        }
    }

    public void delete(E... entities) {
        log.debug("deleting %d entities of type %s", entities.length, entityType.getName());

        preDelete(entities);

        ao.delete(entities);
    }

    protected void preDelete(E... entities) {
    }

    protected E[] findBy(String query, Object... parameters) {
        return ao.find(entityType, query, parameters);
    }
}
