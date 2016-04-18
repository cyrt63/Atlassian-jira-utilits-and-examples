package com.atlassian.pocketknife.internal.search.issue.service;

import com.atlassian.jira.issue.index.DocumentConstants;
import com.atlassian.pocketknife.annotations.lucene.LuceneUsage;
import org.apache.lucene.document.FieldSelector;
import org.apache.lucene.document.FieldSelectorResult;

import java.util.Set;

/**
 * Field selector to restrict the amount of information that's extracted from Lucene. In addition to any fields specified by the callback, we also
 * fetch issueId and issueKey.
 *
 * @author ahennecke
 */
@LuceneUsage(type = LuceneUsage.LuceneUsageType.Unknown, comment = "Lucene types (need to convert consumers)")
public class PluginFieldSelector implements FieldSelector {

    private static final long serialVersionUID = -1279623447149899950L;

    /**
     * these are the fields the caller is interested in fetching
     */
    private final Set<String> fieldNames;

    public PluginFieldSelector(Set<String> fieldNames) {
        this.fieldNames = fieldNames;
    }

    @Override
    public FieldSelectorResult accept(String fieldName) {
        boolean matches = DocumentConstants.ISSUE_KEY.equals(fieldName) || DocumentConstants.ISSUE_ID.equals(fieldName) || fieldNames.contains(fieldName);
        return matches ? FieldSelectorResult.LOAD : FieldSelectorResult.NO_LOAD;
    }
}
