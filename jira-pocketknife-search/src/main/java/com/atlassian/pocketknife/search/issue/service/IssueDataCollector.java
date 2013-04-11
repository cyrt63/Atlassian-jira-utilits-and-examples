package com.atlassian.pocketknife.search.issue.service;

import com.atlassian.jira.issue.statistics.util.FieldableDocumentHitCollector;
import com.atlassian.pocketknife.search.issue.callback.DataCallback;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.FieldSelector;
import org.apache.lucene.search.IndexSearcher;

/**
 * Lucene collector to read the defined data from the document and pass it on to the callback.
 *
 * @author ahennecke
 */
public class IssueDataCollector extends FieldableDocumentHitCollector
{
    private final FieldSelector fieldSelector;
    private final DataCallback callback;

    public IssueDataCollector(IndexSearcher searcher, FieldSelector fieldSelector, DataCallback callback)
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
        for (String fieldName : callback.getFields())
        {
            String[] values = d.getValues(fieldName);
            if (values.length > 0)
            {
                for (String value : values)
                {
                    callback.fieldData(fieldName, value);
                }
            }
            else
            {
                callback.fieldData(fieldName, null);
            }
        }
        callback.documentComplete();
    }
}
