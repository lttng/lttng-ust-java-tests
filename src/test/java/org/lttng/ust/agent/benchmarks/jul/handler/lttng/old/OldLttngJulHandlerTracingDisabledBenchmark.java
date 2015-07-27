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

import static org.junit.Assert.assertTrue;

import org.junit.After;
import org.junit.Before;
import org.lttng.ust.agent.LTTngAgent;
import org.lttng.ust.agent.benchmarks.jul.handler.JulHandlerBenchmarkBase;
import org.lttng.ust.agent.utils.LttngSession;
import org.lttng.ust.agent.utils.LttngSession.Domain;

@SuppressWarnings("deprecation")
public class OldLttngJulHandlerTracingDisabledBenchmark extends JulHandlerBenchmarkBase {

    private LttngSession session;

    @Before
    public void testSetup() {
        LTTngAgent.getLTTngAgent();

        session = new LttngSession(null, Domain.JUL);
        assertTrue(session.enableEvents("non-event"));
        assertTrue(session.start());
    }

    @After
    public void testTeardown() {
        assertTrue(session.stop());
        session.close();

        LTTngAgent.dispose();
    }
}
