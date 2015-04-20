package com.atlassian.pocketknife.internal.querydsl;


import com.atlassian.pocketknife.api.querydsl.ConnectionProvider;
import com.atlassian.pocketknife.api.querydsl.DialectProvider;
import com.atlassian.pocketknife.api.querydsl.QueryFactory;
import com.atlassian.pocketknife.spi.querydsl.AbstractConnectionProvider;
import com.atlassian.pocketknife.spi.querydsl.DefaultDialectConfiguration;
import com.mysema.commons.lang.CloseableIterator;
import com.mysema.query.sql.Configuration;
import com.mysema.query.sql.HSQLDBTemplates;
import com.mysema.query.sql.RelationalPathBase;
import com.mysema.query.sql.SQLQuery;
import com.mysema.query.sql.SQLTemplates;
import com.mysema.query.types.path.NumberPath;
import com.mysema.query.types.path.StringPath;
import org.junit.Assert;
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
    public void setup() throws SQLException{
//    {
//        try
//        {
//            Class clazz = Class.forName("org.h2.Driver");
//        }
//        catch (Exception e)
//        {
//            System.err.println("ERROR: failed to load HSQLDB JDBC driver.");
//            e.printStackTrace();
//            return;
//        }
//
//        connection = DriverManager.getConnection("jdbc:h2:mem:test");
//
//        connectionProvider = new AbstractConnectionProvider()
//        {
//            @Override
//            protected Connection getConnectionImpl(final boolean autoCommit)
//            {
//                return connection;
//            }
//        };
//        dialectProvider = new DefaultDialectConfiguration(connectionProvider);
//
//        queryFactory = new QueryFactoryImpl(connectionProvider, dialectProvider);
    }

    @Test
    public void testSchemaStuff() throws SQLException
    {
        String schema = "HELLO";
        String otherSchema = "BYE";

        connectionProvider = new TestConnectionProvider(schema);
        dialectProvider = new DefaultDialectConfiguration(connectionProvider);

        queryFactory = new QueryFactoryImpl(connectionProvider, dialectProvider);

        connection = connectionProvider.borrowConnection();

        final Statement statement = connection.createStatement();
        statement.execute("create table FOO (ID INTEGER, NAME VARCHAR(64))");
        statement.execute("insert into FOO (ID, NAME) values (1, 'this is my HELLO name')");

        statement.execute("create schema BYE AUTHORIZATION DBA\n"
                + " create table FOO (ID INTEGER, NAME VARCHAR(64))");

        statement.execute("insert into BYE.FOO (ID, NAME) values (1, 'this is my BYE name')");

        final SQLTemplates template = HSQLDBTemplates.builder().printSchema().build();
        final SQLQuery query = new SQLQuery(connection, template);

        final QFooMapping mapping = new QFooMapping("fm", otherSchema, "FOO");
        final SQLQuery queryFrom = query.from(mapping);
        final CloseableIterator<String> iteratee = queryFrom.iterate(mapping.NAME);

        // This shows the schema being included
        System.out.println(queryFrom.getSQL(mapping.NAME).getSQL());

        Assert.assertEquals("this is my BYE name", iteratee.next());

        final SQLQuery pocketknifeQuery = queryFactory.select(connection).from(mapping);

        // This shows the schema being excluded
        System.out.println(pocketknifeQuery.getSQL(mapping.NAME).getSQL());

        final CloseableIterator<String> pocketIteratee = pocketknifeQuery.iterate(mapping.NAME);

        Assert.assertEquals("this is my BYE name", pocketIteratee.next());
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
