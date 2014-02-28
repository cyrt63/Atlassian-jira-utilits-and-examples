package com.atlassian.pocketknife.internal.lifecycle.modules;

import com.atlassian.plugin.ModuleDescriptor;
import com.atlassian.plugin.ModuleDescriptorFactory;
import com.atlassian.plugin.Plugin;
import com.atlassian.plugin.PluginParseException;
import com.atlassian.plugin.descriptors.UnloadableModuleDescriptor;
import com.atlassian.plugin.descriptors.UnrecognisedModuleDescriptor;
import com.atlassian.pocketknife.api.lifecycle.modules.DynamicModuleDescriptorFactory;
import com.atlassian.pocketknife.api.lifecycle.modules.ModuleRegistrationHandle;
import com.google.common.collect.Lists;
import org.dom4j.Element;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.InputStream;
import java.util.List;

import static com.atlassian.plugin.descriptors.UnloadableModuleDescriptorFactory.createUnloadableModuleDescriptor;
import static com.atlassian.plugin.descriptors.UnrecognisedModuleDescriptorFactory.createUnrecognisedModuleDescriptor;

/**
 * The use of this module factory allows a plugin to have a "controlled launch" of its functionality.  The plugin
 * can decide what modules it is going to offer to the world and at what time.
 * <p/>
 * This is in contrast to the declarative "all at once" pattern that atlassian-plugin.xml forces you into.
 * <p/>
 * For example you can wait until you plugin has run all its upgrade tasks and determined licensing BEFORE you release
 * your web-sections and actions to the world.
 * <p/>
 * <quote>
 * "We will decide what modules come to this plugin and the circumstances in which they come!" -  John Howard - Dog Whistling 2002
 * </quote>
 * <p/>
 */
@Service
public class DynamicModuleDescriptorFactoryImpl implements DynamicModuleDescriptorFactory
{
    private static final Logger log = LoggerFactory.getLogger(DynamicModuleDescriptorFactoryImpl.class);

    private final DynamicModuleRegistration dynamicModuleRegistration;
    private final CombinedModuleDescriptorFactoryProvider combinedModuleDescriptorFactoryProvider;

    @Autowired
    public DynamicModuleDescriptorFactoryImpl(DynamicModuleRegistration dynamicModuleRegistration, CombinedModuleDescriptorFactoryProvider combinedModuleDescriptorFactoryProvider)
    {
        this.dynamicModuleRegistration = dynamicModuleRegistration;
        this.combinedModuleDescriptorFactoryProvider = combinedModuleDescriptorFactoryProvider;
    }

    /**
     * This allows you to load one or more auxiliary atlassian-plugin.xml files as modules under your control.
     *
     * @param plugin                        - your plugin that you want the modules to belong to
     * @param pathsToAuxAtlassianPluginXMLs a set of 1 or more auxiliary atlassian-plugin.xml style resources in the class path
     * @return a module registration object that you should hold onto for the life of the plugin and call un-register on when
     *         thr plugin is coming down
     */
    public ModuleRegistrationHandle loadModules(final Plugin plugin, String... pathsToAuxAtlassianPluginXMLs)
    {
        List<ModuleDescriptor> modules = Lists.newArrayList();

        final ModuleDescriptorFactory moduleDescriptorFactory = combinedModuleDescriptorFactoryProvider.getModuleDescriptorFactory();
        for (String auxAtlassianPluginXML : pathsToAuxAtlassianPluginXMLs)
        {
            final PluginDescriptorReader descriptorReader = getPluginDescriptorReader(plugin, auxAtlassianPluginXML);
            for (Element moduleElement : descriptorReader.getModules())
            {
                loadModulesHelper(plugin, moduleElement, moduleDescriptorFactory, modules);
            }
        }
        return dynamicModuleRegistration.registerDescriptors(plugin, modules);
    }

