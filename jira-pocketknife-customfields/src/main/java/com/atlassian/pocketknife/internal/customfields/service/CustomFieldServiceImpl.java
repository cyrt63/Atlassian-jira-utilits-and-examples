package com.atlassian.pocketknife.internal.customfields.service;

import com.atlassian.fugue.Option;
import com.atlassian.jira.bc.ServiceOutcome;
import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.config.ConstantsManager;
import com.atlassian.jira.config.managedconfiguration.ConfigurationItemAccessLevel;
import com.atlassian.jira.config.managedconfiguration.ManagedConfigurationItem;
import com.atlassian.jira.config.managedconfiguration.ManagedConfigurationItemService;
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
import com.atlassian.jira.issue.fields.FieldManager;
import com.atlassian.jira.issue.fields.config.FieldConfig;
import com.atlassian.jira.issue.fields.config.FieldConfigScheme;
import com.atlassian.jira.issue.fields.config.manager.FieldConfigSchemeManager;
import com.atlassian.jira.issue.fields.layout.field.EditableDefaultFieldLayout;
import com.atlassian.jira.issue.fields.layout.field.EditableFieldLayout;
import com.atlassian.jira.issue.fields.layout.field.FieldLayoutItem;
import com.atlassian.jira.issue.fields.layout.field.FieldLayoutManager;
import com.atlassian.jira.issue.fields.screen.FieldScreen;
import com.atlassian.jira.issue.fields.screen.FieldScreenLayoutItem;
import com.atlassian.jira.issue.fields.screen.FieldScreenManager;
import com.atlassian.jira.issue.fields.screen.FieldScreenScheme;
import com.atlassian.jira.issue.fields.screen.FieldScreenTab;
import com.atlassian.jira.issue.fields.screen.issuetype.IssueTypeScreenScheme;
import com.atlassian.jira.issue.fields.screen.issuetype.IssueTypeScreenSchemeEntity;
import com.atlassian.jira.issue.fields.screen.issuetype.IssueTypeScreenSchemeManager;
import com.atlassian.jira.issue.issuetype.IssueType;
import com.atlassian.jira.issue.operation.IssueOperations;
import com.atlassian.jira.issue.operation.ScreenableIssueOperation;
import com.atlassian.jira.util.I18nHelper;
import com.atlassian.pocketknife.api.customfields.service.CustomFieldException;
import com.atlassian.pocketknife.api.customfields.service.CustomFieldMetadata;
import com.atlassian.pocketknife.api.customfields.service.CustomFieldService;
import com.atlassian.pocketknife.api.customfields.service.IssueTypeProvider;
import com.atlassian.pocketknife.spi.info.PocketKnifePluginInfo;
import com.google.common.base.Function;
import com.google.common.collect.Lists;
import org.apache.commons.lang.StringUtils;
import org.ofbiz.core.entity.GenericValue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.apache.commons.lang.Validate.notNull;

@Component
public class CustomFieldServiceImpl implements CustomFieldService
{

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
    private ManagedConfigurationItemService managedConfigurationItemService;

    @Autowired
    PocketKnifePluginInfo pocketKnifeIntegration;

    @Autowired
    private IssueTypeScreenSchemeManager issueTypeScreenSchemeManager;

    @Autowired
    private FieldScreenManager fieldScreenManager;

    @Autowired
    private FieldManager fieldManager;

    @Autowired
    private FieldConfigSchemeManager fieldConfigSchemeManager;

