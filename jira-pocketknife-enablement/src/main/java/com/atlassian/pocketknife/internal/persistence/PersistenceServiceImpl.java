package com.atlassian.pocketknife.internal.persistence;

import com.atlassian.jira.propertyset.JiraPropertySetFactory;
import com.atlassian.jira.util.json.JSONArray;
import com.atlassian.jira.util.json.JSONException;
import com.atlassian.jira.util.json.JSONObject;
import com.atlassian.pocketknife.api.persistence.PersistenceService;
import com.opensymphony.module.propertyset.PropertyException;
import com.opensymphony.module.propertyset.PropertySet;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.apache.commons.lang.Validate.notNull;

/**
 * Implementation of the PersistenceService interface
 * <p/>
 * Wraps JiraPropertySetFactory for persistence.
 */
@Service
public class PersistenceServiceImpl implements PersistenceService
{
    private final Logger log = Logger.getLogger(getClass());

    private JiraPropertySetFactory propertySetFactory;

    @Autowired
    public PersistenceServiceImpl(JiraPropertySetFactory propertySetFactory)
    {
        this.propertySetFactory = propertySetFactory;
    }

    private static void assertPreconditions(String entityName, Long entityId, String key)
    {
        notNull(entityName);
        notNull(entityId);
        notNull(key);
    }

    /**
     * Set a Long property.
     */
    public void setLong(String entityName, Long entityId, String key, Long value)
    {
        assertPreconditions(entityName, entityId, key);
        notNull(value);

        getPropertySet(entityName, entityId).setLong(key, value);
    }

    /**
     * Get a Long property.
     */
    public Long getLong(String entityName, Long entityId, String key)
    {
        assertPreconditions(entityName, entityId, key);

        return exists(entityName, entityId, key) ? getPropertySet(entityName, entityId).getLong(key) : null;
    }

    @Override
    public void setText(String entityName, Long entityId, String key, String value)
    {
        assertPreconditions(entityName, entityId, key);
        notNull(value);

        getPropertySet(entityName, entityId).setText(key, value);
    }

    @Override
    public String getText(String entityName, Long entityId, String key)
    {
        assertPreconditions(entityName, entityId, key);

        return exists(entityName, entityId, key) ? getPropertySet(entityName, entityId).getText(key) : null;
    }

    /**
     * Set a Double property.
     */
    public void setDouble(String entityName, Long entityId, String key, Double value)
    {
        assertPreconditions(entityName, entityId, key);
        notNull(value);

        getPropertySet(entityName, entityId).setDouble(key, value);
    }

    /**
     * Get a Double property.
     */
    public Double getDouble(String entityName, Long entityId, String key)
    {
        assertPreconditions(entityName, entityId, key);

        return exists(entityName, entityId, key) ? getPropertySet(entityName, entityId).getDouble(key) : null;
    }

    @Override
    public void setBoolean(String entityName, Long entityId, String key, Boolean value)
    {
        assertPreconditions(entityName, entityId, key);
        notNull(value);

        getPropertySet(entityName, entityId).setBoolean(key, value);
    }

    @Override
    public Boolean getBoolean(String entityName, Long entityId, String key)
    {
        assertPreconditions(entityName, entityId, key);

        return exists(entityName, entityId, key) ? getPropertySet(entityName, entityId).getBoolean(key) : null;
    }

    /**
     * Returns a map or null if not set
     */
    @SuppressWarnings("unchecked")
    @Override
    public Map<String, Object> getData(String entityName, Long entityId, String key)
    {
        assertPreconditions(entityName, entityId, key);

        // Fetch the value
        String serializedData = getPropertySet(entityName, entityId).getText(key);
        if (serializedData == null)
        {
            return null;
        }
        return toMap(serializedData);
    }

    /**
     * Converts the provided data map into a text property and stores it under key.
     */
    @Override
    public void setData(String entityName, Long entityId, String key, Map<String, Object> data)
    {
        assertPreconditions(entityName, entityId, key);
        notNull(data);

        // serialize the map
        JSONObject jsonObject = new JSONObject(data);
        String serializedData = jsonObject.toString();

        // then store the value
        getPropertySet(entityName, entityId).setText(key, serializedData);
    }

