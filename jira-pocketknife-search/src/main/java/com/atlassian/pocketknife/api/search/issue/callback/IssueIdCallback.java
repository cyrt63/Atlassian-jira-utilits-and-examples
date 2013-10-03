package com.atlassian.pocketknife.api.search.issue.callback;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * DataCallback implementation that collects issue ids of matched documents.
 */
public class IssueIdCallback implements DataCallback
{
    private Set<String> fields = new HashSet<String>(); // all data we need is provide in the documentComplete call
    private List<Long> issueIds;
    private int count = 0, max;

    /**
     * Create a new callback
     */
    public IssueIdCallback()
    {
        this(Integer.MAX_VALUE);
    }

    /**
     * Create a new callback limiting the number of retained ids
     *
     * @param max the maximum number of ids to retain. -1 for unlimited
     */
    public IssueIdCallback(int max)
    {
        issueIds = new ArrayList<Long>();
        this.count = 0;
        this.max = max < 0 ? Integer.MAX_VALUE : max;
    }

    @Override
    public Set<String> getFields()
    {
        return fields;
    }

    @Override
    public void fieldData(Long issueId, String issueKey, String name, String value)
    {
    }

    @Override
    public void documentComplete(Long issueId, String issueKey)
    {
        if (count < max)
        {
            issueIds.add(issueId);
        }
        count++;
    }

    /**
     * Resets the collected data. Max is not changed
     */
    public void reset()
    {
        count = 0;
        issueIds.clear();
    }

    public int getMax()
    {
        return max;
    }

    public void setMax(int max)
    {
        this.max = max;
    }

    /**
     * Get all collected ids, bounded at max
     */
    public List<Long> getIssueIds()
    {
        return issueIds;
    }

    /**
     * Get the complete count of matched issues
     */
    public int getTotalCount()
    {
        return count;
    }
}
