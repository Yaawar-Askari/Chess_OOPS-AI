package com.chess.utils;

import java.util.logging.Level;

/**
 * Simple logging utility for the chess game
 */
public class Logger {
    private final java.util.logging.Logger javaLogger;
    
    private Logger(Class<?> clazz) {
        this.javaLogger = java.util.logging.Logger.getLogger(clazz.getName());
    }
    
    public static Logger getLogger(Class<?> clazz) {
        return new Logger(clazz);
    }
    
    public void info(String message) {
        javaLogger.info(message);
    }
    
    public void warn(String message) {
        javaLogger.warning(message);
    }
    
    public void error(String message) {
        javaLogger.severe(message);
    }
    
    public void error(String message, Throwable throwable) {
        javaLogger.log(Level.SEVERE, message, throwable);
    }
    
    public void debug(String message) {
        javaLogger.fine(message);
    }
} 