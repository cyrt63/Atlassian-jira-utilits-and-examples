package com.atlassian.pocketknife.internal.persistence;

import com.atlassian.pocketknife.api.persistence.GlobalPropertyDao;
import com.atlassian.pocketknife.api.persistence.PersistenceService;
import com.atlassian.pocketknife.spi.persistence.PocketKnifePersistenceInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * DAO layer for global properties. These are stored as one value per property record.
 */
@Service
public class GlobalPropertyDaoImpl implements GlobalPropertyDao {
    private static final long GLOBAL_ENTITY_ID = 1l;

    private final PersistenceService persistenceService;

    private final PocketKnifePersistenceInfo pocketKnifePersistenceInfo;

    @Autowired
    public GlobalPropertyDaoImpl(PersistenceService persistenceService, PocketKnifePersistenceInfo pocketKnifePersistenceInfo) {
        this.persistenceService = persistenceService;
        this.pocketKnifePersistenceInfo = pocketKnifePersistenceInfo;
    }

    private String propertyKey() {
        return pocketKnifePersistenceInfo.getGlobalPropertyDaoEntityName();
    }

    /**
     * @return the Long value for the given property key, or null
     */
    @Override
    public Long getLongProperty(String key) {
        return persistenceService.getLong(propertyKey(), GLOBAL_ENTITY_ID, key);
    }

    /**
     * Set the Long value for the given property key
     */
    @Override
    public void setLongProperty(String key, Long value) {
        persistenceService.setLong(propertyKey(), GLOBAL_ENTITY_ID, key, value);
    }

    /**
     * @param key the property key
     * @return the Boolean value for the given property key, or null
     */
    @Override
    public Boolean getBooleanProperty(String key) {
        return persistenceService.getBoolean(propertyKey(), GLOBAL_ENTITY_ID, key);
    }

    /**
     * Set the Boolean value for the given property key
     *
     * @param key   the key
     * @param value the value
     */
    @Override
    public void setBooleanProperty(String key, Boolean value) {
        persistenceService.setBoolean(propertyKey(), GLOBAL_ENTITY_ID, key, value);
    }

    @Override
    public String getTextProperty(String key) {
        return persistenceService.getText(propertyKey(), GLOBAL_ENTITY_ID, key);
    }

    @Override
    public void setTextProperty(String key, String value) {
        persistenceService.setText(propertyKey(), GLOBAL_ENTITY_ID, key, value);
    }
}
