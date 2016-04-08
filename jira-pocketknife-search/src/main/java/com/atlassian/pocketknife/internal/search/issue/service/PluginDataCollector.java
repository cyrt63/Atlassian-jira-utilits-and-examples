package com.atlassian.pocketknife.internal.search.issue.service;

import com.atlassian.jira.issue.index.DocumentConstants;
import com.atlassian.jira.issue.statistics.util.FieldableDocumentHitCollector;
import com.atlassian.pocketknife.annotations.lucene.LuceneUsage;
import com.atlassian.pocketknife.api.search.issue.callback.DataCallback;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.FieldSelector;
import org.apache.lucene.search.IndexSearcher;

import static com.atlassian.pocketknife.api.search.issue.util.NumberUtil.toLong;

/**
 * Lucene collector to read the defined data from the document and pass it on to the callback.
 *
 * @author ahennecke
 */
@LuceneUsage(type = LuceneUsage.LuceneUsageType.IssueCollection)
public class PluginDataCollector extends FieldableDocumentHitCollector {
    private final FieldSelector fieldSelector;
    private final DataCallback callback;

    public PluginDataCollector(IndexSearcher searcher, FieldSelector fieldSelector, DataCallback callback) {
        super(searcher);
        this.fieldSelector = fieldSelector;
        this.callback = callback;
    }

    @Override
    protected FieldSelector getFieldSelector() {
        return fieldSelector;
    }

    @Override
    public void collect(Document d) {
        Long issueId = toLong(d.get(DocumentConstants.ISSUE_ID));
        String issueKey = d.get(DocumentConstants.ISSUE_KEY);

        for (String fieldName : callback.getFields()) {
            String[] values = d.getValues(fieldName);
            if (values.length > 0) {
                for (String value : values) {
                    callback.fieldData(issueId, issueKey, fieldName, value);
                }
            } else {
                callback.fieldData(issueId, issueKey, fieldName, null);
            }
        }
        callback.documentComplete(issueId, issueKey);
    }
}