    @Override
    public CustomField createCustomField(CustomFieldMetadata fieldMetadata)
    {
        CustomField customField = null;
        try
        {
            I18nHelper i18n = i18nFactoryService.getInstance(applicationProperties.getDefaultLocale());
            String name = i18n.getText(fieldMetadata.getFieldName());
            String desc = i18n.getText(fieldMetadata.getFieldDescription());
            Option<CustomFieldType> type = Option.option(customFieldManager.getCustomFieldType(fieldMetadata.getFieldType()));
            Option<CustomFieldSearcher> searcher = Option.option(customFieldManager.getCustomFieldSearcher(fieldMetadata.getFieldSearcher()));
            Set<String> issueTypeIds = getIssueTypeIds(fieldMetadata.getIssueTypeProvider());
            List<GenericValue> genericIssueTypeValues = convert(issueTypeIds);

            // we use the global context here (all projects), since we're creating a field programmatically and don't really want anything project-specific
            List<JiraContextNode> contexts = CustomFieldUtils.buildJiraIssueContexts(true, null, null, getContextTreeManager());

            customField = customFieldManager.createCustomField(name, desc, type.get(), searcher.getOrNull(), contexts, genericIssueTypeValues);

            // add options
            if (!fieldMetadata.getOptionNames().isEmpty())
            {
                for (String issueTypeId : issueTypeIds)
                {
                    com.atlassian.fugue.Option<FieldConfig> fieldConfig = CustomFieldUtil.getRelevantConfig(customField, new IssueContextImpl(null, issueTypeId));
                    addOptionsToCustomField(customField, fieldConfig, fieldMetadata.getOptionNames(), fieldMetadata.getDefaultOptionName());
                }
                setOptionsOrderFromMetadata(customField, fieldMetadata, issueTypeIds);
            }

            if (fieldMetadata.isRequireField())
            {
                makeFieldRequired(customField);
            }

            if (fieldMetadata.isLockField())
            {
                lockField(customField, fieldMetadata.getFieldDescription());
            }

            return customField;
        }
        catch (Exception ex)
        {
            logger.error("Failed to create custom field {} ", fieldMetadata, ex);

            if (customField != null)
            {
                try
                {
                    customFieldManager.removeCustomField(customField);
                }
                catch (Exception ignore)
                {
                    logger.warn("Exception when attempting to cleanup custom field {} ", customField, ignore);
                }
            }

            throw new CustomFieldException("Exception while trying to create a customField with the following parameters", ex);
        }
    }

    @Override
    public CustomField getCustomField(Long id)
    {
        return customFieldManager.getCustomFieldObject(id);
    }

    @Override
    public CustomField getCustomField(String id)
    {
        return customFieldManager.getCustomFieldObject(id);
    }

    @Override
    public void removeCustomField(CustomField customField)
    {
        try
        {
            customFieldManager.removeCustomField(customField);
        }
        catch (RemoveException e)
        {
            logger.info("Remove custom field {}", customField, e);
            throw new CustomFieldException(e);
        }
    }

    @Override
    public <T extends CustomFieldType> List<CustomField> getCustomFields(Class<T> type)
    {
        return getCustomFields(type, true);
    }

    @Override
    public <T extends CustomFieldType> List<CustomField> getCustomFields(Class<T> type, boolean strict)
    {
        notNull(type, "The Class for the CustomFieldType cannot be null");

        List<CustomField> fields = new ArrayList<CustomField>();

        // JIRA provides no lookup by CFT
        for (CustomField customField : customFieldManager.getCustomFieldObjects())
        {
            if (strict && customField.getCustomFieldType().getClass().equals(type))
            {
                fields.add(customField);
            }
            else if (!strict && type.isAssignableFrom(customField.getCustomFieldType().getClass()))
            {
                fields.add(customField);
            }
        }

        return fields;
    }

    @Override
    public boolean restoreFieldContextAndLock(CustomFieldMetadata fieldMetadata, CustomField customField)
    {
        boolean result = true;

        try
        {
            if (fieldMetadata.isLockField() && !isFieldAlreadyLocked(customField))
            {
                if (restoreFieldContext(fieldMetadata, customField))
                {
                    result = lockField(customField, fieldMetadata.getFieldDescription());
                    if (result)
                    {
                        fieldManager.refresh();
                        customFieldManager.refreshConfigurationSchemes(customField.getIdAsLong());
                        customFieldManager.refresh();
                    }
                }
            }
        }
        catch (RuntimeException e)
        {
            logger.error("Failed to lock custom field {} with metadata {}", new Object[]{customField, fieldMetadata}, e);
            result = false;
        }

        return result;
    }

