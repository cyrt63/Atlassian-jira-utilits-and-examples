package com.atlassian.pocketknife.internal.lifecycle.modules;

import com.atlassian.plugin.PluginParseException;
import com.google.common.base.Predicate;
import com.google.common.base.Predicates;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;

import java.io.InputStream;
import java.util.List;
import javax.annotation.Nullable;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.collect.Iterables.filter;

/**
 * A simple class to read the basic elements of a plugin descriptor.
 * <p/>
 * Taken from plugins 3.0.0 but copy and pasta back-ported as a way to easily read proxy xml descriptors.  I would have used
 * the class itself but it was not API and not quite ready for re-use.
 */
public class PluginDescriptorReader
{
    static final String RESOURCE = "resource";
    static final String PLUGIN_INFO = "plugin-info";

    private final Document descriptor;

    public static PluginDescriptorReader createDescriptorReader(final InputStream source) throws PluginParseException
    {
        final SAXReader reader = new SAXReader();
        reader.setMergeAdjacentText(true);
        try
        {
            Document doc = reader.read(source);
            return new PluginDescriptorReader(doc);
        }
        catch (final DocumentException e)
        {
            throw new PluginParseException("Cannot parse XML plugin descriptor", e);
        }
    }

    public PluginDescriptorReader(Document descriptor)
    {
        this.descriptor = checkNotNull(descriptor);
    }

    private Element getPluginElement()
    {
        return descriptor.getRootElement();
    }

    public Iterable<Element> getModules()
    {
        return filter(filter(elements(getPluginElement()), Predicates.not(Predicates.or(new ElementWithName(PLUGIN_INFO), new ElementWithName(RESOURCE)))),
                new Predicate<Element>()
                {
                    @Override
                    public boolean apply(Element module)
                    {
                        return true;
                    }
                });
    }

    @SuppressWarnings ("unchecked")
    static List<Element> elements(Element e)
    {
        return e.elements();
    }


    private static final class ElementWithName implements Predicate<Element>
    {
        private final String name;

        private ElementWithName(String name)
        {
            this.name = checkNotNull(name);
        }

        @Override
        public boolean apply(@Nullable Element element)
        {
            return element != null && name.equalsIgnoreCase(element.getName());
        }
    }
}
