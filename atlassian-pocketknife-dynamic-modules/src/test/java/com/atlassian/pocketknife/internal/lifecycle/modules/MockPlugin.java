package com.atlassian.pocketknife.internal.lifecycle.modules;

import com.atlassian.plugin.ModuleDescriptor;
import com.atlassian.plugin.Plugin;
import com.atlassian.plugin.PluginException;
import com.atlassian.plugin.PluginInformation;
import com.atlassian.plugin.PluginState;
import com.atlassian.plugin.Resourced;
import com.atlassian.plugin.elements.ResourceDescriptor;
import com.atlassian.plugin.elements.ResourceLocation;
import org.dom4j.Element;

import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 */
public class MockPlugin implements Plugin
{
    Map<String, Element> elements = new HashMap<>();
    List<ModuleDescriptor<?>> moduleDescriptors = new ArrayList<>();

    public MockPlugin(final Map<String, Element> elements)
    {
        this.elements = elements;
        for (String key : elements.keySet())
        {
            moduleDescriptors.add(new MockModuleDescriptor("plugin:" + key));
        }
    }

    public Map<String, Element> getModuleElements() {
        return elements;
    }

    @Override
    public Collection<ModuleDescriptor<?>> getModuleDescriptors()
    {
        return moduleDescriptors;
    }

    @Override
    public int getPluginsVersion()
    {
        throw new UnsupportedOperationException("Not implemented");
    }

    @Override
    public void setPluginsVersion(final int i)
    {
        throw new UnsupportedOperationException("Not implemented");
    }

    @Override
    public String getName()
    {
        throw new UnsupportedOperationException("Not implemented");
    }

    @Override
    public void setName(final String s)
    {
        throw new UnsupportedOperationException("Not implemented");
    }

    @Override
    public String getI18nNameKey()
    {
        throw new UnsupportedOperationException("Not implemented");
    }

    @Override
    public void setI18nNameKey(final String s)
    {
        throw new UnsupportedOperationException("Not implemented");
    }

    @Override
    public String getKey()
    {
        throw new UnsupportedOperationException("Not implemented");
    }

    @Override
    public void setKey(final String s)
    {
        throw new UnsupportedOperationException("Not implemented");
    }

    @Override
    public void addModuleDescriptor(final ModuleDescriptor<?> moduleDescriptor)
    {
        throw new UnsupportedOperationException("Not implemented");
    }

    @Override
    public ModuleDescriptor<?> getModuleDescriptor(final String s)
    {
        throw new UnsupportedOperationException("Not implemented");
    }

    @Override
    public <M> List<ModuleDescriptor<M>> getModuleDescriptorsByModuleClass(final Class<M> mClass)
    {
        throw new UnsupportedOperationException("Not implemented");
    }

    @Override
    public boolean isEnabledByDefault()
    {
        throw new UnsupportedOperationException("Not implemented");
    }

    @Override
    public void setEnabledByDefault(final boolean b)
    {
        throw new UnsupportedOperationException("Not implemented");
    }

    @Override
    public PluginInformation getPluginInformation()
    {
        throw new UnsupportedOperationException("Not implemented");
    }

    @Override
    public void setPluginInformation(final PluginInformation pluginInformation)
    {
        throw new UnsupportedOperationException("Not implemented");
    }

    @Override
    public void setResources(final Resourced resourced)
    {
        throw new UnsupportedOperationException("Not implemented");
    }

    @Override
    public PluginState getPluginState()
    {
        throw new UnsupportedOperationException("Not implemented");
    }

    @Override
    public boolean isEnabled()
    {
        throw new UnsupportedOperationException("Not implemented");
    }

    @Override
    public boolean isSystemPlugin()
    {
        throw new UnsupportedOperationException("Not implemented");
    }

    @Override
    public void setSystemPlugin(final boolean b)
    {
        throw new UnsupportedOperationException("Not implemented");
    }

    @Override
    public boolean containsSystemModule()
    {
        throw new UnsupportedOperationException("Not implemented");
    }

    @Override
    public boolean isBundledPlugin()
    {
        throw new UnsupportedOperationException("Not implemented");
    }

    @Override
    public Date getDateLoaded()
    {
        throw new UnsupportedOperationException("Not implemented");
    }

    @Override
    public boolean isUninstallable()
    {
        throw new UnsupportedOperationException("Not implemented");
    }

    @Override
    public boolean isDeleteable()
    {
        throw new UnsupportedOperationException("Not implemented");
    }

    @Override
    public boolean isDynamicallyLoaded()
    {
        throw new UnsupportedOperationException("Not implemented");
    }

    @Override
    public <T> Class<T> loadClass(final String s, final Class<?> aClass) throws ClassNotFoundException
    {
        throw new UnsupportedOperationException("Not implemented");
    }

    @Override
    public ClassLoader getClassLoader()
    {
        throw new UnsupportedOperationException("Not implemented");
    }

    @Override
    public URL getResource(final String s)
    {
        throw new UnsupportedOperationException("Not implemented");
    }

    @Override
    public InputStream getResourceAsStream(final String s)
    {
        throw new UnsupportedOperationException("Not implemented");
    }

    @Override
    public void setEnabled(final boolean b)
    {
        throw new UnsupportedOperationException("Not implemented");
    }

    @Override
    public void close()
    {
        throw new UnsupportedOperationException("Not implemented");
    }

    @Override
    public void install() throws PluginException
    {
        throw new UnsupportedOperationException("Not implemented");
    }

    @Override
    public void uninstall() throws PluginException
    {
        throw new UnsupportedOperationException("Not implemented");
    }

    @Override
    public void enable() throws PluginException
    {
        throw new UnsupportedOperationException("Not implemented");
    }

    @Override
    public void disable() throws PluginException
    {
        throw new UnsupportedOperationException("Not implemented");
    }

    @Override
    public Set<String> getRequiredPlugins()
    {
        throw new UnsupportedOperationException("Not implemented");
    }

    @Override
    public int compareTo(final Plugin o)
    {
        throw new UnsupportedOperationException("Not implemented");
    }

    @Override
    public List<ResourceDescriptor> getResourceDescriptors()
    {
        throw new UnsupportedOperationException("Not implemented");
    }

    @Override
    public List<ResourceDescriptor> getResourceDescriptors(final String s)
    {
        throw new UnsupportedOperationException("Not implemented");
    }

    @Override
    public ResourceDescriptor getResourceDescriptor(final String s, final String s2)
    {
        throw new UnsupportedOperationException("Not implemented");
    }

    @Override
    public ResourceLocation getResourceLocation(final String s, final String s2)
    {
        throw new UnsupportedOperationException("Not implemented");
    }
}
