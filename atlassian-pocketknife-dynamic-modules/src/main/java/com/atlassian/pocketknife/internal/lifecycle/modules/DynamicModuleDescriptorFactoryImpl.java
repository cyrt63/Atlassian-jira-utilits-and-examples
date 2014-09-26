package com.atlassian.pocketknife.internal.lifecycle.modules;

import com.atlassian.plugin.ModuleDescriptor;
import com.atlassian.plugin.ModuleDescriptorFactory;
import com.atlassian.plugin.Plugin;
import com.atlassian.plugin.PluginParseException;
import com.atlassian.plugin.descriptors.UnloadableModuleDescriptor;
import com.atlassian.plugin.descriptors.UnrecognisedModuleDescriptor;
import com.atlassian.pocketknife.api.lifecycle.modules.DynamicModuleDescriptorFactory;
import com.atlassian.pocketknife.api.lifecycle.modules.LoaderConfiguration;
import com.atlassian.pocketknife.api.lifecycle.modules.ModuleRegistrationHandle;
import com.atlassian.pocketknife.api.lifecycle.modules.ResourceLoader;
import com.google.common.collect.Lists;
import org.dom4j.Element;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.InputStream;
import java.util.Arrays;
import java.util.List;

import static com.atlassian.plugin.descriptors.UnloadableModuleDescriptorFactory.createUnloadableModuleDescriptor;
import static com.atlassian.plugin.descriptors.UnrecognisedModuleDescriptorFactory.createUnrecognisedModuleDescriptor;
import static com.atlassian.pocketknife.internal.lifecycle.modules.Kit.getModuleIdentifier;
import static com.atlassian.pocketknife.internal.lifecycle.modules.Kit.pluginIdentifier;
import static java.lang.String.format;

