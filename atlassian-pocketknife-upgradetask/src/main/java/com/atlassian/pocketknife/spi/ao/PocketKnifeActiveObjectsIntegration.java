package com.atlassian.pocketknife.spi.ao;

/**
 * This allows you to specify the ActiveObject upgrade tasks you expect to be up to
 */
public interface PocketKnifeActiveObjectsIntegration
{
    int getExpectedAOModelVersion();

    /**
     * @return the well know name of your AO tables
     */
    String getTableHash();
}
