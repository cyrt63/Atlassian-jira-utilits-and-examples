package com.atlassian.pocketknife.api.persistence;

/**
 * DAO layer for global properties. These are stored as one value per property record.
 *
 * Think of this as your own personal set of application properties
 */
public interface GlobalPropertyDao
{
    Long getLongProperty(String key);

    void setLongProperty(String key, Long value);

    Boolean getBooleanProperty(String key);

    void setBooleanProperty(String key, Boolean value);

    String getTextProperty(String key);

    void setTextProperty(String key, String value);

}
