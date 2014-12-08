package com.atlassian.pocketknife.spi.querydsl;

import com.atlassian.pocketknife.internal.querydsl.SchemaProviderAccessor;
import com.google.common.base.Predicate;
import com.mysema.query.sql.ColumnMetadata;
import com.mysema.query.sql.PrimaryKey;
import com.mysema.query.sql.RelationalPathBase;
import com.mysema.query.sql.SchemaAndTable;
import com.mysema.query.types.Path;
import com.mysema.query.types.path.BooleanPath;
import com.mysema.query.types.path.DatePath;
import com.mysema.query.types.path.DateTimePath;
import com.mysema.query.types.path.NumberPath;
import com.mysema.query.types.path.StringPath;
import com.mysema.query.types.path.TimePath;

import java.math.BigDecimal;
import java.sql.Types;
import java.util.Collections;
import java.util.List;

import static com.google.common.collect.Iterables.filter;
import static com.google.common.collect.Lists.newArrayList;

/**
 * @formatter:off
 *
 * Enhances {@link RelationalPathBase} with additional functionality designed to make writing and executing queries
 * using table entities that extend this class easier.
 * <p/>
 * This extra functionality includes:
 * <ul>
 *      <li>
 *      Dynamic schema lookup: QueryDSL table classes that extend this class can be instantiated without specifying
 *      a schema. This allows them to be declared as static variables and used at call sites and yet still have the
 *      capability of knowing the underlying schema name which is not known until runtime.
 * </li>
 * <li>
 *      Implicitly adding any columns defined by a call to the create methods to the metadata for table entity. This
 *      allows calls to methods like {@link com.mysema.query.sql.RelationalPathBase#getColumns()} to actually work
 *      correctly.
 * </li>
 * <li>
 *      Adds column builder patterns to allow very specific column paths to be constructed.  This allows for not null
 *      specification as well as primary key
 * </li>
 * <li>
 *      Adds convenience methods like {@link #getAllNonPrimaryKeyColumns()}
 * </li>
 * </ul>
 *
 * @formatter:off
 */
