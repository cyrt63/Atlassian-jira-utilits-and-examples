package com.atlassian.pocketknife.internal.querydsl;

import com.atlassian.pocketknife.api.querydsl.ConnectionProvider;
import com.atlassian.pocketknife.api.querydsl.TransactionalExecutor;
import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Function;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.sql.Connection;
import java.sql.SQLException;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import static com.atlassian.util.concurrent.Assertions.notNull;

@Component
public final class TransactionExecutorImpl implements TransactionalExecutor
{
    private static final Logger log = LoggerFactory.getLogger(TransactionExecutorImpl.class);

    private final ConnectionProvider connectionProvider;

    @Autowired
    public TransactionExecutorImpl(final ConnectionProvider connectionProvider)
    {
        this.connectionProvider = connectionProvider;
    }

    @Override
    public <T> T executeInTransaction(@Nonnull final Function<Connection, T> toExecute)
    {
        notNull("Function to execute is required", toExecute);

        return connectionProvider.withConnection(
                InTransactionExecutor.withFunction(toExecute)
        );
    }

    static class InTransactionExecutor<T> implements Function<Connection, T>
    {
        private Function<Connection, T> toExecute;

        private InTransactionExecutor(final Function<Connection, T> toExecute)
        {
            notNull("toExecute is required", toExecute);
            this.toExecute = toExecute;
        }

        static <T> InTransactionExecutor<T> withFunction(Function<Connection, T> toExecute)
        {
            return new InTransactionExecutor<T>(toExecute);
        }

        @Override
        public T apply(@Nullable final Connection connection)
        {
            T result;
            try
            {
                result = toExecute.apply(connection);
            }
            catch(RuntimeException exceptionThrownFromFunction)
            {
                rollback(connection);

                throw exceptionThrownFromFunction;
            }

            commit(connection);

            return result;
        }

        private void commit(final Connection connection)
        {
            try
            {
                connection.commit();
            }
            catch(SQLException commitException)
            {
                throw new RuntimeException("Unable to commit", commitException);
            }
        }

        private void rollback(final Connection connection)
        {
            try
            {
                connection.rollback();
            }
            catch (SQLException rollbackException)
            {
                log.error("Unable to rollback connection: {}", rollbackException.getMessage());
                // Swallow this as we want to rethrow the original exception
            }
        }

        @VisibleForTesting
        Function<Connection, T> getFunctionToExecute()
        {
            return toExecute;
        }
    }
}
