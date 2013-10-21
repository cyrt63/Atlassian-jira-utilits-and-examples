package com.atlassian.pocketknife.spi.ao;

import com.atlassian.activeobjects.external.ActiveObjects;
import com.atlassian.activeobjects.external.ActiveObjectsUpgradeTask;
import com.atlassian.activeobjects.external.ModelVersion;
import com.atlassian.pocketknife.internal.upgrade.UpgradeVersionServiceImpl;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * A base class for AO upgrade tasks to make them that much easier and better documented
 */
public abstract class AbstractActiveObjectsUpgradeTask implements ActiveObjectsUpgradeTask
{
    @Autowired
    private UpgradeVersionServiceImpl upgradeVersionService;

    // make sure we're fixing the logger for the whole upgrade package, since sub-components of the UT live in other classes
    protected Logger log = Logger.getLogger(getClass().getPackage().getName());

    /**
     * @return the database model version of this upgrade task
     */
    public abstract int modelVersion();

    /**
     * @return a short description as to why you are running this AO upgrade task
     */
    public abstract String getShortDescription();


    @Override
    public ModelVersion getModelVersion()
    {
        // why AO would you choose string when you mean int?
        return ModelVersion.valueOf(String.valueOf(modelVersion()));
    }

    /**
     * This is the template method you are expected to implement
     *
     * @param currentVersion the current version you are coming from
     *
     * @param ao the fresh clean AO object that needs to be migrated to
     */
    abstract protected void doUpgrade(final ModelVersion currentVersion, final ActiveObjects ao);

    @Override
    public void upgrade(ModelVersion currentVersion, ActiveObjects ao)
    {
        // keep current log level
        Level level = log.getLevel();

        // make sure everything is logged as info
        log.setLevel(Level.INFO);
        try
        {
            String buildNumber = makeAoBuildNumber();

            upgradeVersionService.recordUpgradeTaskStarted(buildNumber);

            logUpgradeTaskStart();

            long then = System.currentTimeMillis();

            // do things
            doUpgrade(currentVersion,ao);

            long timeTaken = System.currentTimeMillis() - then;

            upgradeVersionService.recordUpgradeTaskEnded(buildNumber, timeTaken);

            logUpgradeTaskEnd();
        }
        finally
        {
            // restore previous log level
            log.setLevel(level);
        }
    }



    private String makeAoBuildNumber()
    {
        return "AO-" + this.modelVersion();
    }

    private void logUpgradeTaskStart()
    {
        log.info("=========================================");
        log.info("Starting Active Objects upgrade task (modelVersion=" + this.modelVersion() + ") : \n\t" + this.getShortDescription());
    }

    private void logUpgradeTaskEnd()
    {
        log.info("Active Objects upgrade task finished (modelVersion=" + this.modelVersion() + ")");
        log.info("=========================================");
    }

}
