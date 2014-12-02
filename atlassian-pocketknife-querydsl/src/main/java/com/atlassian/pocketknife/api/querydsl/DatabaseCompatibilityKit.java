package com.atlassian.pocketknife.api.querydsl;

import com.mysema.query.sql.dml.SQLInsertClause;

import java.sql.Connection;

/**
 * This utility class provides extra helpers and workarounds for the vagaries of database.  QueryDSL gives us a lot but
 * not everything.  You can use this set of helper methods to patch the difference.
 */
public interface DatabaseCompatibilityKit
{

    /**
     * Modern databases have the ability to return the generated ids after an insert statement.  However 7 year old
     * noddy databases like HSQL 1.8.x do not have this ability in a single statement.  So this method allows us to use
     * QueryDSL to do the normal {@link com.mysema.query.sql.dml.SQLInsertClause#executeWithKey(Class)} on the databases
     * that support it and to patch things up on the databases that don't natively support it.
     * <p/>
     * This will execute the clause and return the generated key cast to the given type. If no rows were created, null
     * is returned, otherwise the key of the first row is returned.
     * <p/>
     * <p/>
     * NOTE : This makes the very strong assumption that the insert will result in a generated key.  Make sure that this
     * in fact the case.
     *
     * @param connection the connection to the database
     * @param insertClause the QueryDSL insert clause to execute
     * @param idClass the class of the id generated
     * @param <T> the Java type id generated
     * @return If no rows were created, null is returned, otherwise the key of the first row is returned.
     */
    <T> T executeWithKey(final Connection connection, final SQLInsertClause insertClause, final Class<T> idClass);

}
