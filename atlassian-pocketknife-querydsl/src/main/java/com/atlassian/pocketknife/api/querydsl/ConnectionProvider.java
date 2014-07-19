package com.atlassian.pocketknife.api.querydsl;

import com.atlassian.annotations.PublicApi;
import com.google.common.base.Function;

import java.sql.Connection;

/**
 */
@PublicApi
public interface ConnectionProvider
{
    /**
     * @return a borrowed connection that does not have auto commit set on it
     */
    Connection borrowConnection();

    /**
     * @return a borrowed connection that does has auto commit set on it
     */
    Connection borrowAutoCommitConnection();

    /**
     * @param connection a a previous borrowed connection
     */
    void returnConnection(Connection connection);

    /**
     * Obtains a connection without auto-commit, does the work and returns the connection implicitly
     *
     * @param callback the code to call with the connection
     * @param <T> the type to return as a value
     * @return T the result
     */
    <T> T withConnection(Function<Connection, T> callback);
}
