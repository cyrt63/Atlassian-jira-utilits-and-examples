package com.atlassian.pocketknife.api.querydsl;

import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Allows access to a Spring-wired {@link SchemaProviderAccessor} bean from a static context.
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

    private static SchemaProvider getSchemaProvider()
    {
        if(instance == null)
        {
            throw new IllegalStateException("You have called this method too early. Spring has not autowired it yet.");
        }

        return instance.schemaProvider;
    }
}
