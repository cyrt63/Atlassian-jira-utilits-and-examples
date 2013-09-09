package com.atlassian.pocketknife.internal.lifecycle.modules;

import com.atlassian.pocketknife.api.lifecycle.modules.ModuleRegistrationHandle;
import com.google.common.collect.Lists;
import org.junit.Assert;
import org.junit.Test;

import java.util.ArrayList;

import static com.atlassian.pocketknife.internal.lifecycle.modules.DynamicModuleRegistration.ModuleRegistrationHandleImpl;
import static com.atlassian.pocketknife.internal.lifecycle.modules.DynamicModuleRegistration.TrackedDynamicModule;

/**
 */
public class DynamicModuleRegistrationTest
{

    private static class MockTrackedDynamicModule extends DynamicModuleRegistration.TrackedDynamicModule
    {
        private int called = 0;

        MockTrackedDynamicModule()
        {
            super(null, null);
        }

        @Override
        void unregister()
        {
            called += 1;
        }

        private int callCount()
        {
            return called;
        }
    }

    @Test
    public void testModuleRegistration() throws Exception
    {

        ArrayList<TrackedDynamicModule> registrations = makeModules();

        ModuleRegistrationHandle handle = new ModuleRegistrationHandleImpl(registrations);
        handle.unregister();

        assertUnregistered(registrations);

        // put many together
        registrations = makeModules();
        ArrayList<TrackedDynamicModule> extras = makeModules();

        handle = new ModuleRegistrationHandleImpl(registrations);
        for (TrackedDynamicModule extra : extras)
        {
            ModuleRegistrationHandleImpl extraHandle = new ModuleRegistrationHandleImpl(Lists.newArrayList(extra));
            handle = handle.union(extraHandle);
        }
        handle.unregister();

        assertUnregistered(registrations);
        assertUnregistered(extras);


        handle.unregister();
        assertUnregistered(registrations);
        assertUnregistered(extras);
    }

    private void assertUnregistered(final ArrayList<TrackedDynamicModule> registrations)
    {
        for (TrackedDynamicModule registration : registrations)
        {
            assertUnregistered((MockTrackedDynamicModule) registration);
        }
    }

    private void assertUnregistered(final MockTrackedDynamicModule registration)
    {
        Assert.assertEquals(1,registration.callCount());
    }

    private ArrayList<TrackedDynamicModule> makeModules()
    {
        MockTrackedDynamicModule mod1 = new MockTrackedDynamicModule();
        MockTrackedDynamicModule mod2 = new MockTrackedDynamicModule();
        MockTrackedDynamicModule mod3 = new MockTrackedDynamicModule();

        return Lists.<TrackedDynamicModule>newArrayList(mod1, mod2, mod3);
    }
}
