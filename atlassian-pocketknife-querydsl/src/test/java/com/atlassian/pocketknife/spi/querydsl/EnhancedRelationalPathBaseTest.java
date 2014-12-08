package com.atlassian.pocketknife.spi.querydsl;

import com.atlassian.pocketknife.api.querydsl.SchemaProvider;
import com.atlassian.pocketknife.internal.querydsl.SchemaProviderAccessor;
import com.google.common.collect.Lists;
import com.mysema.query.sql.ColumnMetadata;
import com.mysema.query.sql.PrimaryKey;
import com.mysema.query.types.Path;
import com.mysema.query.types.path.BooleanPath;
import com.mysema.query.types.path.DatePath;
import com.mysema.query.types.path.DateTimePath;
import com.mysema.query.types.path.NumberPath;
import com.mysema.query.types.path.StringPath;
import com.mysema.query.types.path.TimePath;
import org.hamcrest.Matchers;
import org.junit.Before;
import org.junit.Test;

import java.sql.Date;
import java.sql.Time;
import java.sql.Timestamp;
import java.sql.Types;
import java.util.List;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.Assert.assertThat;

public class EnhancedRelationalPathBaseTest
{

    public static final String QEMAIL_SETTINGS_TABLE_NAME = "QEMAILSETTINGS_TABLE";
    public static final String SCHEMA_X = "schemaX";

    /**
     * An example entity definition based on our extended base
     */
    public static class QEmailSettings extends EnhancedRelationalPathBase<QEmailSettings>
    {
        public final NumberPath<Integer> ID = createIntegerCol("ID").asPrimaryKey().build();

        public final DateTimePath CREATED = createDateTimeCol("CREATED", Timestamp.class).notNull().build();

        public final StringPath DESCRIPTION = createStringCol("DESCRIPTION").notNull().build();

        public final StringPath EMAIL_ADDRESS = createString("EMAIL_ADDRESS");

        public final BooleanPath ENABLED = createBoolean("ENABLED");

        public final NumberPath<Long> JIRA_MAIL_SERVER_ID = createLong("JIRA_MAIL_SERVER_ID");

        public final NumberPath<Long> LAST_PROCEEDED_TIME = createLong("LAST_PROCEEDED_TIME");

        public final BooleanPath ON_DEMAND = createBooleanCol("ON_DEMAND").notNull().build();

        public final NumberPath<Integer> REQUEST_TYPE_ID = createInteger("REQUEST_TYPE_ID");

        public final NumberPath<Integer> SERVICE_DESK_ID = createInteger("SERVICE_DESK_ID");

        public final DatePath UPDATED_DATE = createDateCol("UPDATED_DATE", Date.class).notNull().build();

        public final TimePath UPDATED_TIME = createTimeCol("UPDATED_TIME", Time.class).notNull().build();


        public QEmailSettings(final String tableName)
        {
            super(QEmailSettings.class, tableName);

        }
    }


    QEmailSettings EMAIL_SETTINGS = new QEmailSettings(QEMAIL_SETTINGS_TABLE_NAME);

    @Before
    public void setUp() throws Exception
    {
        SchemaProviderAccessor.initializeWithSchemaProvider(new SchemaProvider()
        {
            @Override
            public String getSchema(final String tableName)
            {
                return SCHEMA_X;
            }
        });

    }

    @SuppressWarnings ({ "ConstantConditions", "unchecked" })
    @Test
    public void test_path_construction() throws Exception
    {
        // assert
        assertThat(EMAIL_SETTINGS.getTableName(), equalTo(QEMAIL_SETTINGS_TABLE_NAME));
        assertThat(EMAIL_SETTINGS.getSchemaAndTable().getTable(), equalTo(QEMAIL_SETTINGS_TABLE_NAME));

        assertThat(EMAIL_SETTINGS.getSchemaName(), equalTo(SCHEMA_X));
        assertThat(EMAIL_SETTINGS.getSchemaAndTable().getSchema(), equalTo(SCHEMA_X));

        PrimaryKey<QEmailSettings> primaryKey = EMAIL_SETTINGS.getPrimaryKey();
        assertThat(primaryKey, notNullValue());

        assertPathsPresent(primaryKey.getLocalColumns(), EMAIL_SETTINGS.ID);

        assertPathsPresent(EMAIL_SETTINGS.getColumns(),
                EMAIL_SETTINGS.ID,
                EMAIL_SETTINGS.CREATED,
                EMAIL_SETTINGS.DESCRIPTION,
                EMAIL_SETTINGS.EMAIL_ADDRESS,
                EMAIL_SETTINGS.ENABLED,
                EMAIL_SETTINGS.JIRA_MAIL_SERVER_ID,
                EMAIL_SETTINGS.LAST_PROCEEDED_TIME,
                EMAIL_SETTINGS.ON_DEMAND,
                EMAIL_SETTINGS.REQUEST_TYPE_ID,
                EMAIL_SETTINGS.SERVICE_DESK_ID,
                EMAIL_SETTINGS.UPDATED_DATE,
                EMAIL_SETTINGS.UPDATED_TIME
        );

        assertPathsPresent(EMAIL_SETTINGS.getAllNonPrimaryKeyColumns(),
                EMAIL_SETTINGS.CREATED,
                EMAIL_SETTINGS.DESCRIPTION,
                EMAIL_SETTINGS.EMAIL_ADDRESS,
                EMAIL_SETTINGS.ENABLED,
                EMAIL_SETTINGS.JIRA_MAIL_SERVER_ID,
                EMAIL_SETTINGS.LAST_PROCEEDED_TIME,
                EMAIL_SETTINGS.ON_DEMAND,
                EMAIL_SETTINGS.REQUEST_TYPE_ID,
                EMAIL_SETTINGS.SERVICE_DESK_ID,
                EMAIL_SETTINGS.UPDATED_DATE,
                EMAIL_SETTINGS.UPDATED_TIME
        );

    }

