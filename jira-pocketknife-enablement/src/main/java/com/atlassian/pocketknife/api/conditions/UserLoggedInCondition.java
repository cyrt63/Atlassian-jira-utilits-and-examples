package com.atlassian.pocketknife.api.conditions;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.plugin.webfragment.conditions.AbstractJiraCondition;
import com.atlassian.jira.plugin.webfragment.model.JiraHelper;
import com.atlassian.jira.user.ApplicationUser;
import com.atlassian.jira.user.ApplicationUsers;

/**
 * Is the user logged in?
 * <p>
 * A reimplementation of a basic condition such that we get around some OSGI problems
 */
public class UserLoggedInCondition extends AbstractJiraCondition {
    public boolean shouldDisplay(User user, JiraHelper jiraHelper) {
        return shouldDisplay(ApplicationUsers.from(user), jiraHelper);
    }

    public boolean shouldDisplay(ApplicationUser user, JiraHelper jiraHelper) {
        return user != null;
    }
}