    public ModuleRegistrationHandle loadModules(final Plugin plugin, Element element)
    {
        List<ModuleDescriptor> modules = Lists.newArrayList();
        final ModuleDescriptorFactory moduleDescriptorFactory = combinedModuleDescriptorFactoryProvider.getModuleDescriptorFactory();
        loadModulesHelper(plugin, element, moduleDescriptorFactory, modules);
        return dynamicModuleRegistration.registerDescriptors(plugin, modules);
    }

    protected ModuleDescriptor<?> createModuleDescriptor(final Plugin plugin, String moduleType, final Element element, final ModuleDescriptorFactory moduleDescriptorFactory) throws PluginParseException
    {
        final ModuleDescriptor<?> moduleDescriptor;

        // Try to retrieve the module descriptor
        try
        {
            moduleDescriptor = moduleDescriptorFactory.getModuleDescriptor(moduleType);
        }
        // When there's a problem loading a module, return an UnrecognisedModuleDescriptor with error
        catch (final Throwable e)
        {
            final UnrecognisedModuleDescriptor descriptor = createUnrecognisedModuleDescriptor(plugin, element, e, moduleDescriptorFactory);

            log.error("There were problems loading the module '{}' in plugin '{}'. The module has been disabled.", moduleType, plugin.getName());
            log.error(descriptor.getErrorText(), e);

            return descriptor;
        }

        // When the module descriptor has been excluded, null is returned (PLUG-5)
        if (moduleDescriptor == null)
        {
            log.info("The module '{}' in plugin '{}' is in the list of excluded module descriptors, so not enabling.", moduleType, plugin.getName());
            return null;
        }
        String moduleKey = element.attributeValue("key");

        // Once we have the module descriptor, create it using the given information
        try
        {
            moduleDescriptor.init(plugin, element);
        }
        // If it fails, return a dummy module that contains the error
        catch (final Exception e)
        {
            final UnloadableModuleDescriptor descriptor = createUnloadableModuleDescriptor(plugin, element, e, moduleDescriptorFactory);

            log.error("There were problems loading the module '{}'. The module and its plugin have been disabled.", moduleType);
            log.error(descriptor.getErrorText(), e);

            return descriptor;
        }

        GhettoCode.addModuleDescriptorElement(plugin, element, moduleKey);

        return moduleDescriptor;
    }

    private void loadModulesHelper(Plugin plugin, Element moduleElement, ModuleDescriptorFactory moduleDescriptorFactory, List<ModuleDescriptor> modules){
        final String moduleType = moduleElement.getName();

        final ModuleDescriptor<?> moduleDescriptor = createModuleDescriptor(plugin, moduleType, moduleElement, moduleDescriptorFactory);

        // If we're not loading the module descriptor, null is returned, so we skip it
        if (moduleDescriptor == null)
        {
            return;
        }

        //
        // if we have a key and we already have a module of that name, then blow up!
        //
        if (moduleDescriptor.getKey() != null && plugin.getModuleDescriptor(moduleDescriptor.getKey()) != null)
        {
            throw new PluginParseException("Found duplicate key '" + moduleDescriptor.getKey() + "' within plugin '" + plugin.getKey() + "'");
        }

        if (moduleDescriptor instanceof UnloadableModuleDescriptor)
        {
            log.error("There were errors loading the module '" + moduleDescriptor.getName() + "'.");
        }
        else
        {
            log.debug("Loaded module '{}' ", moduleDescriptor.getKey());
            modules.add(moduleDescriptor);
        }
    }

    private PluginDescriptorReader getPluginDescriptorReader(Plugin plugin, String auxAtlassianPluginXML)
    {
        log.info(String.format("Reading modules from '%s'", auxAtlassianPluginXML));
        InputStream auxXML = readXML(plugin, auxAtlassianPluginXML);
        if (auxXML == null)
        {
            throw new PluginParseException("Unable to get InputStream for '" + auxAtlassianPluginXML + "'");
        }
        return PluginDescriptorReader.createDescriptorReadert(auxXML);
    }


    private InputStream readXML(Plugin plugin, String pathToAuxPluginsXML)
    {
        return plugin.getClassLoader().getResourceAsStream(pathToAuxPluginsXML);
    }

}
