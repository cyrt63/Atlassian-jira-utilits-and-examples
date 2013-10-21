package com.atlassian.pocketknife.api.ao.util;

import com.atlassian.activeobjects.external.ActiveObjects;
import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.Lists;
import net.java.ao.RawEntity;
import net.java.ao.schema.Table;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;

import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;

import static java.lang.String.format;

/**
 * A series of utility functions for ActiveObjects
 */
@SuppressWarnings("unchecked")
public class ActiveObjectsKit
{
    private static final Logger log = Logger.getLogger(ActiveObjectsKit.class);

    /**
     * This can scan a Schema class object and find all AO entities in it and return them as an array, ready for ao.migrate()
     * <p/>
     * It can also "replace" some of the entities with more specific ones.
     * <p/>
     * NOTE : You MUST have AO {@link net.java.ao.schema.Table} annotations on the entities to use this method so that replacements can be known.
     *
     * @param schema       a class that contains entity objects
     * @param replacements a variable list of possible replacements
     * @return an array of entities
     */
    @VisibleForTesting
    static Class<? extends RawEntity<?>>[] getSchemaEntitiesWithReplacements(Class schema, Class<? extends RawEntity<?>>... replacements)
    {

        verifyReplacements(replacements);

        List<Class<? extends RawEntity<?>>> classList = Lists.newArrayList();

        Class[] declaredClasses = schema.getDeclaredClasses();
        for (Class declaredClass : declaredClasses)
        {
            if (isEntityCandidate(declaredClass))
            {
                // we may need to replace a particular Table schema definition
                Class<? extends RawEntity<?>> targetClass = possibleReplacement(declaredClass, replacements);
                classList.add(targetClass);
            }
        }
        //
        // now throw in all the replacements that have not been replaced already
        placeTheRestOfReplacementsIn(classList, replacements);

        verifyFinalList(classList);

        return classList.toArray(new Class[classList.size()]);
    }

    /**
     * A helper method to migrate from a class based Schema with replacements
     *
     * @param ao           the ActiveObject instance to call migrate on
     * @param schema       a class of AO entity declarations
     * @param replacements the replacement schema objects
     */
    public static void migrateDestructively(ActiveObjects ao, Class schema, Class<? extends RawEntity<?>>... replacements)
    {
        Class<? extends RawEntity<?>>[] schemaEntitiesWithReplacements = getSchemaEntitiesWithReplacements(schema, replacements);

        log.setLevel(Level.INFO);
        log.info(format("Migrating the following %d AO entities : ", schemaEntitiesWithReplacements.length));
        for (Class<? extends RawEntity<?>> entity : schemaEntitiesWithReplacements)
        {
            log.info(format("\tTable '%s' from '%s'", getTableName(entity), entity.getName()));
        }
        migrateDestructively(ao, schemaEntitiesWithReplacements);
    }

    private static void migrateDestructively(ActiveObjects ao, Class<? extends RawEntity<?>>[] schemaEntitiesWithReplacements)
    {
        Method migrateDestructively = getMigrateDestructively(ao, schemaEntitiesWithReplacements);
        if (migrateDestructively != null)
        {
            invokeMigrateDestructively(ao, schemaEntitiesWithReplacements, migrateDestructively);
        }
        else
        {
            ao.migrate(schemaEntitiesWithReplacements);
        }
    }

    private static Method getMigrateDestructively(ActiveObjects ao, Class<? extends RawEntity<?>>[] schemaEntitiesWithReplacements)
    {
        Method migrateDestructively;
        try
        {
            migrateDestructively = ao.getClass().getMethod("migrateDestructively", schemaEntitiesWithReplacements.getClass());
        }
        catch (NoSuchMethodException e)
        {
            log.info("No method migrateDestructively - will use ao.migrate()");
            migrateDestructively = null;
        }
        return migrateDestructively;
    }

    private static void invokeMigrateDestructively(ActiveObjects ao, Class<? extends RawEntity<?>>[] schemaEntitiesWithReplacements, Method migrateDestructively)
    {
        try
        {
            log.info("Invoking migrateDestructively");
            migrateDestructively.invoke(ao, new Object[]{schemaEntitiesWithReplacements});   // must prevent expansion into varargs
        }
        catch (IllegalAccessException e)
        {
            throw new RuntimeException(e);
        }
        catch (InvocationTargetException e)
        {
            throw new RuntimeException(e);
        }
    }

    private static List<Class<? extends RawEntity<?>>> placeTheRestOfReplacementsIn(List<Class<? extends RawEntity<?>>> classList, Class<? extends RawEntity<?>>[] replacements)
    {
        for (Class<? extends RawEntity<?>> replacement : replacements)
        {
            if (!classList.contains(replacement))
            {
                classList.add(replacement);
            }
        }
        return classList;
    }

    private static Class<? extends RawEntity<?>> possibleReplacement(Class declaredClass, Class<? extends RawEntity<?>>[] replacements)
    {
        for (Class<? extends RawEntity<?>> replacement : replacements)
        {
            if (getTableName(replacement).equals(getTableName(declaredClass)))
            {
                return replacement;
            }
        }
        //noinspection unchecked
        return declaredClass;
    }

    private static String getTableName(Class entity)
    {
        for (Annotation annotation : entity.getAnnotations())
        {
            if (annotation.annotationType().equals(Table.class))
            {
                Table table = (Table) annotation;
                return table.value();
            }
        }
        throw new IllegalStateException("All entities MUST have @Table annotations");
    }

    private static boolean isEntityCandidate(Class declaredClass)
    {
        if (RawEntity.class.isAssignableFrom(declaredClass))
        {
            if (declaredClass.isAnnotationPresent(Table.class))
            {
                return true;
            }
        }
        return false;
    }

    private static void verifyReplacements(Class<? extends RawEntity<?>>[] replacements)
    {
        for (Class<? extends RawEntity<?>> replacement : replacements)
        {
            if (!isEntityCandidate(replacement))
            {
                throw new IllegalArgumentException("You MUST provide ActiveObject RawEntity derived replacement classes!");
            }
            if (! replacement.isAnnotationPresent(Table.class))
            {
                throw new IllegalArgumentException("You MUST have @Table on all your entities for replacement");
            }
        }
    }

    private static void verifyFinalList(List<Class<? extends RawEntity<?>>> classList)
    {
        //
        // it makes no sense to give out 0 schema objects since all tables will be deleted!  So we throw exceptions
        if (classList.isEmpty())
        {
            throw new IllegalArgumentException("It makes no sense to have zero entities passed in.  This would delete all data.  Make sure you derive from RawEntity and have @Table annotations");
        }
    }
}
