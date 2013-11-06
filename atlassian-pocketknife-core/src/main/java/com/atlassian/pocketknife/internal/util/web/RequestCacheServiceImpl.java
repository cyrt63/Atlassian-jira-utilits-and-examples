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
 */
@Service
public class RequestCacheServiceImpl implements RequestCacheService
{

    @Autowired
    private PocketKnifePluginInfo pocketKnifePluginInfo;

    @Override
    public void invalidate(String key)
    {
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
        key = pfx(key);
        getRequestCache().put(key, value);
    }

    @Override
    public <T> T get(String key, Class<T> clazz)
    {
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
        checkHttpInvariant();

        // technically not API but we have a job to do here.
        return JiraAuthenticationContextImpl.getRequestCache();
    }

    private void checkHttpInvariant()
    {
        //
        // if we are not inside a HTTP request then the user of this class is meaningless and wrong
        // so we enforce that invariant with that most dramatic of coding constructs! The exception!
        //
        if (ExecutingHttpRequest.get() == null)
        {
            throw new IllegalStateException("You must be inside a HTTP request to call the RequestCacheService.");
        }
    }

    private String pfx(String key)
    {
        return pocketKnifePluginInfo.getPluginKey() + ":" + key;
    }
}
