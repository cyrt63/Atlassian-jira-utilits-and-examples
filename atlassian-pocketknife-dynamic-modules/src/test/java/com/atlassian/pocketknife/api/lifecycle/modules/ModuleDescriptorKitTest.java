package com.atlassian.pocketknife.api.lifecycle.modules;

import com.atlassian.plugin.ModuleDescriptor;
import com.atlassian.pocketknife.internal.lifecycle.modules.GhettoCode;
import com.atlassian.pocketknife.internal.lifecycle.modules.MockPlugin;
import com.google.common.collect.Lists;
import org.dom4j.Element;
import org.dom4j.tree.BaseElement;
import org.junit.Before;
import org.junit.Test;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.assertThat;

public class ModuleDescriptorKitTest
{
    private MockPlugin plugin;

    @Before
    public void setUp() throws Exception
    {
        Map<String, Element> elements = new HashMap<String, Element>();

        BaseElement staticModuleElement = new BaseElement("mod");
        BaseElement dynamicModuleElement = new BaseElement("dynamic");
        dynamicModuleElement.addAttribute(ModuleDescriptorKit.DYNAMIC_MODULE_ATTRNAME, "true");

        elements.put("module1", staticModuleElement);
        elements.put("module2", staticModuleElement);
        elements.put("module3", staticModuleElement);

        elements.put("dmodule1", dynamicModuleElement);
        elements.put("dmodule2", dynamicModuleElement);
        elements.put("dmodule3", dynamicModuleElement);

        plugin = new MockPlugin(elements);
    }

    @Test
    public void testGetStaticModules() throws Exception
    {
        List<ModuleDescriptor<?>> staticModules = Lists.newArrayList(ModuleDescriptorKit.getStaticModules(plugin));
        assertThat(staticModules.size(), equalTo(3));

        List<String> targetStaticModuleKeys = Lists.newArrayList("module1", "module2", "module3");
        for (ModuleDescriptor<?> staticModule : staticModules)
        {
            assertThat(targetStaticModuleKeys, hasItem(staticModule.getKey()));
        }
    }

    @Test
    public void testGetDynamicModules() throws Exception
    {
        List<ModuleDescriptor<?>> staticModules = Lists.newArrayList(ModuleDescriptorKit.getDynamicModules(plugin));
        assertThat(staticModules.size(), equalTo(3));

        List<String> targetDynamicKeys = Lists.newArrayList("dmodule1", "dmodule2", "dmodule3");
        for (ModuleDescriptor<?> staticModule : staticModules)
        {
            assertThat(targetDynamicKeys, hasItem(staticModule.getKey()));
        }
    }

}