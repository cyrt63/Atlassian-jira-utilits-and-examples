package com.atlassian.pocketknife.api.upgrade;

/**
 */
public class UpgradeTaskException extends Exception {
    public UpgradeTaskException(Throwable t) {
        super("The upgrade tasks have failed to run", t);
    }
}
