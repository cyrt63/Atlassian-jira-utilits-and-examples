package com.atlassian.pocketknife.internal.cache;

import com.atlassian.cache.CacheManager;
import com.atlassian.cache.memory.MemoryCacheManager;
import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.pocketknife.api.cache.MinCompatibleJiraVersion;
import com.atlassian.pocketknife.api.logging.Log;
import com.atlassian.pocketknife.api.version.JiraVersionService;
import com.atlassian.pocketknife.api.version.SoftwareVersion;
import com.google.common.base.Supplier;

/**
 * Supplies a cache manager instance.
 */
class CacheManagerSupplier implements Supplier<CacheManager>
{
    Log log = Log.with(getClass());

    private JiraVersionService jiraVersionService;

    public CacheManagerSupplier(JiraVersionService jiraVersionService)
    {
        this.jiraVersionService = jiraVersionService;
    }

    @Override
    public CacheManager get()
    {
        if (useJiraCacheManager())
        {
            log.info("Using JIRA provided CacheManager implementation.");
            return ComponentAccessor.getComponent(CacheManager.class);
        }
        else
        {
            /**
             * We use an in memory cache for older JIRA versions.
             * Cache invalidation is currently handled by the cache "users", thus each service creating a
             * cache should listen onto ClearCacheEvents and call Cache.removeAll itself.
             */
            log.info("Using bundled CacheManager implementation.");
            return new MemoryCacheManager();
        }
    }

    private boolean useJiraCacheManager()
    {
        SoftwareVersion jiraVersion = jiraVersionService.version();
        return jiraVersion.isGreaterThanOrEqualTo(MinCompatibleJiraVersion.JIRA_VERSION_WITH_CACHE_MANAGER_2);
    }
}
