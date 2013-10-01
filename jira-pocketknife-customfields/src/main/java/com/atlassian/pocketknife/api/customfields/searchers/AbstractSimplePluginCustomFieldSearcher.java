package com.atlassian.pocketknife.api.customfields.searchers;

import com.atlassian.jira.issue.customfields.searchers.SimpleCustomFieldSearcherClauseHandler;
import com.atlassian.jira.issue.customfields.searchers.transformer.CustomFieldInputHelper;
import com.atlassian.jira.jql.operand.JqlOperandResolver;
import com.atlassian.jira.jql.query.ClauseQueryFactory;
import com.atlassian.jira.web.FieldVisibilityManager;
import com.atlassian.pocketknife.api.customfields.searchers.clausevalidator.AbstractClauseValidator;

public abstract class AbstractSimplePluginCustomFieldSearcher extends AbstractPluginCustomFieldSearcher
{
    public AbstractSimplePluginCustomFieldSearcher(FieldVisibilityManager fieldVisibilityManager, JqlOperandResolver jqlOperandResolver, CustomFieldInputHelper customFieldInputHelper)
    {
        super(fieldVisibilityManager, jqlOperandResolver, customFieldInputHelper);
    }

    @Override
    protected SimpleCustomFieldSearcherClauseHandler getNewCustomFieldSearcherClauseHandler(AbstractClauseValidator validator, ClauseQueryFactory clauseQueryFactory)
    {
        return new SimpleCustomFieldSearcherClauseHandler(validator, clauseQueryFactory, validator.getValidOperators(), getDataType());
    }
}
