package com.atlassian.pocketknife.internal.querydsl;

import com.atlassian.pocketknife.spi.querydsl.AbstractConnectionProvider;
import org.hamcrest.Matchers;
import org.junit.Assert;

import java.sql.Connection;
import java.util.concurrent.atomic.AtomicInteger;

/**
 */
public class MockConnectionProvider extends AbstractConnectionProvider
{
    private final Connection connection;
    private AtomicInteger borrowCount = new AtomicInteger(0);

    public MockConnectionProvider(Connection connection)
    {
        this.connection = connection;
    }

    public int getBorrowCount()
    {
        return borrowCount.get();
    }

    @Override
    protected Connection getConnectionImpl(final boolean autoCommit)
    {
        borrowCount.incrementAndGet();
        return connection;
    }

    @Override
    public void returnConnection(final Connection connectionIn)
    {
        Assert.assertThat(connectionIn, Matchers.sameInstance(connection));
        borrowCount.decrementAndGet();
    }
}
