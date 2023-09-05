/*
 * Sibilla:  a Java framework designed to support analysis of Collective
 * Adaptive Systems.
 *
 *             Copyright (C) 2020.
 *
 * See the NOTICE file distributed with this work for additional information
 * regarding copyright ownership.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *            http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 *  or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package it.unicam.quasylab.sibilla.core.models.slam.data;

import it.unicam.quasylab.sibilla.core.util.datastructures.Pair;
import it.unicam.quasylab.sibilla.core.util.datastructures.SibillaMap;
import it.unicam.quasylab.sibilla.core.util.values.SibillaValue;

import java.util.List;
import java.util.Map;

/**
 * Represents the memory of an agent.
 */
public final class AgentStore {

    private final double now;

    private final SibillaMap<AgentVariable, SibillaValue> content;

    /**
     * Creates an empty memory at time 0.
     */
    public AgentStore() {
        this(Map.of());
    }

    /**
     * Creates a new memory with the given content.
     *
     * @param content memory content.
     */
    public AgentStore(Map<AgentVariable, SibillaValue> content) {
        this(0.0, content);
    }

    /**
     * Creates a new memory at the given time and given content-
     * @param now current time.
     * @param content initial content.
     */
    public AgentStore(double now, Map<AgentVariable, SibillaValue> content) {
        this(now, SibillaMap.of(content));
    }

    public AgentStore(double now, SibillaMap<AgentVariable, SibillaValue> content) {
        this.now = now;
        this.content = content;
    }

    /**
     * Returns the value associated with the given variable in this memory. Value {@link SlamValue#NONE} is
     * returned when the variable is not defined in this memory.
     *
     * @param var variable.
     * @return the value of the variable associated with the given index.
     */
    public SibillaValue getValue(AgentVariable var) {
        return content.getOrDefault(var, SibillaValue.ERROR_VALUE);
    }

    /**
     * Assigns the given variable with the given value in memory.
     * @param var variable to assign.
     * @param value  variable value.
     */
    public AgentStore set(AgentVariable var, SibillaValue value) {
        return new AgentStore(this.now, this.content.add(var, value));
    }

    public synchronized double now() {
        return now;
    }

    public AgentStore recordTime(double dt) {
        return new AgentStore(this.now+dt, this.content);
    }

    public AgentStore remove(AgentVariable variable) {
        return new AgentStore(now, content.remove(variable));
    }

    public static AgentStore of(AgentVariable[] variables, SibillaValue[] values) {
        if (variables.length != values.length) {
            throw new IllegalArgumentException(String.format("Illegal number of values! Expected %d are %d", variables.length, values.length));
        }
        AgentStore result = new AgentStore();
        for(int i=0; i<variables.length; i++) {
            result = result.set(variables[i], values[i]);
        }
        return result;
    }

    public AgentStore set(List<Pair<AgentVariable, SibillaValue>> assignments) {
        AgentStore store = this;
        for (Pair<AgentVariable, SibillaValue> a : assignments) {
            store = store.set(a.getKey(), a.getValue());
        }
        return store;
    }
}
