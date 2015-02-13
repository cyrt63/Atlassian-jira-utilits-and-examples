package com.atlassian.pocketknife.spi.lifecycle.services;

import com.atlassian.pocketknife.api.lifecycle.services.OptionalService;
import com.atlassian.pocketknife.internal.lifecycle.services.OptionalServiceImpl;
import org.osgi.framework.BundleContext;
import org.osgi.framework.Filter;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * An optional service accessor allows you to access an underlying component service in an optional manner.  If the
 * service is not present, you can know and if it is present then you can use it
 * <p/>
 * This is designed for you to derive a simple @Component class from that names the OSGI service name that backs this
 * object.
 * <p/>
 * <pre>
 * <code>
 *    @ Component
 *    class SomeServiceAcessor extends OptionalServiceAccessor<SomeService> {
 *       public SomeServiceAcessor(final BundleContext bundleContext) {
 *          super(bundleContext, "com.code.SomeService");
 *       }
 *    }
 * </code>
 * </pre>
 */
public class OptionalServiceAccessor<T>
{
    private final BundleContext bundleContext;
    private final String serviceName;

    public OptionalServiceAccessor(BundleContext bundleContext, String serviceName)
    {
        this.bundleContext = checkNotNull(bundleContext);
        this.serviceName = checkNotNull(serviceName);
    }

    /**
     * @return a service reference object that knows if the service is available and how to get it
     */
    public OptionalService<T> obtain()
    {
        return new OptionalServiceImpl<T>(bundleContext, serviceName, null);
    }

    /**
     * This will return a service reference that filters the list of services based on the spec
     *
     * @param filter see {@link org.osgi.framework.Filter} for details on how it filters and its syntax
     * @return a service reference object that knows if the service is available and how to get it
     */
    public OptionalService<T> obtain(Filter filter)
    {
        return new OptionalServiceImpl<T>(bundleContext, serviceName, filter);
    }
}
