package com.atlassian.pocketknife.spi.querydsl;

import com.atlassian.pocketknife.api.querydsl.DialectProvider;
import com.atlassian.pocketknife.internal.querydsl.MockConnectionProvider;
import com.atlassian.util.concurrent.LazyReference;
import com.mysema.query.sql.OracleTemplates;
import com.mysema.query.sql.PostgresTemplates;
import com.mysema.query.sql.SQLTemplates;
import org.hamcrest.Matchers;
import org.junit.Before;
import org.junit.Test;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.SQLException;
import java.util.LinkedHashMap;
import java.util.Map;

import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class DefaultDialectConfigurationTest
{

    private DefaultDialectConfiguration dialectConfiguration;
    private MockConnectionProvider connectionProvider;
    private DatabaseMetaData databaseMetaData;

    @Before
    public void setUp() throws Exception
    {
        final Connection connection = mock(Connection.class);

        connectionProvider = new MockConnectionProvider(connection);
        databaseMetaData = mock(DatabaseMetaData.class);
        when(connection.getMetaData()).thenReturn(databaseMetaData);
    }

    /**
     * As we build out our database support we should expand this class
     */
    @Test
    public void testDatabaseDetectionCoverage() throws Exception
    {
        Map<String, Class> support = new LinkedHashMap<String, Class>();
        support.put("PostgreSQL", PostgresTemplates.Builder.class);
        support.put("Oracle", OracleTemplates.Builder.class);

        for (String databaseName : support.keySet())
        {
            assertDatabaseType(support.get(databaseName), databaseName);
        }
    }

    @Test (expected = LazyReference.InitializationException.class)
    public void testDatabaseDetectionUnknownDB() throws Exception
    {
        assertDatabaseType(OracleTemplates.Builder.class, "SomeDatabaseWeDon'tSupport");
    }

    @Test
    public void testDatabaseDetectionForSideEffect() throws Exception
    {
        assertDatabaseType(PostgresTemplates.Builder.class, "PostgreSQL");
        assertThat(dialectConfiguration.getDialectConfig(), Matchers.notNullValue());
    }

    private void assertDatabaseType(final Class builderClass, String databaseName)
            throws SQLException
    {
        dialectConfiguration = new DefaultDialectConfiguration(connectionProvider)
        {
            @Override
            public SQLTemplates.Builder enrich(final SQLTemplates.Builder builder)
            {
                assertThat(builder, Matchers.instanceOf(builderClass));
                return super.enrich(builder);
            }
        };
        when(databaseMetaData.getDatabaseProductName()).thenReturn(databaseName);
        DialectProvider.Config config = dialectConfiguration.getDialectConfig();

        assertThat(config.getSqlTemplates().isUseQuotes(), Matchers.equalTo(true));
    }
}