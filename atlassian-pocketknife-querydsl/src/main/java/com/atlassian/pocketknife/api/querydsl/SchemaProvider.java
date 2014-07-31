package com.atlassian.pocketknife.api.querydsl;

import com.atlassian.annotations.PublicApi;

@PublicApi
public interface SchemaProvider
{
    /**
     * Support to retrieve schema name of the given table
     *
     * @param tableName name of the table
     * @return schema name
     */
    String getSchema(String tableName);
}
