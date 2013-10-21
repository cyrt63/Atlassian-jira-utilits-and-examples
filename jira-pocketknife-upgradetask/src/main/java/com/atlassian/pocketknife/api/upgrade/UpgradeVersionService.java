package com.atlassian.pocketknife.api.upgrade;


import java.util.List;

/**
 * A service to help an add on run its own upgrade tasks and also give a history when all upgrade tasks have been run
 */
public interface UpgradeVersionService
{
    boolean runUpgradeTasks() throws UpgradeTaskException;

    boolean hasLastUpgradeTaskCompleted();

    boolean hasLastActiveObjectsUpgradeTaskCompleted();

    boolean versionLooksKosher();

    PluginRunInfo recordPluginStarted();

    PluginRunInfo getCurrentPluginRunInfo();

    List<PluginRunInfo> getPluginRunHistory();

    void recordUpgradeTaskStarted(String buildNumber);

    void recordUpgradeTaskEnded(String buildNumber, long timeTaken);

    List<UpgradeHistoryDetail> getUpgradeHistory();

    void checkForDowngrade() throws DowngradeException;
}
