package com.atlassian.pocketknife.modules.conditions;

import com.atlassian.plugin.Plugin;
import com.atlassian.plugin.PluginParseException;
import com.atlassian.plugin.spring.scanner.annotation.imports.ComponentImport;
import com.atlassian.plugin.web.Condition;
import com.atlassian.plugin.web.WebFragmentHelper;
import com.atlassian.plugin.web.baseconditions.AbstractConditionElementParser;
import com.atlassian.plugin.web.conditions.ConditionLoadingException;
import com.atlassian.plugin.web.descriptors.ConditionElementParser;
import org.dom4j.Element;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.Nonnull;

/**
 * This is a copy of {@code com.atlassian.jira.plugin.webfragment.descriptors.ConditionDescriptorFactoryImpl}
 * Copied here to be reused between all the plugins, that don't want to depend on jira-core
 */
@Component
public class ConditionDescriptorFactoryImpl implements ConditionDescriptorFactory, ConditionElementParser.ConditionFactory {
    private ConditionElementParser conditionElementParser;
    private WebFragmentHelper webFragmentHelper;

    @Autowired
    public ConditionDescriptorFactoryImpl(
            @ComponentImport WebFragmentHelper webFragmentHelper
    ) {
        this.conditionElementParser = new ConditionElementParser(this);
        this.webFragmentHelper = webFragmentHelper;
    }

    @Override
    public Condition create(final String className, final Plugin plugin) throws ConditionLoadingException {
        return webFragmentHelper.loadCondition(className, plugin);
    }

    @Nonnull
    @Override
    public Condition retrieveCondition(@Nonnull final Plugin plugin, @Nonnull final Element element) {
        final Element conditionEl = element.element("condition");
        final Element conditionsEl = element.element("conditions");
        if (conditionEl != null || conditionsEl != null) {
            if (conditionEl != null && conditionEl.attribute("class") == null) {
                throw new PluginParseException("class is a required attribute of the condition tag; plugin module: " + plugin.getKey());
            }
            if (conditionsEl != null && !conditionsEl.selectNodes("./condition[not(@class)]").isEmpty()) {
                throw new PluginParseException("class is a required attribute of the conditions tag; plugin module: " + plugin.getKey());
            }
            return conditionElementParser.makeConditions(plugin, element, AbstractConditionElementParser.CompositeType.AND);
        } else {
            return DEFAULT_CONDITION;
        }
    }
}