    /**
     * Have the provider to retrieve or create the associated issue types
     *
     * @param issueTypeProvider the issue type provider
     * @return set of issue type IDs
     */
    private Set<String> getIssueTypeIds(Option<IssueTypeProvider> issueTypeProvider)
    {
        Set<String> issueTypeIds = new HashSet<String>();
        if (issueTypeProvider.isDefined())
        {
            for (IssueType issueType : issueTypeProvider.get().getIssueTypes())
            {
                issueTypeIds.add(issueType.getId());
            }
        }

        return issueTypeIds;
    }

    /**
     * Convert IssueType IDs into GenericValue's
     *
     * @param issueTypeIds set of issue type IDs
     * @return Generic Values of the IDs
     */
    private List<GenericValue> convert(Set<String> issueTypeIds)
    {
        Set<String> ids = new HashSet<String>();
        ids.addAll(issueTypeIds);
        // list of 1 element of special IssueType ID indicates a Global IssueType context
        if (ids.isEmpty())
        {
            ids.add(GLOBAL_ISSUETYPE);
        }

        return CustomFieldUtils.buildIssueTypes(constantsManager, ids.toArray(new String[ids.size()]));
    }

    /**
     * Dynamically access JIRA internal component
     *
     * @return the JIRA context tree manager
     */
    private JiraContextTreeManager getContextTreeManager()
    {
        return ComponentAccessor.getComponent(JiraContextTreeManager.class);
    }

    /**
     * Add options to custom field
     *
     * @param customField       the custom field
     * @param fieldConfigOption the field config
     * @param optionNames       list option names
     * @param defaultOptionName default option name
     */
    private void addOptionsToCustomField(CustomField customField, com.atlassian.fugue.Option<FieldConfig> fieldConfigOption, List<String> optionNames, String defaultOptionName)
    {
        // option names will be translated into default locale
        I18nHelper i18n = i18nFactoryService.getInstance(applicationProperties.getDefaultLocale());

        com.atlassian.fugue.Option<Options> options = CustomFieldUtil.getCustomFieldOptions(customField, fieldConfigOption);
        if (options.isEmpty())
        {
            return;
        }

        // since we got options back, we know the field config is good
        final FieldConfig fieldConfig = fieldConfigOption.get();

        // Add options first, we set their order after the fact in #setOptionsOrderFromMetadata(CustomField customField, CustomFieldMetadata fieldMetadata)
        for (String optionName : optionNames)
        {
            String translatedOption = i18n.getText(optionName);
            com.atlassian.jira.issue.customfields.option.Option option = options.get().addOption(null, translatedOption);
            if (optionName.equals(defaultOptionName))
            {
                // set default option
                customField.getCustomFieldType().setDefaultValue(fieldConfig, option);
            }
        }
    }

