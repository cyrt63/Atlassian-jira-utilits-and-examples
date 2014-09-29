package com.atlassian.pocketknife.api.util.runners;

import com.google.common.collect.Maps;

import java.util.List;
import java.util.Map;

/**
 * Often, in movies and games you will be sent on a quest to unlock something by activating a number of seals. Like the
 * stones at the end of "The Fifth Element". Once they are all activated, something happens. Same concept behind this
 * class.
 * <p/>
 * On construction, you will need to give this class a list of keys and a {@link java.lang.Runnable}. The keys will serve
 * as the seals that need to be broken before the runnable will run.
 * <p/>
 * Once all the seals have been broken, the runnable will run once and never again. If there are no seals provided,
 * then the Runnable will never run.
 * <p/>
 * NOTE: Keys are case insensitive.
 */
public class SealedRunner {
    private boolean hasRun;
    private final Runnable runnable;
    private Map<String, Boolean> seals;

    public SealedRunner(List<String> keys, Runnable runnable) {
        this.hasRun = false;
        this.runnable = runnable;
        this.seals = Maps.newHashMap();
        for (String key : keys) {
            seals.put(key, false);
        }
    }

    /**
     * Breaks a seal based on the key. An already broken seal may be broken again safely.
     * If all seals are broken after this call, the runnable will run.
     *
     * @param key - corresponding to a seal
     * @return false if the key does not match a valid seal.
     */
    public boolean breakSeal(final String key) {
        if (seals.containsKey(key)) {
            seals.put(key, true);
            checkSeals();
            return true;
        }
        return false;
    }

    /**
     * Repair a seal based on the key. An unbroken seal can be repaired safely.
     *
     * @param key - corresponding to a seal
     * @return false if the key does not match a valid seal.
     */
    public boolean repairSeal(final String key) {
        if (seals.containsKey(key)) {
            seals.put(key, false);
            return true;
        }
        return false;
    }

    /**
     * This will check if all the seals are unlocked, if they are, then it will run the runnable
     */
    private void checkSeals() {
        if (hasRun) {
            return;
        }
        boolean allSealsBroken = true;
        for (Boolean b : seals.values()) {
            allSealsBroken = allSealsBroken && b;
        }
        if (allSealsBroken) {
            runnable.run();
            hasRun = true;
        }
    }
}
