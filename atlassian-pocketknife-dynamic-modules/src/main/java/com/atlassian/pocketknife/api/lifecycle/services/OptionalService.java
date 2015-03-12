package com.atlassian.pocketknife.api.lifecycle.services;

import java.io.Closeable;
import java.util.List;
import javax.annotation.concurrent.ThreadSafe;

/**
 * This represents optional references to one or more underlying services.  You can tell if they are  present or
 * not and take action appropriately.
 *
 * NOTE : This optional service pattern has a lifecycle.  You MUST close the object when you are done with it.
 * Its a {@link java.io.Closeable} so you can use it in a try() statement in Java 1.7 and above.
 *
 * In general you should obtain an OptionalService use it and then close it.  The are not particularity expensive to obtain
 * and use.
 *
 * Although the class is ThreadSafe is not really recommended to use across Threads.  Because this would imply it has a lifecycle
 * that has hard to manage.
 *
 * @since v0.x
 */
@ThreadSafe
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
