package com.atlassian.pocketknife.search.issue.service;

import java.util.Set;
import org.apache.lucene.document.FieldSelector;
import org.apache.lucene.document.FieldSelectorResult;

import com.atlassian.jira.issue.index.DocumentConstants;

/**
 * Field selector to restrict the amount of information that's extracted from Lucene. In addition to any fields specified by the callback, we also
 * fetch issueId and issueKey.
 *
 * @author ahennecke
 */
public class IssueDataFieldSelector implements FieldSelector
{

    private static final long serialVersionUID = -1279623447149899950L;

    /** these are the fields the caller is interested in fetching */
    private final Set<String> fieldNames;

    public IssueDataFieldSelector(Set<String> fieldNames)
    {
        this.fieldNames = fieldNames;
    }

    @Override
    public FieldSelectorResult accept(String fieldName)
    {
        boolean matches = DocumentConstants.ISSUE_KEY.equals(fieldName) || DocumentConstants.ISSUE_ID.equals(fieldName) || fieldNames.contains(fieldName);
        return matches ? FieldSelectorResult.LOAD : FieldSelectorResult.NO_LOAD;
    }
}
