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

package org.lttng.ust.agent.integration.context;

/**
 * Values defined by the LTTng tracer related to dynamically-typed contexts
 */
interface LttngContextValues {

    // TODO check that these are all the right values
    String NULL_TYPE_NAME = "";
    String INTEGER_TYPE_NAME = "int";
    String LONG_TYPE_NAME = "long";
    String DOUBLE_TYPE_NAME = "double";
    String CHARACTER_TYPE_NAME = "char";
    String FLOAT_TYPE_NAME = "float";
    String BYTE_TYPE_NAME = "byte";
    String SHORT_TYPE_NAME = "short";
    String BOOLEAN_TYPE_NAME = "bool";
    String STRING_TYPE_NAME = "string";
}
