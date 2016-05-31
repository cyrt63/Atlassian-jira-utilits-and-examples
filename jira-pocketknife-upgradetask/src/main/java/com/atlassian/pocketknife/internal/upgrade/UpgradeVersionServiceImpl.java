package com.atlassian.pocketknife.internal.upgrade;

import com.atlassian.jira.util.lang.Pair;
import com.atlassian.pocketknife.api.autowire.PluginAutowirer;
import com.atlassian.pocketknife.api.persistence.PersistenceService;
import com.atlassian.pocketknife.api.upgrade.DowngradeException;
import com.atlassian.pocketknife.api.upgrade.PluginRunInfo;
import com.atlassian.pocketknife.api.upgrade.UpgradeHistoryDetail;
import com.atlassian.pocketknife.api.upgrade.UpgradeTaskException;
import com.atlassian.pocketknife.api.upgrade.UpgradeVersionService;
import com.atlassian.pocketknife.spi.ao.PocketKnifeActiveObjectsIntegration;
import com.atlassian.pocketknife.spi.info.PocketKnifePluginInfo;
import com.atlassian.pocketknife.spi.upgrade.PocketKnifeUpgradeTaskInfo;
import com.atlassian.sal.api.upgrade.PluginUpgradeTask;
import com.atlassian.util.concurrent.LazyReference;
import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import org.joda.time.DateTime;
import org.joda.time.format.ISODateTimeFormat;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static java.lang.String.valueOf;

/**
 * Keeps track of the last upgrade task number to run. Checks if upgrades are currently running.
 */
@Service
public class UpgradeVersionServiceImpl implements UpgradeVersionService {
    static final String UPGRADE_HISTORY = "Upgrade.History";
    static final String RUN_HISTORY = "Run.History";

    private PersistenceService persistenceService;

    private PluginAutowirer pluginAutowirer;

    private PocketKnifePluginInfo pocketKnifePluginInfo;

    private PocketKnifeUpgradeTaskInfo pocketKnifeUpgradeTaskInfo;

    private PocketKnifeActiveObjectsIntegration pocketKnifeActiveObjectsIntegration;

    private SalUpgradeInfoBackDoor upgradeInfoBackDoor;

    @Autowired
    public UpgradeVersionServiceImpl(
            PersistenceService persistenceService,
            PluginAutowirer pluginAutowirer,
            PocketKnifePluginInfo pocketKnifePluginInfo,
            PocketKnifeUpgradeTaskInfo pocketKnifeUpgradeTaskInfo,
            PocketKnifeActiveObjectsIntegration pocketKnifeActiveObjectsIntegration,
            SalUpgradeInfoBackDoor upgradeInfoBackDoor) {

        this.persistenceService = persistenceService;
        this.pluginAutowirer = pluginAutowirer;
        this.pocketKnifePluginInfo = pocketKnifePluginInfo;
        this.pocketKnifeUpgradeTaskInfo = pocketKnifeUpgradeTaskInfo;
        this.pocketKnifeActiveObjectsIntegration = pocketKnifeActiveObjectsIntegration;
        this.upgradeInfoBackDoor = upgradeInfoBackDoor;
    }

    // sorted by ascending build number
    private static final Comparator<PluginUpgradeTask> UPGRADE_TASK_COMPARATOR = new Comparator<PluginUpgradeTask>() {
        @Override
        public int compare(PluginUpgradeTask task, PluginUpgradeTask task2) {
            return task.getBuildNumber() - task2.getBuildNumber();
        }
    };

    private LazyReference<List<PluginUpgradeTask>> upgradeTasksToRun = new LazyReference<List<PluginUpgradeTask>>() {
        @Override
        protected List<PluginUpgradeTask> create() throws Exception {
            List<Class<? extends PluginUpgradeTask>> upgradeTaskClasses = pocketKnifeUpgradeTaskInfo.getUpgradeTasks();
            if (upgradeTaskClasses == null) {
                return Collections.emptyList();
            }
            List<PluginUpgradeTask> pluginUpgradeTasks = instantiateTasks(upgradeTaskClasses);
            //
            // by sorting the upgrade tasks we KNOW which is the first and last and hence in what order to run them
            Collections.sort(pluginUpgradeTasks, UPGRADE_TASK_COMPARATOR);
            return pluginUpgradeTasks;
        }
    };


