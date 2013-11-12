package com.atlassian.pocketknife.internal.conditions;

import com.atlassian.jira.util.BuildUtilsInfo;
import com.atlassian.plugin.PluginParseException;
import com.atlassian.plugin.web.Condition;
import com.atlassian.pocketknife.api.version.SoftwareVersion;
import com.atlassian.pocketknife.api.version.VersionKit;

import java.util.Map;

/**
 * Abstract condition class, encapsulates the configuration and jira version extraction.
 */
public abstract class AbstractJiraVersionCondition implements Condition
{
    protected final SoftwareVersion jiraVersion;
    protected SoftwareVersion version;

    public AbstractJiraVersionCondition(BuildUtilsInfo buildUtilsInfo)
    {
        String versionString = buildUtilsInfo.getVersion();
        jiraVersion = VersionKit.parse(versionString);
    }

    public void init(final Map<String, String> paramMap) throws PluginParseException
    {
        version = VersionKit.version(toInt(paramMap, "majorVersion"), toInt(paramMap, "minorVersion"));
    }

    private int toInt(Map<String, String> paramMap, final String paramName)
    {
        return Integer.decode(paramMap.get(paramName)).intValue();
    }

    public abstract boolean shouldDisplay(final Map<String, Object> context);
}
