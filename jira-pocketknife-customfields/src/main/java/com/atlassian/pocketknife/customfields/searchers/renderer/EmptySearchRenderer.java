package com.atlassian.pocketknife.customfields.searchers.renderer;

import java.util.Map;

import webwork.action.Action;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.issue.customfields.CustomFieldValueProvider;
import com.atlassian.jira.issue.customfields.searchers.renderer.CustomFieldRenderer;
import com.atlassian.jira.issue.fields.CustomField;
import com.atlassian.jira.issue.search.ClauseNames;
import com.atlassian.jira.issue.search.SearchContext;
import com.atlassian.jira.issue.transport.FieldValuesHolder;
import com.atlassian.jira.plugin.customfield.CustomFieldSearcherModuleDescriptor;
import com.atlassian.jira.web.FieldVisibilityManager;
import com.atlassian.query.Query;

public class EmptySearchRenderer extends CustomFieldRenderer
{
    public EmptySearchRenderer(ClauseNames clauseNames, CustomFieldSearcherModuleDescriptor customFieldSearcherModuleDescriptor, CustomField field,
            CustomFieldValueProvider customFieldValueProvider, FieldVisibilityManager fieldVisibilityManager)
    {
        super(clauseNames, customFieldSearcherModuleDescriptor, field, customFieldValueProvider, fieldVisibilityManager);
    }

    @Override
    public String getEditHtml(User searcher, SearchContext searchContext, FieldValuesHolder fieldValuesHolder, Map<?, ?> displayParameters,
            Action action)
    {
        return "";
    }

    @Override
    public String getViewHtml(User searcher, SearchContext searchContext, FieldValuesHolder fieldValuesHolder, Map<?, ?> displayParameters,
            Action action)
    {
        return "";
    }

    @Override
    public boolean isRelevantForQuery(User searcher, Query query)
    {
        return false;
    }

    @Override
    public boolean isShown(User searcher, SearchContext searchContext)
    {
        return false;
    }
}
