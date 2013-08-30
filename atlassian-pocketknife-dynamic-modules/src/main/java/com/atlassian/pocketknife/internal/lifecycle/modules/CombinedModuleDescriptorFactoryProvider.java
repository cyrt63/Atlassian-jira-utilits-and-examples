package com.atlassian.pocketknife.internal.lifecycle.modules;

import com.atlassian.plugin.ModuleDescriptor;
import com.atlassian.plugin.ModuleDescriptorFactory;
import com.atlassian.plugin.PluginParseException;
import com.atlassian.plugin.descriptors.ChainModuleDescriptorFactory;
import com.atlassian.plugin.descriptors.UnrecognisedModuleDescriptor;
import com.atlassian.plugin.osgi.container.OsgiContainerManager;
import com.atlassian.plugin.osgi.external.ListableModuleDescriptorFactory;
import com.atlassian.sal.api.component.ComponentLocator;
import com.google.common.annotations.VisibleForTesting;
import org.osgi.util.tracker.ServiceTracker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * This class is responsible for getting and combining as many ModuleDescriptorFactories as there are in OSGI land
 * and putting them together into an uber factory.  This code is currently embedded inside OsgiPlugin and not available for direct re-use
 * so we have it here.
 * <p/>
 * That said we use different semantics on the type of module factories.  The plugin system assumes lazy resolution
 * of modules and module factories via service trackers.  We can also have that but we also can use resolved factories
 * when we encounter them
 */
@Service
public class CombinedModuleDescriptorFactoryProvider implements DisposableBean
{
    private final ModuleDescriptorFactory moduleDescriptorFactory;
    private final ServiceTracker moduleDescriptorFactoryTracker;
    private final ServiceTracker listableModuleDescriptorFactoryTracker;

    public CombinedModuleDescriptorFactoryProvider()
    {
        /**
         * We need this code because this is what the OsgiPluginFactory does per plugin BUT it never exposes the resultant
         * chained module descriptor factory so we could use it.  So we repeat this so we can gain access to all of the ModuleDescriptorFactories
         * out there in OSGI service tracker land.
         */
        final OsgiContainerManager osgi = getOsgiContainerManager();

        moduleDescriptorFactoryTracker = osgi.getServiceTracker(ModuleDescriptorFactory.class.getName());
        listableModuleDescriptorFactoryTracker = osgi.getServiceTracker(ListableModuleDescriptorFactory.class.getName());

        moduleDescriptorFactory = getChainedModuleDescriptorFactory(getHostModuleDescriptoryFactory());
    }

    public ModuleDescriptorFactory getModuleDescriptorFactory()
    {
        return moduleDescriptorFactory;
    }

    @Override
    public void destroy() throws Exception
    {
        moduleDescriptorFactoryTracker.close();
        listableModuleDescriptorFactoryTracker.close();
    }

    @VisibleForTesting
    ModuleDescriptorFactory getHostModuleDescriptoryFactory()
    {
        // Brad - why are you doing this?  Because I could not find a way to inject this without all sorts
        // of jiggery spring pokery and this is a library and hence I didn't want to pass that jiggery pokery
        // onto the caller
        return ComponentLocator.getComponent(ModuleDescriptorFactory.class);
    }

    @VisibleForTesting
    OsgiContainerManager getOsgiContainerManager()
    {
        return ComponentLocator.getComponent(OsgiContainerManager.class);
    }

    /**
     * Get a chained module descriptor factory that includes any dynamically available descriptor factories
     *
     * @param originalFactory The factory provided by the host application
     * @return The composite factory
     */
    private ModuleDescriptorFactory getChainedModuleDescriptorFactory(ModuleDescriptorFactory originalFactory)
    {
        List<ModuleDescriptorFactory> factories = new ArrayList<ModuleDescriptorFactory>();

        factories.add(originalFactory);
        Object[] serviceObjs = moduleDescriptorFactoryTracker.getServices();

        // Add all the dynamic module descriptor factories registered as osgi services
        if (serviceObjs != null)
        {
            for (Object fac : serviceObjs)
            {
                ModuleDescriptorFactory dynFactory = (ModuleDescriptorFactory) fac;
                factories.add(dynFactory);
            }
        }

        // get list-able module factories as well if we dont already have them
        serviceObjs = listableModuleDescriptorFactoryTracker.getServices();

        // Add all the dynamic module descriptor factories registered as osgi services
        if (serviceObjs != null)
        {
            for (Object fac : serviceObjs)
            {
                ModuleDescriptorFactory dynFactory = (ModuleDescriptorFactory) fac;
                if (!factories.contains(dynFactory))
                {
                    factories.add(dynFactory);
                }
            }
        }

        //
        // Catch all unknown descriptors as unrecognised.  These unrecognised modules can be resolved later if
        // a MD factory turns up that handles that unrecognised module type.  This is handled in OsgiPlugin via its service trackers
        // specifically UnrecognizedModuleDescriptorServiceTrackerCustomizer
        //
        factories.add(new UnrecognisedModuleDescriptorFallbackFactory());

        return new ChainModuleDescriptorFactory(factories);
    }

    /**
     * Module descriptor factory for deferred modules.  Turns every request for a module descriptor into a deferred
     * module so be sure that this factory is last in a list of factories.
     * <p/>
     * This had to be copied into here because its not public even if its result is a public class
     *
     * @see {@link com.atlassian.plugin.descriptors.UnrecognisedModuleDescriptor}
     * @since 2.1.2
     */
    static class UnrecognisedModuleDescriptorFallbackFactory implements ModuleDescriptorFactory
    {
        private static final Logger log = LoggerFactory.getLogger(UnrecognisedModuleDescriptorFallbackFactory.class);
        public static final String DESCRIPTOR_TEXT = "Support for this module is not currently installed.";

        public UnrecognisedModuleDescriptor getModuleDescriptor(final String type) throws PluginParseException, IllegalAccessException, InstantiationException, ClassNotFoundException
        {
            log.info("Unknown module descriptor of type " + type + " registered as an unrecognised descriptor.");
            final UnrecognisedModuleDescriptor descriptor = new UnrecognisedModuleDescriptor();
            descriptor.setErrorText(DESCRIPTOR_TEXT);
            return descriptor;
        }

        public boolean hasModuleDescriptor(final String type)
        {
            return true;
        }

        public Class<? extends ModuleDescriptor<?>> getModuleDescriptorClass(final String type)
        {
            return UnrecognisedModuleDescriptor.class;
        }
    }

}
