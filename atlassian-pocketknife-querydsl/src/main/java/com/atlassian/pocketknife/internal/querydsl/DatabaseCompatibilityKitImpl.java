package com.atlassian.pocketknife.internal.querydsl;

import com.atlassian.pocketknife.api.querydsl.Connections;
import com.atlassian.pocketknife.api.querydsl.DatabaseCompatibilityKit;
import com.atlassian.pocketknife.api.querydsl.DialectProvider;
import com.mysema.query.sql.dml.SQLInsertClause;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import static com.atlassian.pocketknife.api.querydsl.DialectProvider.SupportedDatabase.HSQLDB;

@Component
public class DatabaseCompatibilityKitImpl implements DatabaseCompatibilityKit
{
    private final DialectProvider dialectProvider;

    @Autowired
    public DatabaseCompatibilityKitImpl(final DialectProvider dialectProvider)
    {
        this.dialectProvider = dialectProvider;
    }

    public <T> T executeWithKey(final Connection connection, final SQLInsertClause insertClause, final Class<T> idClass)
    {
        if (isHSQLBefore20())
        {
            long howMany = insertClause.execute();
            if (howMany > 0)
            {
                return callHsqlIdentity(connection);
            }
            else
            {
                return null;
            }
        }
        else
        {
            return insertClause.executeWithKey(idClass);
        }
    }

    private boolean isHSQLBefore20()
    {
        DialectProvider.DatabaseInfo databaseInfo = dialectProvider.getDialectConfig().getDatabaseInfo();
        return databaseInfo.getSupportedDatabase() == HSQLDB && databaseInfo.getDatabaseMajorVersion() < 2;
    }

    private static <T> T callHsqlIdentity(final Connection connection)
    {
        PreparedStatement prepareStatement = null;
        ResultSet resultSet = null;
        try
        {
            /*
                 See http://hsqldb.org/doc/guide/builtinfunctions-chapt.html#bfc_system_functions

                 Returns the last IDENTITY value inserted into a row by the current session. The statement, CALL IDENTITY() can be made
                 after an INSERT statement that inserts a row into a table with an IDENTITY column. The CALL IDENTITY() statement
                 returns the last IDENTITY value that was inserted into a table by the current session.

                 Each session manages this function call separately and is not affected by inserts in other sessions.

                 The statement can be executed as a direct statement or a prepared statement.

             */
            prepareStatement = connection.prepareStatement("CALL IDENTITY()");
            resultSet = prepareStatement.executeQuery();
            if (resultSet.next())
            {
                Object idValue = resultSet.getObject(1);
                //noinspection unchecked
                return (T) idValue;
            }
            else
            {
                return null;
            }
        }
        catch (SQLException e)
        {
            throw new RuntimeException(e);
        }
        finally
        {
            Connections.close(resultSet);
            Connections.close(prepareStatement);
        }
    }

}
