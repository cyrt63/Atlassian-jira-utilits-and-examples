package com.atlassian.pocketknife.modules.conditions;

import com.atlassian.plugin.Plugin;
import com.atlassian.plugin.web.Condition;
import com.atlassian.plugin.web.conditions.AlwaysDisplayCondition;
import org.dom4j.Element;

import javax.annotation.Nonnull;

/**
 * Provides common functionality of loading conditions from module descriptors.
 * Based on platform web conditions, but can be used for other types of modules as well
 */
public interface ConditionDescriptorFactory {
    Condition DEFAULT_CONDITION = new AlwaysDisplayCondition();

    @Nonnull
    Condition retrieveCondition(@Nonnull Plugin plugin, @Nonnull Element element);
}
