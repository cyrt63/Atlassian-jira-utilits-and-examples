package com.atlassian.pocketknife.search.issue.service;

import org.apache.lucene.search.IndexSearcher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.issue.search.SearchException;
import com.atlassian.jira.issue.search.SearchProvider;
import com.atlassian.jira.issue.search.SearchProviderFactory;
import com.atlassian.jira.util.NotNull;
import com.atlassian.jira.web.bean.PagerFilter;
import com.atlassian.pocketknife.search.issue.callback.DataCallback;
import com.atlassian.query.Query;

/**
 * Abstraction for a common usage pattern of querying a handful of issue fields from Lucene. Besides being convenient, this serves the purpose of
 * keeping Lucene dependencies out of the code as much as possible.
 * 
 * @author ahennecke
 */
@Service
public class IssueDataServiceImpl implements IssueDataService
{
    @Autowired
    private SearchProviderFactory searchProviderFactory;

    @Autowired
    private SearchProvider searchProvider;

    @Override
    @NotNull
    public <T extends DataCallback> boolean find(User user, Query query, T callback)
    {
        return findImpl(user, query, callback, null, false, null);
    }

    @Override
    @NotNull
    public <T extends DataCallback> boolean find(User user, Query query, T callback, org.apache.lucene.search.Query andQuery)
    {
        return findImpl(user, query, callback, null, false, andQuery);
    }

    @Override
    @NotNull
    public <T extends DataCallback> boolean findAndSort(User user, Query query, T callback, PagerFilter<?> pager)
    {
        return findImpl(user, query, callback, pager, false, null);
    }

    @Override
    @NotNull
    public <T extends DataCallback> boolean findOverrideSecurity(User user, Query query, T callback)
    {
        return findImpl(user, query, callback, null, true, null);
    }

    /**
     * Performs the find
     * Note: pager is ignored if overwriteSecurity is true
     */
    private <T extends DataCallback> boolean findImpl(User user, Query query, T callback, PagerFilter<?> pager, boolean overwriteSecurity, org.apache.lucene.search.Query andQuery)
    {
        if (andQuery != null && (overwriteSecurity || pager != null))
        {
            throw new IllegalStateException("andQuery not supported with pager or overrideSecurity.");
        }

        IndexSearcher searcher = searchProviderFactory.getSearcher(SearchProviderFactory.ISSUE_INDEX);
        PluginFieldSelector fieldSelector = new PluginFieldSelector(callback.getFields());
        IssueDataCollector collector = new IssueDataCollector(searcher, fieldSelector, callback);

        try
        {
            long a = System.nanoTime();
            // this will fire off the query, fetch the values for the fields specified in the collector and pass them on to the callback.
            if (overwriteSecurity)
            {
                searchProvider.searchOverrideSecurity(query, user, collector);
            }
            else if (pager != null)
            {
                searchProvider.searchAndSort(query, user, collector, pager);
            }
            else
            {
                searchProvider.search(query, user, collector, andQuery);
            }
            perfLog("Search took: ", a);
        }
        catch (SearchException e)
        {
            return false;
        }

        return true;
    }

    private Logger performanceLogger = LoggerFactory.getLogger(this.getClass());

    private void perfLog(String message, long start)
    {
        if (!performanceLogger.isDebugEnabled())
        {
            return;
        }

        String log = new StringBuilder().append(message).append(" ").append(System.nanoTime() - start).append("ns").toString();
        performanceLogger.debug(log);
    }
}
