package com.atlassian.pocketknife.internal.customfields.service;

import com.atlassian.fugue.Option;
import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.config.ConstantsManager;
import com.atlassian.jira.config.properties.ApplicationProperties;
import com.atlassian.jira.exception.RemoveException;
import com.atlassian.jira.issue.CustomFieldManager;
import com.atlassian.jira.issue.context.IssueContextImpl;
import com.atlassian.jira.issue.context.JiraContextNode;
import com.atlassian.jira.issue.context.manager.JiraContextTreeManager;
import com.atlassian.jira.issue.customfields.CustomFieldSearcher;
import com.atlassian.jira.issue.customfields.CustomFieldType;
import com.atlassian.jira.issue.customfields.CustomFieldUtils;
import com.atlassian.jira.issue.customfields.option.Options;
import com.atlassian.jira.issue.fields.CustomField;
import com.atlassian.jira.issue.fields.config.FieldConfig;
import com.atlassian.jira.issue.fields.layout.field.EditableDefaultFieldLayout;
import com.atlassian.jira.issue.fields.layout.field.EditableFieldLayout;
import com.atlassian.jira.issue.fields.layout.field.FieldLayoutItem;
import com.atlassian.jira.issue.fields.layout.field.FieldLayoutManager;
import com.atlassian.jira.issue.issuetype.IssueType;
import com.atlassian.jira.util.I18nHelper;
import com.atlassian.pocketknife.api.customfields.service.CustomFieldException;
import com.atlassian.pocketknife.api.customfields.service.CustomFieldMetadata;
import com.atlassian.pocketknife.api.customfields.service.CustomFieldService;
import com.atlassian.pocketknife.api.customfields.service.FieldLockingService;
import com.atlassian.pocketknife.api.customfields.service.IssueTypeProvider;
import com.google.common.base.Function;
import com.google.common.collect.Lists;
import org.ofbiz.core.entity.GenericValue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.apache.commons.lang.Validate.notNull;

@Component
public class CustomFieldServiceImpl implements CustomFieldService {

    private static final Logger logger = LoggerFactory.getLogger(CustomFieldServiceImpl.class);

    /**
     * {@link com.atlassian.jira.config.ConstantsManager} does not define the global issue type, so let do it
     */
    private static final String GLOBAL_ISSUETYPE = "-1";

    @Autowired
    private CustomFieldManager customFieldManager;

    @Autowired
    private ConstantsManager constantsManager;

    @Autowired
    private I18nHelper.BeanFactory i18nFactoryService;

    @Autowired
    private ApplicationProperties applicationProperties;

    @Autowired
    private FieldLayoutManager fieldLayoutManager;

    @Autowired
    private FieldLockingService fieldLockingService;

    @Override
    public CustomField createCustomField(CustomFieldMetadata fieldMetadata) {
        logger.info("Create custom field {}", fieldMetadata);

        CustomField customField = null;
        try {
            I18nHelper i18n = i18nFactoryService.getInstance(applicationProperties.getDefaultLocale());
            String name = i18n.getText(fieldMetadata.getFieldName());
            String desc = i18n.getText(fieldMetadata.getFieldDescription());
            Option<CustomFieldType> type = Option.option(customFieldManager.getCustomFieldType(fieldMetadata.getFieldType()));
            Option<CustomFieldSearcher> searcher = Option.option(customFieldManager.getCustomFieldSearcher(fieldMetadata.getFieldSearcher()));
            Set<String> issueTypeIds = getIssueTypeIds(fieldMetadata.getIssueTypeProvider());
            List<GenericValue> genericIssueTypeValues = convert(issueTypeIds);

            // we use the global context here (all projects), since we're creating a field programmatically and don't really want anything project-specific
            List<JiraContextNode> contexts = CustomFieldUtils
                    .buildJiraIssueContexts(true, null, null, getContextTreeManager());

            customField = customFieldManager.createCustomField(name, desc, type.get(), searcher.getOrNull(), contexts, genericIssueTypeValues);

            // add options
            if (!fieldMetadata.getOptionNames().isEmpty()) {
                for (String issueTypeId : issueTypeIds) {
                    com.atlassian.fugue.Option<FieldConfig> fieldConfig = CustomFieldUtil.getRelevantConfig(customField, new IssueContextImpl(null, issueTypeId));
                    addOptionsToCustomField(customField, fieldConfig, fieldMetadata.getOptionNames(), fieldMetadata.getDefaultOptionName());
                }
                setOptionsOrderFromMetadata(customField, fieldMetadata, issueTypeIds);
            }

            if (fieldMetadata.isRequireField()) {
                makeFieldRequired(customField);
            }

            if (fieldMetadata.isLockField()) {
                fieldLockingService.lockField(customField, fieldMetadata.getLockFieldDescription());
            }

            return customField;
        } catch (Exception ex) {
            logger.error("Failed to create custom field {} ", fieldMetadata, ex);

            if (customField != null) {
                try {
                    customFieldManager.removeCustomField(customField);
                } catch (Exception ignore) {
                    logger.warn("Exception when attempting to cleanup custom field {} ", customField, ignore);
                }
            }

            throw new CustomFieldException("Exception while trying to create a customField with the following parameters", ex);
        }
    }

