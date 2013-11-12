package com.atlassian.pocketknife.api.conditions;

import com.atlassian.jira.util.BuildUtilsInfo;
import com.atlassian.pocketknife.internal.conditions.AbstractJiraVersionCondition;

import java.util.Map;

/**
 * Condition to check whether JIRA is older or equal to a given version.
 */
public class IsOrPriorToJiraVersion extends AbstractJiraVersionCondition
{
    public IsOrPriorToJiraVersion(BuildUtilsInfo buildUtilsInfo)
    {
        super(buildUtilsInfo);
    }

    public boolean shouldDisplay(final Map<String, Object> context)
    {
        return jiraVersion.isLessThanOrEqualTo(version);
    }
}
