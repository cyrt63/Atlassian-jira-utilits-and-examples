package com.atlassian.pocketknife.api.querydsl;

import com.mysema.commons.lang.CloseableIterator;

import java.io.Closeable;

/**
 * A closeable iterable can be closed.  This is uber important in the SQL world because connections and results MUST be
 * closed when the results have been consumed.
 * <p/>
 * They must also be closed in try finally blocks as well for defensive reasons such as your code throwing exceptions
 * during its processing
 */
public interface CloseableIterable<T> extends Iterable<T>, Closeable
{
    /**
     * @return the closeable iterator used under the covers
     */
    CloseableIterator<T> iterator();

    /**
     * Closes this iterator and releases any system resources associated with it. If the iterator is already closed then
     * invoking this method has no effect.
     */
    void close();

}
