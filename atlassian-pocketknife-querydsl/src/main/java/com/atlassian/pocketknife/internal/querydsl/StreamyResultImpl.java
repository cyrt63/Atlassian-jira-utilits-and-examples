package com.atlassian.pocketknife.internal.querydsl;

import com.atlassian.fugue.Function2;
import com.atlassian.pocketknife.api.querydsl.ClosePromise;
import com.atlassian.pocketknife.api.querydsl.CloseableIterable;
import com.atlassian.pocketknife.api.querydsl.StreamyResult;
import com.atlassian.pocketknife.api.querydsl.Tuples;
import com.google.common.base.Function;
import com.google.common.base.Functions;
import com.mysema.commons.lang.CloseableIterator;
import com.mysema.query.Tuple;

/**
 */
public class StreamyResultImpl implements StreamyResult
{
    private final CloseableIterator<Tuple> closeableIterator;
    private final ClosePromise closePromise;

    public StreamyResultImpl(final CloseableIterator<Tuple> closeableIterator, final ClosePromise parentPromise)
    {
        this.closeableIterator = closeableIterator;
        this.closePromise = new ClosePromise(parentPromise)
        {
            @Override
            protected void closeEffect()
            {
                closeableIterator.close();
            }
        };
    }

    @Override
    public CloseableIterator<Tuple> iterator()
    {
        CloseableIterable<Tuple> iterable = map(Functions.<Tuple>identity());
        return iterable.iterator();
    }

    @Override
    public <D> CloseableIterable<D> map(final Function<Tuple, D> mapper)
    {
        if (closePromise.isClosed())
        {
            throw new IllegalStateException("This streaming result has already been closed");
        }
        return Tuples.map(closeableIterator, mapper, closePromise);
    }

    @Override
    public <T> T foldLeft(final T initial, Function2<T, Tuple, T> combiningFunction)
    {
        if (closePromise.isClosed())
        {
            throw new IllegalStateException("This streaming result has already been closed");
        }

        try
        {
            T accumulator = initial;
            while (closeableIterator.hasNext())
            {
                accumulator = combiningFunction.apply(accumulator, closeableIterator.next());
            }
            return accumulator;
        }
        finally
        {
            closePromise.close();
        }
    }

    @Override
    public void close()
    {
        closePromise.close();
    }
}
