package com.atlassian.pocketknife.spi.querydsl;

import com.atlassian.annotations.PublicSpi;
import com.atlassian.pocketknife.api.querydsl.DialectProvider;
import com.mysema.query.sql.Configuration;
import com.mysema.query.sql.SQLTemplates;

/**
 */
@PublicSpi
public interface DialectConfiguration extends DialectProvider
{

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
