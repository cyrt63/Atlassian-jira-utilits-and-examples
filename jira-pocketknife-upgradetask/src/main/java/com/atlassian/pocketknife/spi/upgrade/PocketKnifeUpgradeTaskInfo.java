package com.atlassian.pocketknife.spi.upgrade;

import com.atlassian.sal.api.upgrade.PluginUpgradeTask;

import java.util.List;

/**
 * What upgrade tasks do you want to run
 */
public interface PocketKnifeUpgradeTaskInfo {
    /**
     * @return a list of the plugin upgrade task classes you want PocketKnife to be able to run for you.  Provide an empty list if you have not upgrade tasks
     */
    List<Class<? extends PluginUpgradeTask>> getUpgradeTasks();
}
