/*
 * Copyright (C) 2015, EfficiOS Inc., Alexandre Montplaisir <alexmonthy@efficios.com>
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

package org.lttng.ust.agent.integration.events;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.lttng.tools.ILttngSession;

/**
 * Base class for the list events command tests
 */
public abstract class ListEventsITBase {

    protected static final String LOGGER_NAME_1 = "org.lttng";
    protected static final String LOGGER_NAME_2 = "org.lttng.mycomponent";
    protected static final String LOGGER_NAME_3 = "org.lttng.myothercomponent-àéç";

    private ILttngSession session;

    /**
     * Common test setup
     */
    @Before
    public void testSetup() {
        session = ILttngSession.createSession(null, getDomain());
    }

    /**
     * Common test teardown
     */
    @After
    public void testTeardown() {
        session.close();
    }

    protected abstract ILttngSession.Domain getDomain();

    protected abstract void attachHandlerToLogger(int handlerIndex, int loggerIndex);

    protected abstract void detachHandlerFromLogger(int handlerIndex, int loggerIndex);

    /**
     * Test with many loggers existing, but none of them having a LTTng handler
     * attached.
     */
    @Test
    public void testManyLoggersNoneAttached() {
        /* Don't attach anything */
        List<String> actualEvents = session.listEvents();
        assertTrue(actualEvents.isEmpty());
    }

    /**
     * Test with many loggers existing, but only a subset of them has a LTTng
     * handler attached.
     */
    @Test
    public void testManyLoggersSomeAttached() {
        attachHandlerToLogger(1, 1);

        List<String> expectedEvents = Arrays.asList(LOGGER_NAME_1);
        List<String> actualEvents = session.listEvents();

        Collections.sort(expectedEvents);
        Collections.sort(actualEvents);

        assertEquals(expectedEvents, actualEvents);
    }

    /**
     * Test with many loggers existing, and all of them having a LTTng handler
     * attached.
     */
    @Test
    public void testManyLoggersAllAttached() {
        attachHandlerToLogger(1, 1);
        attachHandlerToLogger(2, 2);
        attachHandlerToLogger(2, 3);

        List<String> expectedEvents = Arrays.asList(LOGGER_NAME_1, LOGGER_NAME_2, LOGGER_NAME_3);
        List<String> actualEvents = session.listEvents();

        Collections.sort(expectedEvents);
        Collections.sort(actualEvents);

        assertEquals(expectedEvents, actualEvents);
    }

    /**
     * Test with some loggers having had handlers attached but then detached.
     */
    @Test
    public void testLoggersSomeDetached() {
        attachHandlerToLogger(1, 1);
        attachHandlerToLogger(2, 2);

        attachHandlerToLogger(2, 3);
        detachHandlerFromLogger(2, 3);
        /* Only loggers 1 and 2 will be attached */

        List<String> expectedEvents = Arrays.asList(LOGGER_NAME_1, LOGGER_NAME_2);
        List<String> actualEvents = session.listEvents();

        Collections.sort(expectedEvents);
        Collections.sort(actualEvents);

        assertEquals(expectedEvents, actualEvents);
    }

    /**
     * Test with all loggers having had handlers attached and then detached.
     */
    @Test
    public void testLoggersAllDetached() {
        attachHandlerToLogger(1, 1);
        attachHandlerToLogger(2, 2);
        attachHandlerToLogger(2, 3);
        detachHandlerFromLogger(1, 1);
        detachHandlerFromLogger(2, 2);
        detachHandlerFromLogger(2, 3);

        List<String> actualEvents = session.listEvents();
        assertTrue(actualEvents.isEmpty());
    }
}
