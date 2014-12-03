package com.atlassian.pocketknife.spi.querydsl;

import com.mysema.query.sql.PrimaryKey;
import com.mysema.query.types.Path;
import com.mysema.query.types.path.DateTimePath;
import com.mysema.query.types.path.NumberPath;
import com.mysema.query.types.path.StringPath;
import org.junit.Before;
import org.junit.Test;

import java.sql.Timestamp;
import java.util.List;

import static com.google.common.collect.Lists.newArrayList;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

public class EnhancedRelationalPathBaseTest
{
    private TestEnhancedRelationalPathBase queryDslEntity;

    @Before
    public void setUp() throws Exception
    {
        queryDslEntity = new TestEnhancedRelationalPathBase("test_table");

    }

    @Test
    public void createNumberAndSetAsPrimaryKey__primary_key_is_not_null_after_calling() throws Exception
    {
        assertNotNull(queryDslEntity.getPrimaryKey());
    }

    @Test
    public void column_metadata_is_added_for_all_defined_columns() throws Exception
    {
        assertNotNull(queryDslEntity.getMetadata(queryDslEntity.ID));
        assertNotNull(queryDslEntity.getMetadata(queryDslEntity.NAME));
        assertNotNull(queryDslEntity.getMetadata(queryDslEntity.CREATED_TIMESTAMP));
    }

    @Test
    public void getColumnsWithoutPrimaryKey__returns_all_columns_except_for_primary_key() throws Exception
    {
        List<Path<?>> result = newArrayList(queryDslEntity.getColumnsWithoutPrimaryKey());

        assertEquals(2, result.size());
        assertTrue(result.contains(queryDslEntity.NAME));
        assertTrue(result.contains(queryDslEntity.CREATED_TIMESTAMP));
    }

    private static class TestEnhancedRelationalPathBase extends EnhancedRelationalPathBase<TestEnhancedRelationalPathBase>
    {
        public TestEnhancedRelationalPathBase(final String tableName)
        {
            super(TestEnhancedRelationalPathBase.class, tableName);
        }

        public final NumberPath<Long> ID = createNumberAndSetAsPrimaryKey("ID", Long.class);
        public final StringPath NAME = createString("NAME");
        public final DateTimePath<Timestamp> CREATED_TIMESTAMP = createDateTime("CREATED", java.sql.Timestamp.class);
    }
}