    /**
     * Have the provider to retrieve or create the associated issue types
     *
     * @param issueTypeProvider
     * @return
     */
    private Set<String> getIssueTypeIds(Option<IssueTypeProvider> issueTypeProvider) {
        Set<String> issueTypeIds = new HashSet<String>();
        if (issueTypeProvider.isDefined()) {
            for (IssueType issueType : issueTypeProvider.get().getIssueTypes()) {
                issueTypeIds.add(issueType.getId());
            }
        }

        // list of 1 element of special IssueType ID indicates a Global IssueType context
        if (issueTypeIds.isEmpty()) {
            issueTypeIds.add(GLOBAL_ISSUETYPE);
        }

        return issueTypeIds;
    }

    /**
     * Convert IssueType IDs into GenericValue's
     *
     * @param issueTypeIds
     * @return
     */
    private List<GenericValue> convert(Set<String> issueTypeIds) {
        Set<String> ids = new HashSet<String>();
        ids.addAll(issueTypeIds);

        List<GenericValue> values = CustomFieldUtils.buildIssueTypes(constantsManager, ids.toArray(new String[ids.size()]));
        return values;
    }

    /**
     * Dynamically access JIRA internal component
     *
     * @return
     */
    private JiraContextTreeManager getContextTreeManager() {
        return ComponentAccessor.getComponent(JiraContextTreeManager.class);
    }

    private void addOptionsToCustomField(CustomField customField, com.atlassian.fugue.Option<FieldConfig> fieldConfigOption, List<String> optionNames, String defaultOptionName) {
        // option names will be translated into default locale
        I18nHelper i18n = i18nFactoryService.getInstance(applicationProperties.getDefaultLocale());

        com.atlassian.fugue.Option<Options> options = CustomFieldUtil.getCustomFieldOptions(customField, fieldConfigOption);
        if (options.isEmpty()) {
            return;
        }

        // since we got options back, we know the field config is good
        final FieldConfig fieldConfig = fieldConfigOption.get();

        // Add options first, we set their order after the fact in #setOptionsOrderFromMetadata(CustomField customField, CustomFieldMetadata fieldMetadata)
        for (String optionName : optionNames) {
            String translatedOption = i18n.getText(optionName);
            com.atlassian.jira.issue.customfields.option.Option option = options.get().addOption(null, translatedOption);
            if (optionName.equals(defaultOptionName)) {
                // set default option
                customField.getCustomFieldType().setDefaultValue(fieldConfig, option);
            }
        }
    }

