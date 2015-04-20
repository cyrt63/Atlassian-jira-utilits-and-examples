package com.atlassian.pocketknife.internal.querydsl;


import com.atlassian.pocketknife.api.querydsl.ConnectionProvider;
import com.atlassian.pocketknife.api.querydsl.DialectProvider;
import com.atlassian.pocketknife.api.querydsl.QueryFactory;
import com.atlassian.pocketknife.spi.querydsl.AbstractConnectionProvider;
import com.atlassian.pocketknife.spi.querydsl.DefaultDialectConfiguration;
import com.mysema.commons.lang.CloseableIterator;
import com.mysema.query.sql.RelationalPathBase;
import com.mysema.query.sql.SQLQuery;
import com.mysema.query.types.path.NumberPath;
import com.mysema.query.types.path.StringPath;
import org.junit.Before;
import org.junit.Test;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

public class SchemaInclusionTest
{
    private Connection connection;

    private QueryFactoryImpl queryFactory;
    private ConnectionProvider connectionProvider;
    private DialectProvider dialectProvider;

    @Before
    public void setup() throws SQLException
    {
        try
        {
            Class clazz = Class.forName("org.h2.Driver");
        }
        catch (Exception e)
        {
            System.err.println("ERROR: failed to load HSQLDB JDBC driver.");
            e.printStackTrace();
            return;
        }

        connection = DriverManager.getConnection("jdbc:h2:mem:test");

        connectionProvider = new AbstractConnectionProvider()
        {
            @Override
            protected Connection getConnectionImpl(final boolean autoCommit)
            {
                return connection;
            }
        };
        dialectProvider = new DefaultDialectConfiguration(connectionProvider);

        queryFactory = new QueryFactoryImpl(connectionProvider, dialectProvider);
    }

    @Test
    public void testSchemaStuff() throws SQLException
    {
        System.out.println(connection.isClosed());
        final Statement statement = connection.createStatement();
        statement.execute("create table FOO (ID IDENTITY, NAME VARCHAR2(64))");
        statement.execute("insert into FOO (NAME) values ('hello')");
//        connection.commit();
//
//        connection = DriverManager.getConnection("jdbc:h2:mem:test");
//        connection.

        System.out.println(connection.isClosed());
        final SQLQuery query = queryFactory.select(connection);

        final QFooMapping mapping = new QFooMapping("fm", "", "FOO");
        final CloseableIterator<String> iteratee = query.from(mapping).iterate(mapping.NAME);
        while (iteratee.hasNext())
        {
            System.out.println(iteratee.next());
        }
    }

    private class QFooMapping extends RelationalPathBase<QFooMapping>
    {
        private static final String TABLE_NAME = "FOO";

        public QFooMapping(final String variable, final String schema, final String table)
        {
            super(QFooMapping.class, variable, schema, table);
        }

        private static final String ID_COLUMN = "ID";

        public final NumberPath<Integer> ID = createNumber(ID_COLUMN, Integer.class);

        private static final String NAME_COLUMN = "NAME";

        public final StringPath NAME = createString(NAME_COLUMN);
    }

    private static class TestConnectionProvider extends AbstractConnectionProvider
    {
        private final String schema;

        public TestConnectionProvider(final String schema)
        {
            this.schema = schema;
        }

        @Override
        protected Connection getConnectionImpl(final boolean b)
        {
            try
            {
                return getHSQL(schema);
            }
            catch (Exception e)
            {
                e.printStackTrace();
            }
            return null;
        }

        private static Connection getHSQL(String schema) throws SQLException, ClassNotFoundException
        {
            Class.forName("org.hsqldb.jdbcDriver");
            String url = "jdbc:hsqldb:target/" + schema;
            return DriverManager.getConnection(url, "sa", "");
        }
    }
}
