package com.youzan.sz.common.util;

import org.slf4j.Logger;
import org.slf4j.Marker;

/**
 * Created by Kid on 16/8/10.
 */
public class Log {
    private final Logger logger;

    public Log(Logger logger) {
        this.logger = logger;
    }

    public void trade(String s, Object... objects) {
        if (logger.isTraceEnabled()) {
            logger.trace(s, objects);
        }
    }

    public void trade(String s, Throwable t) {
        if (logger.isTraceEnabled()) {
            logger.trace(s, t);
        }
    }

    public void trade(Marker marker, String s, Object... objects) {
        if (logger.isTraceEnabled()) {
            logger.trace(marker, s, objects);
        }
    }

    public void trade(Marker marker, String s, Throwable t) {
        if (logger.isTraceEnabled()) {
            logger.trace(marker, s, t);
        }
    }


    public void debug(String s, Object... objects) {
        if (logger.isDebugEnabled()) {
            logger.debug(s, objects);
        }
    }

    public void debug(String s, Throwable t) {
        if (logger.isDebugEnabled()) {
            logger.debug(s, t);
        }
    }

    public void debug(Marker marker, String s, Object... objects) {
        if (logger.isDebugEnabled()) {
            logger.debug(marker, s, objects);
        }
    }

    public void debug(Marker marker, String s, Throwable t) {
        if (logger.isDebugEnabled()) {
            logger.debug(marker, s, t);
        }
    }

    public void info(String s, Object... objects) {
        if (logger.isInfoEnabled()) {
            logger.info(s, objects);
        }
    }

    public void info(String s, Throwable t) {
        if (logger.isInfoEnabled()) {
            logger.info(s, t);
        }
    }

    public void info(Marker marker, String s, Object... objects) {
        if (logger.isInfoEnabled()) {
            logger.info(marker, s, objects);
        }
    }

    public void info(Marker marker, String s, Throwable t) {
        if (logger.isInfoEnabled()) {
            logger.info(marker, s, t);
        }
    }

    public void warn(String s, Object... objects) {
        logger.warn(s, objects);
    }

    public void warn(String s, Throwable t) {
        logger.warn(s, t);
    }

    public void warn(Marker marker, String s, Object... objects) {
        logger.warn(marker, s, objects);
    }

    public void warn(Marker marker, String s, Throwable t) {
        logger.warn(marker, s, t);
    }

    public void error(String s, Object... objects) {
        logger.error(s, objects);
    }

    public void error(String s, Throwable t) {
        logger.error(s, t);
    }

    public void error(Marker marker, String s, Object... objects) {
        logger.error(marker, s, objects);
    }

    public void error(Marker marker, String s, Throwable t) {
        logger.error(marker, s, t);
    }

}

