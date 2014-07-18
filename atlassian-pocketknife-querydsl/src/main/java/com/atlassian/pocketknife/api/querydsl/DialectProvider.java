package com.atlassian.pocketknife.api.querydsl;

import com.mysema.query.sql.Configuration;
import com.mysema.query.sql.SQLTemplates;

/**
 * TODO: Document this class / interface here
 *
 * @since v0.x
 */
public interface DialectProvider
{
    Config getDialectConfig();

    public static class Config
    {
        private final SQLTemplates sqlTemplates;
        private final Configuration configuration;

        public Config(final SQLTemplates sqlTemplates, final Configuration configuration)
        {
            this.sqlTemplates = sqlTemplates;
            this.configuration = configuration;
        }

        public SQLTemplates getSqlTemplates()
        {
            return sqlTemplates;
        }

        public Configuration getConfiguration()
        {
            return configuration;
        }
    }

}
