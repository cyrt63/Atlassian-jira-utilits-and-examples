package com.atlassian.pocketknife.internal.cache;

import com.atlassian.cache.CacheManager;
import com.atlassian.pocketknife.api.cache.CacheManagerProvider;
import com.atlassian.pocketknife.api.version.JiraVersionService;
import com.google.common.base.Supplier;
import com.google.common.base.Suppliers;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;

/**
 * Provider for a CacheManager instance. Uses JIRAs CacheManager if available
 */
@Service
public class CacheManagerProviderImpl implements CacheManagerProvider
{

    @Autowired
    private JiraVersionService jiraVersionService;

    /**
     * Supplier for a CacheManager, memoized to always return the same instance.
     */
    private Supplier<CacheManager> impl;

    @PostConstruct
    public void afterPropertiesSet()
    {
        impl = Suppliers.memoize(new CacheManagerSupplier(jiraVersionService));
    }

    /**
     * Get an implementation to use
     */
    @Override
    public CacheManager getCacheManager()
    {
        return impl.get();
    }

}
