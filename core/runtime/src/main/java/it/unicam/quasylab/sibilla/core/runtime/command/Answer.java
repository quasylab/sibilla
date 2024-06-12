/*
 *  Sibilla:  a Java framework designed to support analysis of Collective
 *  Adaptive Systems.
 *
 *              Copyright (C) ${YEAR}.
 *
 *  See the NOTICE file distributed with this work for additional information
 *  regarding copyright ownership.
 *
 *   Licensed under the Apache License, Version 2.0 (the "License");
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *              http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 *    or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 *
 */

package it.unicam.quasylab.sibilla.core.runtime.command;

import java.util.LinkedList;
import java.util.List;
import java.util.Locale;

public final class Answer implements CommandResult {

    private final List<String> messages = new LinkedList<>();

    public Answer() {}

    public void add(String message) {
        this.messages.add(message);
    }

    public void add(String key, double value) {
        add(key," = ",value);
    }

    public void add(String key, String sep, double value) {
        add(String.format(Locale.US, "%s%s%f", key, sep, value));
    }

}
