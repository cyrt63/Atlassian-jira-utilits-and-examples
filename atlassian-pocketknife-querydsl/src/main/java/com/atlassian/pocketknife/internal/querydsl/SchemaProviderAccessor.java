package com.atlassian.pocketknife.internal.querydsl;

import com.atlassian.pocketknife.api.querydsl.SchemaProvider;
import com.atlassian.pocketknife.spi.querydsl.RelationalBasePathWithDynamicSchema;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Allows access to a Spring-wired {@link com.atlassian.pocketknife.api.querydsl.SchemaProvider} from a static context.
 *
 */
@Component
public final class SchemaProviderAccessor implements InitializingBean, DisposableBean
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