    @Test
    public void test_metadata_recording() throws Exception
    {
        // act
        assertColumnMetadata(EMAIL_SETTINGS.ID, "ID", false, Types.INTEGER, true);

        assertColumnMetadata(EMAIL_SETTINGS.CREATED, "CREATED", false, Types.TIMESTAMP);
        assertColumnMetadata(EMAIL_SETTINGS.DESCRIPTION, "DESCRIPTION", false, Types.VARCHAR);
        assertColumnMetadata(EMAIL_SETTINGS.EMAIL_ADDRESS, "EMAIL_ADDRESS", true, Types.VARCHAR);
        assertColumnMetadata(EMAIL_SETTINGS.ENABLED, "ENABLED", true, Types.BOOLEAN);
        assertColumnMetadata(EMAIL_SETTINGS.JIRA_MAIL_SERVER_ID, "JIRA_MAIL_SERVER_ID", true, Types.BIGINT);
        assertColumnMetadata(EMAIL_SETTINGS.LAST_PROCEEDED_TIME, "LAST_PROCEEDED_TIME", true, Types.BIGINT);
        assertColumnMetadata(EMAIL_SETTINGS.ON_DEMAND, "ON_DEMAND", false, Types.BOOLEAN);
        assertColumnMetadata(EMAIL_SETTINGS.REQUEST_TYPE_ID, "REQUEST_TYPE_ID", true, Types.INTEGER);
        assertColumnMetadata(EMAIL_SETTINGS.SERVICE_DESK_ID, "SERVICE_DESK_ID", true, Types.INTEGER);
        assertColumnMetadata(EMAIL_SETTINGS.UPDATED_DATE, "UPDATED_DATE", false, Types.DATE);
        assertColumnMetadata(EMAIL_SETTINGS.UPDATED_TIME, "UPDATED_TIME", false, Types.TIME);

    }

    private void assertColumnMetadata(final Path<?> path, final String name, final boolean nullable, int jdbcType)
    {
        assertColumnMetadata(path, name, nullable, jdbcType, false);
    }

    @SuppressWarnings ("ConstantConditions")
    private void assertColumnMetadata(final Path<?> path, final String name, final boolean nullable, int jdbcType, boolean asPK)
    {
        ColumnMetadata metadata = EMAIL_SETTINGS.getMetadata(path);
        assertThat("column name", metadata.getName(), equalTo(name));
        assertThat("jdbc type", metadata.getJdbcType(), equalTo(jdbcType));
        assertThat("nullable", metadata.isNullable(), equalTo(nullable));
        assertIsPK(path, asPK);

    }

    @SuppressWarnings ("ConstantConditions")
    private void assertIsPK(final Path<?> path, final boolean asPK)
    {
        PrimaryKey<QEmailSettings> primaryKey = EMAIL_SETTINGS.getPrimaryKey();
        assertThat(primaryKey.getLocalColumns().contains(path), equalTo(asPK));
    }

    private void assertPathsPresent(final Path<?>[] columns, final Path<?>... paths)
    {
        assertThat(columns, notNullValue());
        assertPathsPresent(Lists.newArrayList(paths), paths);
    }

    private void assertPathsPresent(List<? extends Path<?>> columns, final Path<?>... paths)
    {
        assertThat(columns, notNullValue());
        assertThat("path columns are not the right size", columns.size(), equalTo(paths.length));
        for (Path<?> path : paths)
        {
            assertThat(columns.contains(path), Matchers.is(true));
        }
    }
}