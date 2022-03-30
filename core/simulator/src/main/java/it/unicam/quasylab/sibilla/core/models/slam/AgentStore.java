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

package it.unicam.quasylab.sibilla.core.models.slam;

import java.util.HashMap;
import java.util.Map;

/**
 * Represents the memory of an agent.
 */
public final class AgentStore {

    private double now;

    private final Map<AgentVariable,SlamValue> content;

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
    public AgentStore(Map<AgentVariable, SlamValue> content) {
        this(0.0, content);
    }

    /**
     * Creates a new memory at the given time and given content-
     * @param now current time.
     * @param content initial content.
     */
    public AgentStore(double now, Map<AgentVariable, SlamValue> content) {
        this.now = now;
        this.content = new HashMap<>(content);
    }

    /**
     * Returns the value associated with the given variable in this memory. Value {@link SlamValue#NONE} is
     * returned when the variable is not defined in this memory.
     *
     * @param var variable.
     * @return the value of the variable associated with the given index.
     */
    public SlamValue getValue(AgentVariable var) {
        return content.getOrDefault(var, SlamValue.NONE);
    }

    /**
     * Returns the number of cells used in this memory.
     *
     * @return the number of cells used in this memory.
     */
    public synchronized int size() {
        return content.size();
    }

    /**
     * Assigns the given variable with the given value in memory.
     * @param var variable to assign.
     * @param value  variable value.
     */
    public synchronized void set(AgentVariable var, SlamValue value) {
        content.put(var, value);
    }

    public synchronized double now() {
        return now;
    }

    public synchronized void recordTime(double dt) {
        this.now += dt;

    }

}
