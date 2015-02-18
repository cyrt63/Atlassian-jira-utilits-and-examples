package com.atlassian.pocketknife.api.lifecycle.services;

import java.io.Closeable;
import java.util.List;

/**
 * This represents optional references to one or more underlying services.  You can tell if they are  present or
 * not and take action appropriately.
 *
 * @since v0.x
 */
public interface OptionalService<T> extends Closeable
{
    /**
     * @return true if the service or services were available when this service reference was obtained
     */
    boolean isAvailable();

    /**
     * @return the first underlying service if its available
     * @throws IllegalStateException if the service is not available.  You should have checked with {@link
     * #isAvailable()} before making this call
     */
    T get();

    /**
     * @return the underlying services if they are available
     * @throws IllegalStateException if the services are not available.  You should have checked with {@link
     * #isAvailable()} before making this call
     */
    List<T> getAll();

    /**
     * Un-registers services.  This is safe to call even if the services are not available in which case its an no-op.
     *
     * In general you should not have to worry about state exceptions, unless something pretty dramatic
     * is happening within your bundle
     *
     * @throws IllegalStateException If this BundleContext is no
     *         longer valid.
     * @throws IllegalArgumentException If the underlying
     *         <code>ServiceReference</code> was not created by the same
     *         framework instance as the underlying <code>BundleContext</code>.
     */
    void close();
}
