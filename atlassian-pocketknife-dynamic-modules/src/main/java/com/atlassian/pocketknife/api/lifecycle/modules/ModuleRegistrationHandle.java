package com.atlassian.pocketknife.api.lifecycle.modules;

/**
 * Once you have loaded some dynamic modules via {@link DynamicModuleDescriptorFactory} you will
 * be given back a module registration handle that you must hold onto for the life of the plugin.
 * <p/>
 * Then when the plugin is coming down you should call {@link #unregister()} to clean up.
 */
public interface ModuleRegistrationHandle
{
    /**
     * Called to clean up previously registered modules
     */
    void unregister();


    /**
     * This allows you to concatenate two module handles together to produce the union of both and hence
     * allow you to un-register two handles in onw call.
     *
     * @param other the other handle
     * @return a union of this handle and the other handle
     */
    ModuleRegistrationHandle union(ModuleRegistrationHandle other);
}