    private void setOptionsOrderFromMetadata(CustomField customField, CustomFieldMetadata fieldMetadata, Set<String> issueTypeIds) {
        List<String> optionValues = getOptionValuesFromNames(fieldMetadata.getOptionNames());

        for (String issueTypeId : issueTypeIds) {
            com.atlassian.fugue.Option<FieldConfig> fieldConfig = CustomFieldUtil.getRelevantConfig(customField, new IssueContextImpl(null, issueTypeId));

            if (fieldConfig.isEmpty()) {
                // GHS-6912 - if the customer has tampered with the Custom Field Contexts of the Epic Status field, this
                // lookup will return null. We want to fail silently here so that they can continue to upgrade/use GH.
                logger.warn("Could not find a Custom Field Configuration for field {}, all projects and issue type {} -- therefore cannot find the Options for the field.",
                        customField.getId(), issueTypeId);
                continue;
            }
            com.atlassian.fugue.Option<Options> optionsResult = CustomFieldUtil.getCustomFieldOptions(customField, fieldConfig);
            if (optionsResult.isEmpty()) {
                continue;
            }
            Options options = optionsResult.get();

            Map<Integer, com.atlassian.jira.issue.customfields.option.Option> optionToPositionMap = new HashMap<Integer, com.atlassian.jira.issue.customfields.option.Option>(options.size());
            List<com.atlassian.jira.issue.customfields.option.Option> rootOptions = options.getRootOptions();

            if (optionValues.size() != options.size()) {
                throw new CustomFieldException("When setting custom field options order, available options must match initially created options");
            }

            for (com.atlassian.jira.issue.customfields.option.Option option : rootOptions) {
                if (!optionValues.contains(option.getValue())) {
                    throw new CustomFieldException("When setting custom field options order, available options must match initially created options");
                }
                optionToPositionMap.put(optionValues.indexOf(option.getValue()), option);
            }

            options.moveOptionToPosition(optionToPositionMap);
        }
    }

    private List<String> getOptionValuesFromNames(final List<String> optionNames) {
        final I18nHelper i18n = i18nFactoryService.getInstance(applicationProperties.getDefaultLocale());

        return Lists.transform(optionNames, new Function<String, String>() {
            @Override
            public String apply(@Nullable final String optionName) {
                return i18n.getText(optionName);
            }
        });
    }

    private void makeFieldRequired(final CustomField field) {
        List<EditableFieldLayout> layouts = fieldLayoutManager.getEditableFieldLayouts();
        for (EditableFieldLayout layout : layouts) {
            FieldLayoutItem fieldLayoutItem = layout.getFieldLayoutItem(field.getId());
            if (fieldLayoutItem != null) {
                layout.makeRequired(fieldLayoutItem);
                if (layout.isDefault()) {
                    fieldLayoutManager.storeEditableDefaultFieldLayout((EditableDefaultFieldLayout) layout);
                } else {
                    fieldLayoutManager.storeEditableFieldLayout(layout);
                }
            }
        }
    }

    @Override
    public CustomField getCustomField(Long id) {
        logger.info("Retrieve custom field {}", id);
        return customFieldManager.getCustomFieldObject(id);
    }

    @Override
    public CustomField getCustomField(String id) {
        logger.info("Retrieve custom field {}", id);
        return customFieldManager.getCustomFieldObject(id);
    }

    @Override
    public void removeCustomField(CustomField customField) {
        logger.info("Remove custom field {}", customField);

        try {
            customFieldManager.removeCustomField(customField);
        } catch (RemoveException e) {
            logger.info("Remove custom field {}", customField, e);
            throw new CustomFieldException(e);
        }
    }

    @Override
    public <T extends CustomFieldType> List<CustomField> getCustomFields(Class<T> type) {
        logger.info("Retrieve custom field {}", type);

        return getCustomFields(type, true);
    }

    @Override
    public <T extends CustomFieldType> List<CustomField> getCustomFields(Class<T> type, boolean strict) {
        logger.info("Retrieve custom field {}", type);

        notNull(type, "The Class for the CustomFieldType cannot be null");

        List<CustomField> fields = new ArrayList<CustomField>();

        // JIRA provides no lookup by CFT
        for (CustomField customField : customFieldManager.getCustomFieldObjects()) {
            if (strict && customField.getCustomFieldType().getClass().equals(type)) {
                fields.add(customField);
            } else if (!strict && type.isAssignableFrom(customField.getCustomFieldType().getClass())) {
                fields.add(customField);
            }
        }

        return fields;
    }

}
