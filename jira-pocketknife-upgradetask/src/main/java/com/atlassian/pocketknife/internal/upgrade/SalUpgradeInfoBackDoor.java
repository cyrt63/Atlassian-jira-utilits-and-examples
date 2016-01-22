package com.atlassian.pocketknife.internal.upgrade;

import com.atlassian.pocketknife.spi.ao.PocketKnifeActiveObjectsIntegration;
import com.atlassian.pocketknife.spi.info.PocketKnifePluginInfo;
import com.atlassian.sal.api.pluginsettings.PluginSettings;
import com.atlassian.sal.api.pluginsettings.PluginSettingsFactory;
import com.atlassian.util.concurrent.LazyReference;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * I wish this class was not necessary but SAL does NOT tell us the upgrade tasks it has run nor the AO
 * tasks that have been run.  It does however record them in PluginSettings.  So we use this backdoor class to
 * gain access to this information.
 * <p>
 * The danger of course is that they change where this information is stored however the risks of that have been
 * weighed up against the fact that SAL and AO do not have API to ask for this information and hence this class exists.
 * <p>
 * If there was an API we would use that!
 * <p>
 * See
 * https://ecosystem.atlassian.net/browse/AO-450
 * https://ecosystem.atlassian.net/browse/SAL-229
 */
@Service
public class SalUpgradeInfoBackDoor {
    public static final String BUILD = ":build";

    @Autowired
    PocketKnifeActiveObjectsIntegration activeObjectsIntegration;

    @Autowired
    PocketKnifePluginInfo pluginInfo;

    @Autowired
    PluginSettingsFactory pluginSettingsFactory;

    private LazyReference<PluginSettings> globalSettings = new LazyReference<PluginSettings>() {
        @Override
        protected PluginSettings create() throws Exception {
            return pluginSettingsFactory.createGlobalSettings();
        }
    };

    /**
     * This is cached for performance reasons.  Jed I know you are having an immutable fit.
     */
    private transient Integer latestUpgradedVersion = null;
    private transient Integer latestActiveObjectsUpgradedVersion = null;


    public Integer getLastUpgradeTaskRun() {
        if (latestUpgradedVersion == null) {

            //
            // This is the code that PluginUpgrader uses in SAL
            //
            //     pluginSettings.put(plugin.getKey() + BUILD, String.valueOf(buildNumber));
            //
            String pluginUpgradeKey = makeUpgradeKey();
            latestUpgradedVersion = toInt(getGlobalSettings().get(pluginUpgradeKey), 0);
        }
        return latestUpgradedVersion;
    }

    public Integer getActiveObjectsModelVersion() {
        if (latestActiveObjectsUpgradedVersion == null) {
            String aoSettingsPrefix = makeAoSchemaKey();
            latestActiveObjectsUpgradedVersion = toInt(getGlobalSettings().get(aoSettingsPrefix), 0);
        }
        return latestActiveObjectsUpgradedVersion;
    }

    public void setLastUpgradeTaskRun(int buildNumber) {
        getGlobalSettings().put(makeUpgradeKey(), String.valueOf(buildNumber));
        latestUpgradedVersion = buildNumber;
    }


    private String makeUpgradeKey() {
        return pluginInfo.getPluginKey() + BUILD;
    }

    private String makeAoSchemaKey() {
        return "AO" + "_" + activeObjectsIntegration.getTableHash() + "_" + "#";
    }

    private Integer toInt(Object o, Integer defaultVal) {
        try {
            if (o != null) {
                return Integer.parseInt(String.valueOf(o));
            }
            return defaultVal;
        } catch (NumberFormatException e) {
            return defaultVal;
        }
    }

    private PluginSettings getGlobalSettings() {
        return globalSettings.get();
    }

}
