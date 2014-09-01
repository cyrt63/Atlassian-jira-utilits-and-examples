package com.atlassian.pocketknife.api.querydsl;

import com.atlassian.fugue.Effect;
import com.atlassian.pocketknife.internal.querydsl.StreamyResultImpl;
import com.mysema.commons.lang.CloseableIterator;
import com.mysema.query.JoinFlag;
import com.mysema.query.QueryFlag;
import com.mysema.query.QueryMetadata;
import com.mysema.query.QueryModifiers;
import com.mysema.query.ResultTransformer;
import com.mysema.query.SearchResults;
import com.mysema.query.Tuple;
import com.mysema.query.sql.ForeignKey;
import com.mysema.query.sql.RelationalFunctionCall;
import com.mysema.query.sql.RelationalPath;
import com.mysema.query.sql.SQLBindings;
import com.mysema.query.sql.SQLListener;
import com.mysema.query.sql.SQLQuery;
import com.mysema.query.sql.Union;
import com.mysema.query.types.EntityPath;
import com.mysema.query.types.Expression;
import com.mysema.query.types.OrderSpecifier;
import com.mysema.query.types.ParamExpression;
import com.mysema.query.types.Path;
import com.mysema.query.types.Predicate;
import com.mysema.query.types.SubQueryExpression;
import com.mysema.query.types.query.ListSubQuery;

import java.sql.ResultSet;
import java.util.List;
import javax.annotation.Nonnegative;

/**
 * SelectQuery is a re-implementation of {@link com.mysema.query.sql.SQLQuery} but with extra mapping methods added to
 * make it easier help write slightly more functional result set consumption.
 * <p/>
 * It also has support under the covers for {@link com.atlassian.pocketknife.api.querydsl.StreamyResult} so that the
 * connection / statement resource aspects are handled
 */
public class SelectQuery
{
    private final com.mysema.query.sql.SQLQuery delegate;
    private final ClosePromise closeEffect;

    public SelectQuery(final SQLQuery delegate)
    {
        this(delegate, ClosePromise.NOOP);
    }

    public SelectQuery(final SQLQuery delegate, final ClosePromise closeEffect)
    {
        this.delegate = delegate;
        this.closeEffect = closeEffect;
    }

    /**
     * Call this to get a memory efficient stream of database results.
     *
     * @param args the database paths to select
     * @return a streamy result that needs to be closed
     */
    public StreamyResult stream(final Expression<?>... args)
    {
        final CloseableIterator<Tuple> iterator = iterate(args);
        return new StreamyResultImpl(iterator, closeEffect);
    }

    public void foreach(final CloseableIterator<Tuple> closeableIterator, final Effect<Tuple> effect)
    {
        Tuples.foreach(closeableIterator, effect, closeEffect);
    }


    /* ---------------------------------------------------------------
     * Delegate methods
     * --------------------------------------------------------------- */

    public void addListener(final SQLListener listener)
    {
        delegate.addListener(listener);
    }


    public long count()
    {
        return delegate.count();
    }

    public SelectQuery forUpdate()
    {
        delegate.forUpdate();
        return this;
    }

    public ResultSet getResults(final Expression<?>... exprs)
    {
        return delegate.getResults(exprs);
    }

    public <RT> CloseableIterator<RT> iterate(final Expression<RT> expr)
    {
        return delegate.iterate(expr);
    }


    public <RT> List<RT> list(final Expression<RT> expr)
    {
        return delegate.list(expr);
    }


    public <RT> SearchResults<RT> listResults(final Expression<RT> expr)
    {
        return delegate.listResults(expr);
    }

    public <RT> RT uniqueResult(final Expression<RT> expr)
    {
        return delegate.uniqueResult(expr);
    }

    public void setUseLiterals(final boolean useLiterals)
    {
        delegate.setUseLiterals(useLiterals);
    }


    public SelectQuery addJoinFlag(final String flag)
    {
        delegate.addJoinFlag(flag);
        return this;
    }

    public SelectQuery addJoinFlag(final String flag, final JoinFlag.Position position)
    {
        delegate.addJoinFlag(flag, position);
        return this;
    }

