package com.atlassian.pocketknife.internal.upgrade;

import com.atlassian.pocketknife.api.upgrade.PluginRunInfo;
import com.atlassian.pocketknife.spi.info.PocketKnifePluginInfo;
import com.google.common.collect.Maps;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.ISODateTimeFormat;

import java.util.HashMap;
import java.util.Map;

import static java.lang.String.valueOf;

/**
 * What was run and when?
 */
public class PluginRunInfoImpl implements PluginRunInfo, Comparable<PluginRunInfo>
{
    static final String DD_MMM_YYYY_HH_MM = "dd MMM yyyy hh:mm";

    private final String currentVersion;
    private final DateTime currentBuildDate;
    private final String previousVersion;
    private final DateTime previousBuildDate;
    private final String currentChangeSet;
    private final String previousChangeSet;
    private final Long latestUpgradeTaskRun;

    private final DateTime previousRanOn;
    private final DateTime currentRanOn;
    private final boolean downgrade;


    public PluginRunInfoImpl(final DateTime previousRanOn, final Long latestUpgradeTaskRun, final PocketKnifePluginInfo pocketKnifePluginInfo)
    {
        this(previousRanOn,
                latestUpgradeTaskRun,
                pocketKnifePluginInfo.getVersion(),
                pocketKnifePluginInfo.getBuildDate(),
                pocketKnifePluginInfo.getChangeSet(),
                pocketKnifePluginInfo);
    }

    public PluginRunInfoImpl(final Map<String, Object> data, final PocketKnifePluginInfo pocketKnifePluginInfo)
    {
        this(parseIsoDate(valueOf(data.get("ranOn"))),
                Long.valueOf(valueOf(data.get("latestUpgradeTaskRun"))),
                valueOf(data.get("pluginVersion")),
                parseIsoDate(valueOf(data.get("buildDate"))),
                valueOf(data.get("changeSet")),
                pocketKnifePluginInfo);
    }

    public PluginRunInfoImpl(final DateTime previousRanOn, final Long latestUpgradeTaskRun, final String previousVersion, final DateTime previousBuildDate, final String previousChangeSet, final PocketKnifePluginInfo pocketKnifePluginInfo)
    {
        this.previousRanOn = previousRanOn;
        this.previousVersion = previousVersion;
        this.previousBuildDate = previousBuildDate;
        this.previousChangeSet = previousChangeSet;
        this.currentVersion = pocketKnifePluginInfo.getVersion();
        this.currentBuildDate = pocketKnifePluginInfo.getBuildDate();
        this.currentChangeSet = pocketKnifePluginInfo.getChangeSet();
        this.currentRanOn = new DateTime();
        this.latestUpgradeTaskRun = latestUpgradeTaskRun;
        this.downgrade = currentBuildDate.isBefore(previousBuildDate);
    }

    public Map<String, Object> toPersistenceMap()
    {
        HashMap<String, Object> map = Maps.newHashMap();
        map.put("pluginVersion", currentVersion);
        map.put("buildDate", toIsoDateStr(currentBuildDate));
        map.put("changeSet", currentChangeSet);
        map.put("latestUpgradeTaskRun", String.valueOf(latestUpgradeTaskRun));
        map.put("ranOn", toIsoDateStr(currentRanOn));
        return map;
    }

    @Override
    public int compareTo(final PluginRunInfo that)
    {
        return previousRanOn.compareTo(that.getPreviousRanOn());
    }

    @Override
    public String getCurrentVersion()
    {
        return currentVersion;
    }

    @Override
    public DateTime getCurrentBuildDate()
    {
        return currentBuildDate;
    }

    @Override
    public String getCurrentChangeSet()
    {
        return currentChangeSet;
    }

    @Override
    public DateTime getCurrentRanOn()
    {
        return currentRanOn;
    }

    @Override
    public Long getLatestUpgradeTaskRun()
    {
        return latestUpgradeTaskRun;
    }

    @Override
    public String getPreviousVersion()
    {
        return previousVersion;
    }

    @Override
    public DateTime getPreviousBuildDate()
    {
        return previousBuildDate;
    }

    @Override
    public String getPreviousChangeSet()
    {
        return previousChangeSet;
    }


    @Override
    public DateTime getPreviousRanOn()
    {
        return previousRanOn;
    }

    @Override
    public String getRanOnStr()
    {
        return previousRanOn.toString(DateTimeFormat.forPattern(DD_MMM_YYYY_HH_MM));
    }

    @Override
    public boolean isDowngrade()
    {
        return downgrade;
    }

    @Override
    public boolean isDifferentPlugin()
    {
        return !previousChangeSet.equals(currentChangeSet);
    }

    static String toIsoDateStr(final DateTime dateTime)
    {
        return dateTime.toString(ISODateTimeFormat.dateTime());
    }

    static DateTime parseIsoDate(final String isoDTStr)
    {
        return ISODateTimeFormat.dateTime().parseDateTime(isoDTStr);
    }
}