    @SuppressWarnings("unchecked")
    @Override
    public List<Object> getListData(String entityName, Long entityId, String key)
    {
        notNull(entityName, "entityName must not be null");
        notNull(entityId, "entityId must not be null");
        notNull(key, "key must not be null");

        // Fetch the value
        String serializedData = getPropertySet(entityName, entityId).getText(key);
        if (serializedData == null)
        {
            return null;
        }

        // convert the data object into a list
        return toList(serializedData);
    }

    @Override
    public void setListData(String entityName, Long entityId, String key, List<Object> data)
    {
        notNull(entityName, "entityName must not be null");
        notNull(entityId, "entityId must not be null");
        notNull(key, "key must not be null");
        notNull(data, "data must not be null");

        // serialize the list
        String serializedData = new JSONArray(data).toString();

        if (log.isDebugEnabled())
        {
            log.debug("Storing list data in property set: " + entityName + ":" + entityId + " => " + key + ":" + serializedData);
        }

        // then store the value
        getPropertySet(entityName, entityId).setText(key, serializedData);
    }

    private Map<String, Object> toMap(String serializedData)
    {
        try
        {
            Map<String, Object> map = new HashMap<String, Object>();
            JSONObject jsonObject = new JSONObject(serializedData);
            for (Iterator<String> it = jsonObject.keys(); it.hasNext(); )
            {
                String key = it.next();
                map.put(key, jsonObject.get(key));
            }
            return map;
        }
        catch (JSONException e)
        {
            throw new RuntimeException("Code Assertion : How did we store JSON and not be able to parse it?", e);
        }
    }

    private List<Object> toList(String serializedData)
    {
        try
        {
            List<Object> list = new ArrayList<Object>();
            JSONArray array = new JSONArray(serializedData);
            for (int i = 0; i < array.length(); i++)
            {
                list.add(array.get(i));

            }
            return list;
        }
        catch (JSONException e)
        {
            throw new RuntimeException("Code Assertion : How did we store JSON and not be able to parse it?", e);
        }
    }

    /**
     * Get all keys defined for an entity name / entity id couple
     */
    @Override
    @SuppressWarnings("rawtypes")
    public Set<String> getKeys(String entityName, Long entityId)
    {
        notNull(entityName);
        notNull(entityId);

        // fetch the keys
        Collection keys = getPropertySet(entityName, entityId).getKeys();
        if (keys.isEmpty())
        {
            return Collections.emptySet();
        }

        // convert to a set of Strings
        Set<String> keySet = new HashSet<String>();
        for (Object key : keys)
        {
            keySet.add((String) key);
        }
        return keySet;
    }

    /**
     * Does a given key exist?
     */
    @Override
    public boolean exists(String entityName, Long entityId, String key)
    {
        return getPropertySet(entityName, entityId).exists(key);
    }

    /**
     * Remove a property for a given entity name and entity id couple
     */
    @Override
    public void delete(String entityName, Long entityId, String key)
    {
        assertPreconditions(entityName, entityId, key);

        PropertySet propertySet = getPropertySet(entityName, entityId);
        removeProperty(propertySet, key);
    }

    /**
     * Remove all properties for an entity name, entity id couple
     */
    @SuppressWarnings("rawtypes")
    @Override
    public void deleteAll(String entityName, Long entityId)
    {
        notNull(entityName);
        notNull(entityId);

        // remove all properties of this set
        PropertySet propertySet = getPropertySet(entityName, entityId);
        Collection keys = propertySet.getKeys();
        for (Object key : keys)
        {
            removeProperty(propertySet, (String) key);
        }
    }

    /**
     * Loads a PropertySet from the storage given a sequenceName/sequenceId mapping.
     *
     * @return a PropertySet for the given entityName and entityId.
     */
    @SuppressWarnings({"rawtypes", "unchecked"})
    private PropertySet getPropertySet(String entityName, Long entityId)
    {
        notNull(entityName);
        notNull(entityId);
        return propertySetFactory.buildCachingPropertySet(entityName, entityId, false);
    }

    /**
     * Removes a property. Silently swallows thrown exception
     */
    private void removeProperty(PropertySet propertySet, String key)
    {
        try
        {
            if (propertySet.exists(key))
            {
                propertySet.remove(key);
            }
        }
        catch (PropertyException e)
        {
            log.warn(e, e);
        }
    }

}
