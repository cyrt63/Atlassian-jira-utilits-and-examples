package com.atlassian.pocketknife.api.customfields.searchers;

import com.atlassian.jira.JiraDataType;
import com.atlassian.jira.issue.customfields.CustomFieldSearcher;
import com.atlassian.jira.issue.customfields.CustomFieldValueProvider;
import com.atlassian.jira.issue.customfields.SingleValueCustomFieldValueProvider;
import com.atlassian.jira.issue.customfields.searchers.AbstractInitializationCustomFieldSearcher;
import com.atlassian.jira.issue.customfields.searchers.CustomFieldSearcherClauseHandler;
import com.atlassian.jira.issue.customfields.searchers.SimpleCustomFieldSearcherClauseHandler;
import com.atlassian.jira.issue.customfields.searchers.information.CustomFieldSearcherInformation;
import com.atlassian.jira.issue.customfields.searchers.renderer.CustomFieldRenderer;
import com.atlassian.jira.issue.customfields.searchers.transformer.CustomFieldInputHelper;
import com.atlassian.jira.issue.fields.CustomField;
import com.atlassian.jira.issue.index.indexers.FieldIndexer;
import com.atlassian.jira.issue.search.ClauseNames;
import com.atlassian.jira.issue.search.searchers.information.SearcherInformation;
import com.atlassian.jira.issue.search.searchers.renderer.SearchRenderer;
import com.atlassian.jira.issue.search.searchers.transformer.SearchInputTransformer;
import com.atlassian.jira.jql.operand.JqlOperandResolver;
import com.atlassian.jira.jql.query.ClauseQueryFactory;
import com.atlassian.jira.jql.util.IndexValueConverter;
import com.atlassian.jira.plugin.customfield.CustomFieldSearcherModuleDescriptor;
import com.atlassian.jira.web.FieldVisibilityManager;
import com.atlassian.pocketknife.api.customfields.searchers.clausevalidator.AbstractClauseValidator;

import java.util.Collections;
import java.util.concurrent.atomic.AtomicReference;

/**
 * This is an abstract class for a searcher that is designed so that plugins can quickly write custom field searchers. It does the initialisation for
 * you so long as you provide it with the bits it needs.
 */
public abstract class AbstractPluginCustomFieldSearcher extends AbstractInitializationCustomFieldSearcher implements CustomFieldSearcher {
    private final FieldVisibilityManager fieldVisibilityManager;
    private final JqlOperandResolver jqlOperandResolver;
    private final CustomFieldInputHelper customFieldInputHelper;

    private volatile CustomFieldSearcherInformation searcherInformation;
    private volatile SearchInputTransformer searchInputTransformer;
    private volatile SearchRenderer searchRenderer;
    private volatile CustomFieldSearcherClauseHandler customFieldSearcherClauseHandler;

    public AbstractPluginCustomFieldSearcher(FieldVisibilityManager fieldVisibilityManager, JqlOperandResolver jqlOperandResolver, CustomFieldInputHelper customFieldInputHelper) {
        this.fieldVisibilityManager = fieldVisibilityManager;
        this.jqlOperandResolver = jqlOperandResolver;
        this.customFieldInputHelper = customFieldInputHelper;
    }

    /**
     * Converts what the user input into their search into a string we can use to match in the index. Should return a new instance of itself
     */
    protected abstract IndexValueConverter getNewIndexValueConverter();

    /**
     * The field indexer determines what fields to add to the issue document for your custom field. It also determines what it should do if search is
     * and is not enabled for your field. It is recommended that the FieldIndexer you return extends AbstractCustomFieldIndexer. Should return a new
     * instance of itself
     */
    protected abstract FieldIndexer getNewCustomFieldIndexer(FieldVisibilityManager fieldVisibilityManager, CustomField field);

