package com.atlassian.pocketknife.internal.util.runner;

import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.user.ApplicationUser;
import com.atlassian.jira.util.Supplier;
import com.atlassian.jira.util.thread.JiraThreadLocalUtil;
import com.atlassian.plugin.spring.scanner.annotation.imports.ComponentImport;
import com.atlassian.pocketknife.api.util.runners.AuthenticationContextUtil;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import static com.google.common.base.Preconditions.checkNotNull;

@Component
public final class AuthenticationContextUtilImpl implements AuthenticationContextUtil {

    private static final Logger LOGGER = Logger.getLogger(AuthenticationContextUtilImpl.class);

    // com.atlassian.jira.util.thread and com.atlassian.jira.security must be in the OSGI imports list in order for
    // this component to work
    private final JiraAuthenticationContext jiraAuthenticationContext;
    private final JiraThreadLocalUtil jiraThreadLocalUtil;

    @Autowired
    public AuthenticationContextUtilImpl(
            @ComponentImport JiraAuthenticationContext jiraAuthenticationContext,
            @ComponentImport JiraThreadLocalUtil jiraThreadLocalUtil) {
        this.jiraAuthenticationContext = jiraAuthenticationContext;
        this.jiraThreadLocalUtil = jiraThreadLocalUtil;
    }

    @Override
    public void runAs(ApplicationUser user, final Runnable action) {
        runAs(user, new Supplier<Void>() {
            @Override
            public Void get() {
                action.run();
                return null;
            }
        });
    }

    @Override
    public <T> T runAs(ApplicationUser user, Supplier<T> action) {
        checkNotNull(action, "action cannot be null");
        ApplicationUser previous = jiraAuthenticationContext.getLoggedInUser();
        try {
            jiraThreadLocalUtil.preCall();
            jiraAuthenticationContext.setLoggedInUser(user);
            return action.get();
        } finally {
            jiraAuthenticationContext.setLoggedInUser(previous);
            jiraThreadLocalUtil.postCall(LOGGER);
        }
    }

}
