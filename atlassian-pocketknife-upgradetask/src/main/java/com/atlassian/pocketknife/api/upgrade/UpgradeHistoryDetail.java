package com.atlassian.pocketknife.api.upgrade;

import org.joda.time.DateTime;

/**
 * Information about what upgrade tasks have run for a plugin before
 */
public interface UpgradeHistoryDetail extends Comparable<UpgradeHistoryDetail>
{
    DateTime getRanOn();

    String getRanOnStr();

    String getBuildNumber();

    String getPluginVersion();

    String getChangeSet();

    String getTimeTaken();

    boolean isStartRecord();

    int compareTo(UpgradeHistoryDetail that);
}
