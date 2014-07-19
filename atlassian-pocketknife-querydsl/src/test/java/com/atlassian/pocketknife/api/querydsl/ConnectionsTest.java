package com.atlassian.pocketknife.api.querydsl;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import java.sql.Connection;
import java.sql.SQLException;

import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;

public class ConnectionsTest
{

    private Connection connection;

    @Before
    public void setUp() throws Exception
    {
        connection = mock(Connection.class);
    }


    @Test
    public void testClose() throws Exception
    {
        Connections.close(connection);
        Mockito.verify(connection).close();

        Connections.close(null); // no problem
        Mockito.verify(connection).close();

    }

    @Test
    public void testCloseSwallowsExceptions() throws Exception
    {
        doThrow(SQLException.class).when(connection).close();
        Connections.close(connection);
    }
}