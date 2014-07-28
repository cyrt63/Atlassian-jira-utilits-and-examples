package com.atlassian.pocketknife.spi.querydsl;

import com.atlassian.annotations.PublicSpi;
import com.atlassian.pocketknife.api.querydsl.ConnectionProvider;
import com.google.common.base.Function;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.SQLException;

/**
 */

@PublicSpi
public abstract class AbstractConnectionProvider implements ConnectionProvider
{
    private static final Logger log = LoggerFactory.getLogger(AbstractConnectionProvider.class);

    /**
     * This is where you implement your connection retrieval
     *
     * @param autoCommit whether the connection should be auto commit or not
     * @return a connection
     */
    protected abstract Connection getConnectionImpl(final boolean autoCommit);

    /**
     * @return your own logger if you choose
     */
    protected Logger log()
    {
        return log;
    }

    public Connection borrowConnection()
    {
        return getConnectionImpl(false);
    }

    public Connection borrowAutoCommitConnection()
    {
        return getConnectionImpl(true);
    }

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
                log().warn(String.format("Unexpected exception when returning borrowed connection %s ", e.getMessage()));
            }
        }
    }
}