/**
 * The use of this module factory allows a plugin to have a "controlled launch" of its functionality.  The plugin can
 * decide what modules it is going to offer to the world and at what time.
 * <p/>
 * This is in contrast to the declarative "all at once" pattern that atlassian-plugin.xml forces you into.
 * <p/>
 * For example you can wait until you plugin has run all its upgrade tasks and determined licensing BEFORE you release
 * your web-sections and actions to the world.
 * <p/>
 * <quote> "We will decide what modules come to this plugin and the circumstances in which they come!" -  John Howard -
 * Dog Whistling 2002 </quote>
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

    @Override
    public ModuleRegistrationHandle loadModules(final Plugin plugin, final String... pathsToAuxAtlassianPluginXMLs)
    {
        final LoaderConfiguration loaderConfiguration = new LoaderConfiguration(plugin);
        loaderConfiguration.setPathsToAuxAtlassianPluginXMLs(Arrays.asList(pathsToAuxAtlassianPluginXMLs));

        return loadModules(loaderConfiguration);
    }

    @Override
    public ModuleRegistrationHandle loadModules(final Plugin plugin, final ResourceLoader resourceLoader, final String... pathsToAuxAtlassianPluginXMLs)
    {
        final LoaderConfiguration loaderConfiguration = new LoaderConfiguration(plugin);
        loaderConfiguration.setResourceLoader(resourceLoader);
        loaderConfiguration.setPathsToAuxAtlassianPluginXMLs(Arrays.asList(pathsToAuxAtlassianPluginXMLs));

        return loadModules(loaderConfiguration);
    }

    @Override
    public ModuleRegistrationHandle loadModules(final LoaderConfiguration loaderConfiguration) {
        List<ModuleDescriptor> modules = Lists.newArrayList();

        final ModuleDescriptorFactory moduleDescriptorFactory = combinedModuleDescriptorFactoryProvider.getModuleDescriptorFactory();
        for (String auxAtlassianPluginXML : loaderConfiguration.getPathsToAuxAtlassianPluginXMLs())
        {
            final PluginDescriptorReader descriptorReader = getPluginDescriptorReader(loaderConfiguration.getResourceLoader(), auxAtlassianPluginXML);
            for (Element moduleElement : descriptorReader.getModules())
            {
                loadModulesHelper(loaderConfiguration, moduleElement, moduleDescriptorFactory, modules);
            }
        }

        return dynamicModuleRegistration.registerDescriptors(loaderConfiguration.getPlugin(), modules);
    }

    @Override
    public ModuleRegistrationHandle loadModules(final Plugin plugin, Element element)
    {
        List<ModuleDescriptor> modules = Lists.newArrayList();
        final ModuleDescriptorFactory moduleDescriptorFactory = combinedModuleDescriptorFactoryProvider.getModuleDescriptorFactory();

        loadModulesHelper(new LoaderConfiguration(plugin), element, moduleDescriptorFactory, modules);
        return dynamicModuleRegistration.registerDescriptors(plugin, modules);
    }

    private void loadModulesHelper(final LoaderConfiguration loaderConfiguration, Element moduleElement, ModuleDescriptorFactory moduleDescriptorFactory, List<ModuleDescriptor> modules)
    {
        final String moduleType = moduleElement.getName();
        String moduleKey = getModuleIdentifier(moduleElement);
        String pluginId = pluginIdentifier(loaderConfiguration.getPlugin());

        final ModuleDescriptor<?> moduleDescriptor = createModuleDescriptor(loaderConfiguration, moduleType, moduleElement, moduleDescriptorFactory);

        // If we're not loading the module descriptor, null is returned, so we skip it
        if (moduleDescriptor == null)
        {
            log.error(format("Skipping the module '%s' with key '%s' in plugin '%s'. Null was returned from the module descriptor factory...", moduleType, moduleKey, pluginId));
            return;
        }

        if (moduleDescriptor instanceof UnloadableModuleDescriptor)
        {
            log.error(format("There were problems loading the module '%s' with key '%s' in plugin '%s'. UnloadableModuleDescriptor returned.", moduleType, moduleKey, pluginId));
        }
        else
        {
            log.info(format("Loaded module '%s' with key '%s' in plugin '%s'.", moduleType, moduleKey, pluginId));
            modules.add(moduleDescriptor);
        }
    }

    protected ModuleDescriptor<?> createModuleDescriptor(
            final LoaderConfiguration loaderConfiguration,
            final String moduleType,
            final Element element,
            final ModuleDescriptorFactory moduleDescriptorFactory)
            throws PluginParseException
    {
        final ModuleDescriptor<?> moduleDescriptor;

        // Try to retrieve the module descriptor
        String moduleIdentifier = getModuleIdentifier(element);
        String pluginId = pluginIdentifier(loaderConfiguration.getPlugin());
        try
        {
            log.info(format("Creating module of type '%s' with key '%s' in plugin '%s'", moduleType, moduleIdentifier, pluginId));
            moduleDescriptor = moduleDescriptorFactory.getModuleDescriptor(moduleType);
            if (moduleDescriptor != null)
            {
                log.info(format("Successfully created module as type '%s'", moduleDescriptor.getClass().getName()));
            }
        }
        // When there's a problem loading a module, return an UnrecognisedModuleDescriptor with error
        catch (final Throwable e)
        {
            final UnrecognisedModuleDescriptor descriptor = createUnrecognisedModuleDescriptor(loaderConfiguration.getPlugin(), element, e, moduleDescriptorFactory);

            log.error(format("There were problems loading the module '%s' with key '%s' in plugin '%s'. The module has been disabled.", moduleType, moduleIdentifier, pluginId));
            log.error(descriptor.getErrorText(), e);

            return descriptor;
        }

        // When the module descriptor has been excluded, null is returned (PLUG-5)
        if (moduleDescriptor == null)
        {
            log.info(format("The module '%s' with key '%s' in plugin '%s' is in the list of excluded module descriptors, so not enabling.", moduleType, moduleIdentifier, pluginId));
            return null;
        }
        else if (moduleDescriptor.getKey() != null && loaderConfiguration.getPlugin().getModuleDescriptor(moduleDescriptor.getKey()) != null)
        {
            if (loaderConfiguration.isFailOnDuplicateKey()) {
                //
                // if have a key and we already have a module of that name, then blow up!
                //
                throw new PluginParseException("Found duplicate key '" + getModuleIdentifier(element) + "' within plugin '" + pluginId + "'");
            }
            else
            {
                log.warn(format("Found duplicate key '%s' within plugin '%s', but ignoring this and moving on....."),  getModuleIdentifier(element), pluginId);
                return null;
            }
        }

        // Once we have the module descriptor, create it using the given information
        try
        {
            log.info(format("Calling init on module of type '%s' with key '%s' in plugin '%s'", moduleType, moduleIdentifier, pluginId));
            moduleDescriptor.init(loaderConfiguration.getPlugin(), element);
        }
        // If it fails, return a dummy module that contains the error
        catch (final Exception e)
        {
            final UnloadableModuleDescriptor descriptor = createUnloadableModuleDescriptor(loaderConfiguration.getPlugin(), element, e, moduleDescriptorFactory);

            log.error(format("There were problems loading the module '%s' with key '%s'. The module and its plugin have been disabled.", moduleType, moduleIdentifier));
            log.error(descriptor.getErrorText(), e);

            return descriptor;
        }

        GhettoCode.addModuleDescriptorElement(loaderConfiguration.getPlugin(), element, element.attributeValue("key"));

        return moduleDescriptor;
    }

    private PluginDescriptorReader getPluginDescriptorReader(ResourceLoader resourceLoader, String auxAtlassianPluginXML)
    {
        InputStream auxXML = readXML(resourceLoader, auxAtlassianPluginXML);
        if (auxXML == null)
        {
            throw new PluginParseException("Unable to get InputStream for '" + auxAtlassianPluginXML + "'");
        }
        return PluginDescriptorReader.createDescriptorReader(auxXML);
    }


    private InputStream readXML(ResourceLoader resourceLoader, String pathToAuxPluginsXML)
    {
        log.info(format("Reading module xml %s", pathToAuxPluginsXML));
        return resourceLoader.getResourceAsStream(pathToAuxPluginsXML);
    }

}
