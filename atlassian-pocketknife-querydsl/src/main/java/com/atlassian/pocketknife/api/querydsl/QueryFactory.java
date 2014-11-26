package com.atlassian.pocketknife.api.querydsl;

import com.atlassian.annotations.PublicApi;
import com.atlassian.fugue.Function2;
import com.google.common.base.Function;
import com.mysema.query.Tuple;
import com.mysema.query.sql.RelationalPath;
import com.mysema.query.sql.SQLQuery;
import com.mysema.query.sql.dml.SQLDeleteClause;
import com.mysema.query.sql.dml.SQLInsertClause;
import com.mysema.query.sql.dml.SQLMergeClause;
import com.mysema.query.sql.dml.SQLUpdateClause;

import java.sql.Connection;
import java.util.List;

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
     * Allows you to obtain a SELECT query by asking implicitly for a connection under the covers and creating a {@link
     * com.mysema.query.sql.SQLQuery} you can use.
     * <p/>
     * CONNECTION LIFECYCLE NOTES :
     * <p/>
     * The connection will be returned as soon as this methods completes.
     *
     * @param function the callback function
     * @return then result of using the passed in SELECT query
     */
    <T> T fetch(Function<SQLQuery, T> function);

    /**
     * Allows you to obtain a SELECT query by asking implicitly for a connection and then passing it into the call back
     * function to use.
     * <p/>
     * CONNECTION LIFECYCLE NOTES :
     * <p/>
     * The connection will NOT be returned as soon as this methods completes. Instead the streamy result should be used
     * to control connection lifecycle.
     *
     * @param function the callback function
     * @return then result of using the passed in SELECT query
     */
    StreamyResult select(Function<SelectQuery, StreamyResult> function);

    /**
     * Run the supplied closure with a streamy query then map over the result and return it, this function is not
     * lazy and will execute the method immediately and close the created StreamyResult which means that it will
     * pull all the transformed results into memory.
     *
     * @param closure The closure that will be executed
     * @param <T> The type of List that will be returned
     * @return The result of running the query specified by
     * {@link com.atlassian.pocketknife.api.querydsl.QueryFactory.StreamyFoldClosure#getQuery()}
     * then running the map from
     * {@link com.atlassian.pocketknife.api.querydsl.QueryFactory.StreamyMapClosure#getMapFunction()}
     */
    <T> List<T> streamyMap(StreamyMapClosure<T> closure);

    /**
     * Run the supplied closure with a streamy query then fold over the result and return it, note that due to the way
     * this function is used this is going to pull all the results back into memory.
     *
     * @param initial The initial value to pass to the closure
     * @param closure The closure that will be executed
     * @param <T> The type that is returned by the fold function
     * @return The result of running the query specified by {@link com.atlassian.pocketknife.api.querydsl.QueryFactory.StreamyFoldClosure#getQuery()}
     * then running the fold from {@link com.atlassian.pocketknife.api.querydsl.QueryFactory.StreamyFoldClosure#getFoldFunction()}
     */
    <T> T streamyFold(T initial, StreamyFoldClosure<T> closure);

    /**
     * Returns a INSERT query given the connection and table. Use this when you want to manage the connection yourself.
     *
     * @param connection the connection to use
     * @param table the table to use
     * @return a INSERT query
     */
    SQLInsertClause insert(Connection connection, RelationalPath<?> table);

    /**
     * Allows you to obtain a INSERT query by asking implicitly for a connection and then passing it into the call back
     * function to use. Changes will be automatically committed.
     * <p/>
     * CONNECTION LIFECYCLE NOTES :
     * <p/>
     * The connection will be returned as soon as this methods completes.
     *
     * @param function the callback function
     * @return the result of using the passed in INSERT query
     */
    <T> T insert(RelationalPath<?> table, Function<SQLInsertClause, T> function);

    /**
     * Returns a UPDATE query given the connection and table. Use this when you want to manage the connection yourself.
     *
     * @param connection the connection to use
     * @param table the table to use
     * @return an UPDATE query
     */
    SQLUpdateClause update(Connection connection, RelationalPath<?> table);

    /**
     * Allows you to obtain an UPDATE query by asking implicitly for a connection and then passing it into the call back
     * function to use. Changes will be automatically committed.
     * <p/>
     * CONNECTION LIFECYCLE NOTES :
     * <p/>
     * The connection will be returned as soon as this methods completes.
     *
     * @param table the table to use
     * @param function the callback function
     * @return the result of using the passed in UPDATE query
     */
    <T> T update(RelationalPath<?> table, Function<SQLUpdateClause, T> function);

    /**
     * Returns a DELETE query given the connection and table. Use this when you want to manage the connection yourself.
     *
     * @param connection the connection to use
     * @param table the table to use
     * @return a DELETE query
     */
    SQLDeleteClause delete(Connection connection, RelationalPath<?> table);

    /**
     * Allows you to obtain an DELETE query by asking implicitly for a connection and then passing it into the call back
     * function to use. Changes will be automatically committed.
     * <p/>
     * CONNECTION LIFECYCLE NOTES :
     * <p/>
     * The connection will be returned as soon as this methods completes.
     *
     * @param table the table to use
     * @param function the callback function
     * @return the result of using the passed in DELETE query
     */
    <T> T delete(RelationalPath<?> table, Function<SQLDeleteClause, T> function);

    /**
     * Returns a MERGE query given the connection and table. Use this when you want to manage the connection yourself.
     *
     * @param connection the connection to use
     * @param table the table to use
     * @return a MERGE query
     */
    SQLMergeClause merge(Connection connection, RelationalPath<?> table);

    /**
     * Allows you to obtain an MERGE query by asking implicitly for a connection and then passing it into the call back
     * function to use. Changes will be automatically committed.
     * <p/>
     * CONNECTION LIFECYCLE NOTES :
     * <p/>
     * The connection will be returned as soon as this methods completes.
     *
     * @param table the table to use
     * @param function the callback function
     * @return the result of using the passed in MERGE query
     */
    <T> T merge(RelationalPath<?> table, Function<SQLMergeClause, T> function);

    /**
     * When running a {@link com.atlassian.pocketknife.api.querydsl.StreamyResult} style query you will often end up
     * needing a closure as the mapping files need to be shared between the query block and the processing function.
     * This interface formalises this closure so that the pattern can be easily applied also see
     * {@link #streamyFold(Object, com.atlassian.pocketknife.api.querydsl.QueryFactory.StreamyFoldClosure)}
     */
    static interface StreamyFoldClosure<T>
    {
        /**
         * Returns the query for this closure, typically this will be passed into
         * {@link #select(com.google.common.base.Function)} t} to create the {@link com.atlassian.pocketknife.api.querydsl.StreamyResult}
         *
         * @return A function that can be passed into select to create a {@link com.atlassian.pocketknife.api.querydsl.StreamyResult}
         */
        Function<SelectQuery, StreamyResult> getQuery();

        /**
         * Returns the function that will be folded through the query
         *
         * @return The function that will be passed to
         * {@link com.atlassian.pocketknife.api.querydsl.StreamyResult#foldLeft(Object, com.atlassian.fugue.Function2)}
         */
        Function2<T, Tuple, T> getFoldFunction();
    }

    /**
     * When running a {@link com.atlassian.pocketknife.api.querydsl.StreamyResult} style query you will often end up
     * needing a closure as the mapping files need to be shared between the query block and the processing function.
     * This interface formalises this closure for map so that the pattern can be easily applied also see
     * @{{@link QueryFactory#streamyMap(com.atlassian.pocketknife.api.querydsl.QueryFactory.StreamyMapClosure)}}
     */
    static interface StreamyMapClosure<T>
    {
        /**
         * Returns the query that will be run, this result of this function is typically used to create the 
         * @{@link com.atlassian.pocketknife.api.querydsl.StreamyResult}
         * via a call to {@link #select(com.google.common.base.Function)}
         * @return
         */
        Function<SelectQuery, StreamyResult> getQuery();

        /**
         * The function that will be passed to
         * {@link com.atlassian.pocketknife.api.querydsl.StreamyResult#map(com.google.common.base.Function)}
         * @return
         */
        Function<Tuple, T> getMapFunction();
    }
}