    public SelectQuery addFlag(final QueryFlag.Position position, final String prefix, final Expression<?> expr)
    {
        delegate.addFlag(position, prefix, expr);
        return this;
    }


    public SelectQuery addFlag(final QueryFlag flag)
    {
        delegate.addFlag(flag);
        return this;
    }


    public SelectQuery addFlag(final QueryFlag.Position position, final String flag)
    {
        delegate.addFlag(position, flag);
        return this;
    }


    public SelectQuery addFlag(final QueryFlag.Position position, final Expression<?> flag)
    {
        delegate.addFlag(position, flag);
        return this;
    }


    public boolean exists()
    {
        return delegate.exists();
    }


    public SelectQuery from(final Expression<?> arg)
    {
        delegate.from(arg);
        return this;
    }


    public SelectQuery from(final Expression<?>... args)
    {
        delegate.from(args);
        return this;
    }


    public SelectQuery from(final SubQueryExpression<?> subQuery, final Path<?> alias)
    {
        delegate.from(subQuery, alias);
        return this;
    }


    public SelectQuery fullJoin(final EntityPath<?> target)
    {
        delegate.fullJoin(target);
        return this;
    }


    public <E> SelectQuery fullJoin(final RelationalFunctionCall<E> target, final Path<E> alias)
    {
        delegate.fullJoin(target, alias);
        return this;
    }


    public SelectQuery fullJoin(final SubQueryExpression<?> target, final Path<?> alias)
    {
        delegate.fullJoin(target, alias);
        return this;
    }


    public <E> SelectQuery fullJoin(final ForeignKey<E> key, final RelationalPath<E> entity)
    {
        delegate.fullJoin(key, entity);
        return this;
    }


    public SelectQuery innerJoin(final EntityPath<?> target)
    {
        delegate.innerJoin(target);
        return this;
    }


    public <E> SelectQuery innerJoin(final RelationalFunctionCall<E> target, final Path<E> alias)
    {
        delegate.innerJoin(target, alias);
        return this;
    }


    public SelectQuery innerJoin(final SubQueryExpression<?> target, final Path<?> alias)
    {
        delegate.innerJoin(target, alias);
        return this;
    }


    public <E> SelectQuery innerJoin(final ForeignKey<E> key, final RelationalPath<E> entity)
    {
        delegate.innerJoin(key, entity);
        return this;
    }


    public SelectQuery join(final EntityPath<?> target)
    {
        delegate.join(target);
        return this;
    }


    public <E> SelectQuery join(final RelationalFunctionCall<E> target, final Path<E> alias)
    {
        delegate.join(target, alias);
        return this;
    }


    public SelectQuery join(final SubQueryExpression<?> target, final Path<?> alias)
    {
        delegate.join(target, alias);
        return this;
    }


    public <E> SelectQuery join(final ForeignKey<E> key, final RelationalPath<E> entity)
    {
        delegate.join(key, entity);
        return this;
    }


    public SelectQuery leftJoin(final EntityPath<?> target)
    {
        delegate.leftJoin(target);
        return this;
    }


    public <E> SelectQuery leftJoin(final RelationalFunctionCall<E> target, final Path<E> alias)
    {
        delegate.leftJoin(target, alias);
        return this;
    }


    public SelectQuery leftJoin(final SubQueryExpression<?> target, final Path<?> alias)
    {
        delegate.leftJoin(target, alias);
        return this;
    }


    public <E> SelectQuery leftJoin(final ForeignKey<E> key, final RelationalPath<E> entity)
    {
        delegate.leftJoin(key, entity);
        return this;
    }


    public SelectQuery rightJoin(final EntityPath<?> target)
    {
        delegate.rightJoin(target);
        return this;
    }


    public <E> SelectQuery rightJoin(final RelationalFunctionCall<E> target, final Path<E> alias)
    {
        delegate.rightJoin(target, alias);
        return this;
    }


    public SelectQuery rightJoin(final SubQueryExpression<?> target, final Path<?> alias)
    {
        delegate.rightJoin(target, alias);
        return this;
    }


