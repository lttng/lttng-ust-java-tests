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

import org.lttng.ust.agent.context.IContextInfoRetriever;

interface ContextInfoRetrieverStubs {

    String CONTEXT_NAME = "some.context_name";

    Integer INTEGER_VALUE = Integer.valueOf(42);
    Long LONG_VALUE = Long.valueOf(9001);
    Double DOUBLE_VALUE = Double.valueOf(11.55);
    Character CHARACTER_VALUE = Character.valueOf('a');
    Float FLOAT_VALUE = Float.valueOf(2.8f);
    Byte BYTE_VALUE = Byte.valueOf((byte) 8);
    Short SHORT_VALUE = Short.valueOf((short) 500);
    String STRING_VALUE = "ContextValue";

    String OBJECT_VALUE_STRING = "ValueToString";
    Object OBJECT_VALUE = new Object() {
        @Override
        public String toString() {
            return OBJECT_VALUE_STRING;
        }
    };


    IContextInfoRetriever NULL_RETRIEVER = (key ->  null);
    IContextInfoRetriever INTEGER_RETRIEVER = (key -> (CONTEXT_NAME.equals(key) ? INTEGER_VALUE : null));
    IContextInfoRetriever LONG_RETRIEVER = (key -> (CONTEXT_NAME.equals(key) ? LONG_VALUE : null));
    IContextInfoRetriever DOUBLE_RETRIEVER = (key -> (CONTEXT_NAME.equals(key) ? DOUBLE_VALUE : null));
    IContextInfoRetriever CHARACTER_RETRIEVER = (key -> (CONTEXT_NAME.equals(key) ? CHARACTER_VALUE : null));
    IContextInfoRetriever FLOAT_RETRIEVER = (key -> (CONTEXT_NAME.equals(key) ? FLOAT_VALUE : null));
    IContextInfoRetriever BYTE_RETRIEVER = (key -> (CONTEXT_NAME.equals(key) ? BYTE_VALUE : null));
    IContextInfoRetriever SHORT_RETRIEVER = (key -> (CONTEXT_NAME.equals(key) ? SHORT_VALUE : null));
    IContextInfoRetriever BOOLEAN_TRUE_RETRIEVER = (key -> (CONTEXT_NAME.equals(key) ? Boolean.TRUE : null));
    IContextInfoRetriever BOOLEAN_FALSE_RETRIEVER = (key -> (CONTEXT_NAME.equals(key) ? Boolean.FALSE : null));
    IContextInfoRetriever STRING_RETRIEVER = (key -> (CONTEXT_NAME.equals(key) ? STRING_VALUE : null));
    IContextInfoRetriever OBJECT_RETRIEVER = (key -> (CONTEXT_NAME.equals(key) ? OBJECT_VALUE : null));

}
