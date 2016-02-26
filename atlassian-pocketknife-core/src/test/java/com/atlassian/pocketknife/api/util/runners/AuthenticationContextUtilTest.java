package com.atlassian.pocketknife.api.util.runners;

import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.security.PermissionManager;
import com.atlassian.jira.user.ApplicationUser;
import com.atlassian.jira.util.thread.JiraThreadLocalUtil;
import com.atlassian.pocketknife.internal.util.runner.AuthenticationContextUtilImpl;
import org.apache.log4j.Logger;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InOrder;
import org.mockito.Mockito;

import java.util.concurrent.Callable;

import static org.mockito.Mockito.any;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.when;

import static org.junit.Assert.assertThat;
import static org.hamcrest.CoreMatchers.equalTo;

public class AuthenticationContextUtilTest {

    private JiraAuthenticationContext jiraAuthenticationContext;
    private JiraThreadLocalUtil jiraThreadLocalUtil;
    private AuthenticationContextUtil authenticationContextUtil;
    private PermissionManager permissionManager;

    private ApplicationUser previousUser = mock(ApplicationUser.class);
    private ApplicationUser user = mock(ApplicationUser.class);

    @Before
    public void setup() {
        jiraAuthenticationContext = mock(JiraAuthenticationContext.class);
        jiraThreadLocalUtil = mock(JiraThreadLocalUtil.class);
        permissionManager = mock(PermissionManager.class);
        authenticationContextUtil = new AuthenticationContextUtilImpl(
                jiraAuthenticationContext,
                jiraThreadLocalUtil,
                permissionManager
        );
    }

    @Test
    public void testRunAsRunnable() {
        when(jiraAuthenticationContext.getLoggedInUser()).thenReturn(previousUser);

        Runnable action = mock(Runnable.class);
        authenticationContextUtil.runAs(user, action);

        InOrder order = inOrder(jiraAuthenticationContext, jiraThreadLocalUtil, permissionManager, action);
        order.verify(jiraAuthenticationContext, times(1)).getLoggedInUser();
        order.verify(jiraThreadLocalUtil, times(1)).preCall();
        order.verify(jiraAuthenticationContext, times(1)).setLoggedInUser(user);
        order.verify(action, times(1)).run();
        order.verify(jiraAuthenticationContext, times(1)).setLoggedInUser(previousUser);
        order.verify(jiraThreadLocalUtil, times(1)).postCall(any(Logger.class));
        order.verify(permissionManager, times(1)).flushCache();
    }

    @Test
    public void testRunAsSupplier() throws Exception {
        when(jiraAuthenticationContext.getLoggedInUser()).thenReturn(previousUser);

        Object value = new Object();
        Callable action = Mockito.mock(Callable.class);
        when(action.call()).thenReturn(value);
        Object returned = authenticationContextUtil.runAs(user, action);
        assertThat(value, equalTo(returned));

        InOrder order = inOrder(jiraAuthenticationContext, jiraThreadLocalUtil, permissionManager, action);
        order.verify(jiraAuthenticationContext, times(1)).getLoggedInUser();
        order.verify(jiraThreadLocalUtil, times(1)).preCall();
        order.verify(jiraAuthenticationContext, times(1)).setLoggedInUser(user);
        order.verify(action, times(1)).call();
        order.verify(jiraAuthenticationContext, times(1)).setLoggedInUser(previousUser);
        order.verify(jiraThreadLocalUtil, times(1)).postCall(any(Logger.class));
        order.verify(permissionManager, times(1)).flushCache();
    }

}
