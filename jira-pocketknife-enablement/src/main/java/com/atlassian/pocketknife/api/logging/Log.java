package com.atlassian.pocketknife.api.logging;

import org.apache.log4j.Level;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Convenience methods for {@link org.slf4j.Logger}.
 */
public class Log {
    private final Logger logger;

    Log(Logger logger) {
        this.logger = logger;
    }

    public static Log with(Logger logger) {
        return new Log(logger);
    }

    public static Log with(Class clazz) {
        return with(LoggerFactory.getLogger(clazz));
    }

    public static Log with(String name) {
        return with(LoggerFactory.getLogger(name));
    }

    public String getName() {
        return logger.getName();
    }

    public void setInfoLogLevel() {
        org.apache.log4j.Logger.getLogger(logger.getName()).setLevel(Level.INFO);
    }

    public boolean isDebugEnabled() {
        return logger.isDebugEnabled();
    }

    public boolean isInfoEnabled() {
        return logger.isInfoEnabled();
    }

    public void exception(Throwable t) {
        exception(t, LogLevel.ERROR);
    }

    public void exception(Throwable t, LogLevel logLevel) {
        try {
            switch (logLevel) {
                case ERROR:
                    logger.error(t.getLocalizedMessage(), t);
                    break;
                case DEBUG:
                    logger.debug(t.getLocalizedMessage(), t);
                    break;
                case WARN:
                    logger.warn(t.getLocalizedMessage(), t);
                    break;
                case INFO:
                    logger.info(t.getLocalizedMessage(), t);
                    break;
                case TRACE:
                    logger.trace(t.getLocalizedMessage(), t);
                    break;
            }

        } catch (Throwable t1) {
            t.printStackTrace();
            t1.printStackTrace();
            if (t instanceof RuntimeException) {
                throw (RuntimeException) t;
            } else {
                throw new RuntimeException(t);
            }
        }
    }

    public void error(String message, Object... params) {
        if (logger.isErrorEnabled()) {
            logger.error(createMessage(message, params));
        }
    }

    public void warn(String message, Object... params) {
        if (logger.isWarnEnabled()) {
            logger.warn(createMessage(message, params));
        }
    }

    public void info(String message, Object... params) {
        if (logger.isInfoEnabled()) {
            logger.info(createMessage(message, params));
        }
    }

    public void debug(String message, Object... params) {
        if (logger.isDebugEnabled()) {
            logger.debug(createMessage(message, params));
        }
    }

    public void trace(String message, Object... params) {
        if (logger.isTraceEnabled()) {
            logger.trace(createMessage(message, params));
        }
    }

    /**
     * Specialisation that logs at warn level and then logs the exception stack trace at debug level
     *
     * @param e       the exception that has happened
     * @param message the message
     * @param params  the params
     */
    public void warnDebug(Exception e, String message, Object... params) {
        if (logger.isWarnEnabled()) {
            logger.warn(createMessage(message, params));
        }
        if (logger.isDebugEnabled()) {
            logger.debug(createMessage(message, params), e);
        }
    }


    /**
     * Specialisation that logs at error level and then logs the exception stack trace at debug level
     *
     * @param e       the exception that has happened
     * @param message the message
     * @param params  the params
     */
    public void errorDebug(Exception e, String message, Object... params) {
        if (logger.isErrorEnabled()) {
            logger.error(createMessage(message, params));
        }
        if (logger.isDebugEnabled()) {
            logger.debug(createMessage(message, params), e);
        }
    }


    public String createMessage(String message, Object[] params) {
        try {
            return String.format(message, params);
        } catch (RuntimeException e) {
            logger.error("Unable to format message: " + message, e);
            return "";
        }
    }

    public static enum LogLevel {
        ERROR,
        WARN,
        DEBUG,
        INFO,
        TRACE
    }
}
