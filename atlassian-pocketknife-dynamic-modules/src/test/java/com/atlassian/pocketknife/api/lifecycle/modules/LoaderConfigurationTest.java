package com.atlassian.pocketknife.api.lifecycle.modules;

import com.atlassian.plugin.Plugin;
import com.atlassian.pocketknife.api.lifecycle.modules.LoaderConfiguration.DefaultResourceLoader;
import com.atlassian.pocketknife.internal.lifecycle.modules.MockPlugin;
import org.dom4j.Element;
import org.junit.Before;
import org.junit.Test;

import java.io.InputStream;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.Assert.*;

public class LoaderConfigurationTest {

    private Plugin mockPlugin;

    @Before
    public void setup() {
        mockPlugin = new MockPlugin(Collections.<String, Element>emptyMap());
    }

    @Test(expected = NullPointerException.class)
    public void testNullPluginThrowsError() {
        new LoaderConfiguration(null);
    }

    @Test
    public void testSetPlugin() {
        final LoaderConfiguration config = new LoaderConfiguration(mockPlugin);

        assertEquals(mockPlugin, config.getPlugin());
    }

    @Test
    public void testDefaultWillFailOnDuplicateKeys() {
        final LoaderConfiguration config = new LoaderConfiguration(mockPlugin);

        assertTrue(config.isFailOnDuplicateKey());
    }

    @Test
    public void testSetFailOnDuplicateKeys() {
        final LoaderConfiguration config = new LoaderConfiguration(mockPlugin);

        config.setFailOnDuplicateKey(false);
        assertFalse(config.isFailOnDuplicateKey());
    }

    @Test
    public void testDefaultResourceLoader() {
        final LoaderConfiguration config = new LoaderConfiguration(mockPlugin);

        assertNotNull(config.getResourceLoader());
        assertTrue(config.getResourceLoader() instanceof DefaultResourceLoader);
    }

    @Test
    public void testSetResourceLoader() {
        final LoaderConfiguration config = new LoaderConfiguration(mockPlugin);
        final ResourceLoader loader = new ResourceLoader() {
            @Override
            public InputStream getResourceAsStream(String resourceName) {
                return null;
            }
        };

        config.setResourceLoader(loader);
        assertEquals(loader, config.getResourceLoader());
    }

    @Test
    public void testEmptyPathsIfNotProvided() {
        final LoaderConfiguration config = new LoaderConfiguration(mockPlugin);

        assertNotNull(config.getPathsToAuxAtlassianPluginXMLs());
        assertEquals(0, config.getPathsToAuxAtlassianPluginXMLs().size());
    }

    @Test
    public void testSetPaths() {
        final LoaderConfiguration config = new LoaderConfiguration(mockPlugin);
        final List<String> list = Collections.singletonList("sssss");

        config.setPathsToAuxAtlassianPluginXMLs(list);

        assertEquals(list, config.getPathsToAuxAtlassianPluginXMLs());
    }

    @Test
    public void testAddPaths() {
        final LoaderConfiguration config = new LoaderConfiguration(mockPlugin);

        config.addPathsToAuxAtlassianPluginXMLs("list", "2", "another");

        assertEquals(Arrays.asList("list", "2", "another"), config.getPathsToAuxAtlassianPluginXMLs());
    }
}
