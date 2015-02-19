package com.atlassian.pocketknife.internal.lifecycle.services;

import com.atlassian.pocketknife.api.lifecycle.services.OptionalService;
import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Throwables;
import com.google.common.collect.Lists;
import org.osgi.framework.BundleContext;
import org.osgi.framework.Filter;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.framework.ServiceReference;
import org.slf4j.Logger;

import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.slf4j.LoggerFactory.getLogger;

public class OptionalServiceImpl<T> implements OptionalService<T>
{
    private static final Logger log = getLogger(OptionalServiceImpl.class);

    private final BundleContext bundleContext;
    private final ServiceReference[] serviceReferences;
    private final String serviceName;
    private final AtomicBoolean closed;

    public OptionalServiceImpl(BundleContext bundleContext, String serviceName, Filter filter)
    {
        this(bundleContext, serviceName, getServiceReferences(bundleContext, serviceName, filter));
    }

    private static ServiceReference[] getServiceReferences(final BundleContext bundleContext, final String serviceName, Filter filter)
    {
        try
        {
            String filterString = filter != null ? filter.toString() : null;
            ServiceReference[] serviceRefs = bundleContext.getServiceReferences(serviceName, filterString);
            return serviceRefs == null ? new ServiceReference[0] : serviceRefs;
        }
        catch (InvalidSyntaxException e)
        {
            throw new RuntimeException(e);
        }
    }

    @VisibleForTesting
    OptionalServiceImpl(BundleContext bundleContext, String serviceName, ServiceReference[] serviceReferences)
    {
        this.bundleContext = bundleContext;
        this.serviceName = serviceName;
        this.serviceReferences = serviceReferences;
        this.closed = new AtomicBoolean(false);
    }

    @Override
    public boolean isAvailable()
    {
        return !closed.get() && serviceReferences.length > 0;
    }

    @Override
    public T get()
    {
        if (!isAvailable())
        {
            throw new IllegalStateException("You have called on get() without checking that the service is in fact available!");
        }
        //noinspection unchecked
        return (T) bundleContext.getService(serviceReferences[0]);
    }

    @Override
    public List<T> getAll()
    {
        if (!isAvailable())
        {
            throw new IllegalStateException("You have called on get() without checking that the service is in fact available!");
        }
        List<T> services = Lists.newArrayList();
        for (ServiceReference serviceReference : serviceReferences)
        {
            //noinspection unchecked
            T service = (T) bundleContext.getService(serviceReference);
            if (service != null)
            {
                services.add(service);
            }
        }
        return services;
    }

    @Override
    public void close()
    {
        if (closed.compareAndSet(false, true))
        {
            Throwable t = null;
            for (ServiceReference serviceReference : serviceReferences)
            {
                // we try to release all references even if some of them fail.
                // we will throw the
                try
                {
                    bundleContext.ungetService(serviceReference);
                }
                catch (Throwable thrown)
                {
                    if (t == null)
                    {
                        t = thrown;
                    }
                    else
                    {
                        t.addSuppressed(thrown);
                    }
                }
            }
            if (t != null)
            {
                Throwables.propagateIfPossible(t);
                throw new RuntimeException("Unable to unregister OSGi service references",t);
            }
        }
    }

    @Override
    public String toString()
    {
        return String.format("%s : %s", serviceName, isAvailable() ? "available" : "not available");
    }
}
