package com.atlassian.pocketknife.internal.lifecycle.modules;

import com.atlassian.plugin.ModuleCompleteKey;
import com.atlassian.plugin.descriptors.AbstractModuleDescriptor;

/**
 */
public class MockModuleDescriptor extends AbstractModuleDescriptor<Object>
{
    private final ModuleCompleteKey completeKey;

    public MockModuleDescriptor(final String completeKey)
    {
        this.completeKey = new ModuleCompleteKey(completeKey);
    }

    @Override
    public String getCompleteKey()
    {
        return completeKey.getCompleteKey();
    }

    @Override
    public String getPluginKey()
    {
        return completeKey.getPluginKey();
    }

    @Override
    public String getKey()
    {
        return completeKey.getModuleKey();
    }

    @Override
    public Object getModule()
    {
        return null;
    }
}
