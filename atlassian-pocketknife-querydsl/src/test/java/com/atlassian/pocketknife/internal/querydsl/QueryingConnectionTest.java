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
import javax.annotation.Nullable;

import static com.atlassian.pocketknife.internal.querydsl.tables.domain.QEmployee.employee;
import static org.hamcrest.Matchers.equalTo;
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

        assertThat(countingConnectionProvider.getBorrowCount(), equalTo(0));

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
            assertThat(countingConnectionProvider.getBorrowCount(), equalTo(0));
        }
    }

    @Test
    public void testStreamyMap() throws Exception
    {
        Connection connection = countingConnectionProvider.borrowConnection();
        SQLQuery query = queryFactory.select(connection).from(employee);
        final List<Tuple> queryResult = query.list(employee.id, employee.firstname);
        final Tuple firstTuple = queryResult.get(0);
        Integer firstEmployeeId = firstTuple.get(employee.id);
        String firstEmployeeName = firstTuple.get(employee.firstname);

        TestHalfStreamyMapClosure closure = new TestHalfStreamyMapClosure(firstEmployeeId);
        List<String> result = queryFactory.halfStreamyMap(closure);
        assertThat(result, Matchers.containsInAnyOrder(firstEmployeeName));
        assertThat(countingConnectionProvider.getBorrowCount(), equalTo(1));
    }

    private class TestHalfStreamyMapClosure implements QueryFactory.HalfStreamyMapClosure<String>
    {
        private final Integer employeeId;

        private TestHalfStreamyMapClosure(final Integer employeeId) {this.employeeId = employeeId;}

        @Override
        public Function<SelectQuery, StreamyResult> getQuery()
        {
            return new Function<SelectQuery, StreamyResult>()
            {
                @Override
                public StreamyResult apply(final SelectQuery input)
                {
                    return input.from(employee).where(employee.id.eq(employeeId)).stream(employee.firstname);
                }
            };
        }

        @Override
        public Function<Tuple, String> getMapFunction()
        {
            return new Function<Tuple, String>()
            {
                @Override
                public String apply(@Nullable final Tuple input)
                {
                    return input.get(employee.firstname);
                }
            };
        }
    }

    @Test
    public void testSelectWithFold() throws Exception
    {
        Connection connection = countingConnectionProvider.borrowConnection();
        SQLQuery countQuery = queryFactory.select(connection).from(employee);
        int numberOfEmployees = countQuery.list(employee.id).size();

        TestHalfStreamyFoldClosure closure = new TestHalfStreamyFoldClosure();

        StreamyResult allEmployeesStreamy = queryFactory.select(closure.getQuery());

        try
        {
            Integer streamyCount = allEmployeesStreamy.foldLeft(0, closure.getFoldFunction());

            assertThat(streamyCount, equalTo(numberOfEmployees));
        }
        finally
        {
            allEmployeesStreamy.close();
            assertThat(countingConnectionProvider.getBorrowCount(), equalTo(1));
        }
    }

    @Test
    public void testStreamyFold() throws Exception
    {
        Connection connection = countingConnectionProvider.borrowConnection();
        SQLQuery countQuery = queryFactory.select(connection).from(employee);
        int numberOfEmployees = countQuery.list(employee.id).size();

        TestHalfStreamyFoldClosure closure = new TestHalfStreamyFoldClosure();
        Integer result = queryFactory.halfStreamyFold(0, closure);
        assertThat(result, equalTo(numberOfEmployees));
        assertThat(countingConnectionProvider.getBorrowCount(), equalTo(1));
    }

    private class TestHalfStreamyFoldClosure implements QueryFactory.HalfStreamyFoldClosure<Integer>
    {
        @Override
        public Function<SelectQuery, StreamyResult> getQuery()
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
            assertThat(countingConnectionProvider.getBorrowCount(), equalTo(0));
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
            assertThat(countingConnectionProvider.getBorrowCount(), equalTo(0));
        }
    }

    @Test
    public void testSelectIsClosedWhenIteratorIsHalfUsed() throws Exception
    {
        StreamyResult streamyResult = createStreamyQuery();
        try
        {
            int i = 0;
            for (Tuple t : streamyResult)
            {
                System.out.println(String.format("Tuple %s %s %s", t.get(employee.id), t.get(employee.firstname), t.get(employee.lastname)));
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

