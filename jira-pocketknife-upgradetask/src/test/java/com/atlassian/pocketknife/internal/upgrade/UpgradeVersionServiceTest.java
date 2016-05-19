package com.atlassian.pocketknife.internal.upgrade;

import com.atlassian.jira.util.lang.Pair;
import com.atlassian.pocketknife.api.persistence.PersistenceService;
import com.atlassian.pocketknife.api.upgrade.PluginRunInfo;
import com.atlassian.pocketknife.spi.info.PocketKnifePluginInfo;
import com.google.common.collect.ImmutableMap;
import org.joda.time.DateTime;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.Map;

import static com.atlassian.pocketknife.internal.upgrade.UpgradeVersionServiceImpl.RUN_HISTORY;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.not;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.anyMap;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.contains;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class UpgradeVersionServiceTest {
    @Mock
    PersistenceService persistenceService;
    @Mock
    SalUpgradeInfoBackDoor salUpgradeInfoBackDoor;

    PocketKnifePluginInfo pocketKnifePluginInfo;

    UpgradeVersionServiceImpl upgradeVersionService;

    @Before
    public void setUp() {
        pocketKnifePluginInfo = new PocketKnifePluginInfo() {
            private String pluginKey = "test-plugin";
            private String version = "3.2.0";
            private DateTime buildDate = new DateTime();
            private String changeSet = "1";
            private String productName = "test-service-desk";

            @Override
            public String getPluginKey() {
                return pluginKey;
            }

            @Override
            public String getVersion() {
                return version;
            }

            @Override
            public DateTime getBuildDate() {
                return buildDate;
            }

            @Override
            public String getChangeSet() {
                return changeSet;
            }

            @Override
            public String getProductName() {
                return productName;
            }
        };

        upgradeVersionService = new UpgradeVersionServiceImpl(
                persistenceService,
                null,
                pocketKnifePluginInfo,
                null,
                null,
                salUpgradeInfoBackDoor
        );
    }

    private void prepareFirstRun() {
        when(
                persistenceService.getData(
                        contains(RUN_HISTORY),
                        eq(1L),
                        eq(RUN_HISTORY)
                )
        ).thenReturn(null);
    }

    private void prepareSubsequentRun(Map<String, Object> persistedData) {
        when(
                persistenceService.getData(
                        contains(RUN_HISTORY),
                        eq(1L),
                        eq(RUN_HISTORY)
                )
        ).thenReturn(persistedData);
    }

    @Test
    public void getPluginRunInfoImpl__returns_same_current_and_previous_data_on_first_run() {
        prepareFirstRun();

        Pair<PluginRunInfoImpl, Boolean> pluginRunInfoPair = upgradeVersionService.getPluginRunInfoImpl();

        assertThat("second supposed to be true on first run", pluginRunInfoPair.second(), equalTo(true));

        PluginRunInfo pluginRunInfo = pluginRunInfoPair.first();

        assertThat("current version should be from pocketknife plugin info", pluginRunInfo.getCurrentVersion(), equalTo(pocketKnifePluginInfo.getVersion()));
        assertThat("previous version should match current version", pluginRunInfo.getPreviousVersion(), equalTo(pluginRunInfo.getCurrentVersion()));

        assertThat("current build date should be from pocketknife plugin info", pluginRunInfo.getCurrentBuildDate(), equalTo(pocketKnifePluginInfo.getBuildDate()));
        assertThat("previous build date should match current build date", pluginRunInfo.getPreviousBuildDate(), equalTo(pluginRunInfo.getCurrentBuildDate()));

        assertThat("there can't be downgrade on first run", pluginRunInfo.isDowngrade(), equalTo(false));
    }

    @Test
    public void getPluginRunInfoImpl__returns_different_current_and_previous_data_on_subsequent_runs() {
        Map<String, Object> persistedData = ImmutableMap.<String, Object>of(
                "pluginVersion", "2.2.0-OD-14-001-SNAPSHOT",
                "ranOn", "2016-05-17T12:41:47.187+10:00",
                "buildDate", "2016-05-17T12:38:43.404+10:00",
                "latestUpgradeTaskRun", "2",
                "changeSet", "DevVersion"
        );

        prepareSubsequentRun(persistedData);

        Pair<PluginRunInfoImpl, Boolean> pluginRunInfoPair = upgradeVersionService.getPluginRunInfoImpl();

        assertThat("second supposed to be false on subsequent runs", pluginRunInfoPair.second(), equalTo(false));

        PluginRunInfo pluginRunInfo = pluginRunInfoPair.first();

        assertThat("current version should be from pocketknife plugin info", pluginRunInfo.getCurrentVersion(), equalTo(pocketKnifePluginInfo.getVersion()));
        assertThat("previous version should be different from current version", pluginRunInfo.getPreviousVersion(), not(equalTo(pluginRunInfo.getCurrentVersion())));
        assertThat("previous version should match persisted data", pluginRunInfo.getPreviousVersion(), equalTo(persistedData.get("pluginVersion")));

        assertThat("current build date should be from pocketknife plugin info", pluginRunInfo.getCurrentBuildDate(), equalTo(pocketKnifePluginInfo.getBuildDate()));
        assertThat("previous build date should be different from current build date", pluginRunInfo.getPreviousBuildDate(), not(equalTo(pluginRunInfo.getCurrentBuildDate())));
        assertThat("previous build date should match persisted data", pluginRunInfo.getPreviousBuildDate(), equalTo(PluginRunInfoImpl.parseIsoDate((String) persistedData.get("buildDate"))));
    }

    @Test
    public void getPluginRunInfoImpl__detects_downgrade_when_current_build_date_is_earlier_than_previous() {
        Map<String, Object> persistedData = ImmutableMap.<String, Object>of(
                "pluginVersion", "2.2.0-OD-14-001-SNAPSHOT",
                "ranOn", "2016-05-17T12:41:47.187+10:00",
                "buildDate", "3016-05-17T12:38:43.404+10:00", // some time in far future
                "latestUpgradeTaskRun", "2",
                "changeSet", "DevVersion"
        );

        prepareSubsequentRun(persistedData);

        Pair<PluginRunInfoImpl, Boolean> pluginRunInfoPair = upgradeVersionService.getPluginRunInfoImpl();
        PluginRunInfo pluginRunInfo = pluginRunInfoPair.first();

        assertThat("downgrade should be detected", pluginRunInfo.isDowngrade(), equalTo(true));
    }

    @Test
    public void recordPluginStarted__writes_to_history_on_first_run() {
        prepareFirstRun();

        upgradeVersionService.recordPluginStarted();

        verify(persistenceService, times(1)).setData(contains(RUN_HISTORY), eq(1L), eq(RUN_HISTORY), anyMap());
        verify(persistenceService, times(1)).setData(contains(RUN_HISTORY), eq(2L), anyString(), anyMap());
    }

    @Test
    public void recordPluginStarted__doesnt_write_to_history_on_subsequent_run_when_changeset_is_the_same() {
        Map<String, Object> persistedData = ImmutableMap.<String, Object>of(
                "pluginVersion", "2.2.0-OD-14-001-SNAPSHOT",
                "ranOn", "2016-05-17T12:41:47.187+10:00",
                "buildDate", "2016-05-17T12:38:43.404+10:00",
                "latestUpgradeTaskRun", "2",
                "changeSet", pocketKnifePluginInfo.getChangeSet()
        );

        prepareSubsequentRun(persistedData);

        upgradeVersionService.recordPluginStarted();

        verify(persistenceService, times(1)).setData(contains(RUN_HISTORY), eq(1L), eq(RUN_HISTORY), anyMap());
        verify(persistenceService, never()).setData(anyString(), eq(2L), anyString(), anyMap());
    }

    @Test
    public void recordPluginStarted__writes_to_history_on_subsequent_runs_when_changeset_is_different() {
        Map<String, Object> persistedData = ImmutableMap.<String, Object>of(
                "pluginVersion", "2.2.0-OD-14-001-SNAPSHOT",
                "ranOn", "2016-05-17T12:41:47.187+10:00",
                "buildDate", "2016-05-17T12:38:43.404+10:00",
                "latestUpgradeTaskRun", "2",
                "changeSet", "2"
        );

        prepareSubsequentRun(persistedData);

        upgradeVersionService.recordPluginStarted();

        verify(persistenceService, times(1)).setData(contains(RUN_HISTORY), eq(1L), eq(RUN_HISTORY), anyMap());
        verify(persistenceService, times(1)).setData(contains(RUN_HISTORY), eq(2L), anyString(), anyMap());
    }
}
