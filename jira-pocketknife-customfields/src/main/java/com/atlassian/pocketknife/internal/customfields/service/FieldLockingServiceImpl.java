package com.atlassian.pocketknife.internal.customfields.service;

import com.atlassian.jira.bc.ServiceOutcome;
import com.atlassian.jira.config.managedconfiguration.ConfigurationItemAccessLevel;
import com.atlassian.jira.config.managedconfiguration.ManagedConfigurationItem;
import com.atlassian.jira.config.managedconfiguration.ManagedConfigurationItemService;
import com.atlassian.jira.issue.fields.CustomField;
import com.atlassian.jira.util.ErrorCollection;
import com.atlassian.jira.util.SimpleErrorCollection;
import com.atlassian.pocketknife.api.customfields.service.FieldLockingService;
import com.atlassian.pocketknife.spi.info.PocketKnifePluginInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Allows locking down field configuration
 */
@Component
public class FieldLockingServiceImpl implements FieldLockingService {
    private final Logger logger = LoggerFactory.getLogger(FieldLockingServiceImpl.class);

    @Autowired
    PocketKnifePluginInfo pocketKnifeIntegration;

    @Autowired
    private ManagedConfigurationItemService managedConfigurationItemService;

    @Override
    public ErrorCollection lockField(final CustomField field, String descI18nKey) {
        logger.info("Configuration locked for field {}", field.getFieldName());

        final ManagedConfigurationItem managedConfigurationItem = managedConfigurationItemService.getManagedCustomField(field);
        if (isLocked(managedConfigurationItem)) {
            return new SimpleErrorCollection();
        }

        String pluginKey = pocketKnifeIntegration.getPluginKey();

        final ManagedConfigurationItem updatedManagedConfigurationItem = managedConfigurationItem.newBuilder()
                .setManaged(true)
                .setConfigurationItemAccessLevel(ConfigurationItemAccessLevel.LOCKED)
                .setSource(pluginKey + ":field-locking-service")
                .setDescriptionI18nKey(descI18nKey)
                .build();

        ServiceOutcome<ManagedConfigurationItem> resultOutcome = managedConfigurationItemService.updateManagedConfigurationItem(updatedManagedConfigurationItem);
        logger.info("Configuration locked for field {} with {} ", field.getFieldName(), resultOutcome.isValid());

        return resultOutcome.getErrorCollection();
    }

    @Override
    public boolean isFieldLocked(CustomField field) {
        return isLocked(managedConfigurationItemService.getManagedCustomField(field));
    }

    private boolean isLocked(ManagedConfigurationItem managedConfigurationItem) {
        return managedConfigurationItem.isManaged() && managedConfigurationItem.getConfigurationItemAccessLevel() == ConfigurationItemAccessLevel.LOCKED;
    }

}
