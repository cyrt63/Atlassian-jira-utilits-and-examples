package com.atlassian.pocketknife.api.querydsl;

import com.atlassian.annotations.PublicApi;
import com.mysema.commons.lang.Pair;
import com.mysema.query.QueryMetadata;
import com.mysema.query.sql.Configuration;
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

import static org.apache.commons.lang.StringUtils.isNotBlank;

/**
 * A logger implementation you can use to cause QueryDSL SQL messages to be logged as they are executed.
 */
@PublicApi
public class LoggingSqlListener implements SQLListener
{
    private static final Logger log = LoggerFactory.getLogger(LoggingSqlListener.class);

    private final Configuration configuration;

    public LoggingSqlListener(Configuration configuration)
    {
        this.configuration = configuration;
    }

    @Override
    public void notifyQuery(final QueryMetadata md)
    {
        if(weShouldLog())
        {
            log(getSelectSql(md));
        }
    }

    @Override
    public void notifyDelete(final RelationalPath<?> entity, final QueryMetadata md)
    {
        if(weShouldLog())
        {
            log(getDeleteSql(entity, md));
        }
    }

    @Override
    public void notifyDeletes(final RelationalPath<?> entity, final List<QueryMetadata> batches)
    {
        if(weShouldLog())
        {
            log(getBatchDeleteSql(entity, batches));
        }
    }

    @Override
    public void notifyMerge(final RelationalPath<?> entity, final QueryMetadata md, final List<Path<?>> keys, final List<Path<?>> columns, final List<Expression<?>> values, final SubQueryExpression<?> subQuery)
    {
        if(weShouldLog())
        {
            log(getMergeSql(entity, md, keys, columns, values, subQuery));
        }
    }

    @Override
    public void notifyMerges(final RelationalPath<?> entity, final QueryMetadata md, final List<SQLMergeBatch> batches)
    {
        if(weShouldLog())
        {
            log(getBatchMergeSql(entity, md, batches));
        }
    }

    @Override
    public void notifyInsert(final RelationalPath<?> entity, final QueryMetadata md, final List<Path<?>> columns, final List<Expression<?>> values, final SubQueryExpression<?> subQuery)
    {
        if(weShouldLog())
        {
            log(getInsertSql(entity, md, columns, values, subQuery));
        }
    }

    @Override
    public void notifyInserts(final RelationalPath<?> entity, final QueryMetadata md, final List<SQLInsertBatch> batches)
    {
        if(weShouldLog())
        {
            log(getBatchInsertSql(entity, md, batches));
        }
    }

    @Override
    public void notifyUpdate(final RelationalPath<?> entity, final QueryMetadata md, final List<Pair<Path<?>, Expression<?>>> updates)
    {
        if(weShouldLog())
        {
            log(getUpdateSql(entity, md, updates));
        }
    }

    @Override
    public void notifyUpdates(final RelationalPath<?> entity, final List<SQLUpdateBatch> batches)
    {
        if(weShouldLog())
        {
            log(getBatchUpdateSql(entity, batches));
        }
    }

    private boolean weShouldLog()
    {
        return log.isDebugEnabled();
    }

    private void log(final String sql)
    {
        if(isNotBlank(sql))
        {
            log.debug(sql);
        }
    }

    private String getSelectSql(final QueryMetadata queryMetadata)
    {
        SQLSerializer serializer = newLiteralPrintingSerializer();
        serializer.serialize(queryMetadata, false);
        return serializer.toString();
    }

    private String getDeleteSql(final RelationalPath<?> entity, final QueryMetadata queryMetadata)
    {
        SQLSerializer serializer = newLiteralPrintingSerializer();
        serializer.serializeDelete(queryMetadata, entity);
        return serializer.toString();
    }

    private String getBatchDeleteSql(final RelationalPath<?> entity, final List<QueryMetadata> deleteQueriesMetadata)
    {
        StringBuilder batchDeleteQueries = new StringBuilder();
        batchDeleteQueries.append("<Start batch delete> ");
        for(QueryMetadata queryMetadata : deleteQueriesMetadata)
        {
            batchDeleteQueries.append(getDeleteSql(entity, queryMetadata));
            batchDeleteQueries.append("; ");
        }
        batchDeleteQueries.append(" <End batch delete>");
        return batchDeleteQueries.toString();
    }

    private String getMergeSql(final RelationalPath<?> entity, final QueryMetadata md, final List<Path<?>> keys, final List<Path<?>> columns, final List<Expression<?>> values, final SubQueryExpression<?> subQuery)
    {
        SQLSerializer serializer = newLiteralPrintingSerializer();
        serializer.serializeMerge(md, entity, keys, columns, values, subQuery);
        return serializer.toString();
    }

    private String getBatchMergeSql(final RelationalPath<?> entity, final QueryMetadata md, final List<SQLMergeBatch> mergeBatches)
    {
        StringBuilder batchMergeQueries = new StringBuilder();
        batchMergeQueries.append("<Start batch merge> ");
        for(SQLMergeBatch mergeBatch : mergeBatches)
        {
            batchMergeQueries.append(getMergeSql(entity, md, mergeBatch.getKeys(), mergeBatch.getColumns(), mergeBatch.getValues(), mergeBatch.getSubQuery()));
            batchMergeQueries.append("; ");
        }
        batchMergeQueries.append(" <End batch merge>");
        return batchMergeQueries.toString();
    }

    private String getInsertSql(final RelationalPath<?> entity, final QueryMetadata md, final List<Path<?>> columns, final List<Expression<?>> values, final SubQueryExpression<?> subQuery)
    {
        SQLSerializer serializer = newLiteralPrintingSerializer();
        serializer.serializeInsert(md, entity, columns, values, subQuery);
        return serializer.toString();
    }

    private String getBatchInsertSql(final RelationalPath<?> entity, final QueryMetadata md, final List<SQLInsertBatch> insertBatches)
    {
        StringBuilder batchInsertQueries = new StringBuilder();
        batchInsertQueries.append("<Start batch insert> ");
        for(SQLInsertBatch insertBatch : insertBatches)
        {
            batchInsertQueries.append(getInsertSql(entity, md, insertBatch.getColumns(), insertBatch.getValues(), insertBatch.getSubQuery()));
            batchInsertQueries.append("; ");
        }
        batchInsertQueries.append(" <End batch insert>");
        return batchInsertQueries.toString();
    }

    private String getUpdateSql(final RelationalPath<?> entity, final QueryMetadata md, final List<Pair<Path<?>, Expression<?>>> updates)
    {
        SQLSerializer serializer = newLiteralPrintingSerializer();
        serializer.serializeUpdate(md, entity, updates);
        return serializer.toString();
    }

    private String getBatchUpdateSql(final RelationalPath<?> entity, final List<SQLUpdateBatch> updateBatches)
    {
        StringBuilder batchUpdateQueries = new StringBuilder();
        batchUpdateQueries.append("<Start batch update> ");
        for(SQLUpdateBatch updateBatch : updateBatches)
        {
            batchUpdateQueries.append(getUpdateSql(entity, updateBatch.getMetadata(), updateBatch.getUpdates()));
            batchUpdateQueries.append("; ");
        }
        batchUpdateQueries.append(" <End batch update>");
        return batchUpdateQueries.toString();
    }

    private SQLSerializer newLiteralPrintingSerializer()
    {
        SQLSerializer literalPrintingSerializer = new SQLSerializer(configuration);
        literalPrintingSerializer.setUseLiterals(true);
        return literalPrintingSerializer;
    }
}
