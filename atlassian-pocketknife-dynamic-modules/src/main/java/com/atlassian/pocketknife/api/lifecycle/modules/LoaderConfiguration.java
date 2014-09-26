package com.atlassian.pocketknife.api.lifecycle.modules;

import com.atlassian.plugin.Plugin;

import java.io.InputStream;
import java.util.Collections;
import java.util.List;

public class LoaderConfiguration {

    private final Plugin plugin;

    private ResourceLoader resourceLoader;
    private boolean failOnDuplicateKey;
    private List<String> pathsToAuxAtlassianPluginXMLs;

    public LoaderConfiguration(Plugin plugin) {
        if (plugin == null) {
            throw new NullPointerException("Plugin has not been specified");
        }
        this.plugin = plugin;

        this.failOnDuplicateKey = true;
        this.resourceLoader = new DefaultResourceLoader();
        this.pathsToAuxAtlassianPluginXMLs = Collections.emptyList();
    }

    /**
     * @return your plugin that you want the modules to belong to
     */
    public Plugin getPlugin() {
        return plugin;
    }

    /**
     * @return the resource loader to use to read objects
     */
    public ResourceLoader getResourceLoader() {
        return resourceLoader;
    }

    public void setResourceLoader(ResourceLoader resourceLoader) {
        this.resourceLoader = resourceLoader;
    }

    /**
     * @return Should we blow up if detect a duplicate key name that has already been loaded
     */
    public boolean isFailOnDuplicateKey() {
        return failOnDuplicateKey;
    }

    public void setFailOnDuplicateKey(boolean failOnDuplicateKey) {
        this.failOnDuplicateKey = failOnDuplicateKey;
    }

    /**
     * @return a list of 1 or more auxiliary atlassian-plugin.xml style resources nn the class path
     */
    public List<String> getPathsToAuxAtlassianPluginXMLs() {
        return pathsToAuxAtlassianPluginXMLs;
    }

    public void setPathsToAuxAtlassianPluginXMLs(List<String> pathsToAuxAtlassianPluginXMLs) {
        this.pathsToAuxAtlassianPluginXMLs = pathsToAuxAtlassianPluginXMLs;
    }

    class DefaultResourceLoader implements ResourceLoader {
        @Override
        public InputStream getResourceAsStream(final String resourceName) {
            return plugin.getClassLoader().getResourceAsStream(resourceName);
        }
    }

}
