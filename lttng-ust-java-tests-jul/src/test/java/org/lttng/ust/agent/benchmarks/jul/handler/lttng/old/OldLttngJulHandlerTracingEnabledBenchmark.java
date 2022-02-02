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

package org.lttng.ust.agent.benchmarks.jul.handler.lttng.old;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import java.lang.reflect.Field;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.lttng.tools.ILttngSession;
import org.lttng.tools.ILttngSession.Domain;
import org.lttng.ust.agent.LTTngAgent;
import org.lttng.ust.agent.benchmarks.jul.handler.JulHandlerBenchmarkBase;
import org.lttng.ust.agent.jul.LttngLogHandler;

/**
 * Benchmark for the LTTng-UST handler, using the legacy API. Tracing is
 * enabled in the tracing session.
 */
@Tag("agent:jul")
@Tag("domain:jul")
@Tag("benchmark")
@SuppressWarnings("deprecation")
public class OldLttngJulHandlerTracingEnabledBenchmark extends JulHandlerBenchmarkBase {

    private ILttngSession session;
    private LTTngAgent agent;
    private LttngLogHandler agentHandler;


    /**
     * Test setup
     */
    @BeforeEach
    public void testSetup() {
        agent = LTTngAgent.getLTTngAgent();

        /*
         * The "old API" works by attaching a handler managed by the agent to
         * the root JUL logger. This causes problems here, because we use
         * logger.setUserParentHandler(false), which does not trigger the
         * handler as would be expected.
         *
         * Instead we will retrieve this handler through reflection, and attach
         * it to our own logger here for the duration of the test.
         */
        try {
            Field julHandlerField = LTTngAgent.class.getDeclaredField("julHandler");
            julHandlerField.setAccessible(true);
            agentHandler = (LttngLogHandler) julHandlerField.get(agent);

            logger.addHandler(agentHandler);

        } catch (ReflectiveOperationException e) {
            fail(e.getMessage());
        }

        session = ILttngSession.createSession(null, Domain.JUL);
        assertTrue(session.enableAllEvents());
        assertTrue(session.start());
    }

    /**
     * Test cleanup
     */
    @AfterEach
    public void testTeardown() {
        assertTrue(session.stop());
        session.close();

        logger.removeHandler(agentHandler);
        agent.dispose();
    }
}
