package com.atlassian.pocketknife.internal.querydsl;

import com.atlassian.fugue.Function2;
import com.atlassian.pocketknife.api.querydsl.QueryFactory;
import com.atlassian.pocketknife.api.querydsl.SelectQuery;
import com.atlassian.pocketknife.api.querydsl.StreamyResult;
import com.atlassian.pocketknife.internal.querydsl.tables.Connections;
import com.atlassian.pocketknife.internal.querydsl.tables.domain.Employee;
import com.atlassian.pocketknife.spi.querydsl.DefaultDialectConfiguration;
import com.google.common.base.Function;
import com.mysema.query.Tuple;
import com.mysema.query.sql.SQLQuery;

import org.hamcrest.Matchers;
import org.junit.Before;
import org.junit.Test;

import java.sql.Connection;
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

    @Test
    public void testSelectWithFold() throws Exception
    {
        Connection connection = countingConnectionProvider.borrowConnection();
        SQLQuery countQuery = queryFactory.select(connection).from(employee);
        int numberOfEmployees = countQuery.list(employee.id).size();

        TestStreamyFoldClosure closure = new TestStreamyFoldClosure();

        StreamyResult allEmployeesStreamy = queryFactory.select(closure.query());

        try
        {
            Integer streamyCount = allEmployeesStreamy.foldLeft(0, closure.getFoldFunction());

            assertThat(streamyCount, Matchers.equalTo(numberOfEmployees));
        }
        finally
        {
            allEmployeesStreamy.close();
            assertThat(countingConnectionProvider.getBorrowCount(), Matchers.equalTo(1));
        }
    }

    @Test
    public void testStreamyFold() throws Exception
    {
        Connection connection = countingConnectionProvider.borrowConnection();
        SQLQuery countQuery = queryFactory.select(connection).from(employee);
        int numberOfEmployees = countQuery.list(employee.id).size();

        TestStreamyFoldClosure closure = new TestStreamyFoldClosure();
        Integer result = queryFactory.streamyFold(0, closure);
        assertThat(result, Matchers.equalTo(numberOfEmployees));
        assertThat(countingConnectionProvider.getBorrowCount(), Matchers.equalTo(1));
    }

    private class TestStreamyFoldClosure implements QueryFactory.StreamyFoldClosure<Integer>
    {

        @Override
        public Function<SelectQuery, StreamyResult> query()
        {
            return new Function<SelectQuery, StreamyResult>()
            {
                @Override
                public StreamyResult apply(final SelectQuery input)
                {
                    return input.from(employee).stream(employee.id);
                }
            };
        }

        @Override
        public Function2<Integer, Tuple, Integer> getFoldFunction()
        {
            return new Function2<Integer, Tuple, Integer>()
            {
                @Override
                public Integer apply(Integer arg0, Tuple arg1)
                {
                    return arg0 + 1;
                }
            };
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

}

