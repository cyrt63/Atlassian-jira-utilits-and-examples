package com.atlassian.pocketknife.spi.querydsl;

import com.atlassian.annotations.PublicSpi;
import com.atlassian.fugue.Pair;
import com.atlassian.pocketknife.api.querydsl.ConnectionProvider;
import com.atlassian.pocketknife.api.querydsl.DialectProvider;
import com.atlassian.pocketknife.api.querydsl.LoggingSqlListener;
import com.atlassian.util.concurrent.LazyReference;
import com.google.common.base.Function;
import com.mysema.query.sql.Configuration;
import com.mysema.query.sql.H2Templates;
import com.mysema.query.sql.HSQLDBTemplates;
import com.mysema.query.sql.MySQLTemplates;
import com.mysema.query.sql.OracleTemplates;
import com.mysema.query.sql.PostgresTemplates;
import com.mysema.query.sql.SQLServerTemplates;
import com.mysema.query.sql.SQLTemplates;
import com.mysema.query.sql.types.AbstractType;
import com.mysema.query.sql.types.Null;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.LinkedHashMap;
import java.util.Map;

import static org.apache.commons.lang.ArrayUtils.isNotEmpty;

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
        Pair<SQLTemplates, SupportedDatabase> pair = buildTemplates(connection);
        SQLTemplates sqlTemplates = pair.left();
        Configuration configuration = enrich(new Configuration(sqlTemplates));
        configuration.addListener(new LoggingSqlListener(configuration));

        // Should be removed when https://github.com/querydsl/querydsl/issues/1079 is fixed
        configuration.register(new NullType());

        return new Config(sqlTemplates, configuration, buildDatabaseInfo(pair.right(), connection));
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

    private static Map<String, Pair<SQLTemplates.Builder, SupportedDatabase>> support = new LinkedHashMap<String, Pair<SQLTemplates.Builder, SupportedDatabase>>();

    static
    {
        support.put(":postgresql:", Pair.pair(PostgresTemplates.builder(), SupportedDatabase.POSTGRESSQL));
        support.put(":oracle:", Pair.pair(OracleTemplates.builder(), SupportedDatabase.ORACLE));
        support.put(":hsqldb:", Pair.pair(HSQLDBTemplates.builder(), SupportedDatabase.HSQLDB));
        support.put(":sqlserver:", Pair.pair(SQLServerTemplates.builder().printSchema(), SupportedDatabase.SQLSERVER));
        support.put(":mysql:", Pair.pair(MySQLTemplates.builder(), SupportedDatabase.MYSQL));
        support.put(":h2:", Pair.pair(H2Templates.builder(), SupportedDatabase.H2));
    }

    private Pair<SQLTemplates, SupportedDatabase> buildTemplates(final Connection connection)
    {
        try
        {
            DatabaseMetaData metaData = connection.getMetaData();
            String connStr = metaData.getURL();
            Pair<SQLTemplates.Builder, SupportedDatabase> pair = null;

            // which databases do we support
            for (String db : support.keySet())
            {
                if (connStr.contains(db))
                {
                    pair = support.get(db);
                    break;
                }
            }
            if (pair == null)
            {
                throw new UnsupportedOperationException(String.format("Unable to detect QueryDSL template support for database %s", connStr));
            }
            SQLTemplates templates = enrich(pair.left()).build();
            return Pair.pair(templates, pair.right());

        }
        catch (SQLException e)
        {
            throw new RuntimeException("Unable to enquire on JDBC metadata to configure QueryDSL", e);
        }
    }

    private DatabaseInfo buildDatabaseInfo(final SupportedDatabase supportedDatabase, final Connection connection)
    {
        try
        {
            DatabaseMetaData metaData = connection.getMetaData();

            return new DatabaseInfo(supportedDatabase,
                    metaData.getDatabaseProductName(),
                    metaData.getDatabaseProductVersion(),
                    metaData.getDatabaseMajorVersion(),
                    metaData.getDatabaseMinorVersion(),
                    metaData.getDriverName(),
                    metaData.getDriverMajorVersion(),
                    metaData.getDriverMinorVersion());
        }
        catch (SQLException e)
        {
            throw new RuntimeException("Unable to enquire on JDBC metadata to determine DatabaseInfo", e);
        }
    }

    // Workaround allowing us to log SQL with null in it (e.g. update table_x set col_1 to null)
    // Should be removed when https://github.com/querydsl/querydsl/issues/1079 is fixed
    private static class NullType extends AbstractType<Null>
    {
        public NullType() {
            super(Types.NULL);
        }

        @Override
        public Null getValue(ResultSet rs, int startIndex) throws SQLException {
            return Null.DEFAULT;
        }

        @Override
        public Class<Null> getReturnedClass() {
            return Null.class;
        }

        @Override
        public void setValue(PreparedStatement st, int startIndex, Null value)
                throws SQLException {

            if(isNotEmpty(getSQLTypes()))
            {
                st.setNull(startIndex, getSQLTypes()[0]);
            }
            else
            {
                throw new RuntimeException("Unable to set database column to null");
            }
        }

        @Override
        public String getLiteral(final Null value)
        {
            return "null";
        }
    }

}