    private List<PluginUpgradeTask> instantiateTasks(List<Class<? extends PluginUpgradeTask>> upgradeTaskClasses) {
        List<PluginUpgradeTask> pluginUpgradeTasks = Lists.newArrayList();
        for (Class<? extends PluginUpgradeTask> upgradeTask : upgradeTaskClasses) {
            PluginUpgradeTask instantiatedUpgradeTask = pluginAutowirer.autowire(upgradeTask);
            pluginUpgradeTasks.add(instantiatedUpgradeTask);
        }
        return pluginUpgradeTasks;
    }

    /**
     * This is called to run the plugins provided upgrade tasks.  You should call this in an appropriate spot such as during the plugin started event
     *
     * @return true if all the plugin upgrade tasks have succeeded
     */
    @Override
    public boolean runUpgradeTasks() throws UpgradeTaskException {
        return new UpgradeTaskRunner(this).runUpgradeTasks(pocketKnifePluginInfo, upgradeTasksToRun.get(), upgradeInfoBackDoor.getLastUpgradeTaskRun());
    }

    @Override
    public boolean hasLastUpgradeTaskCompleted() {
        return getExpectedUpgradeTask() == upgradeInfoBackDoor.getLastUpgradeTaskRun();
    }

    private int getExpectedUpgradeTask() {
        List<PluginUpgradeTask> pluginUpgradeTasks = upgradeTasksToRun.get();
        if (pluginUpgradeTasks.isEmpty()) {
            return 0;
        }
        return Iterables.getLast(pluginUpgradeTasks).getBuildNumber();
    }

    @Override
    public boolean hasLastActiveObjectsUpgradeTaskCompleted() {
        return pocketKnifeActiveObjectsIntegration.getExpectedAOModelVersion() == upgradeInfoBackDoor.getActiveObjectsModelVersion();
    }

    /**
     * @return true if the version looks kosher from an upgrade and plugin version point of view
     */
    @Override
    public boolean versionLooksKosher() {
        return !getCurrentPluginRunInfo().isDowngrade() && hasLastUpgradeTaskCompleted();
    }

    @Override
    public void checkForDowngrade() throws DowngradeException {
        PluginRunInfoImpl currentPluginRunInfo = getCurrentPluginRunInfo();
        if (currentPluginRunInfo.isDowngrade()) {
            throw new DowngradeException(currentPluginRunInfo);
        }
    }

    private String pfx(String key) {
        return pocketKnifePluginInfo.getPluginKey() + ":" + key;
    }


    /**
     * Called to record when a plugin has started and at what version.  If the plugin
     * has changed since the last time it was run it will return information about that
     *
     * @return information about the current run version of SD
     */

    @Override
    public PluginRunInfoImpl recordPluginStarted() {
        Pair<PluginRunInfoImpl, Boolean> pair = getPluginRunInfoImpl();

        PluginRunInfoImpl current = pair.first();

        persistenceService.setData(pfx(RUN_HISTORY), 1L, RUN_HISTORY, current.toPersistenceMap());

        // pair.second means we found no previous data
        if (pair.second() || current.isDifferentPlugin()) {
            //
            // now add a history item to show we have changed versions. NOTE : we use a DIFFERENT entity id (2) here to segment the data
            // and we use now DateTime() as the unique key generation
            //
            persistenceService.setData(pfx(RUN_HISTORY), 2L, toIsoDateStr(new DateTime()), current.toPersistenceMap());
        }

        return current;
    }

    /**
     * @return the current plugin run info, which can tell if there has been a down grade
     */
    @Override
    public PluginRunInfoImpl getCurrentPluginRunInfo() {
        return getPluginRunInfoImpl().first();
    }

