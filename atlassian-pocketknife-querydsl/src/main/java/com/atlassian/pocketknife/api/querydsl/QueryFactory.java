package com.atlassian.pocketknife.api.querydsl;

import com.atlassian.util.concurrent.LazyReference;
import com.google.common.base.Function;
import com.mysema.query.sql.Configuration;
import com.mysema.query.sql.RelationalPath;
import com.mysema.query.sql.SQLQuery;
import com.mysema.query.sql.dml.SQLDeleteClause;
import com.mysema.query.sql.dml.SQLInsertClause;
import com.mysema.query.sql.dml.SQLMergeClause;
import com.mysema.query.sql.dml.SQLUpdateClause;
import org.apache.log4j.Logger;
import org.springframework.stereotype.Component;

import java.sql.Connection;

/**
 * A factory to give off connected Query objects
 */
@Component
public class QueryFactory
{
    private static final Logger log = Logger.getLogger(QueryFactory.class);

    private final ConnectionProvider connectionProvider;
    private final DialectConfiguration dialectConfiguration;
    private final LazyReference<DialectProvider.Config> ref = new LazyReference<DialectProvider.Config>()
    {
        @Override
        protected DialectProvider.Config create() throws Exception
        {
            return connectionProvider.withConnection(new Function<Connection, DialectProvider.Config>()
            {
                @Override
                public DialectProvider.Config apply(final Connection input)
                {
                    return dialectConfiguration.detect(input);
                }
            });
        }
    };

    public QueryFactory(final ConnectionProvider connectionProvider, final DialectConfiguration dialectConfiguration)
    {
        this.connectionProvider = connectionProvider;
        this.dialectConfiguration = dialectConfiguration;
    }


    private Configuration getConfiguration()
    {
        return ref.get().getConfiguration();
    }

    /**
     * Returns a SELECT query given the connection
     *
     * @param connection the connection to use
     * @return a SELECT query
     */
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
    public <T> T select(Function<SQLQuery, T> function)
    {
        Connection connection = connectionProvider.borrowConnection();
        try
        {
            return function.apply(select(connection));
        }
        finally
        {
            Connections.close(connection);
        }
    }

    /**
     * Returns a INSERT query given the connection and table
     *
     * @param connection the connection to use
     * @param table the table to use
     * @return a INSERT query
     */
    public SQLInsertClause insert(Connection connection, RelationalPath<?> table)
    {
        return new SQLInsertClause(connection, getConfiguration(), table);
    }

    /**
     * Allows you to obtain a INSERT query by asking implicitly for a connection and then passing it into the call back
     * function to use.
     *
     * @param function the callback function
     * @return the result of using the passed in INSERT query
     */
    public <T> T insert(RelationalPath<?> table, Function<SQLInsertClause, T> function)
    {
        Connection connection = connectionProvider.borrowConnection();
        try
        {
            return function.apply(insert(connection, table));
        }
        finally
        {
            Connections.close(connection);
        }
    }

    /**
     * Returns a UPDATE query given the connection and table
     *
     * @param connection the connection to use
     * @param table the table to use
     * @return an UPDATE query
     */
    public SQLUpdateClause update(Connection connection, RelationalPath<?> table)
    {
        return new SQLUpdateClause(connection, getConfiguration(), table);
    }

    /**
     * Allows you to obtain an UPDATE query by asking implicitly for a connection and then passing it into the call back
     * function to use.
     *
     * @param table the table to use
     * @param function the callback function
     * @return the result of using the passed in UPDATE query
     */
    public <T> T update(RelationalPath<?> table, Function<SQLUpdateClause, T> function)
    {
        Connection connection = connectionProvider.borrowConnection();
        try
        {
            return function.apply(update(connection, table));
        }
        finally
        {
            Connections.close(connection);
        }
    }

    /**
     * Returns a DELETE query given the connection and table
     *
     * @param connection the connection to use
     * @param table the table to use
     * @return a DELETE query
     */
    public SQLDeleteClause delete(Connection connection, RelationalPath<?> table)
    {
        return new SQLDeleteClause(connection, getConfiguration(), table);
    }

    /**
     * Allows you to obtain an DELETE query by asking implicitly for a connection and then passing it into the call back
     * function to use.
     *
     * @param table the table to use
     * @param function the callback function
     * @return the result of using the passed in DELETE query
     */
    public <T> T delete(RelationalPath<?> table, Function<SQLDeleteClause, T> function)
    {
        Connection connection = connectionProvider.borrowConnection();
        try
        {
            return function.apply(delete(connection, table));
        }
        finally
        {
            Connections.close(connection);
        }
    }

    /**
     * Returns a MERGE query given the connection and table
     *
     * @param connection the connection to use
     * @param table the table to use
     * @return a MERGE query
     */
    public SQLMergeClause merge(Connection connection, RelationalPath<?> table)
    {
        return new SQLMergeClause(connection, getConfiguration(), table);
    }

    /**
     * Allows you to obtain an MERGE query by asking implicitly for a connection and then passing it into the call back
     * function to use.
     *
     * @param table the table to use
     * @param function the callback function
     * @return the result of using the passed in MERGE query
     */
    public <T> T merge(RelationalPath<?> table, Function<SQLMergeClause, T> function)
    {
        Connection connection = connectionProvider.borrowConnection();
        try
        {
            return function.apply(merge(connection, table));
        }
        finally
        {
            Connections.close(connection);
        }
    }

}
