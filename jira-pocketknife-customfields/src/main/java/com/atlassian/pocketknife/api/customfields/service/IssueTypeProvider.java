package com.atlassian.pocketknife.api.customfields.service;

import com.atlassian.jira.issue.issuetype.IssueType;

import java.util.Set;

/**
 * Convenience interface to exchange between literal value of IssueType and the instance itself that managed by JIRA
 */
public interface IssueTypeProvider
{

    /**
     * Map issue types in string literals into {@link com.atlassian.jira.issue.issuetype.IssueType}
     *
     * @return the map
     */
    Set<IssueType> getIssueTypes();

}
