package com.atlassian.pocketknife.api.autowire;

/**
 * A simple class that can auto-wire components for this plugin
 */
public interface PluginAutowirer {
    /**
     * Auto wires a component of the specified class
     *
     * @param componentClass the class of component to auto wire
     * @return a new instance of componentClass auto-wired via this plugin
     */
    <T> T autowire(Class<T> componentClass);
}
