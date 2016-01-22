package com.atlassian.pocketknife.internal.search.issue.service;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.issue.search.SearchException;
import com.atlassian.jira.issue.search.SearchProviderFactory;
import com.atlassian.jira.issue.search.providers.LuceneSearchProvider;
import com.atlassian.jira.user.ApplicationUser;
import com.atlassian.jira.user.ApplicationUsers;
import com.atlassian.jira.web.bean.PagerFilter;
import com.atlassian.pocketknife.api.search.issue.service.ExtendedSearchService;
import com.atlassian.query.Query;
import org.apache.log4j.Logger;
import org.apache.lucene.search.Collector;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.SortField;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * Exposes additional search related methods, but refers the actual work to SearchService internal methods called through
 * reflection.
 */
@Service
public class ExtendedSearchServiceImpl implements ExtendedSearchService {
    private static final Logger log = Logger.getLogger(ExtendedSearchServiceImpl.class);

    @Autowired
    private SearchProviderFactory searchProviderFactory;

    public long searchCountOverrideSecurity(Query query, User searcher, org.apache.lucene.search.Query luceneQuery) throws SearchException {
        return searchCountOverrideSecurity(query, ApplicationUsers.from(searcher), luceneQuery);
    }

    public long searchCountOverrideSecurity(Query query, ApplicationUser searcher, org.apache.lucene.search.Query luceneQuery) throws SearchException {
        try {
            // Fetch the com.atlassian.jira.issue.search.providers.LuceneSearchProvider directly from Spring to avoid getting a proxy
            LuceneSearchProvider luceneSearchProvider = ComponentAccessor.getComponentOfType(LuceneSearchProvider.class);
            final IndexSearcher issueSearcher = searchProviderFactory.getSearcher(SearchProviderFactory.ISSUE_INDEX);
            // private long getHitCount(
            //         final Query searchQuery, final ApplicationUser searchUser, final SortField[] sortField,
            //         final org.apache.lucene.search.Query andQuery, boolean overrideSecurity, IndexSearcher issueSearcher,
            //         final PagerFilter pager
            // ) throws SearchException
            Method getHitCount = LuceneSearchProvider.class.getDeclaredMethod("getHitCount", Query.class, ApplicationUser.class, SortField[].class,
                    org.apache.lucene.search.Query.class, Boolean.TYPE, IndexSearcher.class, PagerFilter.class);
            getHitCount.setAccessible(true);
            return (Long) getHitCount.invoke(luceneSearchProvider, query, searcher, null, luceneQuery, true, issueSearcher, null);
        } catch (NoSuchMethodException | InvocationTargetException | IllegalAccessException | IllegalArgumentException e) {
            log.error("Lucene Search Provider class changed! Cannot call internal method");
            e.printStackTrace();
            throw new SearchException(e);
        }
    }

    public void searchOverrideSecurity(Query query, User searcher, Collector collector, org.apache.lucene.search.Query andQuery) throws SearchException {
        searchOverrideSecurity(query, ApplicationUsers.from(searcher), collector, andQuery);
    }

    public void searchOverrideSecurity(Query query, ApplicationUser searcher, Collector collector, org.apache.lucene.search.Query andQuery) throws SearchException {
        try {
            // Fetch the com.atlassian.jira.issue.search.providers.LuceneSearchProvider directly from Spring to avoid getting a proxy
            LuceneSearchProvider luceneSearchProvider = ComponentAccessor.getComponentOfType(LuceneSearchProvider.class);
            final IndexSearcher issueSearcher = searchProviderFactory.getSearcher(SearchProviderFactory.ISSUE_INDEX);
            // private void search(
            //         final Query searchQuery,
            //         final ApplicationUser user,
            //         final Collector collector,
            //         org.apache.lucene.search.Query andQuery,
            //         boolean overrideSecurity
            // ) throws SearchException
            Method search = LuceneSearchProvider.class.getDeclaredMethod("search", Query.class, ApplicationUser.class, Collector.class,
                    org.apache.lucene.search.Query.class, Boolean.TYPE);
            search.setAccessible(true);
            search.invoke(luceneSearchProvider, query, searcher, collector, andQuery, true);
        } catch (NoSuchMethodException | InvocationTargetException | IllegalAccessException | IllegalArgumentException e) {
            log.error("Lucene Search Provider class changed! Cannot call internal method");
            e.printStackTrace();
            throw new SearchException(e);
        }
    }
}
