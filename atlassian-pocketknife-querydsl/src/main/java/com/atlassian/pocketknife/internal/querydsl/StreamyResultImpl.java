package com.atlassian.pocketknife.internal.querydsl;

import com.atlassian.pocketknife.api.querydsl.ClosePromise;
import com.atlassian.pocketknife.api.querydsl.CloseableIterable;
import com.atlassian.pocketknife.api.querydsl.StreamyResult;
import com.atlassian.pocketknife.api.querydsl.Tuples;
import com.google.common.base.Function;
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
    public <D> CloseableIterable<D> map(final Function<Tuple, D> mapper)
    {
        if (closePromise.isClosed())
        {
            throw new IllegalStateException("This streaming result has already been closed");
        }
        return Tuples.map(closeableIterator, mapper, closePromise);
    }

    @Override
    public void close()
    {
        closePromise.close();
    }
}
