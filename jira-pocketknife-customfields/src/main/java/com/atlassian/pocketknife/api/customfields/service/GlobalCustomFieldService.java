package com.atlassian.pocketknife.api.customfields.service;

import com.atlassian.jira.issue.fields.CustomField;

/**
 * Service for encapsulating custom field logic for creating a single global custom field that will be used by plugins.
 * 
 */
public interface GlobalCustomFieldService
{
    /**
     * This will get the global custom field that is specified in the meta data. If the custom field does not exist, then it will be created.
     * 
     */
    CustomField getGlobalCustomField(CustomFieldMetadata fieldMetadata);
}
