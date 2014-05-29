package com.atlassian.pocketknife.api.ao.util;

import net.java.ao.Entity;
import net.java.ao.RawEntity;
import net.java.ao.schema.Table;
import org.hamcrest.Matchers;
import org.junit.Assert;
import org.junit.Test;

import static com.atlassian.pocketknife.api.ao.util.ActiveObjectsKit.getSchemaEntitiesWithReplacements;

/**
 */
@SuppressWarnings("unchecked")
public class ActiveObjectsKitTest
{

    private static class SchemaWithNothing
    {

    }

    private static class SchemaWithNoEntities
    {

    }

    private static class SchemaWitEntitiesButNoTableAnnotation
    {

        public static interface Moocows extends Entity
        {

            public String getName();
        }


    }

    private static class SchemaWithTwoEntitiesAndSomeOtherStuff
    {

        @Table("cows")
        public static interface Moocows extends Entity
        {

            public String getName();
        }

        @Table("lambs")
        public static interface Baalambs extends Entity
        {

            public String getName();
        }

        class SomeOtherStuff
        {

            public String getThisSHouldNotCount()
            {
                return null;
            }
        }

    }

    private static class SchemaWithTwoInheritance
    {
        public static interface Animal extends Entity
        {

            public String getName();
        }


        @Table("woof")
        public static interface Woofers extends Animal
        {

            public String getName();
        }

        @Table("meow")
        public static interface Meowers extends Animal
        {

            public String getName();
        }


    }

    @Table("cows")
    private static interface CowReplacement extends RawEntity<Long> {

            public String getSounds();
    }

    @Table("elephants")
    private static interface ElephantReplacement extends RawEntity<Long> {

            public String getSounds();
    }



    @Test(expected = IllegalArgumentException.class)
    public void testNothingInTheSchema() throws Exception
    {
        getSchemaEntitiesWithReplacements(SchemaWithNothing.class);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testNoEntitiesInSchema() throws Exception
    {
        getSchemaEntitiesWithReplacements(SchemaWithNoEntities.class);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testNoTableAnnotations() throws Exception
    {
        getSchemaEntitiesWithReplacements(SchemaWitEntitiesButNoTableAnnotation.class);
    }

    @Test
    public void testHasTwoEntities() throws Exception
    {
        Class[] entities = getSchemaEntitiesWithReplacements(SchemaWithTwoEntitiesAndSomeOtherStuff.class);
        Assert.assertThat(entities, Matchers.<Class>arrayContainingInAnyOrder(
                SchemaWithTwoEntitiesAndSomeOtherStuff.Moocows.class,
                SchemaWithTwoEntitiesAndSomeOtherStuff.Baalambs.class
        ));
    }

    @Test
    public void testHasTwoEntitiesWithReplacementAndAddition() throws Exception
    {
        Class[] entities = getSchemaEntitiesWithReplacements(SchemaWithTwoEntitiesAndSomeOtherStuff.class, CowReplacement.class, ElephantReplacement.class);
        Assert.assertThat(entities, Matchers.<Class>arrayContainingInAnyOrder(
                SchemaWithTwoEntitiesAndSomeOtherStuff.Baalambs.class,
                CowReplacement.class,
                ElephantReplacement.class
        ));
    }

    @Test
    public void testHasTwoEntitiesWithInheritance() throws Exception
    {
        Class[] entities = getSchemaEntitiesWithReplacements(SchemaWithTwoInheritance.class);
        Assert.assertThat(entities, Matchers.<Class>arrayContainingInAnyOrder(
                SchemaWithTwoInheritance.Woofers.class,
                SchemaWithTwoInheritance.Meowers.class
        ));
    }
}

