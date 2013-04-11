package com.atlassian.pocketknife.search.issue.service;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.util.NotNull;
import com.atlassian.jira.web.bean.PagerFilter;
import com.atlassian.pocketknife.search.issue.callback.DataCallback;
import com.atlassian.query.Query;

/**
 * Provides easy querying of partial issue data. If you need lots of information from an issue, it might be easier to read DocumentIssues, since they
 * already provide hooks to underlying services. This is meant for a few fields only.
 * 
 * @author ahennecke
 */
public interface IssueDataService
{
    /**
     * Execute the query, and for each issue in the result, extract the data as specified by the fields in the callback and send them over to the
     * callback collector.
     * 
     * For optimal memory usage, try streaming the data directly into the receiving data structure (like a REST template) instead of gathering it internally.
     * 
     * @param callback : This collects the field values of the issues matching the query
     * @return any errors that happened during the search
     */
    @NotNull
    <T extends DataCallback> boolean find(User user, Query query, T callback);

    /**
     * Allows specifying an "and" lucene query in addition to a callback
     */
    @NotNull
    <T extends DataCallback> boolean find(User user, Query query, T callback, org.apache.lucene.search.Query andQuery);

    /**
     * Performs a search taking sorting into account
     */
    @NotNull
    <T extends DataCallback> boolean findAndSort(User user, Query query, T callback, PagerFilter<?> pager);

    /**
     * Executes a find, but overwrites security.
     */
    @NotNull
    <T extends DataCallback> boolean findOverrideSecurity(User user, Query query, T callback);
}
