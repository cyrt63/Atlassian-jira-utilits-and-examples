package com.atlassian.pocketknife.api.customfields.service;

import com.atlassian.jira.issue.fields.CustomField;
import com.atlassian.jira.util.ErrorCollection;

/**
 * The ability to lock a custom field configuration
 */
public interface FieldLockingService
{
    ErrorCollection lockField(CustomField field, String descI18nKey);

    boolean isFieldAlreadyLocked(final CustomField field);
}
