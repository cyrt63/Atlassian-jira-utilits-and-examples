package com.atlassian.pocketknife.api.util.runners;

import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.user.ApplicationUser;
import com.atlassian.jira.util.Supplier;
import com.atlassian.jira.util.thread.JiraThreadLocalUtil;
import com.atlassian.pocketknife.internal.util.runner.AuthenticationContextUtilImpl;
import org.apache.log4j.Logger;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InOrder;
import org.mockito.Mockito;

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

    private ApplicationUser previousUser = mock(ApplicationUser.class);
    private ApplicationUser user = mock(ApplicationUser.class);

    @Before
    public void setup() {
        jiraAuthenticationContext = mock(JiraAuthenticationContext.class);
        jiraThreadLocalUtil = mock(JiraThreadLocalUtil.class);
        authenticationContextUtil = new AuthenticationContextUtilImpl(
                jiraAuthenticationContext,
                jiraThreadLocalUtil
        );
    }

    @Test
    public void testRunAsRunnable() {
        when(jiraAuthenticationContext.getLoggedInUser()).thenReturn(previousUser);

        Runnable action = mock(Runnable.class);
        authenticationContextUtil.runAs(user, action);

        InOrder order = inOrder(jiraAuthenticationContext, jiraThreadLocalUtil, action);
        order.verify(jiraAuthenticationContext, times(1)).getLoggedInUser();
        order.verify(jiraThreadLocalUtil, times(1)).preCall();
        order.verify(jiraAuthenticationContext, times(1)).setLoggedInUser(user);
        order.verify(action, times(1)).run();
        order.verify(jiraAuthenticationContext, times(1)).setLoggedInUser(previousUser);
        order.verify(jiraThreadLocalUtil, times(1)).postCall(any(Logger.class));
    }

    @Test
    public void testRunAsSupplier() {
        when(jiraAuthenticationContext.getLoggedInUser()).thenReturn(previousUser);

        Object value = new Object();
        Supplier action = Mockito.mock(Supplier.class);
        when(action.get()).thenReturn(value);
        Object returned = authenticationContextUtil.runAs(user, action);
        assertThat(value, equalTo(returned));

        InOrder order = inOrder(jiraAuthenticationContext, jiraThreadLocalUtil, action);
        order.verify(jiraAuthenticationContext, times(1)).getLoggedInUser();
        order.verify(jiraThreadLocalUtil, times(1)).preCall();
        order.verify(jiraAuthenticationContext, times(1)).setLoggedInUser(user);
        order.verify(action, times(1)).get();
        order.verify(jiraAuthenticationContext, times(1)).setLoggedInUser(previousUser);
        order.verify(jiraThreadLocalUtil, times(1)).postCall(any(Logger.class));
    }

}
