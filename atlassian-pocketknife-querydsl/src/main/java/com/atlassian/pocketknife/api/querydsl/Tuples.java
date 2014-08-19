package com.atlassian.pocketknife.api.querydsl;

import com.atlassian.fugue.Effect;
import com.google.common.base.Function;
import com.mysema.commons.lang.CloseableIterator;
import com.mysema.query.Tuple;

/**
 * A class to handle processing Query Tuples of data
 */
public class Tuples
{
    /**
     * Use this method to map a result of QueryDSL tuples into domain objects.
     * <p/>
     * This will return an Iterable that closes itself after the last element has been retrieved.  This allows you to
     * use for each syntax to iterate database results without bring them all into memory at the one time
     *
     * @param closeableIterator the closeable iterator that was returned from a QueryDSL
     * @param f a function to map the QueryDSL tuples into actual domain objects
     * @param <T> the desired domain class
     * @return an Iterable that closes itself when the last record is retrieved.
     */
    public static <T> CloseableIterable<T> map(final CloseableIterator<Tuple> closeableIterator, final Function<Tuple, T> f)
    {
        return new TupleIterable<T>(closeableIterator, f, ClosePromise.NOOP);
    }

    /**
     * Use this method to map a result of QueryDSL tuples into domain objects.
     * <p/>
     * This will return an Iterable that closes itself after the last element has been retrieved.  This allows you to
     * use for each syntax to iterate database results without bring them all into memory at the one time
     *
     * @param closeableIterator the closeable iterator that was returned from a QueryDSL
     * @param f a function to map the QueryDSL tuples into actual domain objects
     * @param closePromise this is the code you want to run when the underlying objects are closed
     * @param <T> the desired domain class
     * @return an Iterable that closes itself when the last record is retrieved.
     */
    public static <T> CloseableIterable<T> map(final CloseableIterator<Tuple> closeableIterator, final Function<Tuple, T> f, final ClosePromise closePromise)
    {
        return new TupleIterable<T>(closeableIterator, f, closePromise);
    }


    /**
     * This for each construct is a call back for every row returned.  When the last element is retrieved the database
     * iterator is closed.
     *
     * @param closeableIterator the closeable iterator that was returned from a QueryDSL
     * @param effect a Fugue call back effect
     */
    public static void foreach(final CloseableIterator<Tuple> closeableIterator, final Effect<Tuple> effect)
    {
        foreach(closeableIterator, effect, ClosePromise.NOOP);
    }

    /**
     * This for each construct is a call back for every row returned.  When the last element is retrieved the database
     * iterator is closed.
     *
     * @param closeableIterator the closeable iterator that was returned from a QueryDSL
     * @param effect a Fugue call back effect
     * @param closePromise an effect to run when the underlying code is finished
     */
    public static void foreach(final CloseableIterator<Tuple> closeableIterator, final Effect<Tuple> effect, final ClosePromise closePromise)
    {
        try
        {
            while (closeableIterator.hasNext())
            {
                Tuple t = closeableIterator.next();
                effect.apply(t);
            }
        }
        finally
        {
            closeQuietly(closeableIterator);
            closePromise.close();
        }
    }

    private static void closeQuietly(final CloseableIterator<Tuple> closeableIterator)
    {
        try
        {
            closeableIterator.close();
        }
        catch (Exception ignored)
        {
        }
    }


    private static class TupleIterable<T> implements CloseableIterable<T>
    {
        private final CloseableIterator<Tuple> closeableIterator;
        private final Function<Tuple, T> mapper;
        private final ClosePromise closePromise;

        private TupleIterable(CloseableIterator<Tuple> closeableIterator, Function<Tuple, T> mapper, final ClosePromise parentPromise)
        {
            this.closeableIterator = closeableIterator;
            this.mapper = mapper;
            this.closePromise = new ClosePromise(parentPromise)
            {
                @Override
                protected void closeEffect()
                {
                    closeImpl();
                }
            };
        }

        @Override
        public CloseableIterator<T> iterator()
        {
            return new TupleIterator();
        }

        @Override
        public void close()
        {
            closePromise.close();
        }

        private void closeImpl()
        {
            closeableIterator.close();
        }

        class TupleIterator implements CloseableIterator<T>
        {
            @Override
            public boolean hasNext()
            {
                if (!closePromise.isClosed())
                {
                    if (closeableIterator.hasNext())
                    {
                        return true;
                    }
                    close();
                }
                return false;
            }

            @Override
            public T next()
            {
                // the underlying iterator handles no such element exception
                Tuple next = closeableIterator.next();
                return mapper.apply(next);
            }


            @Override
            public void close()
            {
                closePromise.close();
            }

            @Override
            public void remove()
            {
                throw new UnsupportedOperationException();
            }
        }
    }
}

