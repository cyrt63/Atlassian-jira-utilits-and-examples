package com.atlassian.pocketknife.api.lifecycle.modules;

import com.atlassian.plugin.Plugin;
import org.dom4j.Element;

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
public interface DynamicModuleDescriptorFactory
{
    /**
     * This allows you to load one or more auxiliary atlassian-plugin.xml files as modules under your control.
     *
     * @param plugin                        your plugin that you want the modules to belong to
     * @param pathsToAuxAtlassianPluginXMLs a set of 1 or more auxiliary atlassian-plugin.xml style resources nn the class path
     * @return a module registration object that you should hold onto for the life of the plugin and call unregister on when
     *         thr plugin is coming down
     */
    public ModuleRegistrationHandle loadModules(final Plugin plugin, final String... pathsToAuxAtlassianPluginXMLs);
    /**
     * This allows you to load one or more auxiliary atlassian-plugin.xml files as modules under your control.
     *
     * @param plugin                        your plugin that you want the modules to belong to
     * @param resourceLoader                the resource loader to use to read objects
     * @param pathsToAuxAtlassianPluginXMLs a set of 1 or more auxiliary atlassian-plugin.xml style resources nn the class path
     * @return a module registration object that you should hold onto for the life of the plugin and call unregister on when
     *         thr plugin is coming down
     */
    public ModuleRegistrationHandle loadModules(final Plugin plugin, final ResourceLoader resourceLoader, final String... pathsToAuxAtlassianPluginXMLs);

    /**
     * This allows you to dynamically generated modules from html elements files as modules under your control.
     *
     * @param plugin                        your plugin that you want the modules to belong to
     * @param element an dom element containing the module descriptor as if it were loaded from an actual file.
     * @return a module registration object that you should hold onto for the life of the plugin and call unregister on when
     *         thr plugin is coming down
     */
    public ModuleRegistrationHandle loadModules(final Plugin plugin, final Element element);

}
