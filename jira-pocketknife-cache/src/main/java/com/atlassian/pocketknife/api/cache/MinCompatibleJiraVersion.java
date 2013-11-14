package com.atlassian.pocketknife.api.cache;

import com.atlassian.pocketknife.api.version.SoftwareVersion;
import com.atlassian.pocketknife.api.version.VersionKit;

/**
 * Holds the minimum compatible JIRA version with a compatible Cache version.
 *
 * Older JIRA versions will get the in memory version, newer ones the CacheManager off JIRA.
 */
public interface MinCompatibleJiraVersion
{
    /**
     * First JIRA version that contains a cache manager version 2
     */
    SoftwareVersion JIRA_VERSION_WITH_CACHE_MANAGER_2 = VersionKit.version(6, 2);
}
