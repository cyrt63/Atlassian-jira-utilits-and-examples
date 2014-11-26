package com.atlassian.pocketknife.spi.querydsl;

import com.atlassian.pocketknife.internal.querydsl.SchemaProviderAccessor;
import com.mysema.query.sql.RelationalPathBase;
import com.mysema.query.sql.SchemaAndTable;

/**
 * * QueryDSL table classes that extend this class can be instantiated without specifying a schema.
 * <p/>
 * This allows them to be declared as static variables and used at call sites and yet still have the capability of
 * knowing the underlying schema name which is not known until runtime.
 */
public abstract class RelationalBasePathWithDynamicSchema<T> extends RelationalPathBase<T>
{
    public RelationalBasePathWithDynamicSchema(
            final Class<? extends T> type,
            final String tableName)
    {
        super(type, tableName, "", tableName);
    }

    @Override
    public SchemaAndTable getSchemaAndTable()
    {
        return new SchemaAndTable(
                getSchemaName(),
                getTableName()
        );
    }

    @Override
    public String getSchemaName()
    {
        return SchemaProviderAccessor.getSchemaProvider().getSchema(getTableName());
    }


}
