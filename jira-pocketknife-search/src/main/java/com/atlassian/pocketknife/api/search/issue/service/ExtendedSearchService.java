package com.atlassian.pocketknife.api.search.issue.service;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.issue.search.SearchException;
import com.atlassian.jira.user.ApplicationUser;
import com.atlassian.pocketknife.annotations.lucene.LuceneUsage;
import com.atlassian.query.Query;
import org.apache.lucene.search.Collector;

/**
 * Exposes SearchService methods missing/unexposed in the original
 */
@LuceneUsage(type = LuceneUsage.LuceneUsageType.Unknown, comment = "Lucene types (need to convert consumers)")
public interface ExtendedSearchService {
    /**
     * Perform an issue search using the specified user and query and return the number of results. User's permissions
     * are not taken into account when performing the search.
     *
     * @param query    the lucene query to search for
     * @param andQuery optional lucene query to and with the lucene query
     * @return the outcome
     */
    public long searchCountOverrideSecurity(Query query, User searcher, org.apache.lucene.search.Query andQuery) throws SearchException;

    public long searchCountOverrideSecurity(Query query, ApplicationUser searcher, org.apache.lucene.search.Query andQuery) throws SearchException;

    /**
     * Run a search based on the provided search criteria and, for each match, call Collector.collect() not taking
     * into account any security constraints.
     *
     * @param query     the lucene query to search for
     * @param searcher  the user performing the search which will be used to provide context for the search.
     * @param collector the Lucene object that will have collect called for each match.
     * @param andQuery  optional lucene query to and with the lucene query
     */
    public void searchOverrideSecurity(Query query, User searcher, Collector collector, org.apache.lucene.search.Query andQuery) throws SearchException;

    public void searchOverrideSecurity(Query query, ApplicationUser searcher, Collector collector, org.apache.lucene.search.Query andQuery) throws SearchException;
}
