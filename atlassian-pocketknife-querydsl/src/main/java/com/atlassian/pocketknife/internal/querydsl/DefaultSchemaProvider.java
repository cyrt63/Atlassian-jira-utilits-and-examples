package com.atlassian.pocketknife.internal.querydsl;

import com.atlassian.pocketknife.api.querydsl.ConnectionProvider;
import com.atlassian.pocketknife.api.querydsl.SchemaProvider;
import com.google.common.base.Supplier;
import com.google.common.base.Suppliers;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import javax.annotation.Nonnull;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;
import static org.apache.commons.lang.StringUtils.isEmpty;

@Component
public class DefaultSchemaProvider implements SchemaProvider
{
    private static final String SCHEMA_NAME_KEY = "TABLE_SCHEM";
    private static final String TABLE_NAME_KEY = "TABLE_NAME";
    private static final String COLUMN_NAME_KEY = "COLUMN_NAME";

    private final ConnectionProvider connectionProvider;
    private final Supplier<Map<String, String>> tableToSchema;
    private final Supplier<Map<NameKey, String>> tableColumnNames;

    @Autowired
    public DefaultSchemaProvider(final ConnectionProvider connectionProvider)
    {
        this.connectionProvider = connectionProvider;
        this.tableToSchema = Suppliers.memoize(tableToSchemaSupplier());
        this.tableColumnNames = Suppliers.memoize(tableColumnsNamesSupplier());
    }

    @Override
    public String getSchema(@Nonnull final String tableName)
    {
        checkArgument(!isEmpty(tableName), "Table name is required");

        final String upperName = tableName.toUpperCase();
        if (tableToSchema.get().containsKey(upperName))
        {
            return tableToSchema.get().get(upperName);
        }

        throw new IllegalArgumentException(String.format("Not able to find table %s", tableName));
    }

    @Override
    public String getTableName(@Nonnull final String tableName)
    {
        checkArgument(!isEmpty(tableName), "Table name is required");

        NameKey key = new NameKey(tableName);
        return tableColumnNames.get().get(key);
    }

    @Override
    public String getColumnName(@Nonnull final String tableName, @Nonnull final String columnName)
    {
        checkArgument(!isEmpty(tableName), "Table name is required");
        checkArgument(!isEmpty(columnName), "Column name is required");

        NameKey key = new NameKey(tableName, columnName);
        return tableColumnNames.get().get(key);
    }

    private Supplier<Map<String, String>> tableToSchemaSupplier()
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
                            final String tableName = resultSet.getString(TABLE_NAME_KEY);
                            final String tableSchema = resultSet.getString(SCHEMA_NAME_KEY);
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

    private Supplier<Map<NameKey, String>> tableColumnsNamesSupplier()
    {
        return new Supplier<Map<NameKey, String>>()
        {
            @Override
            public Map<NameKey, String> get()
            {
                final Map<NameKey, String> result = new HashMap<NameKey, String>();
                Connection connection = connectionProvider.borrowConnection();
                try
                {
                    final DatabaseMetaData metaData = connection.getMetaData();

                    findTableNames(metaData, result);
                    findColumnNames(metaData, result);

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

    private void findColumnNames(final DatabaseMetaData metaData, final Map<NameKey, String> result) throws SQLException
    {
        final ResultSet columnResultSet = metaData.getColumns(null, null, null, null);
        try
        {
            while (columnResultSet.next())
            {
                final String tableName = columnResultSet.getString(TABLE_NAME_KEY);
                final String columnName = columnResultSet.getString(COLUMN_NAME_KEY);
                result.put(new NameKey(tableName, columnName), columnName);
            }
        }
        finally
        {
            columnResultSet.close();
        }
    }

    private void findTableNames(final DatabaseMetaData metaData, final Map<NameKey, String> result) throws SQLException
    {
        final ResultSet resultSet = metaData.getTables(null, null, null, null);
        try
        {
            while (resultSet.next())
            {
                final String tableName = resultSet.getString(TABLE_NAME_KEY);
                result.put(new NameKey(tableName), tableName);
            }
        }
        finally
        {
            resultSet.close();
        }
    }

    private static class NameKey
    {
        private final String tableName;
        private final String columnName;


        private NameKey(@Nonnull final String tableName)
        {
            this(tableName, null);
        }

        private NameKey(@Nonnull final String tableName, final String columnName)
        {

            this.tableName = checkNotNull(tableName).toUpperCase();
            this.columnName = columnName == null ? null : columnName.toUpperCase();
        }

        @Override
        public boolean equals(final Object obj)
        {
            if (this == obj) return true;
            if (obj == null || obj.getClass() != getClass()) return false;

            NameKey other = (NameKey)obj;

            if (!tableName.equals(other.tableName)) return false;
            return columnName == null ? (other.columnName == null) : columnName.equals(other.columnName);
        }

        @Override
        public int hashCode()
        {
            int result = tableName.hashCode();
            result = 31 * result + (columnName != null ? columnName.hashCode() : 0);
            return result;
        }
    }

}
