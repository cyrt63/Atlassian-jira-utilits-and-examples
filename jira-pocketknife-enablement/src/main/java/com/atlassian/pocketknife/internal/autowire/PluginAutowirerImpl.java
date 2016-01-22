package com.atlassian.pocketknife.internal.autowire;


import com.atlassian.plugin.PluginAccessor;
import com.atlassian.plugin.module.ContainerManagedPlugin;
import com.atlassian.pocketknife.api.autowire.PluginAutowirer;
import com.atlassian.pocketknife.spi.info.PocketKnifePluginInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class PluginAutowirerImpl implements PluginAutowirer {
    private final PluginAccessor pluginAccessor;
    private final PocketKnifePluginInfo pocketKnifePluginInfo;

    @Autowired
    public PluginAutowirerImpl(PluginAccessor pluginAccessor, PocketKnifePluginInfo pocketKnifePluginInfo) {
        this.pluginAccessor = pluginAccessor;
        this.pocketKnifePluginInfo = pocketKnifePluginInfo;
    }

    @Override
    public <T> T autowire(Class<T> componentClass) {
        ContainerManagedPlugin plugin = (ContainerManagedPlugin) pluginAccessor.getPlugin(pocketKnifePluginInfo.getPluginKey());
        return plugin.getContainerAccessor().createBean(componentClass);
    }
}
