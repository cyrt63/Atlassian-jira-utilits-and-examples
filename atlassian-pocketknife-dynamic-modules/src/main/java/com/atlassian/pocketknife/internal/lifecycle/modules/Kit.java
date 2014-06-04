package com.atlassian.pocketknife.internal.lifecycle.modules;

import com.atlassian.plugin.ModuleDescriptor;
import com.atlassian.plugin.Plugin;
import org.dom4j.Element;

import static org.apache.commons.lang.StringUtils.defaultIfEmpty;

/**
 */
public class Kit
{

    public static final String NOT_SPECIFIED = "not-specified??";

    static String getModuleIdentifier(final Element element)
    {
        return mkId(element.attributeValue("key"), element.attributeValue("name"));
    }

    static String getModuleIdentifier(final ModuleDescriptor descriptor)
    {
        return mkId(descriptor.getKey(), descriptor.getName());
    }

    static String pluginIdentifier(final Plugin plugin)
    {
        return plugin.getKey() + " -  " + plugin.getName();
    }

    private static String mkId(final String key, final String name)
    {
        return defaultIfEmpty(key, NOT_SPECIFIED) + " - " + defaultIfEmpty(name, "");
    }

}
