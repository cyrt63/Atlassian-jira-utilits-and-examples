package com.atlassian.pocketknife.api.util.runners;

import com.atlassian.util.concurrent.Assertions;
import org.apache.commons.lang.StringUtils;

import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;
import javax.annotation.concurrent.ThreadSafe;

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
 */
@ThreadSafe
public class SealedRunner {
    private AtomicBoolean hasRun;
    private final Runnable runnable;
    private ConcurrentHashMap<String, Boolean> seals;

    /**
     * Creates a new sealed runner with the given list of seals and target runnable provided.
     *
     * @param keys the seals to require before the runnable is invoked
     * @param runnable the target code to run once all the seals are broken
     */
    public SealedRunner(List<String> keys, Runnable runnable) {
        this.hasRun = new AtomicBoolean(false);
        this.runnable = Assertions.notNull("runnable", runnable);
        this.seals = new ConcurrentHashMap<String, Boolean>();

        for (String key : keys) {
            if (StringUtils.isNotBlank(key)) {
                seals.put(key, false);
            } else {
                throw new IllegalArgumentException("The seals cannot be null or blank");
            }
        }
    }

    /**
     * Breaks a seal based on the key. An already broken seal may be broken again safely.
     * If all seals are broken after this call, the runnable will run.
     *
     * @param key - corresponding to a seal
     * @throws java.lang.IllegalArgumentException if you have passed in a key that is not known by the sealed runner
     */
    public void breakSeal(final String key) {
        if (key != null && seals.containsKey(key)) {
            seals.put(key, true);
            checkSeals();
        } else {
            throw new IllegalArgumentException("The key you have provided does not conform to a valid seal!");
        }
    }

    /**
     * Repair a seal based on the key. An unbroken seal can be repaired safely.  Its illegal to repair a seal that has been run.
     *
     * @param key - corresponding to a seal
     * @throws java.lang.IllegalArgumentException if you have passed in a key that is not known by the sealed runner
     * @throws java.lang.IllegalStateException if the seal runner has already run.
     */
    public void repairSeal(final String key) {
        if (key != null && seals.containsKey(key)) {
            if (hasRun())
            {
                throw new IllegalStateException("The seal has already been run");
            }
            seals.put(key, false);
        } else {
            throw new IllegalArgumentException("The key you have provided does not conform to a valid seal!");
        }
    }

    /**
     * @return true all the seals have been broken and the callback has been run
     */
    public boolean hasRun()
    {
        return hasRun.get();
    }

    /**
     * This will check if all the seals are unlocked, if they are, then it will run the runnable
     */
    private void checkSeals() {
        if (hasRun.get()) {
            return;
        }
        boolean allSealsBroken = true;
        for (Boolean b : seals.values()) {
            allSealsBroken = allSealsBroken && b;
        }
        if (allSealsBroken && hasRun.compareAndSet(false, true)) {
            runnable.run();
        }
    }
}
