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
     */
    CustomField getGlobalCustomField(CustomFieldMetadata fieldMetadata) throws CustomFieldException;

}
