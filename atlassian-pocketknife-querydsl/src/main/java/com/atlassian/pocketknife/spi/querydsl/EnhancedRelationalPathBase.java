package com.atlassian.pocketknife.spi.querydsl;

import com.atlassian.pocketknife.internal.querydsl.SchemaProviderAccessor;
import com.mysema.query.sql.ColumnMetadata;
import com.mysema.query.sql.PrimaryKey;
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

import java.util.List;

import static com.google.common.collect.Iterables.toArray;
import static com.google.common.collect.Lists.newArrayList;

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
 *     <li>
 *         Adds convenience methods like {@link #createNumberAndSetAsPrimaryKey(String, Class)} and
 *         {@link #getColumnsWithoutPrimaryKey()}.
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

    public Path<?>[] getColumnsWithoutPrimaryKey()
    {
        PrimaryKey<T> primaryKey = getPrimaryKey();
        if(primaryKey == null)
        {
            throw new IllegalStateException("Unable to get columns without primary key. No primary key was set.");
        }

        List<Path<?>> columnsWithoutPrimaryKey = newArrayList(getColumns());
        columnsWithoutPrimaryKey.removeAll(primaryKey.getLocalColumns());
        return toArray(columnsWithoutPrimaryKey, Path.class);
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
        TimePath<A> created = super.createTime(property, type);
        addToMetadata(created, property);
        return created;
    }

    @Override
    protected <A, E> ArrayPath<A, E> createArray(final String property, final Class<? super A> type)
    {
        ArrayPath<A, E> created = super.createArray(property, type);
        addToMetadata(created, property);
        return created;
    }

    @Override
    protected BooleanPath createBoolean(final String property)
    {
        BooleanPath created = super.createBoolean(property);
        addToMetadata(created, property);
        return created;
    }

    @Override
    protected <A, Q extends SimpleExpression<? super A>> CollectionPath<A, Q> createCollection(final String property, final Class<? super A> type, final Class<? super Q> queryType, final PathInits inits)
    {
        CollectionPath<A, Q> created = super.createCollection(property, type, queryType, inits);
        addToMetadata(created, property);
        return created;
    }

    @Override
    protected <A extends Comparable> ComparablePath<A> createComparable(final String property, final Class<? super A> type)
    {
        ComparablePath<A> created = super.createComparable(property, type);
        addToMetadata(created, property);
        return created;
    }

    @Override
    protected <A extends Enum<A>> EnumPath<A> createEnum(final String property, final Class<A> type)
    {
        EnumPath<A> created = super.createEnum(property, type);
        addToMetadata(created, property);
        return created;
    }

    @Override
    protected <A extends Comparable> DatePath<A> createDate(final String property, final Class<? super A> type)
    {
        DatePath<A> created = super.createDate(property, type);
        addToMetadata(created, property);
        return created;
    }

    @Override
    protected <A extends Comparable> DateTimePath<A> createDateTime(final String property, final Class<? super A> type)
    {
        DateTimePath<A> created = super.createDateTime(property, type);
        addToMetadata(created, property);
        return created;
    }

    @Override
    protected <A, E extends SimpleExpression<? super A>> ListPath<A, E> createList(final String property, final Class<? super A> type, final Class<? super E> queryType, final PathInits inits)
    {
        ListPath<A, E> created = super.createList(property, type, queryType, inits);
        addToMetadata(created, property);
        return created;
    }

    @Override
    protected <K, V, E extends SimpleExpression<? super V>> MapPath<K, V, E> createMap(final String property, final Class<? super K> key, final Class<? super V> value, final Class<? super E> queryType)
    {
        MapPath<K, V, E> created = super.createMap(property, key, value, queryType);
        addToMetadata(created, property);
        return created;
    }

    @Override
    protected <A extends Number & Comparable<?>> NumberPath<A> createNumber(final String property, final Class<? super A> type)
    {
        NumberPath<A> created = super.createNumber(property, type);
        addToMetadata(created, property);
        return created;
    }

    @Override
    protected <A, E extends SimpleExpression<? super A>> SetPath<A, E> createSet(final String property, final Class<? super A> type, final Class<? super E> queryType, final PathInits inits)
    {
        SetPath<A, E> created = super.createSet(property, type, queryType, inits);
        addToMetadata(created, property);
        return created;
    }

    @Override
    protected <A> SimplePath<A> createSimple(final String property, final Class<? super A> type)
    {
        SimplePath<A> created = super.createSimple(property, type);
        addToMetadata(created, property);
        return created;
    }

    @Override
    protected StringPath createString(final String property)
    {
        StringPath created = super.createString(property);
        addToMetadata(created, property);
        return created;
    }

    private <P extends Path> void addToMetadata(final P path, String property)
    {
        addMetadata(path, ColumnMetadata.named(property));
    }
}
