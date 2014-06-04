package com.atlassian.pocketknife.api.lifecycle.modules;

import java.io.InputStream;

/**
 * A simple indirection to allow the calling code to be responsible for resource loading however they choose
 * to do that
 */
public interface ResourceLoader
{
    /**
     * Returns a resource as a stream.  Typical implenmentations might use {@link java.lang.ClassLoader#getResourceAsStream(String)}
     * @param resourceName the name of the resource to load
     * @return an stream
     */
    InputStream getResourceAsStream(String resourceName) ;
}
