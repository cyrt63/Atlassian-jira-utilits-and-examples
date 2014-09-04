package com.atlassian.pocketknife.internal.lifecycle.modules;

import com.atlassian.plugin.ModuleCompleteKey;
import com.atlassian.pocketknife.api.lifecycle.modules.ModuleRegistrationHandle;
import com.google.common.collect.Lists;
import org.hamcrest.CoreMatchers;
import org.junit.Assert;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static com.atlassian.pocketknife.internal.lifecycle.modules.DynamicModuleRegistration.ModuleRegistrationHandleImpl;
import static com.atlassian.pocketknife.internal.lifecycle.modules.DynamicModuleRegistration.TrackedDynamicModule;
import static com.google.common.collect.Lists.newArrayList;

/**
 */
public class DynamicModuleRegistrationTest
{

    private static class MockTrackedDynamicModule extends DynamicModuleRegistration.TrackedDynamicModule
    {
        private int called = 0;

        MockTrackedDynamicModule(final String completeKey)
        {
            super(null, new MockModuleDescriptor(completeKey));
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
            ModuleRegistrationHandleImpl extraHandle = new ModuleRegistrationHandleImpl(newArrayList(extra));
            handle = handle.union(extraHandle);
        }
        handle.unregister();

        assertUnregistered(registrations);
        assertUnregistered(extras);


        handle.unregister();
        assertUnregistered(registrations);
        assertUnregistered(extras);
    }

    @Test
    public void testCompleteKeyMerging() throws Exception
    {
        ArrayList<TrackedDynamicModule> registrations = makeModules("plugin1:module");

        ModuleRegistrationHandle handle = new ModuleRegistrationHandleImpl(registrations);

        assertCompleteKeys(handle.getModules(), newArrayList(
                "plugin1:module1", "plugin1:module2", "plugin1:module3"));


        ArrayList<TrackedDynamicModule> registrationsOther = makeModules("plugin2:module");

        handle = handle.union(new ModuleRegistrationHandleImpl(registrationsOther));

        assertCompleteKeys(handle.getModules(), newArrayList(
                "plugin1:module1", "plugin1:module2", "plugin1:module3",
                "plugin2:module1", "plugin2:module2", "plugin2:module3"));
    }

    private void assertCompleteKeys(final Iterable<ModuleCompleteKey> modules, List<String> keys)
    {
        for (ModuleCompleteKey completeKey : modules)
        {
            String key = completeKey.getCompleteKey();
            Assert.assertThat(keys, CoreMatchers.hasItem(key));
        }
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
        Assert.assertEquals(1, registration.callCount());
    }

    private ArrayList<TrackedDynamicModule> makeModules()
    {
        return makeModules("plugin:module");
    }

    private ArrayList<TrackedDynamicModule> makeModules(String prefix)
    {
        MockTrackedDynamicModule mod1 = new MockTrackedDynamicModule(prefix + "1");
        MockTrackedDynamicModule mod2 = new MockTrackedDynamicModule(prefix + "2");
        MockTrackedDynamicModule mod3 = new MockTrackedDynamicModule(prefix + "3");

        return Lists.<TrackedDynamicModule>newArrayList(mod1, mod2, mod3);
    }
}
