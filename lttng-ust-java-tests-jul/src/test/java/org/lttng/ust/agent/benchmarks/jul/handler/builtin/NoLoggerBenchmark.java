/*
 * Copyright (C) 2016, EfficiOS Inc., Alexandre Montplaisir <alexmonthy@efficios.com>
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

package org.lttng.ust.agent.benchmarks.jul.handler.builtin;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.lttng.ust.agent.benchmarks.jul.handler.JulHandlerBenchmarkBase;

/**
 * Benchmark that will avoid creating a Logger entirely, to benchmark just the
 * bare worker.
 */
@Tag("agent:jul")
@Tag("domain:jul")
@Tag("benchmark")
public class NoLoggerBenchmark extends JulHandlerBenchmarkBase {

    /**
     * Override the super class's setup() to avoid creating a Logger.
     */
    @Override
    @BeforeEach
    public void setup() {
        logger = null;
        handler = null;
    }

    /**
     * Override the super class's teardown()
     */
    @Override
    @AfterEach
    public void teardown() {
    }
}
