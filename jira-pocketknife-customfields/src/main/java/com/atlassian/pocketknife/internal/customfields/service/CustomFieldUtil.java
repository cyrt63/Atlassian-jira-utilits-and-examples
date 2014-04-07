package com.atlassian.pocketknife.internal.customfields.service;

import com.atlassian.fugue.Option;
import com.atlassian.jira.issue.context.IssueContext;
import com.atlassian.jira.issue.context.JiraContextNode;
import com.atlassian.jira.issue.customfields.MultipleCustomFieldType;
import com.atlassian.jira.issue.customfields.option.Options;
import com.atlassian.jira.issue.fields.CustomField;
import com.atlassian.jira.issue.fields.config.FieldConfig;
import com.atlassian.jira.issue.fields.config.FieldConfigScheme;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;

public class CustomFieldUtil
{
    private static final Logger logger = LoggerFactory.getLogger(CustomFieldUtil.class);

    /**
     * Returns options for this custom field if it is
     * of {@link com.atlassian.jira.issue.customfields.MultipleCustomFieldType} type. Otherwise returns null.
     * <p/>
     *
     * @param customField the custom field
     * @param fieldConfig relevant field config
     * @return options for this custom field if it is of {@link com.atlassian.jira.issue.customfields.MultipleCustomFieldType} type
     * @see com.atlassian.jira.issue.fields.CustomField#getOptions(String, com.atlassian.jira.issue.fields.config.FieldConfig, com.atlassian.jira.issue.context.JiraContextNode)
     */
    @Nonnull
    public static Option<Options> getCustomFieldOptions(@Nonnull CustomField customField, @Nonnull FieldConfig fieldConfig)
    {
        final Options options = customField.getOptions(null, fieldConfig, null);
        if (options == null)
        {
            logger.warn("Expected custom field [id={}, name={}] to have nonnull options for field config {} but it did not.", new Object[]{customField.getId(), customField.getName(), fieldConfig.getId()});
            return Option.none();
        }
        return Option.some(options);
    }

    /**
     * Returns options for this custom field if it is
     * of {@link com.atlassian.jira.issue.customfields.MultipleCustomFieldType} type. Otherwise returns null.
     * <p/>
     *
     * @param customField the custom field
     * @param fieldConfig relevant field config (or None).
     * @return options for this custom field if it is of {@link com.atlassian.jira.issue.customfields.MultipleCustomFieldType} type
     * @see com.atlassian.jira.issue.fields.CustomField#getOptions(String, com.atlassian.jira.issue.fields.config.FieldConfig, com.atlassian.jira.issue.context.JiraContextNode)
     */
    @Nonnull
    public static Option<Options> getCustomFieldOptions(@Nonnull CustomField customField, @Nonnull Option<FieldConfig> fieldConfig)
    {
        if (fieldConfig.isEmpty())
        {
            logger.warn("Expected custom field [id=%s, name=%s] field config to exist but it did not.", customField.getId(), customField.getName());
            return Option.none();
        }
        return getCustomFieldOptions(customField, fieldConfig.get());
    }

    /**
     * Returns all possible Options for this field.
     *
     * @param customFieldType the custom field type
     * @param fieldConfig     configuration for this field
     * @return all possible Options for this field.
     * @see com.atlassian.jira.issue.customfields.MultipleCustomFieldType#getOptions(com.atlassian.jira.issue.fields.config.FieldConfig, com.atlassian.jira.issue.context.JiraContextNode)
     */
    @Nonnull
    public static Option<Options> getCustomFieldTypeOptions(@Nonnull MultipleCustomFieldType<?, ?> customFieldType, @Nonnull FieldConfig fieldConfig)
    {
        return getCustomFieldTypeOptions(customFieldType, fieldConfig, null);
    }

    /**
     * Returns all possible Options for this field.
     *
     * @param customFieldType the custom field type
     * @param fieldConfig     configuration for this field
     * @return all possible Options for this field.
     * @see com.atlassian.jira.issue.customfields.MultipleCustomFieldType#getOptions(com.atlassian.jira.issue.fields.config.FieldConfig, com.atlassian.jira.issue.context.JiraContextNode)
     */
    @Nonnull
    public static Option<Options> getCustomFieldTypeOptions(@Nonnull MultipleCustomFieldType<?, ?> customFieldType, @Nonnull Option<FieldConfig> fieldConfig)
    {
        if (fieldConfig.isEmpty())
        {
            return Option.none();
        }
        return getCustomFieldTypeOptions(customFieldType, fieldConfig.get(), null);
    }

