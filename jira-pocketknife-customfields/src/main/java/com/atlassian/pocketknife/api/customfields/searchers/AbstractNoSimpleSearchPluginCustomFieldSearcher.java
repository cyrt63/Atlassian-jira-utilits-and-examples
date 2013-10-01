package com.atlassian.pocketknife.api.customfields.searchers;

import com.atlassian.jira.issue.customfields.CustomFieldValueProvider;
import com.atlassian.jira.issue.customfields.searchers.renderer.CustomFieldRenderer;
import com.atlassian.jira.issue.customfields.searchers.transformer.CustomFieldInputHelper;
import com.atlassian.jira.issue.fields.CustomField;
import com.atlassian.jira.issue.search.ClauseNames;
import com.atlassian.jira.issue.search.searchers.transformer.SearchInputTransformer;
import com.atlassian.jira.jql.operand.JqlOperandResolver;
import com.atlassian.jira.jql.query.ActualValueCustomFieldClauseQueryFactory;
import com.atlassian.jira.jql.query.ClauseQueryFactory;
import com.atlassian.jira.jql.util.IndexValueConverter;
import com.atlassian.jira.plugin.customfield.CustomFieldSearcherModuleDescriptor;
import com.atlassian.jira.web.FieldVisibilityManager;
import com.atlassian.pocketknife.api.customfields.searchers.inputtransformer.NoSimpleSearchInputTransformer;
import com.atlassian.pocketknife.api.customfields.searchers.renderer.EmptySearchRenderer;

public abstract class AbstractNoSimpleSearchPluginCustomFieldSearcher extends AbstractSimplePluginCustomFieldSearcher
{
    public AbstractNoSimpleSearchPluginCustomFieldSearcher(FieldVisibilityManager fieldVisibilityManager, JqlOperandResolver jqlOperandResolver, CustomFieldInputHelper customFieldInputHelper)
    {
        super(fieldVisibilityManager, jqlOperandResolver, customFieldInputHelper);
    }

    @Override
    protected CustomFieldRenderer getNewCustomFieldSearchRenderer(ClauseNames clauseNames, CustomFieldSearcherModuleDescriptor customFieldSearcherModuleDescriptor, CustomField field, CustomFieldValueProvider customFieldValueProvider, FieldVisibilityManager fieldVisibilityManager)
    {
        return new EmptySearchRenderer(clauseNames, customFieldSearcherModuleDescriptor, field, customFieldValueProvider, fieldVisibilityManager);
    }

    @Override
    protected SearchInputTransformer getNewSearchInputTransformer(CustomField field, CustomFieldInputHelper customFieldInputHelper)
    {
        return new NoSimpleSearchInputTransformer(field, customFieldInputHelper);
    }

    @Override
    protected ClauseQueryFactory getNewClauseQueryFactory(CustomField field, JqlOperandResolver jqlOperandResolver, IndexValueConverter indexValueConverter)
    {
        return new ActualValueCustomFieldClauseQueryFactory(field.getId(), jqlOperandResolver, indexValueConverter, true);
    }
}
