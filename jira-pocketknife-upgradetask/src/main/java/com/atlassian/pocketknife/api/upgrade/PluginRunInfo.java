package com.atlassian.pocketknife.api.upgrade;

import org.joda.time.DateTime;

/**
 * Information about when a plugin has run before.
 */
public interface PluginRunInfo extends Comparable<PluginRunInfo> {
    int compareTo(PluginRunInfo that);

    String getCurrentVersion();

    DateTime getCurrentBuildDate();

    String getCurrentChangeSet();

    DateTime getCurrentRanOn();

    Long getLatestUpgradeTaskRun();

    String getPreviousVersion();

    DateTime getPreviousBuildDate();

    String getPreviousChangeSet();

    DateTime getPreviousRanOn();

    String getRanOnStr();

    boolean isDowngrade();

    boolean isDifferentPlugin();
}
