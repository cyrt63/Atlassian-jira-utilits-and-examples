package com.atlassian.pocketknife.spi.querydsl;

import com.atlassian.pocketknife.api.querydsl.DialectConfiguration;
import com.mysema.query.sql.Configuration;
import com.mysema.query.sql.OracleTemplates;
import com.mysema.query.sql.PostgresTemplates;
import com.mysema.query.sql.SQLTemplates;
import org.springframework.stereotype.Component;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.SQLException;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

/**
 * This is a dialect configuration that you can use to detect and build the QueryDSL dialect config.
 * <p/>
 * You can use this class to override the way the configuration is built via the {@link
 * #enrich(com.mysema.query.sql.SQLTemplates.Builder)} and {@link #enrich(com.mysema.query.sql.Configuration)} methods
 */
@Component
public class DefaultDialectConfiguration implements DialectConfiguration
{
    AtomicReference<Config> ref = new AtomicReference<Config>();

    @Override
    public Config getDialectConfig()
    {
        return ref.get();
    }

    public Config detect(Connection connection)
    {
        SQLTemplates sqlTemplates = buildTemplates(connection);
        Configuration configuration = enrich(new Configuration(sqlTemplates));

        Config config = new Config(sqlTemplates, configuration);
        ref.set(config);
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
