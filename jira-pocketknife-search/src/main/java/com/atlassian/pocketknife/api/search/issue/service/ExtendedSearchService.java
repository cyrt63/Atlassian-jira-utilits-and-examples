package com.atlassian.pocketknife.api.search.issue.service;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.issue.search.SearchException;
import com.atlassian.query.Query;

/**
 * Exposes SearchService methods missing/unexposed in the original
 */
public interface ExtendedSearchService
{
    /**
     * Perform an issue search using the specified user and query and return the number of results. User's permissions
     * are not taken into account when performing the search.
     *
     * @param query the lucene query to search for
     * @param andQuery optional lucene query to and with the lucene query
     * @return the outcome
     */
    public long searchCountOverrideSecurity(Query query, User searcher, org.apache.lucene.search.Query andQuery) throws SearchException;
}
