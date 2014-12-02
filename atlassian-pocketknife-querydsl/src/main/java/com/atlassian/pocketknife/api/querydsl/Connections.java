package com.atlassian.pocketknife.api.querydsl;

import com.atlassian.annotations.PublicApi;
import org.apache.log4j.Logger;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * Helper functions around closing connections
 */
@PublicApi
public class Connections
{
    private static final Logger log = Logger.getLogger(Connections.class);

    /**
     * Closes a connection with null checks and without throwing an exception of the is a problem
     *
     * @param connection the connection in play
     */
    public static void close(Connection connection)
    {
        try
        {
            if (connection != null)
            {
                log.debug("Closing connection...");

                connection.close();

                log.debug("Closed connection");
            }
        }
        catch (SQLException e)
        {
            log.warn("Unable to close SQL connection " + e.getMessage());
        }
    }


    /**
     * Closes a statement with null checks and without throwing an exception of the is a problem
     *
     * @param statement the statement in play
     */
    public static void close(Statement statement)
    {
        try
        {
            if (statement != null)
            {
                log.debug("Closing statement...");

                statement.close();

                log.debug("Closed statement");
            }
        }
        catch (SQLException e)
        {
            log.warn("Unable to close SQL statement " + e.getMessage());
        }
    }

    /**
     * Closes a result set with null checks and without throwing an exception of the is a problem
     *
     * @param resultSet the result set in play
     */
    public static void close(ResultSet resultSet)
    {
        try
        {
            if (resultSet != null)
            {
                log.debug("Closing result set...");

                resultSet.close();

                log.debug("Closed result set");
            }
        }
        catch (SQLException e)
        {
            log.warn("Unable to close SQL result set " + e.getMessage());
        }
    }

}
