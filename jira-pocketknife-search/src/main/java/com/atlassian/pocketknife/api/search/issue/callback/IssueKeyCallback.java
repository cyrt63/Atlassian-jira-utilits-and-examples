package com.atlassian.pocketknife.api.search.issue.callback;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

/**
 * Data callback for issue keys
 */
public class IssueKeyCallback implements DataCallback {
    private Set<String> fields = Collections.emptySet();
    private List<String> issueKeys;
    private int count = 0, max;

    public IssueKeyCallback() {
        this(Integer.MAX_VALUE);
    }

    /**
     * @param max the maximum number of ids to retain. -1 for unlimited
     */
    public IssueKeyCallback(int max) {
        issueKeys = new ArrayList<String>();
        this.count = 0;
        this.max = max < 0 ? Integer.MAX_VALUE : max;
    }

    @Override
    public Set<String> getFields() {
        return fields;
    }

    @Override
    public void fieldData(Long issueId, String issueKey, String name, String value) {
    }

    @Override
    public void documentComplete(Long issueId, String issueKey) {
        if (count < max) {
            issueKeys.add(issueKey);
        }
        count++;
    }

    /**
     * Resets the collected data. Max is not changed
     */
    public void reset() {
        count = 0;
        issueKeys.clear();
    }

    public int getMax() {
        return max;
    }

    public void setMax(int max) {
        this.max = max;
    }

    /**
     * Get all collected ids, bounded at max
     */
    public List<String> getIssueKeys() {
        return issueKeys;
    }

    /**
     * Get the complete count of matched issues
     */
    public int getTotalCount() {
        return count;
    }
}
