package com.atlassian.pocketknife.api.querydsl;

import com.atlassian.annotations.PublicApi;
import com.google.common.base.Function;

import java.sql.Connection;
import javax.annotation.Nonnull;

/**
 * Provides functionality to execute {@link com.google.common.base.Function}s within a database transaction, with
 * connection retrieval, commit and rollback operations taken care of.
 *
 */
@PublicApi
public interface TransactionalExecutor
{
    /**
     * Executes the provided {@code function} within a new transaction. Connection retrieval is taken care of
     * implicitly.
     * <p/>
     * The transaction will be committed immediately after the function completes. The transaction will be rolled back
     * if an exception is thrown during the execution of the function.
     * <p/>
     * The {@link java.sql.Connection} is provided to the function for convenience (for example, to allow the function
     * to commit in between operations).
     *
     * @param function what will be executed in a new transaction
     * @param <T> the return type of the function, may be {@link java.lang.Void}
     * @throws java.lang.RuntimeException on failure to commit or roll back the transaction, or if the supplied
     *         {@code function} throws an exception
     * @return the result of the function
     */
    <T> T executeInTransaction(@Nonnull Function<Connection, T> function);
}
