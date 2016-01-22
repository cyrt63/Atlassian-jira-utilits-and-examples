package com.atlassian.pocketknife.api.conditions;

import com.atlassian.jira.config.properties.JiraSystemProperties;
import com.atlassian.plugin.PluginParseException;
import com.atlassian.plugin.web.Condition;

import java.util.Map;

/**
 * Is Jira Running in Dev Mode?
 */
public class JiraDevModeCondition implements Condition {

    @Override
    public void init(Map<String, String> stringStringMap) throws PluginParseException {
    }

    @Override
    public boolean shouldDisplay(Map<String, Object> stringObjectMap) {
        return JiraSystemProperties.isDevMode();
    }
}
