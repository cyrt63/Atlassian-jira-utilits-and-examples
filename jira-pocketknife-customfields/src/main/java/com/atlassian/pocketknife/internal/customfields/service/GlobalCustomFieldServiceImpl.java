package com.atlassian.pocketknife.internal.customfields.service;

import java.util.List;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import com.atlassian.pocketknife.api.customfields.service.CustomFieldException;
import com.atlassian.pocketknife.api.customfields.service.CustomFieldMetadata;
import com.atlassian.pocketknife.api.customfields.service.GlobalCustomFieldService;
import org.ofbiz.core.entity.GenericValue;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.config.ConstantsManager;
import com.atlassian.jira.config.properties.ApplicationProperties;
import com.atlassian.jira.issue.CustomFieldManager;
import com.atlassian.jira.issue.context.JiraContextNode;
import com.atlassian.jira.issue.context.manager.JiraContextTreeManager;
import com.atlassian.jira.issue.customfields.CustomFieldSearcher;
import com.atlassian.jira.issue.customfields.CustomFieldType;
import com.atlassian.jira.issue.customfields.CustomFieldUtils;
import com.atlassian.jira.issue.fields.CustomField;
import com.atlassian.jira.util.I18nHelper;
import com.atlassian.pocketknife.api.propertysets.service.PropertySetService;

@Service
public class GlobalCustomFieldServiceImpl implements GlobalCustomFieldService
{
    @Autowired
    private CustomFieldManager customFieldManager;

    @Autowired
    private ConstantsManager constantsManager;

    @Autowired
    private I18nHelper.BeanFactory i18nFactoryService;

    @Autowired
    private ApplicationProperties applicationProperties;

    @Autowired
    private PropertySetService propertySetService;

    private static final String STORAGE_KEY = "pk.customfield";

    private static final long STORAGE_ENTITY_ID = 1l;

    private final ReadWriteLock fieldIdCacheLock = new ReentrantReadWriteLock();

    public synchronized CustomField getGlobalCustomField(CustomFieldMetadata fieldMetadata) {
        // read/write lock required due to quirky cache invalidation
        fieldIdCacheLock.readLock().lock();
        try {
            final CustomField customField;
            final Long id = propertySetService.getLong(STORAGE_KEY, STORAGE_ENTITY_ID, fieldMetadata.getFieldType());
            customField = id != null ? getCustomField(id) : null;
            if (customField != null)
            {
                return customField;
            }
        } finally {
            fieldIdCacheLock.readLock().unlock();
        }

        // create a new one
        fieldIdCacheLock.writeLock().lock();
        try {
            CustomField customField;
            final Long id = propertySetService.getLong(STORAGE_KEY, STORAGE_ENTITY_ID, fieldMetadata.getFieldType());
            if (id != null)
            {
                customField = getCustomField(id);
                if (customField != null) return customField;
            }

            customField = createGlobalCustomField(fieldMetadata);
            propertySetService.setLong(STORAGE_KEY, STORAGE_ENTITY_ID, fieldMetadata.getFieldType(), customField.getIdAsLong());
            return customField;
        } finally {
            fieldIdCacheLock.writeLock().unlock();
        }

    }

    private CustomField createGlobalCustomField(CustomFieldMetadata fieldMetadata)
    {
        I18nHelper i18n = i18nFactoryService.getInstance(applicationProperties.getDefaultLocale());
        String name = i18n.getText(fieldMetadata.getFieldName());
        String desc = i18n.getText(fieldMetadata.getFieldDescription());
        CustomFieldType type = customFieldManager.getCustomFieldType(fieldMetadata.getFieldType());
        CustomFieldSearcher searcher = customFieldManager.getCustomFieldSearcher(fieldMetadata.getFieldSearcher());
        List<GenericValue> issueTypes = CustomFieldUtils.buildIssueTypes(constantsManager, fieldMetadata.getIssueTypes());

        // we use the global context here (all projects), since we're creating a field programmatically and don't really want anything
        // project-specific
        List<JiraContextNode> contexts = CustomFieldUtils.buildJiraIssueContexts(true, null, null, getContextTreeManager());

        try
        {
            return customFieldManager.createCustomField(name, desc, type, searcher, contexts, issueTypes);
        }
        catch (Exception e)
        {
            throw new CustomFieldException("Exception while trying to create a customField with the following parameters: " + fieldMetadata, e);
        }
    }

    JiraContextTreeManager getContextTreeManager()
    {
        return ComponentAccessor.getComponent(JiraContextTreeManager.class);
    }

    private CustomField getCustomField(Long id)
    {
        return customFieldManager.getCustomFieldObject(id);
    }
}
