package com.atlassian.pocketknife.internal.lifecycle.modules;

import com.atlassian.plugin.Plugin;
import org.dom4j.Element;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Map;

/**
 * This ghetto code is here because the plugins system does not have public methods to allow us to do what it allows
 * itself.  That's ok since we can improve it over time but for now we do the needful
 *
 * See https://ecosystem.atlassian.net/browse/PLUG-1037
 */
public class GhettoCode
{
    private static final Logger log = LoggerFactory.getLogger(GhettoCode.class);

    static void addModuleDescriptorElement(Plugin plugin, Element element, String moduleKey)
    {
        try
        {
            //
            // in order to support dynamic resolution of modules, the plugins system squirrels away the
            // the XML elements against that plugin so it can then later "resolve" them as
            // other OSGI services turn up
            //
            // ((OsgiPlugin) plugin).addModuleDescriptorElement(moduleKey, element);
            //
            Method addModuleDescriptorElement = plugin.getClass().getDeclaredMethod("addModuleDescriptorElement", String.class, Element.class);
            addModuleDescriptorElement.setAccessible(true);
            addModuleDescriptorElement.invoke(plugin, moduleKey, element);
        }
        catch (NoSuchMethodException e)
        {
            log.error("Unable to record OsgiPlugin dom.  Has the interface changed? ");
        }
        catch (InvocationTargetException e)
        {
            log.error("Unable to record OsgiPlugin dom.  Has the interface changed? ");
        }
        catch (IllegalAccessException e)
        {
            log.error("Unable to record OsgiPlugin dom.  Has the interface changed? ");
        }
    }

    static void removeModuleDescriptorElement(Plugin plugin, String moduleKey)
    {
        //
        // the map underneath is exposed directly to we can try to clear it.
        //
        if (!removeElementFromMap(plugin, moduleKey))
        {
            //
            // failing that have a go at setting it to null
            //
            // the OsgiPlugin does not expose a proper method to remove elements that have been squirreled away but its
            // does allow null to be set in as a value and its guards against it, so that's what we do to
            // reclaim memory
            //
            addModuleDescriptorElement(plugin, null, moduleKey);
        }
    }

    private static boolean removeElementFromMap(Plugin plugin, String moduleKey)
    {
        // Map<String, Element> getModuleElements()
        try
        {
            Method getModuleElements = plugin.getClass().getDeclaredMethod("getModuleElements");
            getModuleElements.setAccessible(true);
            @SuppressWarnings ("unchecked")
            Map<String, Object> result = (Map<String, Object>) getModuleElements.invoke(plugin);
            result.remove(moduleKey);
            return true;
        }
        catch (Exception e)
        {
            return false;
        }
    }


}
