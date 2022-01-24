/*
 * Copyright (C) 2022, EfficiOS Inc.
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along
 * with this program; if not, write to the Free Software Foundation, Inc.,
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 */

package org.lttng.ust.agent.utils;

import java.net.URI;
import java.net.URL;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.core.LoggerContext;

/**
 * Log4j 2.x test context utilities.
 */
public class Log4j2TestContext {

    private final URI configFileUri;

    private LoggerContext loggerContext;

    /**
     * @param configFile path to the log4j configuration file.
     */
    public Log4j2TestContext(String configFile) {

        URL resource = getClass().getClassLoader().getResource(configFile);

        if (resource == null) {
            throw new IllegalArgumentException("Config file not found: " + configFile);
        }

        try {
            this.configFileUri = resource.toURI();
        } catch (Exception e) {
            throw new IllegalArgumentException("Config file invalid URI: " + resource);
        }
    }

    /**
     * @return the log4j2 logger context.
     */
    public synchronized LoggerContext getLoggerContext() {
        return loggerContext;
    }

    /**
     * Initialize the log4j2 context before running a test.
     */
    public synchronized void beforeTest() {
        loggerContext = (LoggerContext) LogManager.getContext(
                ClassLoader.getSystemClassLoader(), false, configFileUri);
    }

    /**
     * Dispose of the log4j2 context after running a test.
     */
    public synchronized void afterTest() {
        loggerContext.stop();
    }
}
