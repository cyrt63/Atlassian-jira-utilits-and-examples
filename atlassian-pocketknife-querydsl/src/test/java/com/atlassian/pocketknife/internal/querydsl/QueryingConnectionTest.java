package com.atlassian.pocketknife.internal.querydsl;

import com.atlassian.pocketknife.api.querydsl.CloseableIterable;
import com.atlassian.pocketknife.api.querydsl.QueryFactory;
import com.atlassian.pocketknife.api.querydsl.SelectQuery;
import com.atlassian.pocketknife.api.querydsl.StreamyResult;
import com.atlassian.pocketknife.internal.querydsl.tables.Connections;
import com.atlassian.pocketknife.internal.querydsl.tables.domain.Employee;
import com.atlassian.pocketknife.spi.querydsl.AbstractConnectionProvider;
import com.atlassian.pocketknife.spi.querydsl.DefaultDialectConfiguration;
import com.google.common.base.Function;
import com.mysema.query.Tuple;
import org.hamcrest.Matchers;
import org.junit.Before;
import org.junit.Test;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

import static com.atlassian.pocketknife.internal.querydsl.tables.domain.QEmployee.employee;
import static org.junit.Assert.assertThat;

/**
 */
public class QueryingConnectionTest
{
    private QueryFactory queryFactory;
    private CountingConnectionProvider countingConnectionProvider;

    @Before
    public void setUp() throws Exception
    {
        Connections.initHSQL();

        countingConnectionProvider = new CountingConnectionProvider();
        DefaultDialectConfiguration defaultDialectConfiguration = new DefaultDialectConfiguration(countingConnectionProvider);
        queryFactory = new QueryFactoryImpl(countingConnectionProvider, defaultDialectConfiguration);

    }

    @Test
    public void testBasicConnection() throws Exception
    {
        Connection connection = countingConnectionProvider.borrowConnection();
        List<Tuple> list = queryFactory.select(connection).from(employee).list(employee.firstname, employee.lastname);
        for (Tuple tuple : list)
        {
            System.out.println(String.format("Employee %s %s", tuple.get(employee.firstname), tuple.get(employee.lastname)));
        }
        countingConnectionProvider.returnConnection(connection);

        assertThat(countingConnectionProvider.getBorrowCount(), Matchers.equalTo(0));

    }

    @Test
    public void testSelectWithMapping() throws Exception
    {
        StreamyResult streamyResult = createStreamyQuery();
        try
        {
            for (Employee e : streamyResult.map(new Function<Tuple, Employee>()
            {
                @Override
                public Employee apply(final Tuple input)
                {
                    Employee e = new Employee(input.get(employee.id));
                    e.setFirstname(input.get(employee.firstname));
                    e.setLastname(input.get(employee.lastname));
                    return e;
                }
            }))
            {
                System.out.println(String.format("Employee %s %s %s", e.getId(), e.getFirstname(), e.getLastname()));
            }
        }
        finally
        {
            streamyResult.close();
            assertThat(countingConnectionProvider.getBorrowCount(), Matchers.equalTo(0));
        }


    }

    @Test (expected = RuntimeException.class)
    public void testSelectWhereMappingThrowsException() throws Exception
    {
        StreamyResult streamyResult = createStreamyQuery();
        try
        {
            for (Employee e : streamyResult.map(new Function<Tuple, Employee>()
            {
                @Override
                public Employee apply(final Tuple input)
                {
                    //noinspection ConstantConditions,ConstantIfStatement
                    if (true)
                    {
                        throw new RuntimeException("Badness!");
                    }
                    Employee e = new Employee(input.get(employee.id));
                    e.setFirstname(input.get(employee.firstname));
                    e.setLastname(input.get(employee.lastname));
                    return e;
                }
            }))
            {
                System.out.println(String.format("Employee %s %s %s", e.getId(), e.getFirstname(), e.getLastname()));
            }
        }
        finally
        {
            streamyResult.close();
            assertThat(countingConnectionProvider.getBorrowCount(), Matchers.equalTo(0));
        }
    }

    @Test
    public void testSelectIsClosedWhenMappingIsHalfUsed() throws Exception
    {
        StreamyResult streamyResult = createStreamyQuery();
        try
        {
            int i = 0;
            for (Employee e : streamyResult.map(new Function<Tuple, Employee>()
            {
                @Override
                public Employee apply(final Tuple input)
                {
                    Employee e = new Employee(input.get(employee.id));
                    e.setFirstname(input.get(employee.firstname));
                    e.setLastname(input.get(employee.lastname));
                    return e;
                }
            }))
            {
                System.out.println(String.format("Employee %s %s %s", e.getId(), e.getFirstname(), e.getLastname()));
                i++;
                if (i > 0)
                {
                    break;
                }
            }
        }
        finally
        {
            streamyResult.close();
            assertThat(countingConnectionProvider.getBorrowCount(), Matchers.equalTo(0));
        }
    }


    private StreamyResult createStreamyQuery()
    {
        return queryFactory.select(new Function<SelectQuery, StreamyResult>()
        {
            @Override
            public StreamyResult apply(final SelectQuery input)
            {
                return input.from(employee)
                        .where(employee.lastname.contains("Smith"))
                        .stream(employee.id, employee.firstname, employee.lastname);
            }
        });
    }

    static class CountingConnectionProvider extends AbstractConnectionProvider
    {
        int borrowCount = 0;

        public int getBorrowCount()
        {
            return borrowCount;
        }

        @Override
        protected Connection getConnectionImpl(boolean autoCommit)
        {
            Connection connection = Connections.getConnection();
            try
            {
                connection.setAutoCommit(autoCommit);
            }
            catch (SQLException e)
            {
                throw new RuntimeException(e);
            }
            borrowCount++;
            return connection;
        }

        @Override
        public void returnConnection(Connection connection)
        {
            // we dont close our connections.  We are not pooling
            borrowCount--;
        }
    }
}

