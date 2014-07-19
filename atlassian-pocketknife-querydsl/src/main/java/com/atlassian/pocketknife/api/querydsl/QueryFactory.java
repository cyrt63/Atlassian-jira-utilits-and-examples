package com.atlassian.pocketknife.api.querydsl;

import com.atlassian.annotations.PublicApi;
import com.google.common.base.Function;
import com.mysema.query.sql.RelationalPath;
import com.mysema.query.sql.SQLQuery;
import com.mysema.query.sql.dml.SQLDeleteClause;
import com.mysema.query.sql.dml.SQLInsertClause;
import com.mysema.query.sql.dml.SQLMergeClause;
import com.mysema.query.sql.dml.SQLUpdateClause;

import java.sql.Connection;

/**
 * QueryFactory gives of QueryDSL objects, connected to database {@link java.sql.Connection}s
 */
@PublicApi
public interface QueryFactory
{
    /**
     * Returns a SELECT query given the connection
     *
     * @param connection the connection to use
     * @return a SELECT query
     */
    SQLQuery select(Connection connection);

    /**
     * Allows you to obtain a SELECT query by asking implicitly for a connection and then passing it into the call back
     * function to use.
     *
     * @param function the callback function
     * @return then result of using the passed in SELECT query
     */
    <T> T select(Function<SQLQuery, T> function);

    /**
     * Returns a INSERT query given the connection and table
     *
     * @param connection the connection to use
     * @param table the table to use
     * @return a INSERT query
     */
    SQLInsertClause insert(Connection connection, RelationalPath<?> table);

    /**
     * Allows you to obtain a INSERT query by asking implicitly for a connection and then passing it into the call back
     * function to use.
     *
     * @param function the callback function
     * @return the result of using the passed in INSERT query
     */
    <T> T insert(RelationalPath<?> table, Function<SQLInsertClause, T> function);

    /**
     * Returns a UPDATE query given the connection and table
     *
     * @param connection the connection to use
     * @param table the table to use
     * @return an UPDATE query
     */
    SQLUpdateClause update(Connection connection, RelationalPath<?> table);

    /**
     * Allows you to obtain an UPDATE query by asking implicitly for a connection and then passing it into the call back
     * function to use.
     *
     * @param table the table to use
     * @param function the callback function
     * @return the result of using the passed in UPDATE query
     */
    <T> T update(RelationalPath<?> table, Function<SQLUpdateClause, T> function);

    /**
     * Returns a DELETE query given the connection and table
     *
     * @param connection the connection to use
     * @param table the table to use
     * @return a DELETE query
     */
    SQLDeleteClause delete(Connection connection, RelationalPath<?> table);

    /**
     * Allows you to obtain an DELETE query by asking implicitly for a connection and then passing it into the call back
     * function to use.
     *
     * @param table the table to use
     * @param function the callback function
     * @return the result of using the passed in DELETE query
     */
    <T> T delete(RelationalPath<?> table, Function<SQLDeleteClause, T> function);

    /**
     * Returns a MERGE query given the connection and table
     *
     * @param connection the connection to use
     * @param table the table to use
     * @return a MERGE query
     */
    SQLMergeClause merge(Connection connection, RelationalPath<?> table);

    /**
     * Allows you to obtain an MERGE query by asking implicitly for a connection and then passing it into the call back
     * function to use.
     *
     * @param table the table to use
     * @param function the callback function
     * @return the result of using the passed in MERGE query
     */
    <T> T merge(RelationalPath<?> table, Function<SQLMergeClause, T> function);
}
