package com.atlassian.pocketknife.search.issue.callback;

import java.util.Set;

/**
 * Abstraction for fetching a limited number of issue field values from Lucene. This is the interface you need to implement if you want to search the
 * issue index
 * 
 * @author ahennecke
 */
public interface IssueDataCallback
{
    /**
     * see DocumentConstants for possible values. These are the issue fields that we want to read from the Lucene index. You can also define your own
     * values if you write your own custom field indexer
     */
    Set<String> getFields();

    /**
     * This callback is being called for every field in each issue that matches the query. For each issue found, this will be called once for each
     * field that you have defined in the getField method. For memory efficiency, try to avoid buffering all results in memory but rather stream them
     * to the target object.
     * 
     * @param fieldName : The name of the field that this value belongs to. One of the fields specified in getFields(). See DocumentConstants for
     *            possible values.
     * @param data : The raw Lucene data for the given field. Can be null.
     */
    void fieldData(Long issueId, String issueKey, String fieldName, String data);

    /**
     * Called when all fields that were defined in the getFields method have passed through the fieldData method for a single issue.
     */
    void issueComplete(Long issueId, String issueKey);
}
