package com.atlassian.pocketknife.spi.info;

import org.joda.time.DateTime;

/**
 * Identify yourself!
 */
public interface PocketKnifePluginInfo {

    /**
     * @return the plugin key
     */
    String getPluginKey();

    /**
     * Get the version of this build
     */
    String getVersion();

    /**
     * Get the build date.
     */
    DateTime getBuildDate();

    /**
     * Get the change set this build is made of.
     */
    String getChangeSet();

    /**
     * @return the name of the product
     */
    String getProductName();
}
