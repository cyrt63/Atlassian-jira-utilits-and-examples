package com.atlassian.pocketknife.spi.querydsl;

import com.atlassian.annotations.PublicSpi;
import com.atlassian.pocketknife.api.querydsl.ConnectionProvider;
import com.atlassian.pocketknife.api.querydsl.DialectProvider;
import com.atlassian.util.concurrent.LazyReference;
import com.google.common.base.Function;
import com.mysema.query.sql.Configuration;
import com.mysema.query.sql.OracleTemplates;
import com.mysema.query.sql.PostgresTemplates;
import com.mysema.query.sql.SQLTemplates;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.SQLException;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * This is a dialect configuration that you can use to detect and build the QueryDSL dialect config.
 * <p/>
 * You can use this class to override the way the configuration is built via the {@link
 * #enrich(com.mysema.query.sql.SQLTemplates.Builder)} and {@link #enrich(com.mysema.query.sql.Configuration)} methods
 */
@Component
@PublicSpi
public class DefaultDialectConfiguration implements DialectConfiguration
{
    private final ConnectionProvider connectionProvider;

    private final LazyReference<Config> ref = new LazyReference<DialectProvider.Config>()
    {
        @Override
        protected DialectProvider.Config create() throws Exception
        {
            return connectionProvider.withConnection(new Function<Connection, Config>()
            {
                @Override
                public DialectProvider.Config apply(final Connection input)
                {
                    return detect(input);
                }
            });
        }
    };


    @Autowired
    public DefaultDialectConfiguration(ConnectionProvider connectionProvider)
    {
        this.connectionProvider = connectionProvider;
    }

    @Override
    public Config getDialectConfig()
    {
        return ref.get();
    }

    private Config detect(Connection connection)
    {
        SQLTemplates sqlTemplates = buildTemplates(connection);
        Configuration configuration = enrich(new Configuration(sqlTemplates));

        Config config = new Config(sqlTemplates, configuration);
        return config;
    }

    @Override
    public SQLTemplates.Builder enrich(final SQLTemplates.Builder builder)
    {
        return builder
                .newLineToSingleSpace()
                .quote();

    }

    @Override
    public Configuration enrich(final Configuration configuration)
    {
        return configuration;
    }

    private static Map<String, SQLTemplates.Builder> support = new LinkedHashMap<String, SQLTemplates.Builder>();

    static
    {
        support.put("PostgreSQL", PostgresTemplates.builder());
        support.put("Oracle", OracleTemplates.builder());
    }

    private SQLTemplates buildTemplates(final Connection connection)
    {
        try
        {
            DatabaseMetaData metaData = connection.getMetaData();
            String databaseProductName = metaData.getDatabaseProductName();
            SQLTemplates.Builder builder = null;

            //
            // which databases do we support
            for (String db : support.keySet())
            {
                if (databaseProductName.contains(db))
                {
                    builder = support.get(db);
                    break;
                }
            }
            if (builder == null)
            {
                throw new UnsupportedOperationException(String.format("Unable to detect QueryDSL template support for database %s", databaseProductName));
            }
            return enrich(builder).build();

        }
        catch (SQLException e)
        {
            throw new RuntimeException("Unable to enquire on JDBC metadata to configure QueryDSL", e);
        }
    }
}