    @VisibleForTesting
    Pair<PluginRunInfoImpl, Boolean> getPluginRunInfoImpl() {

        PluginRunInfoImpl previousRunInfo;
        Map<String, Object> prevData = persistenceService.getData(pfx(RUN_HISTORY), 1L, RUN_HISTORY);
        if (prevData == null) {
            // we have never recorded this before - synthesize one
            Integer latestUpgradeTaskRun = nvl(upgradeInfoBackDoor.getLastUpgradeTaskRun(), 0);
            previousRunInfo = new PluginRunInfoImpl(new DateTime(), Long.valueOf(latestUpgradeTaskRun), pocketKnifePluginInfo);
        } else {
            previousRunInfo = new PluginRunInfoImpl(prevData, pocketKnifePluginInfo);
        }

        return Pair.of(previousRunInfo, prevData == null);
    }

    /**
     * This can be called to get a list of the history of a plugin running
     *
     * @return the list of plugin run history
     */
    @Override
    public List<PluginRunInfo> getPluginRunHistory() {
        List<PluginRunInfo> runInfoList = Lists.newArrayList();
        Set<String> keys = defaultSet(persistenceService.getKeys(pfx(RUN_HISTORY), 2L));
        for (String key : keys) {
            Map<String, Object> data = persistenceService.getData(pfx(RUN_HISTORY), 2L, key);
            if (data != null) {
                runInfoList.add(new PluginRunInfoImpl(data, pocketKnifePluginInfo));
            }
        }
        Collections.sort(runInfoList);
        return runInfoList;
    }

    /**
     * Call this to record that a plugin upgrade task has been started
     *
     * @param buildNumber the build number of the upgrade task
     */
    public void recordUpgradeTaskStarted(String buildNumber) {
        recordUpgradeTaskRan(valueOf(buildNumber), -1, 1L);
    }

    /**
     * Call this to record that a plugin upgrade task has been completed
     *
     * @param buildNumber the build number of the upgrade task
     */
    public void recordUpgradeTaskEnded(String buildNumber, long timeTaken) {
        recordUpgradeTaskRan(valueOf(buildNumber), timeTaken, 2L);
    }

    void setLatestUpgradedVersion(int buildNumber) {
        upgradeInfoBackDoor.setLastUpgradeTaskRun(buildNumber);
    }

    private void recordUpgradeTaskRan(final String buildNumber, final long timeTaken, final Long entityId) {
        Map<String, Object> data = Maps.newHashMap();
        data.put("ranOn", toIsoDateStr(new DateTime()));
        data.put("buildNumber", valueOf(buildNumber));
        data.put("pluginVersion", pocketKnifePluginInfo.getVersion());
        data.put("changeSet", pocketKnifePluginInfo.getChangeSet());
        data.put("timeTaken", valueOf(timeTaken));

        persistenceService.setData(pfx(UPGRADE_HISTORY), entityId, valueOf(new DateTime()), data);
    }

    /**
     * @return a history of the plugin upgrade that have run
     */
    @Override
    public List<UpgradeHistoryDetail> getUpgradeHistory() {

        Set<String> startedKeys = defaultSet(persistenceService.getKeys(pfx(UPGRADE_HISTORY), 1L));
        Set<String> endedKeys = defaultSet(persistenceService.getKeys(pfx(UPGRADE_HISTORY), 2L));

        List<UpgradeHistoryDetail> upgradeHistoryDetails = Lists.newArrayList();
        for (String key : startedKeys) {
            Map<String, Object> data = persistenceService.getData(pfx(UPGRADE_HISTORY), 1L, key);
            if (data != null) {
                upgradeHistoryDetails.add(new UpgradeHistoryDetailImpl(data));
            }
        }
        for (String key : endedKeys) {
            Map<String, Object> data = persistenceService.getData(pfx(UPGRADE_HISTORY), 2L, key);
            if (data != null) {
                upgradeHistoryDetails.add(new UpgradeHistoryDetailImpl(data));
            }
        }
        Collections.sort(upgradeHistoryDetails);
        return upgradeHistoryDetails;
    }

    private Set<String> defaultSet(final Set<String> keys) {
        return keys == null ? Sets.<String>newHashSet() : keys;
    }

    private Integer nvl(final Integer l1, final Integer defaultVal) {
        return l1 == null ? defaultVal : l1;
    }

    private static String toIsoDateStr(final DateTime dateTime) {
        return dateTime.toString(ISODateTimeFormat.dateTime());
    }

}
