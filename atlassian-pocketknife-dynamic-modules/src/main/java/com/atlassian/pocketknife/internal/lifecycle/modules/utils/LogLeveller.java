package com.atlassian.pocketknife.internal.lifecycle.modules.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * slf4j does not allow usa to set log levels but log4j does.  So lets level up!
 * <p/>
 * WARNING :
 * <p/>
 * If you are using this in an OSGI world, and these days who isn't, then make sure you import org.apache.log4j
 * explicitly as the reflective nature means that BND will never find this dependency and hence you wont be able to see
 * log4j classes
 */
@SuppressWarnings ("UnusedDeclaration")
public class LogLeveller
{
    private static final Logger log = LoggerFactory.getLogger(LogLeveller.class);

    public static Logger setInfo(Logger slf4jLogger)
    {
        if (!slf4jLogger.isInfoEnabled())
        {
            setLevelImpl(slf4jLogger, "INFO");
        }
        return slf4jLogger;
    }

    public static Logger setWarn(Logger slf4jLogger)
    {
        if (!slf4jLogger.isWarnEnabled())
        {
            setLevelImpl(slf4jLogger, "WARN");
        }
        return slf4jLogger;
    }

    public static Logger setWarnIfDevMode(Logger slf4jLogger)
    {
        if (isDevMode())
        {
            setWarn(slf4jLogger);
        }
        return slf4jLogger;
    }

    public static Logger setInfoIfDevMode(Logger slf4jLogger)
    {
        if (isDevMode())
        {
            setInfo(slf4jLogger);
        }
        return slf4jLogger;
    }

    private static boolean isDevMode()
    {
        return Boolean.getBoolean("jira.dev.mode") || Boolean.getBoolean("atlassian.dev.mode");
    }

    @SuppressWarnings ("unchecked")
    private static void setLevelImpl(final Logger slf4jLogger, String levelName)
    {
        Class log4JClass = findClass("org.apache.log4j.Logger");
        if (log4JClass != null)
        {
            try
            {
                // Logger.getLogger("x)
                Method getLogger = log4JClass.getMethod("getLogger", String.class);
                Object logger = getLogger.invoke(null, slf4jLogger.getName());

                // Level.INFO for example
                Class levelClass = findClass("org.apache.log4j.Level");
                Field logLevelField = levelClass.getField(levelName);
                Object levelInstance = logLevelField.get(null);

                // logger.setLevel(INFO) for example
                Method setLevel = log4JClass.getMethod("setLevel", levelClass);
                setLevel.invoke(logger, levelInstance);
            }
            catch (NoSuchMethodException ignored)
            {
            }
            catch (InvocationTargetException ignored)
            {
            }
            catch (IllegalAccessException ignored)
            {
            }
            catch (NoSuchFieldException ignored)
            {
            }
        }
    }

    private static Class findClass(String className)
    {
        try
        {
            return Class.forName(className);
        }
        catch (ClassNotFoundException e)
        {
            log.warn(String.format("Unable to find class '%s'.  Have you OSGI imported it??", className));
            return null;
        }
    }
}
