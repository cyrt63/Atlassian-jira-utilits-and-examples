package com.atlassian.pocketknife.internal.util.web;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.atlassian.jira.security.JiraAuthenticationContextImpl;
import com.atlassian.jira.web.ExecutingHttpRequest;
import com.atlassian.pocketknife.api.util.web.RequestCacheService;
import com.atlassian.pocketknife.spi.info.PocketKnifePluginInfo;
import com.google.common.base.Supplier;

/**
 * Implementation of a request level cache. Uses JIRA's request cache under the hood.
 *
 * Note: caching is a no-op for threads that aren't in request scope.
 */
@Service
public class RequestCacheServiceImpl implements RequestCacheService
{

    @Autowired
    private PocketKnifePluginInfo pocketKnifePluginInfo;

    @Override
    public void invalidate(String key)
    {
        if (!isHttpRequest())
        {
            return;
        }

        key = pfx(key);

        Map<String, Object> cache = getRequestCache();
        if (cache.containsKey(key))
        {
            cache.remove(key);
        }
    }

    @Override
    public void set(String key, Object value)
    {
        if (!isHttpRequest())
        {
            return;
        }

        key = pfx(key);
        getRequestCache().put(key, value);
    }

    @Override
    public <T> T get(String key, Class<T> clazz)
    {
        if (!isHttpRequest())
        {
            return null;
        }

        key = pfx(key);

        Map<String, Object> cache = getRequestCache();
        if (cache.containsKey(key))
        {
            @SuppressWarnings({ "unchecked", "UnnecessaryLocalVariable" })
            T o = (T) cache.get(key);
            return o;
        }

        return null;
    }

    @Override
    public <T> T getOrSet(String key, Class<T> clazz, Supplier<T> supplierOfValue)
    {
        if (!isHttpRequest())
        {
            return supplierOfValue.get();
        }

        T value = get(key, clazz);
        if (value == null)
        {
            value = supplierOfValue.get();
            set(key, value);
        }
        return value;
    }

    private Map<String, Object> getRequestCache()
    {
        // technically not API but we have a job to do here.
        return JiraAuthenticationContextImpl.getRequestCache();
    }

    private boolean isHttpRequest()
    {
        return ExecutingHttpRequest.get() != null;
    }

    private String pfx(String key)
    {
        return pocketKnifePluginInfo.getPluginKey() + ":" + key;
    }
}
