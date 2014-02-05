package com.atlassian.pocketknife.unit;

import net.sf.ehcache.Cache;
import net.sf.ehcache.CacheManager;
import net.sf.ehcache.Ehcache;
import org.joda.time.DateMidnight;
import org.joda.time.DateTime;
import org.joda.time.LocalDate;
import org.powermock.reflect.exceptions.FieldNotFoundException;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

import static org.apache.commons.lang.Validate.notNull;

/**
 * Utilities for Unit Tests
 * 
 * TODO for whatever reason, surefire resists all attempts in the pom.xml to exclude this and tries to run it, which breaks the build. Workaround is
 * renaming this from "TestUtils" to "UnitTestUtils" to evade Maven.
 * 
 * @author ahennecke
 */
public class UnitTestUtils {

    /**
     * Sets the value object onto all compatible fields in the target object. 
     * 
     * @param target The object to set the value on
     * @param value The value to set in the fields.
     */
    public static void setField(Object target, Object value) {
        Class<?> valueClass = value.getClass();
        Class<?> targetClass = target.getClass();
        while (targetClass != null)
        {
            Field[] fields = targetClass.getDeclaredFields();
            for (Field field : fields)
            {
                Class<?> fieldType = field.getType();
                if (fieldType.isAssignableFrom(valueClass))
                {
                    setField(target, field.getName(), value);
                }
            }
            targetClass = targetClass.getSuperclass();
        }
    }
    
    /**
     * Set the field in the target Object to the given value. If the field is not found in the class of the target Object, the superclass chain will
     * be searched. Works on private fields, as long as the SecurityManager allows it.
     * 
     * @param target The object to set the value on
     * @param name The name of the field
     * @param value The value to set the field to
     */
    public static void setField(Object target, String name, Object value) {
        try {
            notNull(target, "target object was null");
            notNull(name, "field name was null");
            
            Field field = findField(name, target.getClass(), null);
            notNull(field, "field " + name + " not found in " + target.getClass().getSimpleName());
            field.setAccessible(true);
            field.set(target, value);

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Set the static field in the target class object to the given value. If the field is not found in the class of the target Object, the superclass
     * chain will be searched. Works on private fields, as long as the SecurityManager allows it.
     * 
     * @param target The target class which holds the static field
     * @param name The name of the field
     * @param value The value to set the field to
     */
    public static void setField(Class<?> target, String name, Object value) {
        try {
            Field field = findField(name, target);
            field.setAccessible(true);
            field.set(null, value);

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
    
    /**
     * Returns the value of the field in target with the given name.
     * 
     * @param target : The object to look for the field value in
     * @param name : The name of the field to look for
     * @return the value of the field
     */
    public static Object getFieldValue(Object target, String name) {
        try {
            Field field = findField(name, target.getClass());
            field.setAccessible(true);
            return field.get(target);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Finds a field with the given name in the class or in one of its superclasses. If more than one field exists in the hierarchy, the first match
     * will be returned.
     * 
     * @param name the name of the field
     * @param targetClass the class to start looking in
     * @return Reflection field
     * 
     * @throws org.powermock.reflect.exceptions.FieldNotFoundException if no matching field could be found
     */
    public static Field findField(String name, Class<?> targetClass) {
        return findField(name, targetClass, null);
    }

    /**
     * Finds a field with the given name and of the given type in the class or in one of its superclasses. If more than one field exists in the
     * hierarchy, the first match will be returned.
     *
     * @param name the name of the field
     * @param targetClass the class to start looking in
     * @param type optional, the type of the field
     * @return Reflection field
     *
     * @throws org.powermock.reflect.exceptions.FieldNotFoundException if no matching field could be found
     */
    public static Field findField(String name, Class<?> targetClass, Class<?> type) {
        Class<?> search = targetClass;
        while (!Object.class.equals(search) && search != null) {
            for (Field field : search.getDeclaredFields()) {
                if (name.equals(field.getName()) && (type == null || type.equals(field.getType()))) {
                    return field;
                }
            }

            search = search.getSuperclass();
        }

        throw new FieldNotFoundException("No field with name '" + name + "' found in class hierarchy of '" + targetClass.getName() + "'");
    }


    /**
     * Construct a typesafe list out of a given array of elements. Unlike Arrays.asList() this is a real list, not a crippled inner class.
     *
     * @param values dynamic array of objects to put in the list
     * @return list of the given objects
     */
    public static <T> List<T> buildList(T... values) {
        List<T> list = new ArrayList<T>(values.length);
        for (T t : values) {
            list.add(t);
        }

        return list;
    }

    /**
     * Construct a typesafe set out of a given array of elements.
     *
     * @param values dynamic array of objects to put in the set
     * @return set of the given objects
     */
    public static <T> Set<T> buildSet(T... values) {
        Set<T> set = new HashSet<T>(values.length, 1);
        for (T t : values)
        {
            set.add(t);
        }

        return set;
    }

    /**
     * Construct a typesafe sorted set out of a given array of elements.
     *
     * @param values dynamic array of objects to put in the set
     * @return set of the given objects
     */
    public static <T> SortedSet<T> buildSortedSet(T... values) {
        SortedSet<T> set = new TreeSet<T>();
        for (T t : values)
        {
            set.add(t);
        }

        return set;
    }

    /**
     * Build a java.util.Date at the given day, at 00:00 hours.
     *
     * @param year the year
     * @param month the month, starting at 1 = Jan
     * @param day the day of the month, starting at 1
     * @return Date for the given day
     */
    public static Date buildDate(int year, int month, int day) {
        return new LocalDate(year, month, day).toDateMidnight().toDate();
    }

    /**
     * Build a java.util.Date at the given day, at 00:00 hours.
     *
     * @param year the year
     * @param month the month, starting at 1 = Jan
     * @param day the day of the month, starting at 1
     * @param hour : the hour, starting at 0, in 24h notation
     * @param minute : the minute, starting at 0
     */
    public static Date buildTime(int year, int month, int day, int hour, int minute) {
        return new DateTime(year, month, day, hour, minute, 0, 0).toDate();
    }

    /**
     * Build a {@link org.joda.time.DateMidnight} at the given day, at 00:00 hours.
     *
     * @param year : the year
     * @param month : the month, starting at 1 = Jan
     * @param day : the day of the month, starting at 1
     * @return DateMidnight for the given day
     */
    public static DateMidnight buildDateMidnight(int year, int month, int day) {
        return new LocalDate(year, month, day).toDateMidnight();
    }

    /**
     * Short version for buildDateMidnight
     */
    public static DateMidnight dm(int year, int month, int day) {
        return buildDateMidnight(year, month, day);
    }

    /**
     * shortcut to build a not too precise dateTime object
     */
    public static DateTime dt(int year, int month, int day, int hour, int minute) {
        return new DateTime(year, month, day, hour, minute, 0, 0);
    }

    /**
     * Register the given {@link net.sf.ehcache.Cache} instance. Previous instances of the same name
     * are cleared.
     * 
     * @param name : the name of the cache instance
     * @param cache : the configured cache
     */
    public static void registerCleanCache(String name, Ehcache cache) {
        CacheManager manager = CacheManager.getInstance();
        manager.removeCache(name);
        manager.addCache(cache);
    }
    
    /**
     * Build and register a cache with default settings
     * 
     * @param name : the name of the cache instance
     * @return the configured cache instance
     */
    public static Ehcache buildCache(String name) {
        Ehcache cache = new Cache(name, 30, false, false, 1800, 1800);
        registerCleanCache(name, cache);
        return cache;
    }
    
}
