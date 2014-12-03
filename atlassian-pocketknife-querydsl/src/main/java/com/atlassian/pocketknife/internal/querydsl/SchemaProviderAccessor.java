package com.atlassian.pocketknife.internal.querydsl;

import com.atlassian.pocketknife.api.querydsl.SchemaProvider;
import com.google.common.annotations.VisibleForTesting;
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

    /**
     * <em>Not to be used with production code.</em> Mimics the Spring field injection and
     * {@code afterPropertiesSet()} call to initialize SchemaProviderAccessor with the supplied
     * {@code providerToInitializeWith}.
     * <p/>
     * This enables SchemaProviderAccessor to be used with unit tests, allowing a
     * {@link com.atlassian.pocketknife.api.querydsl.SchemaProvider} to be injected in.
     */
    @VisibleForTesting
    public static void initializeWithSchemaProvider(final SchemaProvider providerToInitializeWith)
    {
        SchemaProviderAccessor newInstance = new SchemaProviderAccessor();
        newInstance.schemaProvider = providerToInitializeWith;
        instance = newInstance;
    }
}
