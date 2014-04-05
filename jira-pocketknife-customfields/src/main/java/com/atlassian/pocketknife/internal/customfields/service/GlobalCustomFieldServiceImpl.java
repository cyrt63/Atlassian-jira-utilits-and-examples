package com.atlassian.pocketknife.internal.customfields.service;

import com.atlassian.jira.issue.fields.CustomField;
import com.atlassian.jira.ofbiz.OfBizDelegator;
import com.atlassian.pocketknife.api.customfields.service.CustomFieldException;
import com.atlassian.pocketknife.api.customfields.service.CustomFieldMetadata;
import com.atlassian.pocketknife.api.customfields.service.CustomFieldService;
import com.atlassian.pocketknife.api.customfields.service.GlobalCustomFieldService;
import com.atlassian.pocketknife.api.persistence.GlobalPropertyDao;
import org.ofbiz.core.entity.GenericValue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Implementation for global custom field service
 */
@Component
public class GlobalCustomFieldServiceImpl implements GlobalCustomFieldService
{
    private final Logger logger = LoggerFactory.getLogger(GlobalCustomFieldServiceImpl.class);

    @Autowired
    private GlobalPropertyDao globalPropertyDao;

    @Autowired
    private CustomFieldService customFieldService;

    @Autowired
    private OfBizDelegator ofBizDelegator;

    /**
     * Implementation detail to support lazily creation of custom field
     * First retrieve the value, and if not existed yet, then will block the thread and create the field
     * Delegate custom field creation to {@link com.atlassian.pocketknife.api.customfields.service.CustomFieldService}
     */
    @Override
    public CustomField getGlobalCustomField(CustomFieldMetadata fieldMetadata) throws CustomFieldException
    {
        logger.info("Retrieve or create custom field {}", fieldMetadata);

        try
        {
            CustomField field = getDefaultFieldOrNull(fieldMetadata);
            if (field == null)
            {
                synchronized (this)
                {
                    field = getDefaultFieldOrNull(fieldMetadata);
                    if (field == null)
                    {
                        field = createGlobalCustomField(fieldMetadata);
                    }
                }
            }
            return field;
        }
        catch (Exception e)
        {
            logger.error("Failed to get or create custom field", e);
            throw new CustomFieldException(e);
        }
    }

    /**
     * Internal implementation to lazily create global custom field.
     * The outer public method getGlobalCustomField is aimed to lock/prevent race condition of creating the field in multi-thread environment
     * which delegates the creation task to this method
     *
     * @param fieldMetadata
     * @return
     */
    private CustomField getDefaultFieldOrNull(CustomFieldMetadata fieldMetadata)
    {
        Long id = globalPropertyDao.getLongProperty(fieldMetadata.getFieldType());
        if (id != null)
        {
            CustomField field = customFieldService.getCustomField(id);
            if (field != null)
            {
                return field;
            }
            // check in DB if the custom field really doesn't exist
            if (verifyCustomFieldExistsInDB(id))
            {
                throw new RuntimeException(String.format("Custom field '%s' with ID '%d' exists in DB, but was not returned by the custom field service.", fieldMetadata.getFieldName(), id));
            }
        }
        return null;
    }

    /**
     * A wrapped implementation to support creation of custom field
     * Delegate custom field creation to {@link com.atlassian.pocketknife.api.customfields.service.CustomFieldService}
     */
    private CustomField createGlobalCustomField(CustomFieldMetadata fieldMetadata) throws CustomFieldException
    {
        CustomField field = customFieldService.createCustomField(fieldMetadata);

        globalPropertyDao.setLongProperty(fieldMetadata.getFieldType(), field.getIdAsLong());

        return field;
    }

    private boolean verifyCustomFieldExistsInDB(final Long customFieldId)
    {
        GenericValue customField = ofBizDelegator.findById("CustomField", customFieldId);
        return customField != null;
    }

}
