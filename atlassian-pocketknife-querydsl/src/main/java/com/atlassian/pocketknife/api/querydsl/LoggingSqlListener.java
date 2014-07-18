package com.atlassian.pocketknife.api.querydsl;

import com.mysema.commons.lang.Pair;
import com.mysema.query.QueryMetadata;
import com.mysema.query.sql.RelationalPath;
import com.mysema.query.sql.SQLListener;
import com.mysema.query.sql.SQLSerializer;
import com.mysema.query.sql.dml.SQLInsertBatch;
import com.mysema.query.sql.dml.SQLMergeBatch;
import com.mysema.query.sql.dml.SQLUpdateBatch;
import com.mysema.query.types.Expression;
import com.mysema.query.types.Path;
import com.mysema.query.types.SubQueryExpression;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * A logger implementation you can use to cause QueryDSL SQL messages to be logged as they are executed
 * <p/>
 * Note : This does not log all the values on the batch methods.
 */
public class LoggingSqlListener implements SQLListener
{
    private final DialectProvider dialectProvider;
    private final String loggerNameToUse;

    public LoggingSqlListener(final DialectProvider dialectProvider, String loggerNameToUse)
    {
        this.dialectProvider = dialectProvider;
        this.loggerNameToUse = loggerNameToUse;
    }

    private Logger log()
    {
        return LoggerFactory.getLogger(loggerNameToUse);
    }

    private void log(final RelationalPath<?> entity, final QueryMetadata md)
    {
        StringBuilder sb = new StringBuilder();
        if (entity != null)
        {
            sb.append(entity.getTableName());
        }
        if (sb.length() > 0)
        {
            sb.append(" ");
        }
        if (md != null)
        {
            SQLSerializer sqlSerializer = new SQLSerializer(dialectProvider.getDialectConfig().getConfiguration());
            sqlSerializer.serialize(md, false);
            String sql = sqlSerializer.toString();
            sb.append(sql);
        }
        if (sb.length() > 0)
        {
            log().debug(sb.toString());
        }
    }

    @Override
    public void notifyQuery(final QueryMetadata md)
    {
        log(null, md);
    }

    @Override
    public void notifyDelete(final RelationalPath<?> entity, final QueryMetadata md)
    {
        log(entity, md);
    }

    @Override
    public void notifyDeletes(final RelationalPath<?> entity, final List<QueryMetadata> batches)
    {
        log(entity, null);
    }

    @Override
    public void notifyMerge(final RelationalPath<?> entity, final QueryMetadata md, final List<Path<?>> keys, final List<Path<?>> columns, final List<Expression<?>> values, final SubQueryExpression<?> subQuery)
    {
        log(entity, md);
    }

    @Override
    public void notifyMerges(final RelationalPath<?> entity, final QueryMetadata md, final List<SQLMergeBatch> batches)
    {
        log(entity, md);
    }

    @Override
    public void notifyInsert(final RelationalPath<?> entity, final QueryMetadata md, final List<Path<?>> columns, final List<Expression<?>> values, final SubQueryExpression<?> subQuery)
    {
        log(entity, md);
    }

    @Override
    public void notifyInserts(final RelationalPath<?> entity, final QueryMetadata md, final List<SQLInsertBatch> batches)
    {
        log(entity, md);
    }

    @Override
    public void notifyUpdate(final RelationalPath<?> entity, final QueryMetadata md, final List<Pair<Path<?>, Expression<?>>> updates)
    {
        log(entity, md);
    }

    @Override
    public void notifyUpdates(final RelationalPath<?> entity, final List<SQLUpdateBatch> batches)
    {
        log(entity, null);
    }
}
