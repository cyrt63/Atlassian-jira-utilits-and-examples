package com.atlassian.pocketknife.internal.querydsl;

import com.atlassian.pocketknife.api.querydsl.ConnectionProvider;
import com.atlassian.pocketknife.spi.querydsl.AbstractConnectionProvider;
import com.google.common.base.Function;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;

import java.sql.Connection;
import java.sql.SQLException;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class TransactionExecutorImplTest
{
    private TransactionExecutorImpl transactionExecutor;

    @Before
    public void setUp() throws Exception
    {
        transactionExecutor = new TransactionExecutorImpl(connectionProviderWithMockConnection());
    }

    @Test(expected = IllegalArgumentException.class)
    public void executeInTransaction__null_function_throws_exception() throws Exception
    {
        transactionExecutor.executeInTransaction(null);
    }

    @Test
    public void executeInTransaction__supplied_toExecute_function_is_passed_to_connectionProvider_withConnection_method() throws Exception
    {
        // arrange
        Function toExecute = mock(Function.class);
        ConnectionProvider connectionProvider = connectionProviderWithMockConnection();
        ConnectionProvider connectionProviderSpy = spy(connectionProvider);
        transactionExecutor = new TransactionExecutorImpl(connectionProviderSpy);

        // act
        transactionExecutor.executeInTransaction(toExecute);

        // assert
        ArgumentCaptor<TransactionExecutorImpl.InTransactionExecutor> toExecuteCaptor = ArgumentCaptor.forClass(TransactionExecutorImpl.InTransactionExecutor.class);
        verify(connectionProviderSpy, times(1)).withConnection(toExecuteCaptor.capture());
        assertEquals(toExecute, toExecuteCaptor.getValue().getFunctionToExecute());
    }

    @Test
    public void executeInTransaction__supplied_toExecute_function_is_invoked() throws Exception
    {
        // arrange
        Function toExecute = mock(Function.class);
        ConnectionProvider connectionProvider = connectionProviderWithMockConnection();
        transactionExecutor = new TransactionExecutorImpl(connectionProvider);

        // act
        transactionExecutor.executeInTransaction(toExecute);

        // assert
        verify(toExecute, times(1)).apply(any(Connection.class));
    }

    @Test
    public void executeInTransaction__supplied_toExecute_function_is_given_connection_from_connection_provider() throws Exception
    {
        // arrange
        Function toExecute = mock(Function.class);
        Connection connectionFromConnectionProvider = mock(Connection.class);
        ConnectionProvider connectionProvider = connectionProviderWithConnection(connectionFromConnectionProvider);
        transactionExecutor = new TransactionExecutorImpl(connectionProvider);

        // act
        transactionExecutor.executeInTransaction(toExecute);

        // assert
        verify(toExecute).apply(connectionFromConnectionProvider);
    }

    @Test
    public void executeInTransaction__exception_thrown_from_toExecute_function_is_thrown_upwards() throws Exception
    {
        // arrange
        Function toExecute = mock(Function.class);
        when(toExecute.apply(any())).thenThrow(new RuntimeException());

        Connection connection = mock(Connection.class);
        transactionExecutor = new TransactionExecutorImpl(connectionProviderWithConnection(connection));

        // act
        try
        {
            transactionExecutor.executeInTransaction(toExecute);
        }
        catch (RuntimeException e)
        {
            // Expected
        }

        // assert
        verify(connection, times(1)).rollback();
    }

    @Test
    public void executeInTransaction__connection_is_rolled_back_when_toExecute_function_throws_exception() throws Exception
    {
        // arrange
        Function toExecute = mock(Function.class);
        RuntimeException exceptionThrownFromFunction = new RuntimeException();
        when(toExecute.apply(any())).thenThrow(exceptionThrownFromFunction);

        Connection connection = mock(Connection.class);
        transactionExecutor = new TransactionExecutorImpl(connectionProviderWithConnection(connection));

        // act
        try
        {
            transactionExecutor.executeInTransaction(toExecute);
            fail("Should have thrown exception");
        }
        catch (RuntimeException thrownException)
        {
            assertEquals(exceptionThrownFromFunction, thrownException);
        }

        // assert
        verify(connection, times(1)).rollback();
    }

    @Test
    public void executeInTransaction__toExecute_function_exception_is_thrown_even_when_rollback_throws_exception() throws Exception
    {
        // arrange
        Function toExecute = mock(Function.class);
        RuntimeException exceptionThrownFromFunction = new RuntimeException();
        when(toExecute.apply(any())).thenThrow(exceptionThrownFromFunction);

        Connection connection = mock(Connection.class);

        doThrow(new SQLException()).when(connection).rollback();
        transactionExecutor = new TransactionExecutorImpl(connectionProviderWithConnection(connection));

        // act
        try
        {
            transactionExecutor.executeInTransaction(toExecute);
            fail("Should have thrown exception");
        }
        catch(RuntimeException thrownException)
        {
            assertEquals(exceptionThrownFromFunction, thrownException);
        }
    }

    private ConnectionProvider connectionProviderWithMockConnection()
    {
        return connectionProviderWithConnection(mock(Connection.class));
    }

    private ConnectionProvider connectionProviderWithConnection(final Connection connection)
    {
        return new AbstractConnectionProvider()
        {
            @Override
            protected Connection getConnectionImpl(final boolean autoCommit)
            {
                return connection;
            }
        };
    }
}