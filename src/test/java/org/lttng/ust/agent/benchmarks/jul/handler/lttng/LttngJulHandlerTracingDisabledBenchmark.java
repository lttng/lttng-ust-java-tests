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

package org.lttng.ust.agent.benchmarks.jul.handler.lttng;

import static org.junit.Assert.assertTrue;

import java.io.IOException;

import org.junit.After;
import org.junit.Before;
import org.lttng.tools.ILttngSession;
import org.lttng.tools.ILttngSession.Domain;
import org.lttng.ust.agent.benchmarks.jul.handler.JulHandlerBenchmarkBase;
import org.lttng.ust.agent.jul.LttngLogHandler;

/**
 * Benchmark the LTTng-JUL handler, but with tracing disabled in the tracing
 * session.
 */
public class LttngJulHandlerTracingDisabledBenchmark extends JulHandlerBenchmarkBase {

    private ILttngSession session;

    /**
     * Test setup
     *
     * @throws IOException
     */
    @Before
    public void testSetup() throws IOException {
        handler = new LttngLogHandler();

        session = ILttngSession.createSession(null, Domain.JUL);
        assertTrue(session.enableEvents("non-event"));
        assertTrue(session.start());
    }

    /**
     * Test cleanup
     */
    @After
    public void testTeardown() {
        assertTrue(session.stop());
        session.close();
    }
}
