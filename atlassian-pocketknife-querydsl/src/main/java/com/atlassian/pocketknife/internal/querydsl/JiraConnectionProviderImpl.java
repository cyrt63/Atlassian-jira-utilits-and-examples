package com.atlassian.pocketknife.internal.querydsl;

import com.atlassian.jira.ofbiz.DefaultOfBizConnectionFactory;
import com.atlassian.plugin.spring.scanner.annotation.component.JiraComponent;
import com.atlassian.pocketknife.spi.querydsl.AbstractConnectionProvider;
import com.google.common.annotations.VisibleForTesting;

import java.sql.Connection;
import java.sql.SQLException;

/**
 * JIRA rules the waves!
 */
@JiraComponent
public class JiraConnectionProviderImpl extends AbstractConnectionProvider
{
    protected Connection getConnectionImpl(final boolean autoCommit)
    {
        DefaultOfBizConnectionFactory instance = getDefaultOfBizConnectionFactory();

        log().debug("Getting a connection with auto-commit '" + autoCommit +
                "' using a DefaultOfBizConnectionFactory for schema '" + instance.getDatasourceInfo().getSchemaName() + "'");

        try
        {
            Connection connection = instance.getConnection();
            connection.setAutoCommit(autoCommit);

            log().debug("Got connection with auto-commit '" + autoCommit + "'for schema '" + connection.getSchema() + "'");

            return connection;
        }
        catch (SQLException e)
        {
            throw new RuntimeException(e);
        }
    }

    @VisibleForTesting
    DefaultOfBizConnectionFactory getDefaultOfBizConnectionFactory()
    {
        return DefaultOfBizConnectionFactory.getInstance();
    }
}