    /**
     * Set options from meta data
     *
     * @param customField   the custom field
     * @param fieldMetadata field meta data
     * @param issueTypeIds  issue type IDs
     */
    private void setOptionsOrderFromMetadata(CustomField customField, CustomFieldMetadata fieldMetadata, Set<String> issueTypeIds)
    {
        List<String> optionValues = getOptionValuesFromNames(fieldMetadata.getOptionNames());

        for (String issueTypeId : issueTypeIds)
        {
            com.atlassian.fugue.Option<FieldConfig> fieldConfig = CustomFieldUtil.getRelevantConfig(customField, new IssueContextImpl(null, issueTypeId));

            if (fieldConfig.isEmpty())
            {
                // GHS-6912 - if the customer has tampered with the Custom Field Contexts of the Epic Status field, this
                // lookup will return null. We want to fail silently here so that they can continue to upgrade/use GH.
                logger.warn("Could not find a Custom Field Configuration for field {}, all projects and issue type {} -- therefore cannot find the Options for the field.",
                        customField.getId(), issueTypeId);
                continue;
            }
            com.atlassian.fugue.Option<Options> optionsResult = CustomFieldUtil.getCustomFieldOptions(customField, fieldConfig);
            if (optionsResult.isEmpty())
            {
                continue;
            }
            Options options = optionsResult.get();

            Map<Integer, com.atlassian.jira.issue.customfields.option.Option> optionToPositionMap = new HashMap<Integer, com.atlassian.jira.issue.customfields.option.Option>(options.size());
            List<com.atlassian.jira.issue.customfields.option.Option> rootOptions = options.getRootOptions();

            if (optionValues.size() != options.size())
            {
                throw new CustomFieldException("When setting custom field options order, available options must match initially created options");
            }

            for (com.atlassian.jira.issue.customfields.option.Option option : rootOptions)
            {
                if (!optionValues.contains(option.getValue()))
                {
                    throw new CustomFieldException("When setting custom field options order, available options must match initially created options");
                }
                optionToPositionMap.put(optionValues.indexOf(option.getValue()), option);
            }

            options.moveOptionToPosition(optionToPositionMap);
        }
    }

    /**
     * Convenience method to retrieve i18n texts from keys
     *
     * @param optionNames keys to retrieve
     * @return text literals
     */
    private List<String> getOptionValuesFromNames(final List<String> optionNames)
    {
        final I18nHelper i18n = i18nFactoryService.getInstance(applicationProperties.getDefaultLocale());

        return Lists.transform(optionNames, new Function<String, String>()
        {
            @Override
            public String apply(@Nullable final String optionName)
            {
                return i18n.getText(optionName);
            }
        });
    }

    /**
     * Set the custom field required
     *
     * @param field custom field
     */
    private void makeFieldRequired(final CustomField field)
    {
        List<EditableFieldLayout> layouts = fieldLayoutManager.getEditableFieldLayouts();
        for (EditableFieldLayout layout : layouts)
        {
            FieldLayoutItem fieldLayoutItem = layout.getFieldLayoutItem(field.getId());
            if (fieldLayoutItem != null)
            {
                layout.makeRequired(fieldLayoutItem);
                if (layout.isDefault())
                {
                    fieldLayoutManager.storeEditableDefaultFieldLayout((EditableDefaultFieldLayout) layout);
                }
                else
                {
                    fieldLayoutManager.storeEditableFieldLayout(layout);
                }
            }
        }
    }

    /**
     * Restore the field context from meta data before locking it down
     *
     * @param customFieldMetadata field meta data
     * @param customField         the custom field
     * @return true if the restore was successful, false otherwise
     */
    private boolean restoreFieldContext(CustomFieldMetadata customFieldMetadata, CustomField customField)
    {
        boolean valid = false;
        try
        {
            restoreFieldSearcher(customFieldMetadata, customField);
            restoreFieldConfiguration(customFieldMetadata, customField);
            valid = validateAndRestoreFieldConfigOptions(customFieldMetadata, customField);
        }
        catch (Exception ex)
        {
            logger.error("Failed to restore context {} for field {}", new Object[]{customFieldMetadata, customField}, ex);
            valid = false;
        }
        return valid;
    }

    /**
     * Restore the field searcher
     *
     * @param customFieldMetadata field meta data
     * @param customField         the custom field
     */
    private void restoreFieldSearcher(CustomFieldMetadata customFieldMetadata, CustomField customField)
    {
        if (customFieldMetadata.getFieldSearcher() != null || customField.getCustomFieldSearcher() == null)
        {
            CustomFieldSearcher searcher = customFieldManager.getCustomFieldSearcher(customFieldMetadata.getFieldSearcher());
            customField.setCustomFieldSearcher(searcher);
            customField.store();
        }
    }

