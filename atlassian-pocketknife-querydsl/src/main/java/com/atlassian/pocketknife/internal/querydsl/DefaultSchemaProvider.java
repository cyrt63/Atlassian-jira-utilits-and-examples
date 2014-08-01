package com.atlassian.pocketknife.internal.querydsl;

import com.atlassian.pocketknife.api.querydsl.ConnectionProvider;
import com.atlassian.pocketknife.api.querydsl.SchemaProvider;
import com.google.common.base.Supplier;
import com.google.common.base.Suppliers;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

@Component
public class DefaultSchemaProvider implements SchemaProvider
{
    private final ConnectionProvider connectionProvider;
    private final Supplier<Map<String, String>> allTables;

    @Autowired
    public DefaultSchemaProvider(final ConnectionProvider connectionProvider)
    {
        this.connectionProvider = connectionProvider;
        this.allTables = Suppliers.memoize(allTableSupplier());
    }

    @Override
    public String getSchema(String tableName)
    {
        if (StringUtils.isEmpty(tableName))
        {
            throw new IllegalArgumentException("Need a table name");
        }

        final String upperName = tableName.toUpperCase();
        if (allTables.get().containsKey(upperName))
        {
            return allTables.get().get(upperName);
        }

        throw new IllegalArgumentException(String.format("Not able to find table %s", tableName));
    }

    private Supplier<Map<String, String>> allTableSupplier()
    {
        return new Supplier<Map<String, String>>()
        {
            @Override
            public Map<String, String> get()
            {
                final Map<String, String> result = new HashMap<String, String>();
                Connection connection = connectionProvider.borrowConnection();
                try
                {
                    final ResultSet resultSet = connection.getMetaData().getTables(null, null, null, null);
                    try
                    {
                        while (resultSet.next())
                        {
                            final String tableName = resultSet.getString("TABLE_NAME");
                            final String tableSchema = resultSet.getString("TABLE_SCHEM");
                            String schemaName = "";
                            if (StringUtils.isNotEmpty(tableSchema))
                            {
                                schemaName = tableSchema;
                            }
                            result.put(tableName.toUpperCase(), schemaName);
                        }
                    }
                    finally
                    {
                        resultSet.close();
                    }
                }
                catch (SQLException ex)
                {
                    throw new RuntimeException("Unable to enquire table names available in the system");
                }
                finally
                {
                    connectionProvider.returnConnection(connection);
                }

                return result;
            }
        };
    }

}
