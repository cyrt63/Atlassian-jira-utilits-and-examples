package com.atlassian.pocketknife.api.util;

import com.atlassian.fugue.Either;
import com.atlassian.fugue.Option;
import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.util.ErrorCollection;
import com.atlassian.jira.util.SimpleErrorCollection;


/**
 * Static helper methods to work with service results
 * <p>
 * Service Results are always an Either, either containing an ErrorCollection or an object in case of success.
 * A Void success returns an Option.none() as result object.
 */
public class ServiceResult {
    private static String translateError(String key, Object... params) {
        com.atlassian.jira.util.I18nHelper.BeanFactory helper = ComponentAccessor.getI18nHelperFactory();
        JiraAuthenticationContext jiraAuthenticationContext = ComponentAccessor.getJiraAuthenticationContext();
        return helper.getInstance(jiraAuthenticationContext.getUser()).getText(key, params);
    }

    /**
     * Convenience method that returns an ok result with the provided returned value.
     */
    public static <T> Either<ErrorCollection, T> ok(T returnedValue) {
        return Either.right(returnedValue);
    }

    /**
     * Convenience method that returns a new ServiceOutcomeImpl instance containing no errors, and with no provided
     * return value
     */
    public static Either<ErrorCollection, Option<Object>> ok() {
        return Either.right(Option.none());
    }

    /**
     * Convenience method that returns a new ServiceOutcomeImpl instance containing the provided error message, and no
     * return value.
     */
    public static <T> Either<ErrorCollection, T> error(ErrorCollection.Reason reason, String messageKey, Object... params) {
        ErrorCollection errors = new SimpleErrorCollection();
        errors.addErrorMessage(translateError(messageKey, params), reason);
        return Either.left(errors);
    }

    /**
     * Convenience method that returns a new ServiceOutcomeImpl instance containing the provided contextual error message,
     * and no return value.
     *
     * @param fieldId    context of the error
     * @param reason     the reason for the error
     * @param messageKey the key of the message
     * @param params     the parameters to the key
     * @return a new ServiceOutcomeImpl
     */
    public static <T> Either<ErrorCollection, T> error(String fieldId, ErrorCollection.Reason reason, String messageKey, Object... params) {
        ErrorCollection errors = new SimpleErrorCollection();
        errors.addError(fieldId, translateError(messageKey, params), reason);
        return Either.left(errors);
    }

    /**
     * Convenience method that returns a new ServiceOutcomeImpl instance containing the provided contextual error message,
     * and no return value.
     */
    public static <T, U> Either<ErrorCollection, T> error(Either<ErrorCollection, U> e) {
        return Either.left(e.left().get());
    }

    /**
     * Convenience method that returns a new ServiceOutcomeImpl instance containing the provided contextual error message,
     * and no return value.
     */
    public static <T, U> Either<ErrorCollection, T> error(ErrorCollection e) {
        return Either.left(e);
    }
}
