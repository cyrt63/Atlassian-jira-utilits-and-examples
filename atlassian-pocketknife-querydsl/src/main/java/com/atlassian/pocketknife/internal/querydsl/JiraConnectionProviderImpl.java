package com.atlassian.pocketknife.internal.querydsl;

import com.atlassian.jira.ofbiz.DefaultOfBizConnectionFactory;
import com.atlassian.plugin.spring.scanner.annotation.component.JiraComponent;
import com.atlassian.pocketknife.api.querydsl.ConnectionProvider;
import com.google.common.base.Function;
import org.apache.log4j.Logger;
import org.springframework.stereotype.Component;

import java.sql.Connection;
import java.sql.SQLException;

/**
 *
 */
@JiraComponent
public class JiraConnectionProviderImpl implements ConnectionProvider
{
    private static final Logger log = Logger.getLogger(JiraConnectionProviderImpl.class);

    @Override
    public Connection borrowConnection()
    {
        return getConnectionImpl(false);
    }

    @Override
    public Connection borrowAutoCommitConnection()
    {
        return getConnectionImpl(true);
    }

    @Override
    public void returnConnection(final Connection connection)
    {
        if (connection != null)
        {
            try
            {
                connection.close();
            }
            catch (SQLException e)
            {
                log.warn(String.format("Unexpected exception when returning borrowed connection %s ", e.getMessage()));
            }
        }
    }

    @Override
    public <T> T withConnection(final Function<Connection, T> callback)
    {
        Connection connection = borrowConnection();
        try
        {
            return callback.apply(connection);
        }
        finally
        {
            returnConnection(connection);
        }
    }

    private Connection getConnectionImpl(final boolean autoCommit)
    {
        DefaultOfBizConnectionFactory instance = DefaultOfBizConnectionFactory.getInstance();
        try
        {
            Connection connection = instance.getConnection();
            connection.setAutoCommit(autoCommit);
            return connection;
        }
        catch (SQLException e)
        {
            throw new RuntimeException(e);
        }
    }
}
