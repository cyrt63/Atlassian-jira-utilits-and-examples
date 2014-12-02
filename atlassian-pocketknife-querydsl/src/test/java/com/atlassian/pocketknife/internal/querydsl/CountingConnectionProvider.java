package com.atlassian.pocketknife.internal.querydsl;

import com.atlassian.pocketknife.internal.querydsl.tables.Connections;
import com.atlassian.pocketknife.spi.querydsl.AbstractConnectionProvider;

import java.sql.Connection;
import java.sql.SQLException;

/**
*/
public class CountingConnectionProvider extends AbstractConnectionProvider
{
    int borrowCount = 0;

    public int getBorrowCount()
    {
        return borrowCount;
    }

    @Override
    protected Connection getConnectionImpl(boolean autoCommit)
    {
        Connection connection = Connections.getConnection();
        try
        {
            connection.setAutoCommit(autoCommit);
        }
        catch (SQLException e)
        {
            throw new RuntimeException(e);
        }
        borrowCount++;
        return connection;
    }

    @Override
    public void returnConnection(Connection connection)
    {
        // we dont close our connections.  We are not pooling
        borrowCount--;
    }
}
