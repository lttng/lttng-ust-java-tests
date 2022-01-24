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

import java.util.Optional;

import org.junit.jupiter.api.extension.AfterTestExecutionCallback;
import org.junit.jupiter.api.extension.BeforeTestExecutionCallback;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.ExtensionContext.Namespace;
import org.junit.jupiter.api.extension.ExtensionContext.Store;
import org.junit.jupiter.api.extension.TestWatcher;

/**
 * Test extension that will print the name of the test being run to stdout.
 */
public class TestPrintExtension implements BeforeTestExecutionCallback, AfterTestExecutionCallback, TestWatcher {

    private static final String START_TIME = "start time";


    @Override
    public void beforeTestExecution(ExtensionContext context) throws Exception {
        System.out.println("\nStarted " + context.getDisplayName() + "\n");
        getStore(context).put(START_TIME, Long.valueOf(System.currentTimeMillis()));
    }

    @Override
    public void afterTestExecution(ExtensionContext context) throws Exception {
        long startTime = getStore(context).remove(START_TIME, long.class).longValue();
        long duration = System.currentTimeMillis() - startTime;

        System.out.println("\nEnded " + context.getDisplayName() + " in " + duration + " ms\n");
    }

    @Override
    public void testDisabled(ExtensionContext context, Optional<String> reason) {
        System.out.println("\nSKIPPING TEST: " + context.getDisplayName() + "\n");
    }

    private Store getStore(ExtensionContext context) {
        return context.getStore(Namespace.create(getClass(), context.getRequiredTestMethod()));
    }
}