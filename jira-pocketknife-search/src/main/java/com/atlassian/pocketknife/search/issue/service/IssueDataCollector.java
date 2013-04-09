package com.atlassian.pocketknife.search.issue.service;

import org.apache.commons.lang.math.NumberUtils;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.FieldSelector;
import org.apache.lucene.search.IndexSearcher;

import com.atlassian.jira.issue.index.DocumentConstants;
import com.atlassian.jira.issue.statistics.util.FieldableDocumentHitCollector;
import com.atlassian.pocketknife.search.issue.callback.IssueDataCallback;

/**
 * Lucene collector to read the defined data from the document and pass it on to the callback.
 *
 * @author ahennecke
 */
public class IssueDataCollector extends FieldableDocumentHitCollector
{
    private final FieldSelector fieldSelector;
    private final IssueDataCallback callback;

    public IssueDataCollector(IndexSearcher searcher, FieldSelector fieldSelector, IssueDataCallback callback)
    {
        super(searcher);
        this.fieldSelector = fieldSelector;
        this.callback = callback;
    }

    @Override
    protected FieldSelector getFieldSelector()
    {
        return fieldSelector;
    }

    @Override
    public void collect(Document d)
    {
        String issueIdRaw = d.get(DocumentConstants.ISSUE_ID);
        Long issueId = d.get(DocumentConstants.ISSUE_ID) == null ? null : NumberUtils.toLong(issueIdRaw);
        String issueKey = d.get(DocumentConstants.ISSUE_KEY);

        for (String fieldName : callback.getFields())
        {
            String[] values = d.getValues(fieldName);
            if (values.length > 0)
            {
                for (String value : values)
                {
                    callback.fieldData(issueId, issueKey, fieldName, value);
                }
            }
            else
            {
                callback.fieldData(issueId, issueKey, fieldName, null);
            }
        }
        callback.issueComplete(issueId, issueKey);
    }
}
