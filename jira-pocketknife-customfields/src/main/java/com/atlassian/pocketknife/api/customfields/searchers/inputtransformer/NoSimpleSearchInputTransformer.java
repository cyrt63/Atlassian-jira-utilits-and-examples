package com.atlassian.pocketknife.api.customfields.searchers.inputtransformer;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.issue.customfields.searchers.transformer.AbstractSingleValueCustomFieldSearchInputTransformer;
import com.atlassian.jira.issue.customfields.searchers.transformer.CustomFieldInputHelper;
import com.atlassian.jira.issue.fields.CustomField;
import com.atlassian.jira.issue.search.SearchContext;
import com.atlassian.jira.user.ApplicationUser;
import com.atlassian.jira.user.ApplicationUsers;
import com.atlassian.query.Query;

/**
 * Transforms between advance and simple search. In this case we want to deny that transformation if our clause exists. Adapted from GreenHopper code
 */
public class NoSimpleSearchInputTransformer extends AbstractSingleValueCustomFieldSearchInputTransformer {
    public NoSimpleSearchInputTransformer(CustomField field, CustomFieldInputHelper customFieldInputHelper) {
        super(field, field.getClauseNames(), "", customFieldInputHelper);
    }

    public boolean doRelevantClausesFitFilterForm(User searcher, Query query, SearchContext searchContext) {
        return doRelevantClausesFitFilterForm(ApplicationUsers.from(searcher), query, searchContext);
    }

    public boolean doRelevantClausesFitFilterForm(ApplicationUser searcher, Query query, SearchContext searchContext) {
        return false;
    }
}
