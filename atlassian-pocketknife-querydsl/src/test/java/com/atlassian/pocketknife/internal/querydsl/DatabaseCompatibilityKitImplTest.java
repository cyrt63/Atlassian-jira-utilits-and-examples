package com.atlassian.pocketknife.internal.querydsl;

import com.atlassian.pocketknife.api.querydsl.QueryFactory;
import com.atlassian.pocketknife.internal.querydsl.tables.Connections;
import com.atlassian.pocketknife.internal.querydsl.tables.Constants;
import com.atlassian.pocketknife.spi.querydsl.DefaultDialectConfiguration;
import com.mysema.query.sql.dml.SQLInsertClause;
import org.hamcrest.Matchers;
import org.junit.Before;
import org.junit.Test;

import java.math.BigDecimal;
import java.sql.Connection;

import static com.atlassian.pocketknife.internal.querydsl.tables.domain.QEmployee.employee;
import static org.junit.Assert.assertThat;

public class DatabaseCompatibilityKitImplTest
{

    private QueryFactory queryFactory;
    private CountingConnectionProvider connectionProvider;
    private DatabaseCompatibilityKitImpl databaseCompatibilityKit;

    @Before
    public void setUp() throws Exception
    {
        Connections.initHSQL();

        connectionProvider = new CountingConnectionProvider();
        DefaultDialectConfiguration defaultDialectConfiguration = new DefaultDialectConfiguration(connectionProvider);
        queryFactory = new QueryFactoryImpl(connectionProvider, defaultDialectConfiguration);
        databaseCompatibilityKit = new DatabaseCompatibilityKitImpl(defaultDialectConfiguration);
    }

    @Test
    public void test_execute_with_key() throws Exception
    {
        // arrange
        Connection connection = connectionProvider.borrowConnection();
        SQLInsertClause insert = queryFactory.insert(connection, employee);
        insert = insert.set(employee.firstname, "First Name")
                .set(employee.lastname, "Last Name")
                .set(employee.salary, new BigDecimal(666))
                .set(employee.datefield, Constants.date)
                .set(employee.timefield, Constants.time);


        // act
        Integer id = databaseCompatibilityKit.executeWithKey(connection, insert, Integer.class);

        // assert
        assertThat(id, Matchers.notNullValue());
    }
}