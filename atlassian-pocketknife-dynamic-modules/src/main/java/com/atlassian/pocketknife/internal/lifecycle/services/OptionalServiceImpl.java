package com.atlassian.pocketknife.internal.lifecycle.services;

import com.atlassian.pocketknife.api.lifecycle.services.OptionalService;
import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Throwables;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import org.osgi.framework.BundleContext;
import org.osgi.framework.Filter;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.framework.ServiceReference;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

public class OptionalServiceImpl<T> implements OptionalService<T>
{
    private final BundleContext bundleContext;
    private final List<ServiceReference> serviceReferences;
    private final List<T> services;
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

    @SuppressWarnings ("unchecked")
    @VisibleForTesting
    OptionalServiceImpl(BundleContext bundleContext, String serviceName, ServiceReference[] serviceReferences)
    {
        this.bundleContext = bundleContext;
        this.serviceName = serviceName;
        this.serviceReferences = Lists.newArrayList(serviceReferences);
        this.closed = new AtomicBoolean(false);
        // ok now obtain the underlying services now, since this pattern has lifecycle and that way the service
        // will be know to be available and avoid concurrency problems if we use a lazy pattern
        this.services = new ArrayList<T>(this.serviceReferences.size());
        for (ServiceReference serviceReference : serviceReferences)
        {
            if (serviceReference != null)
            {
                T service = (T) bundleContext.getService(serviceReference);
                if (service != null)
                {
                    services.add(service);
                }
            }
        }
    }

    private void stateCheck()
    {
        if (!isAvailable())
        {
            throw new IllegalStateException("You have called on get() without checking that the service is in fact available!");
        }
    }

    @Override
    public boolean isAvailable()
    {
        return !closed.get() && !services.isEmpty();
    }

    @Override
    public T get()
    {
        stateCheck();
        return services.get(0);
    }

    @Override
    public List<T> getAll()
    {
        stateCheck();
        return ImmutableList.copyOf(services);
    }

    @Override
    public void close()
    {
        if (closed.compareAndSet(false, true))
        {
            // null out of service list just for form
            this.services.clear();
            Throwable t = null;
            //
            // now try to release the service references themselves
            for (ServiceReference serviceReference : serviceReferences)
            {
                // we try to release all references even if some of them fail.
                // we will throw the
                try
                {
                    if (serviceReference != null)
                    {
                        bundleContext.ungetService(serviceReference);
                    }
                }
                catch (Throwable thrown)
                {
                    if (t == null)
                    {
                        t = thrown; // our first exception
                    }
                    else
                    {
                        t.addSuppressed(thrown); // and splat the next one into te previous one
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
