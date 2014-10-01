package com.atlassian.pocketknife.api.util.runners;

/**
 * A seal can either be released or not. It is identified by a key.
 * Used for testing.
 */
public class Seal {
    private String key;
    private boolean sealBroken;
    private int timesBroken;

    public Seal(String key) {
        this(key, false);
    }

    public Seal(String key, boolean sealBroken) {
        this.key = key;
        this.sealBroken = sealBroken;
        this.timesBroken = 0;
    }

    public void breakSeal() {
        sealBroken = true;
        timesBroken++;
    }

    public boolean isSealBroken() {
        return sealBroken;
    }

    public int getTimesBroken() {
        return timesBroken;
    }
}
