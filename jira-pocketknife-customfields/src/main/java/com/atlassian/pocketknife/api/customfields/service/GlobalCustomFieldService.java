package com.atlassian.pocketknife.api.customfields.service;

import com.atlassian.jira.issue.fields.CustomField;

/**
 * Service to work with global custom field
 */
public interface GlobalCustomFieldService
{

    /**
     * Get the global custom field that is specified in the meta data. If the custom field does not exist, then it will be created.
     *
     * @param fieldMetadata the field meta data
     * @return the custom field
     * @throws CustomFieldException if failed to create the field
     */
    CustomField getGlobalCustomField(CustomFieldMetadata fieldMetadata) throws CustomFieldException;

    /**
     * Restore the field context by definition in field metadata if any changes and lock the field down, or does nothing if it's already locked
     *
     * @param fieldMetadata the field metadata
     * @param customField   the custom field
     * @return true if success, false otherwise
     */
    boolean restoreFieldContextAndLock(CustomFieldMetadata fieldMetadata, CustomField customField) throws CustomFieldException;

}
