package com.atlassian.pocketknife.api.persistence;

import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * This service wraps the JiraPropertySetFactory as well as provides automatic serialization for a map of data
 */
public interface PersistenceService
{
    /**
     * Remove a property given the entity name, entity id and property key.
     */
    public void delete(String entityName, Long entityId, String key);

    /**
     * Delete all properties for a given an entity name and entity id
     */
    public void deleteAll(String entityName, Long entityId);

    /**
     * Set a property value of type Long
     */
    public void setLong(String entityName, Long entityId, String key, Long value);

    /**
     * Get a property value of type Long. If there is no data, null is returned.
     */
    public Long getLong(String entityName, Long entityId, String key);


    /**
     * Set a property value of type String
     */
    public void setText(String entityName, Long entityId, String key, String value);

    /**
     * Get a property value of type String. If there is no data, null is returned.
     */
    public String getText(String entityName, Long entityId, String key);

    /**
     * Set a property value of type Double
     */
    public void setDouble(String entityName, Long entityId, String key, Double value);

    /**
     * Get a property value of type Double. If there is no data, null is returned.
     */
    public Double getDouble(String entityName, Long entityId, String key);
    
    /**
     * Set a property value of type Boolean. 
     */
    public void setBoolean(String entityName, Long entityId, String key, Boolean value);
    
    /**
     * Get a property value of type Boolean. If there is no data, null is returned. 
     */
    public Boolean getBoolean(String entityName, Long entityId, String key);
    
    /**
     * Get a map data property for a given entity name, entity id and property key
     * 
     * @return deserialised data or null if no record exists
     */
    public Map<String, Object> getData(String entityName, Long entityId, String key);

    /**
     * Set a map data property. The map is serialized using xstream and stored in JIRA as a text property.
     */
    public void setData(String entityName, Long entityId, String key, Map<String, Object> data);
 
    /**
     * Get a list data property for a given entity name, entity id and property key
     * 
     * @return deserialised data or null if no record exists
     */
    public List<Object> getListData(String entityName, Long entityId, String key);

    /**
     * Set a list data property. The list is serialised using xstream and stored in JIRA as a text property.
     */
    public void setListData(String entityName, Long entityId, String key, List<Object> data);
    
    /**
     * @return all keys for a given entity name and entity id, or an empty set if there are none
     */
    public Set<String> getKeys(String entityName, Long entityId);

    /**
     * Exists a property for a given entity name and id
     * 
     * @return true if the property exists, false otherwise
     */
    public boolean exists(String entityName, Long entityId, String key);

}