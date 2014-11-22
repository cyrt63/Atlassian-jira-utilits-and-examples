package com.atlassian.pocketknife.internal.querydsl;

import com.atlassian.pocketknife.api.querydsl.ClosePromise;
import com.atlassian.pocketknife.api.querydsl.ConnectionProvider;
import com.atlassian.pocketknife.api.querydsl.DialectProvider;
import com.atlassian.pocketknife.api.querydsl.QueryFactory;
import com.atlassian.pocketknife.api.querydsl.SelectQuery;
import com.atlassian.pocketknife.api.querydsl.StreamyResult;
import com.google.common.base.Function;
import com.mysema.query.sql.Configuration;
import com.mysema.query.sql.RelationalPath;
import com.mysema.query.sql.SQLQuery;
import com.mysema.query.sql.dml.SQLDeleteClause;
import com.mysema.query.sql.dml.SQLInsertClause;
import com.mysema.query.sql.dml.SQLMergeClause;
import com.mysema.query.sql.dml.SQLUpdateClause;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.sql.Connection;

/**
 * A factory to give off connected Query objects
 */
@Component
public class QueryFactoryImpl implements QueryFactory
{
    private static final Logger log = Logger.getLogger(QueryFactoryImpl.class);

    private final ConnectionProvider connectionProvider;
    private final DialectProvider dialectProvider;

    @Autowired
    public QueryFactoryImpl(final ConnectionProvider connectionProvider, final DialectProvider dialectProvider)
    {
        this.connectionProvider = connectionProvider;
        this.dialectProvider = dialectProvider;
    }


    private Configuration getConfiguration()
    {
        return dialectProvider.getDialectConfig().getConfiguration();
    }

    /**
     * Returns a SELECT query given the connection
     *
     * @param connection the connection to use
     * @return a SELECT query
     */
    @Override
    public SQLQuery select(Connection connection)
    {
        return new SQLQuery(connection, getConfiguration());
    }

    /**
     * Allows you to obtain a SELECT query by asking implicitly for a connection and then passing it into the call back
     * function to use.
     *
     * @param function the callback function
     * @return then result of using the passed in SELECT query
     */
    @Override
    public <T> T fetch(Function<SQLQuery, T> function)
    {
        Connection connection = connectionProvider.borrowConnection();
        try
        {
            return function.apply(select(connection));
        }
        finally
        {
            connectionProvider.returnConnection(connection);
        }
    }

    @Override
    public StreamyResult select(final Function<SelectQuery, StreamyResult> function)
    {
        final Connection connection = connectionProvider.borrowConnection();
        final ClosePromise closeEffect = returnConnection(connection);
        try
        {
            SQLQuery queryDSLSelect = select(connection);
            SelectQuery select = new SelectQuery(queryDSLSelect, closeEffect);
            return function.apply(select);
        }
        catch (RuntimeException rte)
        {
            closeEffect.close();
            throw rte;
        }
    }

    @Override
    public <T> T streamyFold(StreamyFoldClojure<T> clojure)
    {
        StreamyResult resultStream = select(clojure.query());
        return resultStream.foldLeft(clojure.getInitialValue(), clojure.getFoldFunction());
    }

    private ClosePromise returnConnection(final Connection connection)
    {
        return new ClosePromise()
        {
            @Override
            protected void closeEffect()
            {
                connectionProvider.returnConnection(connection);
            }
        };
    }

    /**
     * Returns a INSERT query given the connection and table. Use this when you want to manage the connection yourself.
     *
     * @param connection the connection to use
     * @param table the table to use
     * @return a INSERT query
     */
    @Override
    public SQLInsertClause insert(Connection connection, RelationalPath<?> table)
    {
        return new SQLInsertClause(connection, getConfiguration(), table);
    }

    /**
     * Allows you to obtain a INSERT query by asking implicitly for a connection and then passing it into the call back
     * function to use. Changes will be automatically committed.
     *
     * @param function the callback function
     * @return the result of using the passed in INSERT query
     */
    @Override
    public <T> T insert(RelationalPath<?> table, Function<SQLInsertClause, T> function)
    {
        Connection connection = connectionProvider.borrowAutoCommitConnection();
        try
        {
            return function.apply(insert(connection, table));
        }
        finally
        {
            connectionProvider.returnConnection(connection);
        }
    }

    /**
     * Returns a UPDATE query given the connection and table. Use this when you want to manage the connection yourself.
     *
     * @param connection the connection to use
     * @param table the table to use
     * @return an UPDATE query
     */
    @Override
    public SQLUpdateClause update(Connection connection, RelationalPath<?> table)
    {
        return new SQLUpdateClause(connection, getConfiguration(), table);
    }

    /**
     * Allows you to obtain an UPDATE query by asking implicitly for a connection and then passing it into the call back
     * function to use. Changes will be automatically committed.
     *
     * @param table the table to use
     * @param function the callback function
     * @return the result of using the passed in UPDATE query
     */
    @Override
    public <T> T update(RelationalPath<?> table, Function<SQLUpdateClause, T> function)
    {
        Connection connection = connectionProvider.borrowAutoCommitConnection();
        try
        {
            return function.apply(update(connection, table));
        }
        finally
        {
            connectionProvider.returnConnection(connection);
        }
    }

    /**
     * Returns a DELETE query given the connection and table. Use this when you want to manage the connection yourself.
     *
     * @param connection the connection to use
     * @param table the table to use
     * @return a DELETE query
     */
    @Override
    public SQLDeleteClause delete(Connection connection, RelationalPath<?> table)
    {
        return new SQLDeleteClause(connection, getConfiguration(), table);
    }

    /**
     * Allows you to obtain an DELETE query by asking implicitly for a connection and then passing it into the call back
     * function to use. Changes will be automatically committed.
     *
     * @param table the table to use
     * @param function the callback function
     * @return the result of using the passed in DELETE query
     */
    @Override
    public <T> T delete(RelationalPath<?> table, Function<SQLDeleteClause, T> function)
    {
        Connection connection = connectionProvider.borrowAutoCommitConnection();
        try
        {
            return function.apply(delete(connection, table));
        }
        finally
        {
            connectionProvider.returnConnection(connection);
        }
    }

    /**
     * Returns a MERGE query given the connection and table. Use this when you want to manage the connection yourself.
     *
     * @param connection the connection to use
     * @param table the table to use
     * @return a MERGE query
     */
    @Override
    public SQLMergeClause merge(Connection connection, RelationalPath<?> table)
    {
        return new SQLMergeClause(connection, getConfiguration(), table);
    }

    /**
     * Allows you to obtain an MERGE query by asking implicitly for a connection and then passing it into the call back
     * function to use. Changes will be automatically committed.
     *
     * @param table the table to use
     * @param function the callback function
     * @return the result of using the passed in MERGE query
     */
    @Override
    public <T> T merge(RelationalPath<?> table, Function<SQLMergeClause, T> function)
    {
        Connection connection = connectionProvider.borrowAutoCommitConnection();
        try
        {
            return function.apply(merge(connection, table));
        }
        finally
        {
            connectionProvider.returnConnection(connection);
        }
    }
}
