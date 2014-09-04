package com.atlassian.pocketknife.api.lifecycle.modules;

import com.atlassian.annotations.Internal;
import com.atlassian.plugin.ModuleCompleteKey;
import com.atlassian.plugin.ModuleDescriptor;
import com.atlassian.plugin.Plugin;
import com.atlassian.pocketknife.internal.lifecycle.modules.GhettoCode;
import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;
import org.dom4j.Element;

import java.util.Collection;
import java.util.Map;

import static com.google.common.base.Predicates.not;
import static com.google.common.collect.Iterables.filter;
import static com.google.common.collect.Lists.newArrayList;

/**
 * This class can help you find what modules you have statically and dynamically registered
 * and whether they are ready to be unregistered via a handle.
 */
public class ModuleDescriptorKit
{
    @Internal
    public static final String DYNAMIC_MODULE_ATTRNAME = "com.atlassian.pocketknife.internal.lifecycle.modules.DynamicModule";

    /**
     * This will return the module descriptors in a plugin that have been static created
     *
     * @param plugin the OSGI plugin to inspect
     * @return the module descriptors that are statically created
     */
    public static Iterable<ModuleDescriptor<?>> getStaticModules(final Plugin plugin)
    {
        final Map<String, Element> moduleElements = GhettoCode.getModuleElements(plugin);

        final Collection<ModuleDescriptor<?>> moduleDescriptors = plugin.getModuleDescriptors();
        return newArrayList(filter(moduleDescriptors, not(isDynamicModule(moduleElements))));
    }

    /**
     * This will return the module descriptors in a plugin that have been dynamically created by PocketKnife
     *
     * @param plugin the OSGI plugin to inspect
     * @return the module descriptors that are dynamically created
     */
    public static Iterable<ModuleDescriptor<?>> getDynamicModules(final Plugin plugin)
    {
        final Map<String, Element> moduleElements = GhettoCode.getModuleElements(plugin);

        final Collection<ModuleDescriptor<?>> moduleDescriptors = plugin.getModuleDescriptors();
        return newArrayList(filter(moduleDescriptors, isDynamicModule(moduleElements)));
    }

    /**
     * This will return the module descriptors that exist in an OSGI plugin that are NOT covered by the provided
     * handle.
     * <p/>
     * That is if you called {@link ModuleRegistrationHandle#unregister()} on the handle, those dynamic modules would
     * not be unregistered.
     *
     * @param plugin the OSGI plugin to inspect
     * @param handle the handle of registrations
     * @return the module descriptors not covered by the handle
     */
    public static Iterable<ModuleDescriptor<?>> getDynamicModulesNotInHandle(final Plugin plugin, final ModuleRegistrationHandle handle)
    {
        Iterable<ModuleDescriptor<?>> dynamicModules = getDynamicModules(plugin);
        return newArrayList(filter(dynamicModules, not(isModuleInHandle(handle))));
    }

    private static Predicate<ModuleDescriptor<?>> isModuleInHandle(final ModuleRegistrationHandle handle)
    {
        final Iterable<ModuleCompleteKey> handleModules = handle.getModules();
        return new Predicate<ModuleDescriptor<?>>()
        {
            @Override
            public boolean apply(final ModuleDescriptor<?> moduleDescriptor)
            {
                ModuleCompleteKey moduleCompleteKey = Iterables.find(handleModules, new Predicate<ModuleCompleteKey>()
                {
                    @Override
                    public boolean apply(final ModuleCompleteKey input)
                    {
                        return input.getCompleteKey().equals(moduleDescriptor.getCompleteKey());
                    }
                }, null);
                return moduleCompleteKey != null;
            }
        };
    }

    private static Predicate<ModuleDescriptor<?>> isDynamicModule(final Map<String, Element> moduleElements)
    {
        return new Predicate<ModuleDescriptor<?>>()
        {
            @Override
            public boolean apply(final ModuleDescriptor<?> input)
            {
                Element element = moduleElements.get(input.getKey());
                return element != null && "true".equals(element.attributeValue(DYNAMIC_MODULE_ATTRNAME));
            }
        };
    }
}
