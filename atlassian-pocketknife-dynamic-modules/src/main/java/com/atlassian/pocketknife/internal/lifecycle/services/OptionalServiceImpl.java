package com.atlassian.pocketknife.internal.lifecycle.services;

import com.atlassian.pocketknife.api.lifecycle.services.OptionalService;
import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.Lists;
import org.osgi.framework.BundleContext;
import org.osgi.framework.Filter;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.framework.ServiceReference;

import java.util.List;

public class OptionalServiceImpl<T> implements OptionalService<T>
{

    private final BundleContext bundleContext;
    private final ServiceReference[] serviceReferences;
    private final String serviceName;

    public OptionalServiceImpl(BundleContext bundleContext, String serviceName, Filter filter)
    {
        this(bundleContext, serviceName, getServiceReferences(bundleContext, serviceName, filter));
    }

    private static ServiceReference[] getServiceReferences(final BundleContext bundleContext, final String serviceName, Filter filter)
    {
        try
        {
            String filterString = filter != null ? filter.toString() : null;
            return bundleContext.getServiceReferences(serviceName, filterString);
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
    }

    @Override
    public boolean isAvailable()
    {
        return serviceReferences != null && serviceReferences.length > 0;
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
    public boolean release()
    {
        boolean flag = true;
        if (serviceReferences != null)
        {
            for (ServiceReference serviceReference : serviceReferences)
            {
                flag = flag && bundleContext.ungetService(serviceReference);
            }
        }
        return flag;
    }

    @Override
    public String toString()
    {
        return String.format("%s : %s", serviceName, isAvailable() ? "available" : "not available");
    }
}