    /**
     * Returns all possible Options for this field.
     *
     * @param customFieldType the custom field type
     * @param fieldConfig     configuration for this field
     * @param jiraContextNode the JIRA context node
     * @return all possible Options for this field.
     * @see com.atlassian.jira.issue.customfields.MultipleCustomFieldType#getOptions(com.atlassian.jira.issue.fields.config.FieldConfig, com.atlassian.jira.issue.context.JiraContextNode)
     */
    @Nonnull
    public static Option<Options> getCustomFieldTypeOptions(@Nonnull MultipleCustomFieldType<?, ?> customFieldType, @Nonnull FieldConfig fieldConfig, @Nullable JiraContextNode jiraContextNode)
    {
        final Options options = customFieldType.getOptions(fieldConfig, jiraContextNode);
        if (options == null)
        {
            logger.warn("Expected custom field type [name=%s] to have nonnull options for field config %d but it did not.", customFieldType.getName(), fieldConfig.getId());
            return Option.none();
        }
        return Option.some(options);
    }

    /**
     * Returns all possible Options for this field.
     *
     * @param customFieldType the custom field type
     * @param fieldConfig     configuration for this field
     * @param jiraContextNode the JIRA context node
     * @return all possible Options for this field.
     * @see com.atlassian.jira.issue.customfields.MultipleCustomFieldType#getOptions(com.atlassian.jira.issue.fields.config.FieldConfig, com.atlassian.jira.issue.context.JiraContextNode)
     */
    @Nonnull
    public static Option<Options> getCustomFieldTypeOptions(@Nonnull MultipleCustomFieldType<?, ?> customFieldType, @Nonnull Option<FieldConfig> fieldConfig, @Nullable JiraContextNode jiraContextNode)
    {
        if (fieldConfig.isEmpty())
        {
            return Option.none();
        }

        final Options options = customFieldType.getOptions(fieldConfig.get(), jiraContextNode);
        if (options == null)
        {
            logger.warn("Expected custom field type [name=%s] to have nonnull options for field config %d but it did not.", customFieldType.getName(), fieldConfig.get().getId());
            return Option.none();
        }
        return Option.some(options);
    }

    /**
     * Retrieves the global {@link com.atlassian.jira.issue.fields.config.FieldConfig} associated to the custom field's first {@link com.atlassian.jira.issue.fields.config.FieldConfigScheme}.
     *
     * @param field the custom field
     * @return the field config, or None
     * @see com.atlassian.jira.issue.fields.config.FieldConfigScheme#getOneAndOnlyConfig()
     */
    @Nonnull
    public static Option<FieldConfig> getOneAndOnlyConfig(@Nonnull CustomField field)
    {
        List<FieldConfigScheme> schemes = field.getConfigurationSchemes();
        if (!schemes.isEmpty())
        {
            final FieldConfig oneAndOnlyConfig = schemes.iterator().next().getOneAndOnlyConfig();
            if (oneAndOnlyConfig != null)
            {
                return Option.some(oneAndOnlyConfig);
            }
        }

        return Option.none();
    }

    /**
     * Retrieves the relevant field config for the field and {@link com.atlassian.jira.issue.context.IssueContext}.
     *
     * @param field        the field
     * @param issueContext the issue context
     * @return the field config, or None
     * @see com.atlassian.jira.issue.fields.CustomField#getRelevantConfig(com.atlassian.jira.issue.context.IssueContext)
     */
    @Nonnull
    public static Option<FieldConfig> getRelevantConfig(@Nonnull CustomField field, @Nonnull IssueContext issueContext)
    {
        final FieldConfig relevantConfig = field.getRelevantConfig(issueContext);
        if (relevantConfig == null)
        {
            return Option.none();
        }
        return Option.some(relevantConfig);
    }

}
