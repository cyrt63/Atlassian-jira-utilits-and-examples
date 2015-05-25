package com.atlassian.pocketknife.internal.lifecycle.modules;

import com.atlassian.plugin.ModuleCompleteKey;
import com.atlassian.plugin.ModuleDescriptor;
import com.atlassian.plugin.Plugin;
import com.atlassian.plugin.StateAware;
import com.atlassian.pocketknife.api.lifecycle.modules.ModuleRegistrationHandle;
import com.atlassian.pocketknife.internal.lifecycle.modules.utils.BundleUtil;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static com.atlassian.pocketknife.internal.lifecycle.modules.Kit.getModuleIdentifier;
import static com.atlassian.pocketknife.internal.lifecycle.modules.Kit.pluginIdentifier;
import static com.google.common.collect.Lists.newArrayList;

/**
 * Helper component that registers dynamic module descriptors into OSGI land and hence as services of a specific
 * plugin.
 */
@Service
public class DynamicModuleRegistration
{
    private static final Logger log = LoggerFactory.getLogger(DynamicModuleRegistration.class);

    private final BundleContext bundleContext;

    @Autowired
    public DynamicModuleRegistration(BundleContext bundleContext)
    {
        this.bundleContext = bundleContext;
    }

    /**
     * Registers descriptors.  <strong>Important: make sure any osgi service references passed into these descriptors
     * are not proxies created by the p3 plugin, as it will cause ServiceProxyDestroyed exceptions when the p3 plugin is
     * upgraded.</strong>
     *
     * @param plugin the plugin to register on behalf of
     * @param descriptors the modules to register
     * @return a module registration object ready for unloading at plugin close time
     */
    public ModuleRegistrationHandle registerDescriptors(final Plugin plugin, Iterable<ModuleDescriptor> descriptors)
    {
        String pluginId = pluginIdentifier(plugin);

        Bundle bundle = BundleUtil.findBundleForPlugin(bundleContext, plugin.getKey());
        BundleContext targetBundleContext = bundle.getBundleContext();
        final List<TrackedDynamicModule> registrations = newArrayList();
        for (ModuleDescriptor descriptor : descriptors)
        {
            String moduleIdentifier = getModuleIdentifier(descriptor);
            log.debug("Registering module '{}' of type '{}' into plugin '{}'",
                    new Object[] { moduleIdentifier, descriptor.getClass().getSimpleName(), pluginId });

            ModuleDescriptor<?> existingDescriptor = plugin.getModuleDescriptor(descriptor.getKey());
            if (existingDescriptor != null)
            {
                log.error("Duplicate key '{}' detected in plugin '{}'; disabling previous instance",
                        moduleIdentifier, pluginId);
                ((StateAware) existingDescriptor).disabled();
            }

            ServiceRegistration serviceRegistration = registerModule(targetBundleContext, descriptor);
            registrations.add(new TrackedDynamicModule(serviceRegistration, descriptor));

        }
        // and give them back a handle so they can unregister on thw way out
        return new ModuleRegistrationHandleImpl(registrations);
    }

    private ServiceRegistration registerModule(BundleContext targetBundleContext, ModuleDescriptor<?> descriptor)
    {
        return targetBundleContext.registerService(ModuleDescriptor.class.getName(), descriptor, null);
    }

    static class TrackedDynamicModule
    {

        private final ServiceRegistration serviceRegistration;
        private final ModuleDescriptor<?> moduleDescriptor;
        private final ModuleCompleteKey moduleCompleteKey;


        TrackedDynamicModule(ServiceRegistration serviceRegistration, ModuleDescriptor<?> moduleDescriptor)
        {
            this.serviceRegistration = serviceRegistration;
            this.moduleDescriptor = moduleDescriptor;
            this.moduleCompleteKey = new ModuleCompleteKey(moduleDescriptor.getCompleteKey());
        }

        void unregister()
        {
            try
            {
                String moduleIdentifier = getModuleIdentifier(moduleDescriptor);
                log.debug("Un-registering module '{}' of type '{}' ", moduleIdentifier,
                        moduleDescriptor.getClass().getSimpleName());
                serviceRegistration.unregister();
            }
            catch (IllegalStateException ignored)
            {
                // if its already been unregistered with OSGI then that's cool by us.
                // dead is dead!
            }
            //
            // save memory by clearing out the XML we have recorded for this plugin
            GhettoCode.removeModuleDescriptorElement(moduleDescriptor.getPlugin(), moduleDescriptor.getKey());
        }

        public ModuleCompleteKey getModuleCompleteKey()
        {
            return moduleCompleteKey;
        }
    }


    static class ModuleRegistrationHandleImpl implements ModuleRegistrationHandle
    {
        private final List<TrackedDynamicModule> registrations;
        private final List<ModuleRegistrationHandle> theOthers;

        ModuleRegistrationHandleImpl(List<TrackedDynamicModule> registrations)
        {
            this(registrations, Collections.<ModuleRegistrationHandle>emptyList());
        }

        ModuleRegistrationHandleImpl(List<TrackedDynamicModule> registrations, List<ModuleRegistrationHandle> theOthers)
        {
            this.registrations = registrations;
            this.theOthers = theOthers;
        }

        @Override
        public void unregister()
        {
            for (ModuleRegistrationHandle theOther : theOthers)
            {
                theOther.unregister();
            }

            for (TrackedDynamicModule reg : registrations)
            {
                reg.unregister();
            }
            registrations.clear();
            theOthers.clear();
        }

        @Override
        public Iterable<ModuleCompleteKey> getModules()
        {
            List<ModuleCompleteKey> keys = new ArrayList<ModuleCompleteKey>();
            if (theOthers != null)
            {
                for (ModuleRegistrationHandle handle : theOthers)
                {
                    Iterables.addAll(keys, handle.getModules());
                }
            }
            for (TrackedDynamicModule module : registrations)
            {
                keys.add(module.getModuleCompleteKey());
            }
            return keys;
        }

        @Override
        public ModuleRegistrationHandle union(ModuleRegistrationHandle other)
        {
            return new ModuleRegistrationHandleImpl(this.registrations, smoosh(other));
        }

        private List<ModuleRegistrationHandle> smoosh(ModuleRegistrationHandle other)
        {
            if (other == this)
            {
                return this.theOthers;
            }
            ArrayList<ModuleRegistrationHandle> list = Lists.newArrayList();
            list.addAll(this.theOthers);
            list.add(other);
            return list;
        }
    }
}