    /**
     * Restore the field configuration
     *
     * @param customFieldMetadata field meta data
     * @param customField         the custom field
     */
    private void restoreFieldConfiguration(CustomFieldMetadata customFieldMetadata, CustomField customField)
    {
        List<EditableFieldLayout> editableFieldLayouts = fieldLayoutManager.getEditableFieldLayouts();
        for (EditableFieldLayout editableFieldLayout : editableFieldLayouts)
        {
            FieldLayoutItem fieldLayoutItem = editableFieldLayout.getFieldLayoutItem(customField.getId());
            if (fieldLayoutItem != null && fieldLayoutItem.isHidden())
            {
                editableFieldLayout.show(fieldLayoutItem);
                saveFieldLayout(editableFieldLayout);

                // reload the layout
                editableFieldLayout = fieldLayoutManager.getEditableFieldLayout(editableFieldLayout.getId());
                if (editableFieldLayout == null)
                {
                    fieldLayoutItem = editableFieldLayout.getFieldLayoutItem(customField.getId());
                    if (fieldLayoutItem == null)
                    {
                        // restore the description
                        I18nHelper i18n = i18nFactoryService.getInstance(applicationProperties.getDefaultLocale());
                        if (!StringUtils.equals(fieldLayoutItem.getFieldDescription(), i18n.getText(customFieldMetadata.getFieldDescription())))
                        {
                            editableFieldLayout.setDescription(fieldLayoutItem, i18n.getText(customFieldMetadata.getFieldDescription()));
                            saveFieldLayout(editableFieldLayout);
                        }

                        // restore the requirement attribute
                        if (customFieldMetadata.isRequireField())
                        {
                            for (String issueTypeId : getIssueTypeIds(customFieldMetadata.getIssueTypeProvider()))
                            {
                                // at the moment, we don't support making a field required for ALL issues types
                                if (issueTypeId.equals(GLOBAL_ISSUETYPE))
                                {
                                    addFieldForIssueOperationOnType(IssueOperations.CREATE_ISSUE_OPERATION, customField, issueTypeId, 0);
                                }
                            }

                            if (!fieldLayoutItem.isRequired())
                            {
                                editableFieldLayout.makeRequired(fieldLayoutItem);
                                saveFieldLayout(editableFieldLayout);
                            }
                        }
                        else if (!customFieldMetadata.isRequireField() && fieldLayoutItem.isRequired())
                        {
                            editableFieldLayout.makeOptional(fieldLayoutItem);
                            saveFieldLayout(editableFieldLayout);
                        }
                    }
                }

            }
        }
    }

    /**
     * Associate field on issue type operation
     *
     * @param issueOperation issue operation
     * @param field          the custom field
     * @param issueTypeId    issue type
     * @param position       the position
     */
    private void addFieldForIssueOperationOnType(final ScreenableIssueOperation issueOperation, final CustomField field, String issueTypeId, @Nullable final Integer position)
    {
        Set<FieldScreenScheme> screenSchemes = new LinkedHashSet<FieldScreenScheme>();
        Collection<IssueTypeScreenScheme> issueTypeScreenSchemes = issueTypeScreenSchemeManager.getIssueTypeScreenSchemes();
        for (IssueTypeScreenScheme issueTypeScreenScheme : issueTypeScreenSchemes)
        {
            addScreenScheme(issueTypeId, screenSchemes, issueTypeScreenScheme);
        }

        addFieldToScreens(issueOperation, field, position, screenSchemes);
    }

