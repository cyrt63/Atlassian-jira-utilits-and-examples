package com.atlassian.pocketknife.api.lifecycle.services;

import java.util.List;

/**
 * This represents optional references to one or more underlying services.  You can tell if they are  present or
 * not and take action appropriately.
 *
 * @since v0.x
 */
public interface OptionalService<T>
{
    /**
     * @return true if the service or services where available when this service reference was obtained
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
     * Returns services.  This is safe to call even if the services are not available in which case its an no-op.
     *
     * @return true if the services are able to be released.  In general you should not have to worry about this return value.
     */
    boolean release();
}