    public <E> SelectQuery rightJoin(final ForeignKey<E> key, final RelationalPath<E> entity)
    {
        delegate.rightJoin(key, entity);
        return this;
    }


    public QueryMetadata getMetadata()
    {
        return delegate.getMetadata();
    }


    public CloseableIterator<Tuple> iterate(final Expression<?>... args)
    {
        return delegate.iterate(args);
    }


    public List<Tuple> list(final Expression<?>... args)
    {
        return delegate.list(args);
    }


    public SearchResults<Tuple> listResults(final Expression<?>... args)
    {
        return delegate.listResults(args);
    }


    public SelectQuery on(final Predicate condition)
    {
        delegate.on(condition);
        return this;
    }


    public SelectQuery on(final Predicate... conditions)
    {
        delegate.on(conditions);
        return this;
    }


    public <RT> Union<RT> union(final ListSubQuery<RT>... sq)
    {
        return delegate.union(sq);
    }


    public <RT> SelectQuery union(final Path<?> alias, final ListSubQuery<RT>... sq)
    {
        delegate.union(alias, sq);
        return this;
    }


    public <RT> Union<RT> union(final SubQueryExpression<RT>... sq)
    {
        return delegate.union(sq);
    }


    public <RT> SelectQuery union(final Path<?> alias, final SubQueryExpression<RT>... sq)
    {
        delegate.union(alias, sq);
        return this;
    }


    public <RT> Union<RT> unionAll(final ListSubQuery<RT>... sq)
    {
        return delegate.unionAll(sq);
    }


    public <RT> SelectQuery unionAll(final Path<?> alias, final ListSubQuery<RT>... sq)
    {
        delegate.unionAll(alias, sq);
        return this;
    }


    public <RT> Union<RT> unionAll(final SubQueryExpression<RT>... sq)
    {
        return delegate.unionAll(sq);
    }


    public <RT> SelectQuery unionAll(final Path<?> alias, final SubQueryExpression<RT>... sq)
    {
        delegate.unionAll(alias, sq);
        return this;
    }


    public Tuple uniqueResult(final Expression<?>... args)
    {
        return delegate.uniqueResult(args);
    }


    public SQLBindings getSQL(final Expression<?>... exprs)
    {
        return delegate.getSQL(exprs);
    }


    public String toString()
    {
        return delegate.toString();
    }


    public <T> T transform(final ResultTransformer<T> transformer)
    {
        return delegate.transform(transformer);
    }


    public SelectQuery distinct()
    {
        delegate.distinct();
        return this;
    }


    public SelectQuery groupBy(final Expression<?> e)
    {
        delegate.groupBy(e);
        return this;
    }


    public SelectQuery groupBy(final Expression<?>... o)
    {
        delegate.groupBy(o);
        return this;
    }


    public SelectQuery having(final Predicate e)
    {
        delegate.having(e);
        return this;
    }


    public SelectQuery having(final Predicate... o)
    {
        delegate.having(o);
        return this;
    }


    public SelectQuery orderBy(final OrderSpecifier<?> o)
    {
        delegate.orderBy(o);
        return this;
    }


    public SelectQuery orderBy(final OrderSpecifier<?>... o)
    {
        delegate.orderBy(o);
        return this;
    }


    public SelectQuery where(final Predicate o)
    {
        delegate.where(o);
        return this;
    }


    public SelectQuery where(final Predicate... o)
    {
        delegate.where(o);
        return this;
    }


    public SelectQuery limit(@Nonnegative final long limit)
    {
        delegate.limit(limit);
        return this;
    }


    public SelectQuery offset(final long offset)
    {
        delegate.offset(offset);
        return this;
    }


    public SelectQuery restrict(final QueryModifiers modifiers)
    {
        delegate.restrict(modifiers);
        return this;
    }


    public <P> SelectQuery set(final ParamExpression<P> param, final P value)
    {
        delegate.set(param, value);
        return this;
    }

    @SuppressWarnings ("EqualsWhichDoesntCheckParameterClass")

    public boolean equals(final Object o)
    {
        return delegate.equals(o);
    }


    public int hashCode()
    {
        return delegate.hashCode();
    }
}
