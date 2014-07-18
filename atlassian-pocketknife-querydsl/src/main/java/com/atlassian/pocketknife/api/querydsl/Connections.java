package com.atlassian.pocketknife.api.querydsl;

import org.apache.log4j.Logger;

import java.sql.Connection;
import java.sql.SQLException;

/**
 */
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
                connection.close();
            }
        }
        catch (SQLException e)
        {
            log.warn("Unable to close SQL connection " + e.getMessage());
        }
    }

}
