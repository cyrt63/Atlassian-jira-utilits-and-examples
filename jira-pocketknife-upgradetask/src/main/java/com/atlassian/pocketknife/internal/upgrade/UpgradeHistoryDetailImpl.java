package com.atlassian.pocketknife.internal.upgrade;

import com.atlassian.pocketknife.api.upgrade.UpgradeHistoryDetail;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;

import java.util.Map;

import static java.lang.String.valueOf;

/**
*/
public class UpgradeHistoryDetailImpl implements UpgradeHistoryDetail
{
    private final DateTime ranOn;
    private final String buildNumber;
    private final String pluginVersion;
    private final String changeSet;
    private final String timeTaken;
    private final boolean startRecord;

    public UpgradeHistoryDetailImpl(Map<String, Object> data)
    {
        this.ranOn = PluginRunInfoImpl.parseIsoDate(valueOf(data.get("ranOn")));
        this.buildNumber = valueOf(data.get("buildNumber"));
        this.pluginVersion = valueOf(data.get("pluginVersion"));
        this.changeSet = valueOf(data.get("changeSet"));
        String timeTakenStr = valueOf(data.get("timeTaken"));
        if ("-1".equals(timeTakenStr))
        {
            timeTaken = "";
            startRecord = true;
        }
        else
        {
            timeTaken = timeTakenStr;
            startRecord = false;
        }
    }

    @Override
    public DateTime getRanOn()
    {
        return ranOn;
    }

    @Override
    public String getRanOnStr()
    {
        return ranOn.toString(DateTimeFormat.forPattern("dd MMM yyyy hh:mm"));
    }

    @Override
    public String getBuildNumber()
    {
        return buildNumber;
    }

    @Override
    public String getPluginVersion()
    {
        return pluginVersion;
    }

    @Override
    public String getChangeSet()
    {
        return changeSet;
    }

    @Override
    public String getTimeTaken()
    {
        return timeTaken;
    }

    @Override
    public boolean isStartRecord()
    {
        return startRecord;
    }

    @Override
    public int compareTo(final UpgradeHistoryDetail that)
    {
        return this.getRanOn().compareTo(that.getRanOn());
    }
}
