package com.atlassian.pocketknife.internal.querydsl;

import com.atlassian.pocketknife.spi.querydsl.AbstractConnectionProvider;
import org.apache.commons.lang.StringUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

import static org.junit.Assert.assertEquals;

/**
 * Unit tests for the {@link DefaultSchemaProviderTest}
 */
public class DefaultSchemaProviderTest {
    private TestConnectionProvider connectionProvider;

    private DefaultSchemaProvider classUnderTest;

    @Before
    public void setup() throws Exception {

        connectionProvider = new TestConnectionProvider();

        classUnderTest = new DefaultSchemaProvider(connectionProvider);

        seedDatabase();
    }

    @After
    public void teardown() throws Exception {
        cleanupDatabase();
    }

    @Test(expected = IllegalArgumentException.class)
    public void testGetSchemaWithNullTable() {
        classUnderTest.getSchema(null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testGetSchemaWithEmptyTable() {
        classUnderTest.getSchema("");
    }

    @Test
    public void testGetSchemaWithExistentTable() {
        assertEquals("PUBLIC", classUnderTest.getSchema("TEST_TABLE"));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testGetSchemaWithNonExistentTable() {
        classUnderTest.getSchema("missing_table");
    }

    @Test
    public void testGetTableNameWithMatchingCase() {
        assertEquals("TEST_TABLE", classUnderTest.getTableName("TEST_TABLE"));
    }

    @Test
    public void testGetTableNameWithNonMatchingCase() {
        assertEquals("TEST_TABLE", classUnderTest.getTableName("test_table"));
    }

    @Test
    public void testGetTableNameWithNonExistentTable() {
        assertEquals(null, classUnderTest.getTableName("missing_table"));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testGetTableNameWithEmptyTable() {
        classUnderTest.getTableName("");
    }

    @Test(expected = IllegalArgumentException.class)
    public void testGetTableNameWithNullTable() {
        classUnderTest.getTableName(null);
    }

    @Test
    public void testGetColumnNameWithMatchingCase() {
        assertEquals("ID", classUnderTest.getColumnName("TEST_TABLE", "ID"));
    }

    @Test
    public void testGetColumnNameWithNonMatchingCase() {
        assertEquals("ID", classUnderTest.getColumnName("test_table", "id"));
    }

    @Test
    public void testGetColumnNameWithNonExistentTable() {
        assertEquals(null, classUnderTest.getColumnName("missing_table", "id"));
    }

    @Test
    public void testGetColumnNameWithNonExistentColumn() {
        assertEquals(null, classUnderTest.getColumnName("test_table", "missing"));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testGetColumnNameWithEmptyTable() {
        classUnderTest.getColumnName("", "id");
    }

    @Test(expected = IllegalArgumentException.class)
    public void testGetColumnNameWithEmptyColumn() {
        classUnderTest.getColumnName("test_table", "");
    }

    private void cleanupDatabase() throws Exception {
        Connection c = connectionProvider.borrowConnection();

        try {
            runStatement(c, "DROP TABLE \"TEST_TABLE\" IF EXISTS;");
        } finally {
            connectionProvider.returnConnection(c);
        }
    }

    private void seedDatabase() throws Exception {
        Connection c = connectionProvider.borrowConnection();

        try {
            runStatement(c, "CREATE TABLE \"TEST_TABLE\"(\"ID\" BIGINT NOT NULL, \"NAME\" VARCHAR);");
        } finally {
            connectionProvider.returnConnection(c);
        }
    }

    private void runStatement(final Connection connection, String statement) throws SQLException {
        if (StringUtils.isBlank(statement)) {
            return;
        }

        Statement stmt = null;
        try {
            stmt = connection.createStatement();
            stmt.executeUpdate(statement);
            connection.commit();
        } finally {
            if (stmt != null) {
                stmt.close();
            }
        }
    }

    private static class TestConnectionProvider extends AbstractConnectionProvider {

        @Override
        protected Connection getConnectionImpl(final boolean b) {
            try {
                return getHSQL();
            } catch (Exception e) {
                e.printStackTrace();
            }
            return null;
        }

        private static Connection getHSQL() throws SQLException, ClassNotFoundException {
            Class.forName("org.hsqldb.jdbcDriver");
            String url = "jdbc:hsqldb:target/" + DefaultSchemaProviderTest.class.getSimpleName();
            return DriverManager.getConnection(url, "sa", "");
        }

    }
}