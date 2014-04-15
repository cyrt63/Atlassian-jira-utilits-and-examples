package com.atlassian.pocketknife.api.customfields.service;

import com.atlassian.fugue.Option;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Describes the configuration keys necessary for managing a custom field in JIRA
 *
 * @author ahennecke
 */
public class CustomFieldMetadata
{

    private final String fieldName;
    private final String fieldDescription;
    private final String fieldType;
    private final String fieldSearcher;
    private final Option<IssueTypeProvider> issueTypeProvider;
    private final List<String> optionNames;
    private final String defaultOptionName;
    private final boolean lockField;
    private final String lockFieldDescription;
    private final boolean requireField;

    public CustomFieldMetadata(String fieldName, String fieldDescription, String fieldType, String fieldSearcher, Option<IssueTypeProvider> issueTypeProvider, List<String> optionNames, String defaultOptionName, boolean lockField, String lockFieldDescription, boolean requireField)
    {
        this.fieldName = fieldName;
        this.fieldDescription = fieldDescription;
        this.fieldType = fieldType;
        this.fieldSearcher = fieldSearcher;
        this.issueTypeProvider = issueTypeProvider;
        this.optionNames = optionNames;
        this.defaultOptionName = defaultOptionName;
        this.lockField = lockField;
        this.lockFieldDescription = lockFieldDescription;
        this.requireField = requireField;
    }

    public static Builder builder()
    {
        return new Builder();
    }

    /**
     * the custom field name used by JIRA
     */
    public String getFieldName()
    {
        return fieldName;
    }

    /**
     * the custom field description used by JIRA
     */
    public String getFieldDescription()
    {
        return fieldDescription;
    }

    /**
     * the key of the custom field declaration in atlassian-plugins.xml
     */
    public String getFieldType()
    {
        return fieldType;
    }

    /**
     * the key of the custom field searcher declaration in atlassian-plugins.xml
     */
    public String getFieldSearcher()
    {
        return fieldSearcher;
    }

    public Option<IssueTypeProvider> getIssueTypeProvider()
    {
        return issueTypeProvider;
    }

    public List<String> getOptionNames()
    {
        return optionNames;
    }

    public String getDefaultOptionName()
    {
        return defaultOptionName;
    }

    public boolean isLockField()
    {
        return lockField;
    }

    public String getLockFieldDescription()
    {
        return lockFieldDescription;
    }

    /**
     * Used to determine both "requiredness" and whether to add to the issue creation screen.
     *
     * @return
     */
    public boolean isRequireField()
    {
        return requireField;
    }

    @Override
    public String toString()
    {
        return new ToStringBuilder(this, ToStringStyle.SHORT_PREFIX_STYLE).append("fieldName", fieldName).append("fieldDescription", fieldDescription).append("fieldType", fieldType).append("fieldSearcher", fieldSearcher).append("optionNames", optionNames).append("defaultOptionName", defaultOptionName).append("lockField", lockField).append("lockFieldDescription", lockFieldDescription).append("requireField", requireField).toString();
    }

    /**
     * Use the builder class for convenience support on working with {@link com.atlassian.jira.issue.issuetype.IssueType} managed by JIRA
     * Provide a {@link com.atlassian.pocketknife.api.customfields.service.IssueTypeProvider} which acts a util to convert
     * IssueType from String literal to the instance that JIRA managed
     */
    public static class Builder
    {
        private String fieldName;
        private String fieldDescription;
        private String fieldType;
        private String fieldSearcher;
        private Option<IssueTypeProvider> issueTypeProvider = Option.none();
        private Set<String> issueTypes = new HashSet<String>();
        private List<String> optionNames = new ArrayList<String>();
        private String defaultOptionName;
        private boolean lockField;
        private String lockFieldDescription;
        private boolean requireField;

        public Builder()
        {
        }

        public Builder(String fieldName, String fieldDescription, String fieldType, String fieldSearcher, Option<IssueTypeProvider> issueTypeProvider, Set<String> issueTypes, List<String> optionNames, String defaultOptionName, boolean lockField, String lockFieldDescription, boolean requireField)
        {
            this.fieldName = fieldName;
            this.fieldDescription = fieldDescription;
            this.fieldType = fieldType;
            this.fieldSearcher = fieldSearcher;
            this.issueTypeProvider = issueTypeProvider;
            this.issueTypes = issueTypes;
            this.optionNames = optionNames;
            this.defaultOptionName = defaultOptionName;
            this.lockField = lockField;
            this.lockFieldDescription = lockFieldDescription;
            this.requireField = requireField;
        }

        public Builder fieldName(String fieldName)
        {
            this.fieldName = fieldName;
            return this;
        }

        public Builder fieldDescription(String fieldDescription)
        {
            this.fieldDescription = fieldDescription;
            return this;
        }

        public Builder fieldType(String fieldType)
        {
            this.fieldType = fieldType;
            return this;
        }

        public Builder fieldSearcher(String fieldSearcher)
        {
            this.fieldSearcher = fieldSearcher;
            return this;
        }

        public Builder issueTypeProvider(IssueTypeProvider issueTypeProvider)
        {
            if (issueTypeProvider != null)
            {
                this.issueTypeProvider = Option.option(issueTypeProvider);
            }
            else
            {
                this.issueTypeProvider = Option.none();
            }
            return this;
        }

        public Builder issueTypes(String... issueTypes)
        {
            this.issueTypes.clear();
            this.issueTypes.addAll(Arrays.asList(issueTypes));
            return this;
        }

        public Builder issueTypes(Set<String> issueTypes)
        {
            this.issueTypes.clear();
            this.issueTypes.addAll(issueTypes);
            return this;
        }

        public Builder optionNames(String... optionNames)
        {
            this.optionNames.clear();
            this.optionNames.addAll(Arrays.asList(optionNames));
            return this;
        }

        public Builder optionNames(String defaultOptionName)
        {
            if (this.optionNames.contains(defaultOptionName))
            {
                this.defaultOptionName = defaultOptionName;
            }
            return this;
        }

        public Builder lockField(boolean lockField)
        {
            this.lockField = lockField;
            return this;
        }

        public Builder lockFieldDescription(String lockFieldDescription)
        {
            this.lockFieldDescription = lockFieldDescription;
            return this;
        }

        public Builder requireField(boolean requireField)
        {
            this.requireField = requireField;
            return this;
        }

        public CustomFieldMetadata build()
        {
            return new CustomFieldMetadata(fieldName, fieldDescription, fieldType, fieldSearcher, issueTypeProvider, optionNames, defaultOptionName, lockField, lockFieldDescription, requireField);
        }
    }
}
