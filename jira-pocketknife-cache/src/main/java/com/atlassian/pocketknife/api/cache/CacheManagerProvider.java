package com.atlassian.pocketknife.api.cache;

import com.atlassian.cache.CacheManager;

/**
 * Provider for a CacheManager instance.
 */
public interface CacheManagerProvider
{
    /**
     * Get a CacheManager
     *
     * This bridge will return the JIRA internal CacheManager if available, otherwise will create and
     * return an in memory cache.
     */
    public CacheManager getCacheManager();
}
