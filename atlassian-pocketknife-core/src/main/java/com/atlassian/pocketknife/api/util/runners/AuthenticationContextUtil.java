package com.atlassian.pocketknife.api.util.runners;

import com.atlassian.jira.user.ApplicationUser;

import java.util.concurrent.Callable;

/**
 * Utility component for executing code under a specific user authentication context
 */
public interface AuthenticationContextUtil {

    /**
     * Runs the specified action as the user passed as parameter
     *
     * @param user user that will perform the action
     * @param action action to be performed
     */
    public void runAs(ApplicationUser user, Runnable action);

    /**
     * Runs the specified action as the user passed as parameter
     *
     * @param user user that will perform the action
     * @param action action to be performed
     * @param <T> return type of the action
     * @return result of the action
     */
    public <T> T runAs(ApplicationUser user, Callable<T> action);

}
