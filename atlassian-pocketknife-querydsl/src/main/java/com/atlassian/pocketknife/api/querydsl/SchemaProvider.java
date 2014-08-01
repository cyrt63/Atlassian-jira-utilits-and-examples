package com.atlassian.pocketknife.api.querydsl;

import com.atlassian.annotations.PublicApi;

@PublicApi
public interface SchemaProvider
{
    /**
     * Support to retrieve schema name of the given table
     *
     * @param tableName name of the table
     * @return the schema name associated to the table, or empty if none is available
     */
    String getSchema(String tableName);
}
