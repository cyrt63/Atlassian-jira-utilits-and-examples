package com.atlassian.pocketknife.api.search.issue.util;

public class NumberUtil {

    /**
     * Convert a String to a Long. Returns null if the String doesn't represent a valid long. For conversion into long primitives, please use
     * org.apache.commons.lang.math.NumberUtils
     */
    public static Long toLong(String s) {
        if (s == null) {
            return null;
        }

        try {
            return Long.valueOf(s);

        } catch (NumberFormatException e) {
            return null;
        }
    }

    /**
     * Convert a String to an Integer. Returns null if the String doesn't represent a valid Integer. For conversion into long primitives, please use
     * org.apache.commons.lang.math.NumberUtils
     */
    public static Integer toInteger(String s) {
        try {
            return Integer.valueOf(s);

        } catch (NumberFormatException e) {
            return null;
        }
    }

    /**
     * Convert a Double into an Integer, returns null for null
     */
    public static Integer toInteger(Double value) {
        if (value == null) {
            return null;
        }
        return value.intValue();
    }

    /**
     * Convert an Integer to a Double, returns null for null
     */
    public static Double toDouble(Integer value) {
        if (value == null) {
            return null;
        }
        return value.doubleValue();
    }

    /**
     * Convert an String to a Double, returns null for null
     */
    public static Double toDouble(String value) {
        if (value == null) {
            return null;
        }
        try {
            return Double.valueOf(value);

        } catch (NumberFormatException e) {
            return null;
        }
    }
}
