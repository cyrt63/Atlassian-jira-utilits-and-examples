package com.atlassian.pocketknife.api.querydsl;

import com.atlassian.fugue.Function2;
import com.google.common.base.Function;
import com.mysema.query.Tuple;

import java.io.Closeable;

/**
 * A StreamedResult is one that does NOT load all the SQL data into memory but rather lazily maps from database result
 * objects into domain objects.
 * <p/>
 * Its uber important that you close this object (either via the mapped iterable or directly on this object) to ensure
 * that connections and result sets are properly closed.
 */
public interface StreamyResult extends Closeable
{
    /**
     * This will map a fetch query of Tuple objects into an Iterable of domain object T
     *
     * @param mapper the tuple mapper function
     * @param <D> the domain type
     * @return and iterable of domain objects
     */
    <D> CloseableIterable<D> map(Function<Tuple, D> mapper);
    
    /**
     * Fold the function f over the query and return the accumulated value, executes immediately and closes the
     * iterable.
     * 
     * @param initial The initial value to be passed to f
     * @param f A function that takes a tuple and the accumulating value and returns the new accumulating value
     * @return The result of applying f to each element in the list starting with #initial and passing in the result of
     *         the previous call as it traverses the result.
     */
    <O> O foldLeft(O initial, Function2<O, Tuple, O> f);

    /**
     * Closes this result and releases any system resources associated with it. If the object is already closed then
     * invoking this method has no effect.
     */
    void close();

}
