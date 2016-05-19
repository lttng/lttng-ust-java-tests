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

package org.lttng.ust.agent.context;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.IOException;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.lttng.ust.agent.utils.TestPrintRunner;

/**
 * Generic tests related to the context retrieval mechanisms.
 *
 * @author Alexandre Montplaisir
 */
@RunWith(TestPrintRunner.class)
public class ContextRegistrationIT {

    private ContextInfoManager mgr;

    /**
     * Test setup
     */
    @Before
    public void testSetup() {
        try {
            mgr = ContextInfoManager.getInstance();
        } catch (SecurityException | IOException e) {
            fail(e.getMessage());
        }
    }

    /**
     * Test registration and unregistration of a basic retriever.
     */
    @Test
    public void testRegistration() {
        final String retrieverName = "test.retriever";
        final IContextInfoRetriever emptyRetriever = (key -> null);

        boolean ret = mgr.registerContextInfoRetriever(retrieverName, emptyRetriever);
        assertTrue(ret);

        IContextInfoRetriever retriever2 = mgr.getContextInfoRetriever(retrieverName);
        assertEquals(emptyRetriever, retriever2);

        mgr.unregisterContextInfoRetriever(retrieverName);
    }

    /**
     * Test registration of retrievers with invalid names.
     */
    @Test
    public void testRegistrationInvalid() {
        String[] invalidNames = new String[] { "test.rétriever", "with space", "1numberfirst" };
        for (String retrieverName : invalidNames) {
            final IContextInfoRetriever emptyRetriever = (key -> null);

            boolean ret = mgr.registerContextInfoRetriever(retrieverName, emptyRetriever);
            assertFalse(ret);

            IContextInfoRetriever retriever = mgr.getContextInfoRetriever(retrieverName);
            assertNull(retriever);
        }
    }
}
