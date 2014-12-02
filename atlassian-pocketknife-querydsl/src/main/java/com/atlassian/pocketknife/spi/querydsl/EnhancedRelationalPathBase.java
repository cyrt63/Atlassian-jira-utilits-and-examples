package com.atlassian.pocketknife.spi.querydsl;

import com.atlassian.pocketknife.internal.querydsl.SchemaProviderAccessor;
import com.mysema.query.sql.ColumnMetadata;
import com.mysema.query.sql.RelationalPathBase;
import com.mysema.query.sql.SchemaAndTable;
import com.mysema.query.types.Path;
import com.mysema.query.types.expr.SimpleExpression;
import com.mysema.query.types.path.ArrayPath;
import com.mysema.query.types.path.BooleanPath;
import com.mysema.query.types.path.CollectionPath;
import com.mysema.query.types.path.ComparablePath;
import com.mysema.query.types.path.DatePath;
import com.mysema.query.types.path.DateTimePath;
import com.mysema.query.types.path.EnumPath;
import com.mysema.query.types.path.ListPath;
import com.mysema.query.types.path.MapPath;
import com.mysema.query.types.path.NumberPath;
import com.mysema.query.types.path.PathInits;
import com.mysema.query.types.path.SetPath;
import com.mysema.query.types.path.SimplePath;
import com.mysema.query.types.path.StringPath;
import com.mysema.query.types.path.TimePath;

/**
 * Enhances {@link RelationalPathBase} with additional functionality designed to make writing and executing queries
 * using table entities that extend this class easier.
 * <p/>
 * This extra functionality includes:
 * <ul>
 *     <li>
 *         Dynamic schema lookup: QueryDSL table classes that extend this class can be instantiated without specifying
 *         a schema. This allows them to be declared as static variables and used at call sites and yet still have the
 *         capability of knowing the underlying schema name which is not known until runtime.
 *     </li>
 *     <li>
 *         Implicitly adding any columns defined by a call to the create methods to the metadata for table entity. This
 *         allows calls to methods like {@link com.mysema.query.sql.RelationalPathBase#getColumns()} to actually work
 *         correctly.
 *     </li>
 * </ul>
 */
public abstract class EnhancedRelationalPathBase<T> extends RelationalPathBase<T>
{
    public EnhancedRelationalPathBase(
            final Class<? extends T> type,
            final String tableName)
    {
        super(type, tableName, "", tableName);
    }

    /**
     * Creates a new number path (column), and then marks this column as the primary key.
     * <p/>
     * Knowing which column is the primary key helps QueryDSL to optimize the SQL queries it generates.
     *
     * @return the newly created number path
     */
    protected <A extends Number & Comparable<?>> NumberPath<A> createNumberAndSetAsPrimaryKey(final String property, final Class<? super A> type)
    {
        NumberPath<A> createdColumn = createNumber(property, type);
        createPrimaryKey(createdColumn);
        return createdColumn;
    }

    @Override
    public SchemaAndTable getSchemaAndTable()
    {
        return new SchemaAndTable(
                getSchemaName(),
                getTableName()
        );
    }

    @Override
    public String getSchemaName()
    {
        return SchemaProviderAccessor.getSchemaProvider().getSchema(getTableName());
    }

    @Override
    protected <A extends Comparable> TimePath<A> createTime(final String property, final Class<? super A> type)
    {
        return addToMetadataAndReturn(
                super.createTime(property, type),
                property);
    }

    @Override
    @SuppressWarnings("unchecked") // We know for sure that super.createArray() returns an ArrayPath<A, E>
    protected <A, E> ArrayPath<A, E> createArray(final String property, final Class<? super A> type)
    {
        return addToMetadataAndReturn(
                (ArrayPath<A, E>) super.createArray(property, type),
                property);
    }

    @Override
    protected BooleanPath createBoolean(final String property)
    {
        return addToMetadataAndReturn(
                super.createBoolean(property),
                property);
    }

    @Override
    protected <A, Q extends SimpleExpression<? super A>> CollectionPath<A, Q> createCollection(final String property, final Class<? super A> type, final Class<? super Q> queryType, final PathInits inits)
    {
        return addToMetadataAndReturn(
                super.createCollection(property, type, queryType, inits),
                property);
    }

    @Override
    protected <A extends Comparable> ComparablePath<A> createComparable(final String property, final Class<? super A> type)
    {
        return addToMetadataAndReturn(
                super.createComparable(property, type),
                property);
    }

    @Override
    protected <A extends Enum<A>> EnumPath<A> createEnum(final String property, final Class<A> type)
    {
        return addToMetadataAndReturn(
                super.createEnum(property, type),
                property);
    }

    @Override
    protected <A extends Comparable> DatePath<A> createDate(final String property, final Class<? super A> type)
    {
        return addToMetadataAndReturn(
                super.createDate(property, type),
                property);
    }

    @Override
    protected <A extends Comparable> DateTimePath<A> createDateTime(final String property, final Class<? super A> type)
    {
        return addToMetadataAndReturn(
                super.createDateTime(property, type),
                property);
    }

    @Override
    protected <A, E extends SimpleExpression<? super A>> ListPath<A, E> createList(final String property, final Class<? super A> type, final Class<? super E> queryType, final PathInits inits)
    {
        return addToMetadataAndReturn(
                super.createList(property, type, queryType, inits),
                property);
    }

    @Override
    protected <K, V, E extends SimpleExpression<? super V>> MapPath<K, V, E> createMap(final String property, final Class<? super K> key, final Class<? super V> value, final Class<? super E> queryType)
    {
        return addToMetadataAndReturn(
                super.createMap(property, key, value, queryType),
                property);
    }

    @Override
    protected <A extends Number & Comparable<?>> NumberPath<A> createNumber(final String property, final Class<? super A> type)
    {
        return addToMetadataAndReturn(
                super.createNumber(property, type),
                property);
    }

    @Override
    protected <A, E extends SimpleExpression<? super A>> SetPath<A, E> createSet(final String property, final Class<? super A> type, final Class<? super E> queryType, final PathInits inits)
    {
        return addToMetadataAndReturn(
                super.createSet(property, type, queryType, inits),
                property);
    }

    @Override
    protected <A> SimplePath<A> createSimple(final String property, final Class<? super A> type)
    {
        return addToMetadataAndReturn(
                super.createSimple(property, type),
                property);
    }

    @Override
    protected StringPath createString(final String property)
    {
        return addToMetadataAndReturn(
                super.createString(property),
                property);
    }

    private <P extends Path> P addToMetadataAndReturn(final P path, String property)
    {
        addMetadata(path, ColumnMetadata.named(property));
        return path;
    }
}