    /**
     * Renderer that draws the 'view' and 'edit' views for the 'simple search' on the issue nav. If you do not want your custom field to appear on the
     * issue nav's simple search(ie, you only want it searched using JQL) then return an instance of the EmptySearchRenderer. Should return a new
     * instance of itself
     */
    protected abstract CustomFieldRenderer getNewCustomFieldSearchRenderer(ClauseNames clauseNames, CustomFieldSearcherModuleDescriptor customFieldSearcherModuleDescriptor, CustomField field, CustomFieldValueProvider customFieldValueProvider, FieldVisibilityManager fieldVisibilityManager);

    /**
     * This transforms the simple search into advanced search and back . It also controls if the switch is allowed. If you don't have a simple search
     * view, It is recommended that you use the NoSimpleSearchInputTransformer. Should return a new instance of itself
     */
    protected abstract SearchInputTransformer getNewSearchInputTransformer(CustomField field, final CustomFieldInputHelper customFieldInputHelper);

    /**
     * The clause validator that checks to see if the clauses and operations are valid. Should return a new instance of itself
     */
    protected abstract AbstractClauseValidator getNewClauseValidator();

    /**
     * Provides the logic to build a query for a clause. It is strongly recommended that you return an instance of one of the ClauseQueryFactories
     * already defined by JIRA.
     */
    protected abstract ClauseQueryFactory getNewClauseQueryFactory(CustomField field, JqlOperandResolver jqlOperandResolver, IndexValueConverter indexValueConverter);

    /**
     * This is a wrapper object for various things related to the custom field. Pick the one that suits you best
     */
    protected abstract SimpleCustomFieldSearcherClauseHandler getNewCustomFieldSearcherClauseHandler(AbstractClauseValidator validator, ClauseQueryFactory clauseQueryFactory);

    /**
     * This is the data type for your field. If you can, try and use the predefined values in JiraDataTypes. If they don't work, then just define your
     * own. Should return a new instance of itself
     */
    protected abstract JiraDataType getDataType();

    @Override
    public void init(CustomField field) {
        ClauseNames names = field.getClauseNames();
        IndexValueConverter indexValueConverter = getNewIndexValueConverter();

        FieldIndexer indexer = getNewCustomFieldIndexer(fieldVisibilityManager, field);

        CustomFieldValueProvider customFieldValueProvider = new SingleValueCustomFieldValueProvider();
        this.searcherInformation = new CustomFieldSearcherInformation(field.getId(), field.getNameKey(), Collections.<FieldIndexer>singletonList(indexer), new AtomicReference<CustomField>(field));
        this.searchRenderer = getNewCustomFieldSearchRenderer(names, getDescriptor(), field, customFieldValueProvider, fieldVisibilityManager);
        this.searchInputTransformer = getNewSearchInputTransformer(field, customFieldInputHelper);

        ClauseQueryFactory clauseQueryFactory = getNewClauseQueryFactory(field, jqlOperandResolver, indexValueConverter);
        this.customFieldSearcherClauseHandler = getNewCustomFieldSearcherClauseHandler(getNewClauseValidator(), clauseQueryFactory);
    }

    /**
     * The methods below are getters that are called by the custom field system. Nothing interesting happens there
     */

    @Override
    public SearcherInformation<CustomField> getSearchInformation() {
        if (searcherInformation == null) {
            throw new IllegalStateException("Attempt to retrieve SearcherInformation off uninitialised custom field searcher.");
        }
        return searcherInformation;
    }

    @Override
    public SearchInputTransformer getSearchInputTransformer() {
        if (searchInputTransformer == null) {
            throw new IllegalStateException("Attempt to retrieve searchInputTransformer off uninitialised custom field searcher.");
        }
        return searchInputTransformer;
    }

    @Override
    public SearchRenderer getSearchRenderer() {
        if (searchRenderer == null) {
            throw new IllegalStateException("Attempt to retrieve searchRenderer off uninitialised custom field searcher.");
        }
        return searchRenderer;
    }

    @Override
    public CustomFieldSearcherClauseHandler getCustomFieldSearcherClauseHandler() {
        if (customFieldSearcherClauseHandler == null) {
            throw new IllegalStateException("Attempt to retrieve customFieldSearcherClauseHandler off uninitialised custom field searcher.");
        }
        return customFieldSearcherClauseHandler;
    }
}
