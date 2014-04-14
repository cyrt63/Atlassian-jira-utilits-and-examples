package com.atlassian.pocketknife.api.customfields.service;

import com.atlassian.jira.issue.customfields.CustomFieldType;
import com.atlassian.jira.issue.fields.CustomField;

import java.util.List;

/**
 * Service for encapsulating custom field logic, especially the rather complex JIRA logic behind creating and working with these things.
 */
public interface CustomFieldService
{
    /**
     * Create a new custom field in JIRA with the given configuration
     */
    CustomField createCustomField(CustomFieldMetadata fieldMetadata) throws CustomFieldException;

    /**
     * @return the {@link com.atlassian.jira.issue.fields.CustomField} instance for the given ID, or null if it doesn't exist
     */
    CustomField getCustomField(Long id) throws CustomFieldException;

    /**
     * @return the {@link com.atlassian.jira.issue.fields.CustomField} instance for the given String ID, or null if it doesn't exist
     */
    CustomField getCustomField(String id);

    /**
     * Return all custom field instances which are of type {@link T} (but not sub-classes).
     *
     * @param type the type to retrieve
     * @param <T>  the CustomFieldType to check for. Equality checking is done on the custom field's type.
     * @return the list of custom fields; never null
     */
    <T extends CustomFieldType> List<CustomField> getCustomFields(Class<T> type) throws CustomFieldException;

    /**
     * Return all custom field instances which are of type {@link T}, or a subclass of {@link T}.
     *
     * @param type   the type to retrieve
     * @param strict set to <code>true</code> to do a strict equality check on the specified type.
     * @param <T>    the CustomFieldType to check for. Equality checking is done on the custom field's type.
     * @return the list of custom fields; never null
     */
    <T extends CustomFieldType> List<CustomField> getCustomFields(Class<T> type, boolean strict) throws CustomFieldException;

    /**
     * Removes the custom field and all associated data. Note that this requires the custom field to be fully recognisable
     * in JIRA's current state (i.e. the CustomFieldType for this object must still be available).
     *
     * @param customField the custom field object
     * @see com.atlassian.jira.issue.CustomFieldManager#removeCustomField(com.atlassian.jira.issue.fields.CustomField)
     */
    void removeCustomField(CustomField customField) throws CustomFieldException;

    /**
     * Restore the field context by definition in field metadata if any changes and lock the field down, or does nothing if it's already locked
     *
     * @param fieldMetadata the field metadata
     * @param customField   the custom field
     * @return true if success, false otherwise
     */
    boolean restoreFieldContextAndLock(CustomFieldMetadata fieldMetadata, CustomField customField);
}