    /**
     * Add issue type to screen scheme
     *
     * @param issueTypeId           issue type
     * @param screenSchemes         screen scheme
     * @param issueTypeScreenScheme issue type screen scheme
     */
    private void addScreenScheme(final String issueTypeId, final Set<FieldScreenScheme> screenSchemes, final IssueTypeScreenScheme issueTypeScreenScheme)
    {
        IssueTypeScreenSchemeEntity screenSchemeEntity = issueTypeScreenScheme.getEntity(issueTypeId);
        if (screenSchemeEntity == null)
        {
            IssueTypeScreenSchemeEntity defaultEntity = issueTypeScreenScheme.getEntity(null);
            if (defaultEntity != null)
            {
                screenSchemes.add(defaultEntity.getFieldScreenScheme());
            }
            else
            {
                logger.warn("For some reason there is no Default Screen Scheme associated in Issue Type Screen Scheme '{}' ({}). PK will ignore this ITSS.",
                        issueTypeScreenScheme.getName(), issueTypeScreenScheme.getId());
            }
        }
        else
        {
            screenSchemes.add(screenSchemeEntity.getFieldScreenScheme());
        }
    }

    /**
     * Add field to screens
     *
     * @param issueOperation issue operation
     * @param field          the custom field
     * @param position       the position
     * @param screenSchemes  screen scheme
     */
    private void addFieldToScreens(final ScreenableIssueOperation issueOperation, final CustomField field, final Integer position, final Collection<FieldScreenScheme> screenSchemes)
    {
        Set<FieldScreen> fieldScreens = new LinkedHashSet<FieldScreen>();
        for (FieldScreenScheme fieldScreenScheme : screenSchemes)
        {
            try
            {
                fieldScreens.add(fieldScreenScheme.getFieldScreen(issueOperation));
            }
            catch (IllegalArgumentException e)
            {
                logger.warn("Failed to get screen", e);
            }
        }

        for (FieldScreen fieldScreen : fieldScreens)
        {
            addFieldToScreen(fieldScreen, field, position);
        }
    }

    /**
     * Add field to individual screen
     *
     * @param screen   field screen
     * @param field    the custom field
     * @param position the position
     */
    private void addFieldToScreen(final FieldScreen screen, final CustomField field, @Nullable final Integer position)
    {
        List<FieldScreenTab> tabs = fieldScreenManager.getFieldScreenTabs(screen);
        FieldScreenTab fieldScreenTab = tabs.get(0);

        // alter existing item if present
        FieldScreenLayoutItem existingFieldLayoutItem = fieldScreenTab.getFieldScreenLayoutItem(field.getId());
        if (existingFieldLayoutItem != null)
        {
            // if specified move layout item to requested position, otherwise do nothing as the field is already on the screen
            if (position != null)
            {
                // move layout item to requested position
                Map<Integer, FieldScreenLayoutItem> positionToItem = new HashMap<Integer, FieldScreenLayoutItem>();
                positionToItem.put(position, existingFieldLayoutItem);
                fieldScreenTab.moveFieldScreenLayoutItemToPosition(positionToItem);
            }
        }
        else
        {
            if (position != null)
            {
                fieldScreenTab.addFieldScreenLayoutItem(field.getId(), position);
            }
            else
            {
                fieldScreenTab.addFieldScreenLayoutItem(field.getId());
            }
        }

    }

    /**
     * Check and restore the field configuration options if feasible
     *
     * @param customFieldMetadata field meta data
     * @param customField         the custom field
     * @return true if the restore is successful, false otherwise
     */
    private boolean validateAndRestoreFieldConfigOptions(CustomFieldMetadata customFieldMetadata, CustomField customField)
    {
        boolean valid = false;
        if (customFieldMetadata.getOptionNames().isEmpty())
        {
            validateAndRestoreEmptyFieldConfigOptions(customFieldMetadata, customField);
            valid = true;
        }
        else
        {
            FieldConfigScheme validFieldConfigScheme = validateAndRestoreFieldConfigScheme(customFieldMetadata, customField);
            if (validFieldConfigScheme != null)
            {
                valid = validateAndRestoreMultipleFieldConfigOptions(customFieldMetadata, customField, validFieldConfigScheme);
            }
        }
        return valid;
    }