public abstract class EnhancedRelationalPathBase<T> extends RelationalPathBase<T>
{
    public EnhancedRelationalPathBase(
            final Class<? extends T> type,
            final String tableName)
    {
        super(type, tableName, "", tableName);
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


    //
    // the following override the base path construction methods so that metadata is added at the same time
    // as the path creation
    //

    /**
     * Creates a boolean column with sensible default metadata
     *
     * @param columnName the name of the column
     * @return the new path
     */
    @Override
    protected BooleanPath createBoolean(final String columnName)
    {
        BooleanPath path = super.createBoolean(columnName);
        addMetadata(path, ColumnMetadata.named(columnName).ofType(Types.BOOLEAN));
        return path;
    }

    /**
     * Creates a date column with sensible default metadata
     *
     * @param columnName the name of the column
     * @return the new path
     */
    @Override
    protected <A extends Comparable> DatePath<A> createDate(final String columnName, final Class<? super A> type)
    {
        DatePath<A> path = super.createDate(columnName, type);
        addMetadata(path, ColumnMetadata.named(columnName).ofType(Types.DATE));
        return path;
    }

    /**
     * Creates a datetime column with sensible default metadata
     *
     * @param columnName the name of the column
     * @return the new path
     */
    @Override
    protected <A extends Comparable> DateTimePath<A> createDateTime(final String columnName, final Class<? super A> type)
    {
        DateTimePath<A> path = super.createDateTime(columnName, type);
        addMetadata(path, ColumnMetadata.named(columnName).ofType(Types.TIMESTAMP));
        return path;
    }

    /**
     * Creates a number column with sensible default metadata.
     * <p/>
     * <em>Note:</em> this method should only be called when the number type is not known at compile time. Typically,
     * this type is known, in which case the appropriate method for that type should be called instead
     * (e.g. {@link #createInteger(java.lang.String}).
     *
     * @param columnName the name of the column
     * @param the type of the column
     * @return the new path
     */
    @Override
    protected <A extends Number & Comparable<?>> NumberPath<A> createNumber(final String columnName, final Class<? super A> type)
    {
        NumberPath<A> path = super.createNumber(columnName, type);
        addMetadata(path, ColumnMetadata.named(columnName).ofType(mapJavaNumberType(type)));
        return path;
    }

    /**
     * Creates an integer column with sensible default metadata
     *
     * @param columnName the name of the column
     * @return the new path
     */
    protected NumberPath<Integer> createInteger(final String columnName)
    {
        NumberPath<Integer> path = super.createNumber(columnName, Integer.class);
        addMetadata(path, ColumnMetadata.named(columnName).ofType(Types.INTEGER));
        return path;
    }

    /**
     * Creates a long column with sensible default metadata
     *
     * @param columnName the name of the column
     * @return the new path
     */
    protected NumberPath<Long> createLong(final String columnName)
    {
        NumberPath<Long> path = super.createNumber(columnName, Long.class);
        addMetadata(path, ColumnMetadata.named(columnName).ofType(Types.BIGINT));
        return path;
    }

    /**
     * Creates a double column with sensible default metadata
     *
     * @param columnName the name of the column
     * @return the new path
     */
    protected NumberPath<Double> createDouble(final String columnName)
    {
        NumberPath<Double> path = super.createNumber(columnName, Double.class);
        addMetadata(path, ColumnMetadata.named(columnName).ofType(Types.DOUBLE));
        return path;
    }

    /**
     * Creates a big decimal column with sensible default metadata
     *
     * @param columnName the name of the column
     * @return the new path
     */
    protected NumberPath<BigDecimal> createBigDecimal(final String columnName)
    {
        NumberPath<BigDecimal> path = super.createNumber(columnName, BigDecimal.class);
        addMetadata(path, ColumnMetadata.named(columnName).ofType(Types.DECIMAL));
        return path;
    }

    /**
     * Creates a float column with sensible default metadata
     *
     * @param columnName the name of the column
     * @return the new path
     */
    protected NumberPath<Float> createFloat(final String columnName)
    {
        NumberPath<Float> path = super.createNumber(columnName, Float.class);
        addMetadata(path, ColumnMetadata.named(columnName).ofType(Types.DECIMAL));
        return path;
    }

    /**
     * Creates a string column with sensible default metadata
     *
     * @param columnName the name of the column
     * @return the new path
     */
    @Override
    protected StringPath createString(final String columnName)
    {
        StringPath path = super.createString(columnName);
        addMetadata(path, ColumnMetadata.named(columnName).ofType(Types.VARCHAR));
        return path;
    }

    /**
     * Creates a time column with sensible default metadata
     *
     * @param columnName the name of the column
     * @return the new path
     */
    @Override
    protected <A extends Comparable> TimePath<A> createTime(final String columnName, final Class<? super A> type)
    {
        TimePath<A> path = super.createTime(columnName, type);
        addMetadata(path, ColumnMetadata.named(columnName).ofType(Types.TIME));
        return path;
    }

    //
    // the following are the builder style column creation methods that allow full metadata creation
    //

    /**
     * Creates a boolean column with sensible default metadata that you can add to
     *
     * @param columnName the name of the column
     * @return the builder of column metadata
     */
    protected ColumnWithMetadataBuilder<BooleanPath> createBooleanCol(final String columnName)
    {
        BooleanPath path = super.createBoolean(columnName);
        return new ColumnWithMetadataBuilder<BooleanPath>(path, ColumnMetadata.named(columnName).ofType(Types.BOOLEAN));
    }

    /**
     * Creates a date column with sensible default metadata that you can add to
     *
     * @param columnName the name of the column
     * @return the builder of column metadata
     */
    protected <A extends Comparable> ColumnWithMetadataBuilder<DatePath<A>> createDateCol(final String columnName, final Class<? super A> type)
    {
        DatePath<A> path = super.createDate(columnName, type);
        return new ColumnWithMetadataBuilder<DatePath<A>>(path, ColumnMetadata.named(columnName).ofType(Types.DATE));
    }

    /**
     * Creates a date time column with sensible default metadata that you can add to
     *
     * @param columnName the name of the column
     * @return the builder of column metadata
     */
    protected <A extends Comparable> ColumnWithMetadataBuilder<DateTimePath<A>> createDateTimeCol(final String columnName, final Class<? super A> type)
    {
        DateTimePath<A> path = super.createDateTime(columnName, type);
        return new ColumnWithMetadataBuilder<DateTimePath<A>>(path, ColumnMetadata.named(columnName).ofType(Types.TIMESTAMP));
    }


    // The method below does not compile with Java 6 due to a bug in the Java 6 JDK: http://bugs.java.com/view_bug.do?bug_id=6302954
    // This bug has been fixed in Java 7, so when we only support that, this method can be restored. For now, it has been
    // replaced with Number subclass specific versions.
    /*
    protected <A extends Number & Comparable<?>> ColumnWithMetadataBuilder<NumberPath<A>> createNumberCol(final String columnName, final Class<? super A> type)
    {
        NumberPath<A> path = super.createNumber(columnName, type);
        return new ColumnWithMetadataBuilder<NumberPath<A>>(path, ColumnMetadata.named(columnName).ofType(mapJavaNumberType(type)));
    }
    */

    /**
     * Creates an integer column with sensible default metadata that you can add to
     *
     * @param columnName the name of the column
     * @return the builder of column metadata
     */
    protected ColumnWithMetadataBuilder<NumberPath<Integer>> createIntegerCol(final String columnName)
    {
        NumberPath<Integer> path = super.createNumber(columnName, Integer.class);
        return new ColumnWithMetadataBuilder<NumberPath<Integer>>(path, ColumnMetadata.named(columnName).ofType(Types.INTEGER));
    }

    /**
     * Creates a long column with sensible default metadata that you can add to
     *
     * @param columnName the name of the column
     * @return the builder of column metadata
     */
    protected ColumnWithMetadataBuilder<NumberPath<Long>> createLongCol(final String columnName)
    {
        NumberPath<Long> path = super.createNumber(columnName, Long.class);
        return new ColumnWithMetadataBuilder<NumberPath<Long>>(path, ColumnMetadata.named(columnName).ofType(Types.BIGINT));
    }

    /**
     * Creates a double column with sensible default metadata that you can add to
     *
     * @param columnName the name of the column
     * @return the builder of column metadata
     */
    protected ColumnWithMetadataBuilder<NumberPath<Double>> createDoubleCol(final String columnName)
    {
        NumberPath<Double> path = super.createNumber(columnName, Double.class);
        return new ColumnWithMetadataBuilder<NumberPath<Double>>(path, ColumnMetadata.named(columnName).ofType(Types.DOUBLE));
    }

    /**
     * Creates a big decimal column with sensible default metadata that you can add to
     *
     * @param columnName the name of the column
     * @return the builder of column metadata
     */
    protected ColumnWithMetadataBuilder<NumberPath<BigDecimal>> createBigDecimalCol(final String columnName)
    {
        NumberPath<BigDecimal> path = super.createNumber(columnName, BigDecimal.class);
        return new ColumnWithMetadataBuilder<NumberPath<BigDecimal>>(path, ColumnMetadata.named(columnName).ofType(Types.DECIMAL));
    }

    /**
     * Creates a float column with sensible default metadata that you can add to
     *
     * @param columnName the name of the column
     * @return the builder of column metadata
     */
    protected ColumnWithMetadataBuilder<NumberPath<Float>> createFloatCol(final String columnName)
    {
        NumberPath<Float> path = super.createNumber(columnName, Float.class);
        return new ColumnWithMetadataBuilder<NumberPath<Float>>(path, ColumnMetadata.named(columnName).ofType(Types.DECIMAL));
    }

    /**
     * Creates a string column with sensible default metadata that you can add to
     *
     * @param columnName the name of the column
     * @return the builder of column metadata
     */
    protected ColumnWithMetadataBuilder<StringPath> createStringCol(final String columnName)
    {
        StringPath path = super.createString(columnName);
        return new ColumnWithMetadataBuilder<StringPath>(path, ColumnMetadata.named(columnName).ofType(Types.VARCHAR));
    }

    /**
     * Creates a time column with sensible default metadata that you can add to
     *
     * @param columnName the name of the column
     * @return the builder of column metadata
     */
    protected <A extends Comparable> ColumnWithMetadataBuilder<TimePath<A>> createTimeCol(final String columnName, final Class<? super A> type)
    {
        TimePath<A> path = super.createTime(columnName, type);
        return new ColumnWithMetadataBuilder<TimePath<A>>(path, ColumnMetadata.named(columnName).ofType(Types.TIME));
    }

    /**
     * This allows a path to be build up in a builder style at the entity declaration point
     * <p/>
     * StringPath sCol = createStringCol("COL_NAME").withIndex(1).ofType(Types.VARCHAR).notNull().asPrimaryKey().build();
     *
     * @param <P> a {@link com.mysema.query.types.Path} type
     * @see com.mysema.query.sql.ColumnMetadata
     */
    public class ColumnWithMetadataBuilder<P extends Path<?>>
    {
        private final P path;
        private ColumnMetadata metadata;
        private boolean asPK = false;

        public ColumnWithMetadataBuilder(final P path, final ColumnMetadata startingMetadata)
        {
            this.path = path;
            this.metadata = startingMetadata;
        }

        public ColumnWithMetadataBuilder<P> asPrimaryKey()
        {
            asPK = true;
            metadata = metadata.notNull(); // PK cant be null
            return this;
        }

        public ColumnWithMetadataBuilder<P> notNull()
        {
            metadata = metadata.notNull();
            return this;
        }

        public ColumnWithMetadataBuilder<P> ofType(int jdbcType)
        {
            metadata = metadata.ofType(jdbcType);
            return this;
        }

        public ColumnWithMetadataBuilder<P> withIndex(int index)
        {
            metadata = metadata.withIndex(index);
            return this;
        }

        public ColumnWithMetadataBuilder<P> withSize(int size)
        {
            metadata = metadata.withSize(size);
            return this;
        }

        public ColumnWithMetadataBuilder<P> withDigits(int decimalDigits)
        {
            metadata = metadata.withDigits(decimalDigits);
            return this;
        }

        /**
         * Builds the column path and metadata in one step
         *
         * @return the new path
         */
        public P build()
        {
            addMetadata(path, metadata);
            if (asPK)
            {
                PrimaryKey<T> currentPK = getPrimaryKey();
                if (currentPK != null)
                {
                    throw new IllegalStateException("You have already set a primary key.  I am not sure you know what you are doing");
                }
                createPrimaryKey(path);
            }
            return path;
        }
    }

    /**
     * @return an array of all the paths that are not primary keys.  Useful for inserts.
     * @see #getColumns()
     */
    public Path<?>[] getAllNonPrimaryKeyColumns()
    {
        final PrimaryKey<T> primaryKey = getPrimaryKey();
        // primaryKey can be null as can its local columns
        final List<? extends Path<?>> pkColumns = (primaryKey != null && primaryKey.getLocalColumns() != null)
                ? primaryKey.getLocalColumns()
                : Collections.<Path<?>>emptyList();
        ;
        List<Path<?>> columns = newArrayList(filter(getColumns(), new Predicate<Path<?>>()
        {
            @Override
            public boolean apply(final Path<?> input)
            {
                for (Path<?> pkColumn : pkColumns)
                {
                    if (pkColumn.equals(input))
                    {
                        return false;
                    }
                }
                return true;
            }
        }));

        return newArrayList(columns).toArray(new Path[columns.size()]);
    }

    private int mapJavaNumberType(final Class<?> javaType)
    {
        if (javaType.equals(Integer.class))
        {
            return Types.INTEGER;
        }
        else if (javaType.equals(Long.class))
        {
            return Types.BIGINT;
        }
        else if (javaType.equals(Double.class))
        {
            return Types.DOUBLE;
        }
        else if (javaType.equals(Float.class))
        {
            return Types.DECIMAL;
        }
        else
        {
            throw new UnsupportedOperationException("Unable to map number class " + javaType + " to JDBC type");
        }
    }
}
