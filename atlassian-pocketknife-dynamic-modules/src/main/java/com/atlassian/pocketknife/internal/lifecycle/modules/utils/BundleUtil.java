package com.atlassian.pocketknife.internal.lifecycle.modules.utils;

import com.atlassian.plugin.osgi.factory.OsgiPlugin;

import com.google.common.base.Function;
import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;

import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;

import static com.google.common.collect.Lists.newArrayList;

/**
 * Copied from https://stash.atlassian.com/projects/AC/repos/atlassian-connect/browse/plugin/src/main/java/com/atlassian/plugin/connect/plugin/util/BundleUtil.java
 * Used to get the OSGI bundle of another plugin.
 */
public final class BundleUtil
{
    private static final Bundle NOT_FOUND_BUNDLE = null;

    private BundleUtil()
    {
    }

    public static Bundle findBundleForPlugin(BundleContext bundleContext, String pluginKey)
    {
        return findBundleForPlugin(newArrayList(bundleContext.getBundles()), pluginKey);
    }

    private static Bundle findBundleForPlugin(Iterable<Bundle> bundles, final String pluginKey)
    {
        return Iterables.find(bundles,
                new Predicate<Bundle>()
                {
                    @Override
                    public boolean apply(Bundle b)
                    {
                        return pluginKey.equals(b.getHeaders().get(OsgiPlugin.ATLASSIAN_PLUGIN_KEY));
                    }
                },
                NOT_FOUND_BUNDLE);
    }
}
