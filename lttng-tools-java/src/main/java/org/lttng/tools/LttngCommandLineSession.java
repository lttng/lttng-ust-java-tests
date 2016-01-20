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

package org.lttng.tools;

import static org.lttng.tools.utils.ShellUtils.executeCommand;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import org.lttng.tools.utils.ShellUtils;

/**
 * Implementation of {@link ILttngSession} which uses the command-line "lttng"
 * tool to manipulate the session. Creating an instance will run "lttng create",
 * close()'ing it will run "lttng destroy".
 *
 * @author Alexandre Montplaisir
 */
class LttngCommandLineSession implements ILttngSession {

    private final String sessionName;
    private final Domain domain;

    private volatile boolean channelCreated = false;

    /**
     * Constructor to create a new LTTng tracing session.
     *
     * @param sessionName
     *            The name of the session to use. It can be null, in which case
     *            we will provide a unique random name.
     * @param domain
     *            The tracing domain of this session
     */
    public LttngCommandLineSession(String sessionName, Domain domain) {
        if (sessionName != null) {
            this.sessionName = sessionName;
        } else {
            this.sessionName = UUID.randomUUID().toString();
        }
        this.domain = domain;

        /* Create the session in LTTng */
        executeCommand(Arrays.asList("lttng", "create", this.sessionName));
    }

    @Override
    public void close() {
        /* Destroy the session */
        executeCommand(Arrays.asList("lttng", "destroy", sessionName));
     // FIXME also delete the trace we generated ?
    }

    @Override
    public boolean enableEvent(String eventName, String loglevel, boolean loglevelOnly, String filter) {
        channelCreated = true;

        List<String> command = new ArrayList<>();
        command.add("lttng");
        command.add("enable-event");
        command.add(domain.flag());
        command.add(eventName);

        if (loglevel != null) {
            if (loglevelOnly) {
                command.add("--loglevel-only");
            } else {
                command.add("--loglevel");
            }
            command.add(loglevel);
        }

        if (filter != null) {
            command.add("--filter");
            command.add(filter);
        }

        command.add("-s");
        command.add(sessionName);

        return executeCommand(command);
    }

    @Override
    public boolean enableAllEvents() {
        channelCreated = true;
        return executeCommand(Arrays.asList(
                "lttng", "enable-event", domain.flag(), "-a", "-s", sessionName));
    }

    @Override
    public boolean enableEvents(String... enabledEvents) {
        if (enabledEvents == null || enabledEvents.length == 0) {
            throw new IllegalArgumentException();
        }
        channelCreated = true;
        return executeCommand(Arrays.asList(
                "lttng", "enable-event", domain.flag(),
                Arrays.stream(enabledEvents).collect(Collectors.joining(",")),
                "-s", sessionName));
    }

    @Override
    public boolean disableEvents(String... disabledEvents) {
        if (disabledEvents == null || disabledEvents.length == 0) {
            throw new IllegalArgumentException();
        }
        return executeCommand(Arrays.asList(
                "lttng", "disable-event", domain.flag(),
                Arrays.stream(disabledEvents).collect(Collectors.joining(",")),
                "-s", sessionName));
    }

    @Override
    public boolean disableAllEvents() {
        return executeCommand(Arrays.asList(
                "lttng", "disable-event", domain.flag(), "-a", "-s", sessionName));
    }

    @Override
    public Set<String> listEvents() {
        List<String> output = ShellUtils.getOutputFromCommand(true, Arrays.asList("lttng", "list", domain.flag()));
        return output.stream()
                .map(e -> e.trim())
                .filter(e -> e.startsWith("- "))
                .map(e -> e.substring(2))
                .collect(Collectors.toSet());
    }

    @Override
    public boolean enableAppContext(String retrieverName, String contextName) {
        return executeCommand(Arrays.asList(
                "lttng", "add-context", domain.flag(),
                "-t", "$app." + retrieverName + ':' + contextName,
                "-s", sessionName));
    }

    @Override
    public boolean start() {
        /*
         * We have to enable a channel for 'lttng start' to work. However, we
         * cannot enable a channel directly, see
         * https://bugs.lttng.org/issues/894 . Instead we will enable an event
         * we know does not exist
         */
        if (!channelCreated) {
            enableEvents("non-event");
        }
        return executeCommand(Arrays.asList("lttng", "start", sessionName));
    }

    @Override
    public boolean stop() {
        return executeCommand(Arrays.asList("lttng", "stop", sessionName));
    }

    @Override
    public List<String> view() {
        return ShellUtils.getOutputFromCommand(true, Arrays.asList("lttng", "view", sessionName));
    }
}
