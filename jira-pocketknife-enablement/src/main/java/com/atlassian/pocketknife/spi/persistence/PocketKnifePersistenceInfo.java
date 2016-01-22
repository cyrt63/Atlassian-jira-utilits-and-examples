package com.atlassian.pocketknife.spi.persistence;

/**
 * Where should Pocketknife store your configuration data
 */
public interface PocketKnifePersistenceInfo {
    /**
     * Don't change this value over time once you have decided on it because you will lose your data strored
     *
     * @return the name to use as the {@link com.atlassian.pocketknife.api.persistence.GlobalPropertyDao} entity name
     */
    String getGlobalPropertyDaoEntityName();
}
