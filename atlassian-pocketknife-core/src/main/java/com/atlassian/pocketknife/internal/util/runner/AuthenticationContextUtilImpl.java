package com.atlassian.pocketknife.internal.util.runner;

import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.security.PermissionManager;
import com.atlassian.jira.user.ApplicationUser;
import com.atlassian.jira.util.thread.JiraThreadLocalUtil;
import com.atlassian.plugin.spring.scanner.annotation.imports.ComponentImport;
import com.atlassian.pocketknife.api.util.runners.AuthenticationContextUtil;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.concurrent.Callable;

import static com.google.common.base.Preconditions.checkNotNull;

@Component
public final class AuthenticationContextUtilImpl implements AuthenticationContextUtil {

    private static final Logger LOGGER = Logger.getLogger(AuthenticationContextUtilImpl.class);

    // com.atlassian.jira.util.thread and com.atlassian.jira.security must be in the OSGI imports list in order for
    // this component to work
    private final JiraAuthenticationContext jiraAuthenticationContext;
    private final JiraThreadLocalUtil jiraThreadLocalUtil;
    private final PermissionManager permissionManager;

    @Autowired
    public AuthenticationContextUtilImpl(
            @ComponentImport JiraAuthenticationContext jiraAuthenticationContext,
            @ComponentImport JiraThreadLocalUtil jiraThreadLocalUtil,
            @ComponentImport PermissionManager permissionManager) {
        this.jiraAuthenticationContext = jiraAuthenticationContext;
        this.jiraThreadLocalUtil = jiraThreadLocalUtil;
        this.permissionManager = permissionManager;
    }

    @Override
    public void runAs(ApplicationUser user, final Runnable action) {
        runAs(user, new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                action.run();
                return null;
            }
        });
    }

    @Override
    public <T> T runAs(ApplicationUser user, Callable<T> action) {
        checkNotNull(action, "action cannot be null");
        ApplicationUser previous = jiraAuthenticationContext.getLoggedInUser();
        try {
            jiraThreadLocalUtil.preCall();
            jiraAuthenticationContext.setLoggedInUser(user);
            return action.call();
        } catch (Exception e) {
            String msg = String.format("Unexpected error while running action as user '%s'", user.getUsername());
            LOGGER.error(msg, e);
            throw new RuntimeException(msg, e);
        } finally {
            jiraAuthenticationContext.setLoggedInUser(previous);
            jiraThreadLocalUtil.postCall(LOGGER);
            permissionManager.flushCache();
        }
    }

}
