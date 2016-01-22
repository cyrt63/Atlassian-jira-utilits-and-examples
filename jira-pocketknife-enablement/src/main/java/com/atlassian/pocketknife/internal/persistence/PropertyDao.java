package com.atlassian.pocketknife.internal.persistence;

import com.atlassian.pocketknife.api.persistence.PersistenceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * DAO layer for global properties. These are stored as one value per PropertySet record.
 */
@Service
public class PropertyDao {
    private static final String KEY_PROPS = "vp.properties";

    private static final long GLOBAL_ENTITY_ID = 1l;

    @Autowired
    private PersistenceService persistenceService;

    /**
     * @return the Long value for the given property key, or null
     */
    public Long getLongProperty(String key) {
        return persistenceService.getLong(KEY_PROPS, GLOBAL_ENTITY_ID, key);
    }

    /**
     * Set the Long value for the given property key
     */
    public void setLongProperty(String key, Long value) {
        persistenceService.setLong(KEY_PROPS, GLOBAL_ENTITY_ID, key, value);
    }

    /**
     * @param key the property key
     * @return the Boolean value for the given property key, or null
     */
    public Boolean getBooleanProperty(String key) {
        return persistenceService.getBoolean(KEY_PROPS, GLOBAL_ENTITY_ID, key);
    }

    /**
     * Set the Boolean value for the given property key
     *
     * @param key   the key
     * @param value the value
     */
    public void setBooleanProperty(String key, Boolean value) {
        persistenceService.setBoolean(KEY_PROPS, GLOBAL_ENTITY_ID, key, value);
    }
}
