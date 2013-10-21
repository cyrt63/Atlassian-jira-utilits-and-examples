package com.atlassian.pocketknife.internal.upgrade;

import com.atlassian.pocketknife.spi.info.PocketKnifePluginInfo;
import com.atlassian.sal.api.message.Message;
import com.atlassian.sal.api.upgrade.PluginUpgradeTask;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Collection;

public abstract class AbstractUpgradeTask implements PluginUpgradeTask
{
    @Autowired
    protected PocketKnifePluginInfo pocketKnifePluginInfo;

    @Override
    public final Collection<Message> doUpgrade() throws Exception
    {
        performUpgrade();
        return null;
    }

    @Override
    public String getPluginKey()
    {
        return pocketKnifePluginInfo.getPluginKey();
    }


    protected abstract void performUpgrade() throws Exception;
}