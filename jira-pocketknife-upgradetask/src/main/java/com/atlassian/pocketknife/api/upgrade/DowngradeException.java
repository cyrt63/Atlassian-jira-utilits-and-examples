package com.atlassian.pocketknife.api.upgrade;

import com.atlassian.pocketknife.internal.upgrade.PluginRunInfoImpl;

/**
 */
public class DowngradeException extends Exception
{
    public DowngradeException(PluginRunInfoImpl runInfo)
    {
        super(String.format("A downgrade has been detected from '%s' down from '%s'", runInfo.getCurrentVersion(), runInfo.getPreviousVersion()));
    }
}
