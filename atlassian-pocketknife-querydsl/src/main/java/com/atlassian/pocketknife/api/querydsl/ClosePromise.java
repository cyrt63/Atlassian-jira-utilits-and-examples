package com.atlassian.pocketknife.api.querydsl;

import java.io.Closeable;
import javax.annotation.concurrent.NotThreadSafe;

/**
 * A ClosePromise will call the {@link #closeEffect()} once and only once.
 * <p/>
 * This promise is not thread safe.
 */
@NotThreadSafe
public abstract class ClosePromise implements Closeable
{
    public static final ClosePromise NOOP = new ClosePromise()
    {
        @Override
        protected void closeEffect()
        {
        }
    };

    private boolean closed = false;
    private ClosePromise parentPromise;

    protected ClosePromise()
    {
    }

    /**
     * You can chain together promises such that the child is closed first then the parent
     *
     * @param parentPromise the parent to close after yourself
     */
    protected ClosePromise(ClosePromise parentPromise)
    {
        this.parentPromise = parentPromise;
    }

    @Override
    public void close()
    {
        if (!closed)
        {
            closed = true;
            closeEffect();
        }
        if (parentPromise != null)
        {
            parentPromise.close();
        }
    }

    public boolean isClosed()
    {
        return closed;
    }

    protected abstract void closeEffect();
}
