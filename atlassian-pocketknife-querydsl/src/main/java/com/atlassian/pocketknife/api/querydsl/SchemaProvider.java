package com.atlassian.pocketknife.api.querydsl;

import com.atlassian.annotations.PublicApi;

/**
 * A helper that can be used to provide database names (schema, table, column) at runtime.
 *
 * Useful for retrieving names in environments where case changes between database engines,
 * or where names aren't known ahead of time.
 */
@PublicApi
public interface SchemaProvider
{
    /**
     * Support to retrieve schema name of the given table
     *
     * @param tableName name of the table
     *
     * @return the schema name associated to the table, or empty if none is available
     */
    String getSchema(String tableName);

    /**
     * Retrieve the case-sensitive table name of the actual table that matches the given table name (if it exists)
     *
     * @param tableName The table to lookup
     *
     * @return The actual (case-sensitive) table name for the given table, or <code>null</code> if none exists.
     */
    String getTableName(String tableName);

    /**
     * Retrieve the case-sensitive column name of the column in the given table (if it exists)
     *
     * @param tableName The table to for columns in
     * @param columnName The column to lookup
     *
     * @return The actual (case-sensitive) column name for the column in the given table,
     * or <code>null</code> if none is found.
     */
    String getColumnName(String tableName, String columnName);
}
