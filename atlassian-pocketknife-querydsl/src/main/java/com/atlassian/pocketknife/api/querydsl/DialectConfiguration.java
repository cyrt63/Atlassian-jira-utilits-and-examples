package com.atlassian.pocketknife.api.querydsl;

import com.atlassian.annotations.PublicApi;
import com.mysema.query.sql.Configuration;
import com.mysema.query.sql.SQLTemplates;

import java.sql.Connection;

/**
 */
@PublicApi
public interface DialectConfiguration extends DialectProvider
{

    /**
     * Detects the QueryDSL dialects to use.
     *
     * @param connection a live database connection to query metadata on
     * @return a config for QueryDSL
     * @throws java.lang.UnsupportedOperationException if the database is not supported by QueryDSL and this code
     */
    Config detect(Connection connection);


    /**
     * This is chance to enrich the SQL templates to your liking
     *
     * @param sqlTemplatesBuilder a builder you can tweak
     * @return the enriched builder
     */
    SQLTemplates.Builder enrich(SQLTemplates.Builder sqlTemplatesBuilder);

    /**
     * This is chance to enrich the configuration to your liking
     *
     * @param configuration the configuration to enrich
     * @return the enriched configuration
     */
    Configuration enrich(Configuration configuration);

}
