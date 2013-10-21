package com.atlassian.pocketknife.internal.upgrade;

import com.atlassian.pocketknife.api.upgrade.UpgradeTaskException;
import com.atlassian.pocketknife.spi.info.PocketKnifePluginInfo;
import com.atlassian.sal.api.upgrade.PluginUpgradeTask;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;

import java.util.List;

/**
 * A class that runs upgrade tasks on behalf of the UpgradeVersionService
 */
class UpgradeTaskRunner
{
    private static final Logger log = Logger.getLogger(UpgradeTaskRunner.class);

    private final UpgradeVersionServiceImpl upgradeVersionService;

    public UpgradeTaskRunner(UpgradeVersionServiceImpl upgradeVersionService)
    {
        this.upgradeVersionService = upgradeVersionService;
    }

    public boolean runUpgradeTasks(PocketKnifePluginInfo pocketKnifePluginInfo, List<PluginUpgradeTask> sortedPluginUpgradeTasks, Integer latestUpgradeTaskRun) throws UpgradeTaskException
    {
        long startFrom = latestUpgradeTaskRun == null ? -1 : latestUpgradeTaskRun;
        try
        {
            for (PluginUpgradeTask upgradeTask : sortedPluginUpgradeTasks)
            {
                if (upgradeTask.getBuildNumber() > startFrom)
                {
                    runTask(upgradeTask);
                }
            }
            return true;
        }
        catch (Exception e)
        {
            log.error(String.format("Unable to run all upgrade tasks for '%s'", pocketKnifePluginInfo.getPluginKey()), e);
            throw new UpgradeTaskException(e);
        }
    }

    private void runTask(PluginUpgradeTask task) throws Exception
    {
        Logger log = Logger.getLogger(task.getClass().getPackage().getName());

        // keep current log level
        Level level = log.getLevel();

        // make sure everything is logged as info
        log.setLevel(Level.INFO);

        upgradeVersionService.recordUpgradeTaskStarted(makeBuildNumber(task.getBuildNumber()));

        logUpgradeTaskStart(log, task);

        long then = System.currentTimeMillis();

        // do things
        try
        {
            task.doUpgrade();
        }
        finally
        {
            // restore previous log level
            log.setLevel(level);
        }

        upgradeVersionService.recordUpgradeTaskEnded(makeBuildNumber(task.getBuildNumber()), System.currentTimeMillis() - then);
        logUpgradeTaskEnd(log, task);


        // set the latest upgrade version
        upgradeVersionService.setLatestUpgradedVersion(task.getBuildNumber());
    }

    private String makeBuildNumber(int buildNumber)
    {
        return "UPG-" + buildNumber;
    }

    private void logUpgradeTaskStart(Logger log, PluginUpgradeTask task)
    {
        log.info("=========================================");
        log.info("Starting upgrade task (buildNumber=" + task.getBuildNumber() + ") : " + task.getShortDescription());
    }

    private void logUpgradeTaskEnd(Logger log, PluginUpgradeTask task)
    {
        log.info("Upgrade task finished (buildNumber=" + task.getBuildNumber() + ")");
        log.info("=========================================");
    }
}
