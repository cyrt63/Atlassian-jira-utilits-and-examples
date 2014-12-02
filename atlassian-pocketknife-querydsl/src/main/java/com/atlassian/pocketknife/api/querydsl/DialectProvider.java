package com.atlassian.pocketknife.api.querydsl;

import com.atlassian.annotations.PublicApi;
import com.mysema.query.sql.Configuration;
import com.mysema.query.sql.SQLTemplates;

/**
 */
@PublicApi
public interface DialectProvider
{
    Config getDialectConfig();

    /**
     * These are the database products that Pocketknife knows about.  If we add new supported databases, such as H2 perhaps, then we should add a new enum
     */
    enum SupportedDatabase
    {
        POSTGRESSQL, ORACLE, MYSQL, SQLSERVER, HSQLDB
    }

    /**
     * Information about the current database
     */
    public static class DatabaseInfo {
        private final String databaseProductName;
        private final String databaseProductVersion;
        private final String driverName;
        private final int databaseMajorVersion;
        private final int databaseMinorVersion;
        private final int driverMajorVersion;
        private final int driverMinorVersion;
        private final SupportedDatabase supportedDatabase;

        public DatabaseInfo(final SupportedDatabase supportedDatabase, final String databaseProductName, final String databaseProductVersion, final int databaseMajorVersion, final int databaseMinorVersion, final String driverName, final int driverMajorVersion, final int driverMinorVersion)
        {
            this.supportedDatabase = supportedDatabase;
            this.databaseProductName = databaseProductName;
            this.databaseProductVersion = databaseProductVersion;
            this.databaseMajorVersion = databaseMajorVersion;
            this.databaseMinorVersion = databaseMinorVersion;
            this.driverName = driverName;
            this.driverMajorVersion = driverMajorVersion;
            this.driverMinorVersion = driverMinorVersion;
        }

        public SupportedDatabase getSupportedDatabase()
        {
            return supportedDatabase;
        }

        public String getDatabaseProductName()
        {
            return databaseProductName;
        }

        public String getDatabaseProductVersion()
        {
            return databaseProductVersion;
        }

        public String getDriverName()
        {
            return driverName;
        }

        public int getDatabaseMajorVersion()
        {
            return databaseMajorVersion;
        }

        public int getDatabaseMinorVersion()
        {
            return databaseMinorVersion;
        }

        public int getDriverMajorVersion()
        {
            return driverMajorVersion;
        }

        public int getDriverMinorVersion()
        {
            return driverMinorVersion;
        }

    }

    public static class Config
    {
        private final SQLTemplates sqlTemplates;
        private final Configuration configuration;
        private final DatabaseInfo databaseInfo;

        public Config(final SQLTemplates sqlTemplates, final Configuration configuration, final DatabaseInfo databaseInfo)
        {
            this.sqlTemplates = sqlTemplates;
            this.configuration = configuration;
            this.databaseInfo = databaseInfo;
        }

        public SQLTemplates getSqlTemplates()
        {
            return sqlTemplates;
        }

        public Configuration getConfiguration()
        {
            return configuration;
        }

        public DatabaseInfo getDatabaseInfo()
        {
            return databaseInfo;
        }
    }

}
