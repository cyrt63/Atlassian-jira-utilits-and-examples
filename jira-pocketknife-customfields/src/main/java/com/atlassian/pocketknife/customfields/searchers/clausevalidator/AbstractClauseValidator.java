package com.atlassian.pocketknife.customfields.searchers.clausevalidator;

import java.util.Set;

import com.atlassian.jira.jql.validator.ClauseValidator;
import com.atlassian.jira.jql.validator.SupportedOperatorsValidator;
import com.atlassian.query.operator.Operator;

public abstract class AbstractClauseValidator implements ClauseValidator
{
    /**
     * Use the values defined in OperatorClasses to implement this method
     * 
     * @return a list of valid operators for this clause
     */
    abstract public Set<Operator> getValidOperators();

    @SuppressWarnings("unchecked")
    protected SupportedOperatorsValidator getSupportedOperatorsValidator()
    {
        return new SupportedOperatorsValidator(getValidOperators());
    }

}
