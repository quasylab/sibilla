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

import java.util.Arrays;
import java.util.function.IntFunction;

/**
 * Represents the memory of an agent.
 */
public final class AgentMemory {

    private double now;

    private final SlamValue[] content;

    /**
     * Creates a new memory with the given content.
     *
     * @param content memory content.
     */
    public AgentMemory(SlamValue ... content) {
        this(0.0, content);
    }

    public AgentMemory(double now, SlamValue ... content) {
        this.now = now;
        this.content = content;
    }

    /**
     * Returns the value of the variable associated with the given index. An {@link IndexOutOfBoundsException} is
     * thrown if an invalid index is passed.
     *
     * @param idx variable index.
     * @return the value of the variable associated with the given index.
     */
    public SlamValue getValue(int idx) {
        return content[idx];
    }

    /**
     * Returns the number of cells used in this memory.
     *
     * @return the number of cells used in this memory.
     */
    public synchronized int size() {
        return content.length;
    }

    /**
     * Sets the value of the given variable to the given value. An {@link IndexOutOfBoundsException} is
     * thrown if an invalid index is passed.
     * @param idx variable index.
     * @param value  variable value.
     */
    public synchronized void set(int idx, SlamValue value) {
        content[idx] = value;
    }

    public synchronized double now() {
        return now;
    }

    public synchronized void recordTime(double dt) {
        this.now += dt;
    }

}
