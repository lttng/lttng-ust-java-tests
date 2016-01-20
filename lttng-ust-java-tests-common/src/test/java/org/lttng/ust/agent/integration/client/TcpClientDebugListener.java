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

package org.lttng.ust.agent.integration.client;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.lttng.ust.agent.client.ILttngTcpClientListener;
import org.lttng.ust.agent.session.EventRule;

/**
 * TCP client listener used for test. Instead of "handling" commands, it just
 * keep tracks of commands it receives.
 *
 * @author Alexandre Montplaisir
 */
public class TcpClientDebugListener implements ILttngTcpClientListener {

    private final List<EventRule> enabledEventCommands = Collections.synchronizedList(new ArrayList<>());
    private final List<String> disabledEventCommands = Collections.synchronizedList(new ArrayList<>());

    private final List<String> enabledAppContextCommands = Collections.synchronizedList(new ArrayList<>());
    private final List<String> disabledAppContextCommands = Collections.synchronizedList(new ArrayList<>());

    @Override
    public boolean eventEnabled(EventRule rule) {
        enabledEventCommands.add(rule);
        return true;
    }

    @Override
    public boolean eventDisabled(String name) {
        disabledEventCommands.add(name);
        return true;
    }

    @Override
    public boolean appContextEnabled(String contextRetrieverName, String contextName) {
        enabledAppContextCommands.add(contextRetrieverName + ':' + contextName);
        return true;
    }

    @Override
    public boolean appContextDisabled(String contextRetrieverName, String contextName) {
        disabledAppContextCommands.add(contextRetrieverName + ':' + contextName);
        return true;
    }

    /**
     * Not yet implemented
     */
    @Override
    public List<String> listAvailableEvents() {
        // TODO NYI
        return Collections.EMPTY_LIST;
    }

    /**
     * @return The "enable-event" commands that were received, since
     *         instantiation or the last {@link #clearAllCommands}.
     */
    public List<EventRule> getEnabledEventCommands() {
        synchronized (enabledEventCommands) {
            return new ArrayList<>(enabledEventCommands);
        }
    }

    /**
     * @return The "disable-event" commands that were received, since
     *         instantiation or the last {@link #clearAllCommands}.
     */
    public List<String> getDisabledEventCommands() {
        synchronized (disabledEventCommands) {
            return new ArrayList<>(disabledEventCommands);
        }
    }

    /**
     * @return The "add-context" commands that were received since instantiation
     *         or the last {@link #clearAllCommands}.
     */
    public List<String> getEnabledAppContextCommands() {
        synchronized (enabledAppContextCommands) {
            return new ArrayList<>(enabledAppContextCommands);
        }
    }

    /**
     * Return the number of "context disabled" commands received.
     *
     * There is no equivalent command in the lttng CLI, but the sessiond will
     * send such messages through the agent socket when a session is destroyed
     * and had contexts enabled.
     *
     * @return The number of "context disabled" commands received.
     */
    public List<String> getDisabledAppContextCommands() {
        synchronized (disabledAppContextCommands) {
            return new ArrayList<>(disabledAppContextCommands);
        }
    }

    /**
     * Clear all tracked data.
     */
    public void clearAllCommands() {
        enabledEventCommands.clear();
        disabledEventCommands.clear();
        enabledAppContextCommands.clear();
        disabledAppContextCommands.clear();
    }

}
