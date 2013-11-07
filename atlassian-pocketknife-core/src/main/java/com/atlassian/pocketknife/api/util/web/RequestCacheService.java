package com.atlassian.pocketknife.api.util.web;

import com.google.common.base.Supplier;

/**
 * Puts values into the current JIRA request such that hey are automatically cleaned up after the request finishes
 */
public interface RequestCacheService
{
    /**
     * Gets a value from the request cache under the specified key in a type safe manner
     * <p/>
     * The key is actually prefixed with the plugin key to allow for add on isolation.
     * 
     * @param key the key to retrieve it from
     * @param clazz the class of the value
     * @param <T> for 2 and 2 for T
     * @return the value or null if it can be found
     */
    <T> T get(String key, Class<T> clazz);

    /**
     * A more functional version that will called the supplier if there is no value in cache, in effect a pull though cache. Its thread safe because
     * by definition this service is NOT thread safe and is request specific
     * 
     * @param key the key to retrieve it from
     * @param clazz the class of the value
     * @param supplierOfValue is called when the cached value is null
     * @param <T> for 2 and 2 for T
     * @return a cached value or one form the supplier
     */
    <T> T getOrSet(String key, Class<T> clazz, Supplier<T> supplierOfValue);

    /**
     * Sets a value into the request cache under the specified key
     * <p/>
     * The key is actually prefixed with the plugin key to allow for add on isolation.
     * 
     * @param key the key to put the value under
     * @param value the value to put
     */
    void set(String key, Object value);

    /**
     * Invalidate the cache entry for the specified key
     * 
     * @param key the key to clear
     */
    void invalidate(String key);

}
