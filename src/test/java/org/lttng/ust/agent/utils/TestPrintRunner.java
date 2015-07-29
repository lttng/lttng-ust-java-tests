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

package org.lttng.ust.agent.utils;

import org.junit.runner.Description;
import org.junit.runner.notification.Failure;
import org.junit.runner.notification.RunListener;
import org.junit.runner.notification.RunNotifier;
import org.junit.runners.BlockJUnit4ClassRunner;
import org.junit.runners.model.InitializationError;

/**
 * Test runner that will print the name of the test being run to stdout.
 *
 * Thanks to http://stackoverflow.com/a/27070843/4227853 for the tips.
 *
 * @author Alexandre Montplaisir
 */
public class TestPrintRunner extends BlockJUnit4ClassRunner {

    /**
     * Consructor
     *
     * @param klass
     * @throws InitializationError
     */
    public TestPrintRunner(Class<?> klass) throws InitializationError {
        super(klass);
    }

    @Override
    public void run(RunNotifier notifier) {
        notifier.addListener(new TestPrintListener());
        super.run(notifier);
    }

    /**
     * Listener that will print the class and test name to stdout.
     */
    public static class TestPrintListener extends RunListener {

        @Override
        public void testStarted(Description description) {
            System.out.println("Running " + getTestName(description));
        }

        @Override
        public void testAssumptionFailure(Failure failure) {
            System.out.println("SKIPPING TEST: " + getTestName(failure.getDescription()));
            System.out.println(failure.getMessage());
        }

        /**
         * Get the className#methodName from a Description.
         */
        private static String getTestName(Description description) {
            return description.getClassName() + '#' + description.getMethodName();
        }
    }

}
