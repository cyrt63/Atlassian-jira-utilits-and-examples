package com.atlassian.pocketknife.api.version;

/**
 * A class to allow you to make JIRA version specific decisions
 */
public interface JiraVersionService
{
    /**
     * @return the the version of JIRA that is currently running
     */
    public SoftwareVersion version();
}
