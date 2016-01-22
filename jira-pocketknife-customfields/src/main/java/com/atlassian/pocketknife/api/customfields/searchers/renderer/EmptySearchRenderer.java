package com.atlassian.pocketknife.api.customfields.searchers.renderer;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.issue.customfields.CustomFieldValueProvider;
import com.atlassian.jira.issue.customfields.searchers.renderer.CustomFieldRenderer;
import com.atlassian.jira.issue.fields.CustomField;
import com.atlassian.jira.issue.search.ClauseNames;
import com.atlassian.jira.issue.search.SearchContext;
import com.atlassian.jira.issue.transport.FieldValuesHolder;
import com.atlassian.jira.plugin.customfield.CustomFieldSearcherModuleDescriptor;
import com.atlassian.jira.user.ApplicationUser;
import com.atlassian.jira.user.ApplicationUsers;
import com.atlassian.jira.web.FieldVisibilityManager;
import com.atlassian.query.Query;
import webwork.action.Action;

import java.util.Map;

public class EmptySearchRenderer extends CustomFieldRenderer {
    public EmptySearchRenderer(ClauseNames clauseNames, CustomFieldSearcherModuleDescriptor customFieldSearcherModuleDescriptor, CustomField field,
                               CustomFieldValueProvider customFieldValueProvider, FieldVisibilityManager fieldVisibilityManager) {
        super(clauseNames, customFieldSearcherModuleDescriptor, field, customFieldValueProvider, fieldVisibilityManager);
    }

    public String getEditHtml(User searcher, SearchContext searchContext, FieldValuesHolder fieldValuesHolder, Map<?, ?> displayParameters,
                              Action action) {
        return getEditHtml(ApplicationUsers.from(searcher), searchContext, fieldValuesHolder, displayParameters, action);
    }

    public String getEditHtml(ApplicationUser searcher, SearchContext searchContext, FieldValuesHolder fieldValuesHolder, Map<?, ?> displayParameters,
                              Action action) {
        return "";
    }

    public String getViewHtml(User searcher, SearchContext searchContext, FieldValuesHolder fieldValuesHolder, Map<?, ?> displayParameters,
                              Action action) {
        return getViewHtml(ApplicationUsers.from(searcher), searchContext, fieldValuesHolder, displayParameters, action);
    }

    public String getViewHtml(ApplicationUser searcher, SearchContext searchContext, FieldValuesHolder fieldValuesHolder, Map<?, ?> displayParameters,
                              Action action) {
        return "";
    }

    public boolean isRelevantForQuery(User searcher, Query query) {
        return isRelevantForQuery(ApplicationUsers.from(searcher), query);
    }

    public boolean isRelevantForQuery(ApplicationUser searcher, Query query) {
        return false;
    }

    public boolean isShown(User searcher, SearchContext searchContext) {
        return isShown(ApplicationUsers.from(searcher), searchContext);
    }

    public boolean isShown(ApplicationUser searcher, SearchContext searchContext) {
        return false;
    }
}
