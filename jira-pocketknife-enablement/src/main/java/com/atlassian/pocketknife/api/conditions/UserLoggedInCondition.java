package com.atlassian.pocketknife.api.conditions;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.plugin.webfragment.conditions.AbstractJiraCondition;
import com.atlassian.jira.plugin.webfragment.model.JiraHelper;

/**
 * Is the user logged in?
 *
 * A reimplementation of a basic condition such that we get around some OSGI problems
 */
public class UserLoggedInCondition extends AbstractJiraCondition
{
    public boolean shouldDisplay(User user, JiraHelper jiraHelper)
    {
        return user != null;
    }
}