    /**
     * Check and restore field config options if field meta data does not define option names
     *
     * @param customFieldMetadata field meta data
     * @param customField         the custom field
     * @return field config scheme
     */
    private FieldConfigScheme validateAndRestoreEmptyFieldConfigOptions(CustomFieldMetadata customFieldMetadata, CustomField customField)
    {
        FieldConfigScheme defaultScheme = null;
        List<FieldConfigScheme> schemes = customField.getConfigurationSchemes();
        Set<String> expectedIssueTypeIds = getIssueTypeIds(customFieldMetadata.getIssueTypeProvider());
        for (FieldConfigScheme scheme : schemes)
        {
            if (scheme.isAllProjects() && expectedIssueTypeIds.equals(scheme.getConfigs().keySet()))
            {
                defaultScheme = scheme;
            }
            else
            {
                fieldConfigSchemeManager.removeFieldConfigScheme(scheme.getId());
            }
        }

        if (defaultScheme == null)
        {
            defaultScheme = associateCustomFieldContext(customFieldMetadata, customField);
        }

        return defaultScheme;
    }

    /**
     * Associate default field scheme from meta data
     *
     * @param customFieldMetadata field meta data
     * @param customField         the custom field
     * @return field config schema\e
     */
    private FieldConfigScheme associateCustomFieldContext(final CustomFieldMetadata customFieldMetadata, final CustomField customField)
    {
        Set<String> issueTypeIds = getIssueTypeIds(customFieldMetadata.getIssueTypeProvider());
        List<GenericValue> issueTypes = convert(issueTypeIds);
        List<JiraContextNode> contexts = CustomFieldUtils.buildJiraIssueContexts(true, null, null, getContextTreeManager());
        return fieldConfigSchemeManager.createDefaultScheme(customField, contexts, issueTypes);
    }

    /**
     * Check and restore the field config scheme
     *
     * @param customFieldMetadata field meta data
     * @param customField         the custom field
     * @return the valid field config schema if possible, or null if failure
     */
    private FieldConfigScheme validateAndRestoreFieldConfigScheme(CustomFieldMetadata customFieldMetadata, CustomField customField)
    {
        FieldConfigScheme validFieldConfigScheme = null;

        Set<String> issueTypeIds = getIssueTypeIds(customFieldMetadata.getIssueTypeProvider());
        List<FieldConfigScheme> schemes = customField.getConfigurationSchemes();
        if (schemes.size() == 0)
        {
            associateCustomFieldContext(customFieldMetadata, customField);
        }
        else if (schemes.size() == 1)
        {
            FieldConfigScheme scheme = schemes.get(0);
            if (scheme.isAllProjects() && issueTypeIds.equals(scheme.getConfigs().keySet()))
            {
                validFieldConfigScheme = scheme;
            }
        }

        return validFieldConfigScheme;
    }

