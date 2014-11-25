package com.atlassian.pocketknife.spi.querydsl;

import com.atlassian.pocketknife.api.querydsl.SchemaProvider;
import com.mysema.query.sql.RelationalPathBase;
import com.mysema.query.sql.SchemaAndTable;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

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
            final String variable,
            final String table)
    {
        super(type, variable, "", table);
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


    /**
     * Allows access to a Spring-wired {@link com.atlassian.pocketknife.api.querydsl.SchemaProvider} from a static context.
     *
     */
    @Component
    private static final class SchemaProviderAccessor implements InitializingBean, DisposableBean
    {
        private static SchemaProviderAccessor instance = null;

        @Autowired
        SchemaProvider schemaProvider;

        @Override
        public void afterPropertiesSet() throws Exception
        {
            instance = this;
        }

        @Override
        public void destroy() throws Exception
        {
            instance = null;
        }

        public static SchemaProvider getSchemaProvider()
        {
            if(instance == null)
            {
                throw new IllegalStateException("You have called this method too early. Spring has not autowired it yet.");
            }

            return instance.schemaProvider;
        }
    }
}
