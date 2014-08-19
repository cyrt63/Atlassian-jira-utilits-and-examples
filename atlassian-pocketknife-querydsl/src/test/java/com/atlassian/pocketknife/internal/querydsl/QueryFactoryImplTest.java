package com.atlassian.pocketknife.internal.querydsl;

import com.atlassian.pocketknife.api.querydsl.DialectProvider;
import com.atlassian.pocketknife.api.querydsl.QueryFactory;
import com.atlassian.pocketknife.spi.querydsl.DialectConfiguration;
import com.google.common.base.Function;
import com.mysema.query.sql.Configuration;
import com.mysema.query.sql.PostgresTemplates;
import com.mysema.query.sql.SQLQuery;
import com.mysema.query.sql.SQLTemplates;
import org.hamcrest.Matchers;
import org.junit.Before;
import org.junit.Test;

import java.sql.Connection;
import javax.annotation.Nullable;

import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class QueryFactoryImplTest
{
    private final Object WELL_KNOWN_VALUE = new Object();

    private MockConnectionProvider connectionProvider;
    private DialectConfiguration dialectConfiguration;
    private Connection connection;
    private QueryFactory queryFactory;

    @Before
    public void setUp() throws Exception
    {
        SQLTemplates sqlTemplates = PostgresTemplates.builder().build();
        DialectProvider.Config config = new DialectProvider.Config(sqlTemplates, new Configuration(sqlTemplates));

        dialectConfiguration = mock(DialectConfiguration.class);
        when(dialectConfiguration.getDialectConfig()).thenReturn(config);

        connection = mock(Connection.class);
        connectionProvider = new MockConnectionProvider(connection);
        queryFactory = new QueryFactoryImpl(connectionProvider, dialectConfiguration);
    }

    @Test
    public void testSelect() throws Exception
    {
        SQLQuery select = queryFactory.select(connection);
        assertThat(select, Matchers.notNullValue());
    }

    @Test
    public void testSelectBorrowingAndCleanup() throws Exception
    {
        Object returnValue = queryFactory.fetch(new Function<SQLQuery, Object>()
        {
            @Override
            public Object apply(@Nullable final SQLQuery select)
            {
                assertThat(select, Matchers.notNullValue());
                return WELL_KNOWN_VALUE;
            }
        });
        assertThat(returnValue, Matchers.sameInstance(WELL_KNOWN_VALUE));

        assertThat(connectionProvider.getBorrowCount(), Matchers.equalTo(0));
    }

    @Test (expected = IllegalStateException.class)
    public void testSelectBorrowingAndCleanupWithException() throws Exception
    {
        try
        {
            queryFactory.fetch(new Function<SQLQuery, Object>()
            {
                @Override
                public Object apply(@Nullable final SQLQuery select)
                {
                    assertThat(select, Matchers.notNullValue());
                    throw new IllegalStateException();
                }
            });
        }
        finally
        {
            // and did they put things back after the end
            assertThat(connectionProvider.getBorrowCount(), Matchers.equalTo(0));
        }
    }
}