    /**
     * Check and restore field config options if multiple options are defined in field meta data
     *
     * @param customFieldMetadata field meta data
     * @param customField         the custom field
     * @param fieldConfigScheme   field config scheme
     * @return true if the restore is successful, false otherwise
     */
    private boolean validateAndRestoreMultipleFieldConfigOptions(CustomFieldMetadata customFieldMetadata, CustomField customField, FieldConfigScheme fieldConfigScheme)
    {
        boolean valid = false;
        I18nHelper i18n = i18nFactoryService.getInstance(applicationProperties.getDefaultLocale());
        FieldConfig fieldConfig = fieldConfigScheme.getOneAndOnlyConfig();
        if (fieldConfig != null)
        {
            com.atlassian.fugue.Option<Options> optionsResult = CustomFieldUtil.getCustomFieldOptions(customField, fieldConfig);
            if (!optionsResult.isEmpty())
            {
                final Options options = optionsResult.get();
                if (options.isEmpty())
                {
                    // no field option, now restore from metadata
                    String defaultOptionName = customFieldMetadata.getDefaultOptionName();
                    for (String optionName : customFieldMetadata.getOptionNames())
                    {
                        String translatedOption = i18n.getText(optionName);
                        com.atlassian.jira.issue.customfields.option.Option option = options.addOption(null, translatedOption);
                        if (optionName.equals(defaultOptionName))
                        {
                            customField.getCustomFieldType().setDefaultValue(fieldConfig, option);
                        }
                    }
                    Set<String> issueTypeIds = getIssueTypeIds(customFieldMetadata.getIssueTypeProvider());
                    setOptionsOrderFromMetadata(customField, customFieldMetadata, issueTypeIds);
                    valid = true;
                }
                else
                {
                    // validate options
                    List<String> keyNames = customFieldMetadata.getOptionNames();
                    if (keyNames != null && keyNames.size() == options.size())
                    {
                        List<String> actualNames = new ArrayList<String>(options.size());
                        for (com.atlassian.jira.issue.customfields.option.Option option : options)
                        {
                            actualNames.add(option.getValue());
                        }
                        List<String> expectedNames = new ArrayList<String>(keyNames.size());
                        for (String keyName : keyNames)
                        {
                            expectedNames.add(i18n.getText(keyName));
                        }
                        expectedNames.removeAll(actualNames);
                        valid = expectedNames.size() == 0;
                    }

                    //validate default option
                    if (valid)
                    {
                        com.atlassian.jira.issue.customfields.option.Option actualOption = (com.atlassian.jira.issue.customfields.option.Option) customField.getCustomFieldType().getDefaultValue(fieldConfig);
                        String expectedOption = i18n.getText(customFieldMetadata.getDefaultOptionName());
                        valid = actualOption != null && expectedOption.equals(actualOption.getValue());
                    }
                }
            }
        }

        return valid;
    }

    /**
     * Update the field layout
     *
     * @param editableFieldLayout the field layout
     */
    private void saveFieldLayout(EditableFieldLayout editableFieldLayout)
    {
        if (editableFieldLayout.isDefault())
        {
            fieldLayoutManager.storeEditableDefaultFieldLayout((EditableDefaultFieldLayout) editableFieldLayout);
        }
        else
        {
            fieldLayoutManager.storeEditableFieldLayout(editableFieldLayout);
        }
    }

    /**
     * Lock the custom field once the field context is validated and restored
     *
     * @param field       the field
     * @param descI18nKey the description key
     * @return
     */
    private boolean lockField(final CustomField field, String descI18nKey)
    {
        if (!isFieldAlreadyLocked(field))
        {
            String pluginKey = pocketKnifeIntegration.getPluginKey();
            ManagedConfigurationItem managedConfigurationItem = managedConfigurationItemService.getManagedCustomField(field);
            managedConfigurationItem = managedConfigurationItem.newBuilder()
                    .setManaged(true)
                    .setConfigurationItemAccessLevel(ConfigurationItemAccessLevel.LOCKED)
                    .setSource(pluginKey + ":field-locking-service")
                    .setDescriptionI18nKey(descI18nKey)
                    .build();
            ServiceOutcome<ManagedConfigurationItem> resultOutcome = managedConfigurationItemService.updateManagedConfigurationItem(managedConfigurationItem);
            logErrors(resultOutcome);
            return resultOutcome.isValid();
        }
        else
        {
            return true;
        }
    }

    /**
     * Is the field currently locked
     *
     * @param field the field
     * @return true if field is locked, false otherwise
     */
    private boolean isFieldAlreadyLocked(final CustomField field)
    {
        ManagedConfigurationItem managedConfigurationItem = managedConfigurationItemService.getManagedCustomField(field);
        return managedConfigurationItem.getConfigurationItemAccessLevel() == ConfigurationItemAccessLevel.LOCKED;
    }

    private void logErrors(ServiceOutcome<?> outcome)
    {
        if (!outcome.isValid())
        {
            Map<String, String> errors = outcome.getErrorCollection().getErrors();
            for (String key : errors.keySet())
            {
                logger.error("Failure reason {} and message {}", key, errors.get(key));
            }
        }
    }
}
