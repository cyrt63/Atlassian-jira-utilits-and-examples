package com.atlassian.pocketknife.api.conditions;

import com.atlassian.jira.util.BuildUtilsInfo;
import com.atlassian.pocketknife.internal.conditions.AbstractJiraVersionCondition;

import java.util.Map;

/**
 * Condition to check whether JIRA is newer than a given version.
 */
public class IsAfterJiraVersion extends AbstractJiraVersionCondition {
    public IsAfterJiraVersion(BuildUtilsInfo buildUtilsInfo) {
        super(buildUtilsInfo);
    }

    public boolean shouldDisplay(final Map<String, Object> context) {
        return jiraVersion.isGreaterThan(version);
    }
}
