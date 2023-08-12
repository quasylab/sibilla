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

package it.unicam.quasylab.sibilla.core.models.lio;

import it.unicam.quasylab.sibilla.core.util.values.SibillaValue;

import java.util.Arrays;
import java.util.Objects;

/**
 * The instances of this class are used to represent the name of an agent.
 * This consists of a string and an array of indexes.
 */
public final class LIOAgentName {

    private final String name;

    private final SibillaValue[] indexes;

    /**
     * Creates a new instance with the given name and indexes.
     *
     * @param name agent name
     * @param indexes agent indexes
     */
    public LIOAgentName(String name, SibillaValue ... indexes) {
        this.name = name;
        this.indexes = indexes;
    }

    /**
     * Returns the name of the agent.
     *
     * @return the name of the agent.
     */
    public String getName() {
        return name;
    }

    /**
     * Returns the number of indexes in agent name.
     *
     * @return the number of arguments in agent name.
     */
    public int numberOfIndexes() {
        return indexes.length;
    }

    /**
     * Returns the index in position i.
     * @param i the position of the index.
     * @return the index in position i.
     */
    public SibillaValue get(int i) {
        return indexes[i];
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        LIOAgentName agentName = (LIOAgentName) o;
        return Objects.equals(name, agentName.name) && Arrays.equals(indexes, agentName.indexes);
    }

    @Override
    public int hashCode() {
        int result = Objects.hash(name);
        result = 31 * result + Arrays.hashCode(indexes);
        return result;
    }

    @Override
    public String toString() {
        if (indexes.length == 0) {
            return name;
        } else {
            return name+"["+Arrays.toString(indexes)+"]";
        }
    }

    /**
     * Returns the indexes of this agent name.
     *
     * @return the indexes of this agent name.
     */
    public SibillaValue[] getIndexes() {
        return indexes;
    }
}
