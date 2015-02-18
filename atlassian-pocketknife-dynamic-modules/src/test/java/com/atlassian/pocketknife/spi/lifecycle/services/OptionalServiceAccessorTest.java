package com.atlassian.pocketknife.spi.lifecycle.services;


import com.atlassian.pocketknife.api.lifecycle.services.OptionalService;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;
import org.osgi.framework.BundleContext;
import org.osgi.framework.Filter;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.framework.ServiceReference;

import java.util.List;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@RunWith (MockitoJUnitRunner.class)
public class OptionalServiceAccessorTest
{

    static final String SERVICE_NAME = "com.code.SomeServiceToCall";
    static final String FILTER_STRING = "filterString";

    private Filter filter;

    private class SomeServiceToCall
    {

        public String someApiToCall()
        {
            return "hello";
        }
    }

    private OptionalServiceAccessor<SomeServiceToCall> serviceAccessor;

    @Mock
    BundleContext bundleContext;

    ServiceReference[] serviceReferences;
    ServiceReference serviceReference;


    private SomeServiceToCall someServiceToCall = new SomeServiceToCall();

    @Before
    public void setUp() throws Exception
    {
        filter = mock(Filter.class);
        when(filter.toString()).thenReturn("filterString");

        serviceReferences = new ServiceReference[3];
        serviceReferences[0] = mock(ServiceReference.class);
        serviceReferences[1] = mock(ServiceReference.class);
        serviceReferences[2] = mock(ServiceReference.class);

        serviceReference = serviceReferences[0];

        serviceAccessor = new OptionalServiceAccessor<SomeServiceToCall>(bundleContext, SERVICE_NAME);

    }

    @Test
    public void service_not_available() throws Exception
    {
        // assemble
        when(bundleContext.getServiceReferences(SERVICE_NAME, null)).thenReturn(null);

        // act
        OptionalService<SomeServiceToCall> reference = serviceAccessor.obtain();

        // assert
        assertThat(reference.isAvailable(), equalTo(false));
    }

    @Test
    public void service_is_available() throws Exception
    {
        // assemble
        assembleMocks();

        // act
        OptionalService<SomeServiceToCall> reference = serviceAccessor.obtain();

        // assert
        assertThat(reference.isAvailable(), equalTo(true));
        assertThat(reference.get(), equalTo(someServiceToCall));
    }

    @Test
    public void multiple_services_are_available() throws Exception
    {
        // assemble
        assembleMocks();

        // act
        OptionalService<SomeServiceToCall> reference = serviceAccessor.obtain();

        // assert
        assertThat(reference.isAvailable(), equalTo(true));
        assertThat(reference.get(), equalTo(someServiceToCall));

        List<SomeServiceToCall> allServices = reference.getAll();
        assertThat(allServices.size(), equalTo(3));
    }

    @Test
    public void multiple_services_with_filter() throws Exception
    {
        // assemble
        assembleMocks(FILTER_STRING);

        // act
        OptionalService<SomeServiceToCall> reference = serviceAccessor.obtain(filter);

        // assert
        assertThat(reference.isAvailable(), equalTo(true));
        assertThat(reference.get(), equalTo(someServiceToCall));

        List<SomeServiceToCall> allServices = reference.getAll();
        assertThat(allServices.size(), equalTo(3));
    }

    @Test
    public void closed_works() throws Exception
    {
        // assemble
        assembleMocks();

        // act
        OptionalService<SomeServiceToCall> reference = serviceAccessor.obtain();
        reference.close();

        // assert
        Mockito.verify(bundleContext,Mockito.times(3)).ungetService(Mockito.any(ServiceReference.class));
    }

    @Test
    public void close_is_idempotent() throws Exception
    {
        // assemble
        assembleMocks();

        // act
        OptionalService<SomeServiceToCall> reference = serviceAccessor.obtain();
        reference.close();
        reference.close();
        reference.close();

        // assert
        assertThat(reference.isAvailable(),equalTo(false));
        Mockito.verify(bundleContext,Mockito.times(3)).ungetService(Mockito.any(ServiceReference.class));
    }

    @Test(expected = RuntimeException.class)
    public void close_catches_exceptions() throws Exception
    {
        // assemble
        when(bundleContext.getServiceReferences(SERVICE_NAME, null)).thenReturn(serviceReferences);

        when(bundleContext.ungetService(serviceReferences[0])).thenThrow(new IllegalStateException("testing testing"));
        when(bundleContext.ungetService(serviceReferences[1])).thenReturn(true);
        when(bundleContext.ungetService(serviceReferences[2])).thenThrow(new IllegalStateException("testing testing"));

        // act
        OptionalService<SomeServiceToCall> reference = serviceAccessor.obtain();
        try
        {
            reference.close();
        }
        finally
        {
            Mockito.verify(bundleContext, Mockito.times(3)).ungetService(Mockito.any(ServiceReference.class));
        }
    }

    private void assembleMocks() throws InvalidSyntaxException
    {
        assembleMocks(null);
    }

    private void assembleMocks(String filterStr) throws InvalidSyntaxException
    {

        when(bundleContext.getServiceReferences(SERVICE_NAME, filterStr)).thenReturn(serviceReferences);
        when(bundleContext.getService(serviceReferences[0])).thenReturn(someServiceToCall);
        when(bundleContext.getService(serviceReferences[1])).thenReturn(someServiceToCall);
        when(bundleContext.getService(serviceReferences[2])).thenReturn(someServiceToCall);

        when(bundleContext.ungetService(serviceReferences[0])).thenReturn(true);
        when(bundleContext.ungetService(serviceReferences[1])).thenReturn(true);
        when(bundleContext.ungetService(serviceReferences[2])).thenReturn(true);
    }

}