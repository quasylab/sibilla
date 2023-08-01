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

package it.unicam.quasylab.sibilla.core.models.util;

import it.unicam.quasylab.sibilla.core.models.ImmutableState;
import it.unicam.quasylab.sibilla.core.models.State;
import it.unicam.quasylab.sibilla.core.util.values.SibillaValue;

import java.util.Arrays;
import java.util.Map;
import java.util.function.Function;
import java.util.function.ToIntFunction;

/**
 * This is a utility class used to represent states where variables are mapped to values (double or integers).
 */
public class MappingState implements ImmutableState {

    /**
     * Table used to index variables.
     */
    private final VariableTable table;

    /**
     * Array with values.
     */
    private final SibillaValue[] state;

    /**
     * Create a new state from the given table and values.
     *
     * @param table index of variables.
     * @param state values.
     */
    public MappingState(VariableTable table, SibillaValue[] state) {
        this.state = Arrays.copyOf(state, state.length);
        this.table = table;
    }

    /**
     * Create a new state as a copy of the given one.
     *
     * @param state state to copy.
     */
    public MappingState(MappingState state) {
        this(state.table, state.state);
    }


    /**
     * Return the value of variabile with index i as an integer.
     *
     * @param i variable index.
     * @return the value of variabile with index i as an integer.
     */
    public int getIntValue(int i) {
        return state[i].intOf();
    }

    /**
     * Return the value of variable with index i as a double.
     *
     * @param i variable index.
     * @return the value of variable with index i as a double.
     */
    public double getDoubleValue(int i) {
        return state[i].doubleOf();
    }

    /**
     * Return the value of the given variable as an integer.
     *
     * @param var variable name.
     * @return the value of the given variable as an integer.
     */
    public int getIntValue(String var) {
        return getIntValue(table.indexOf(var));
    }

    /**
     * Return the value of the given variable as a double.
     *
     * @param var variable name.
     * @return the value of the given variable as a double.
     */
    public double getDoubleValue(String var) {
        return getDoubleValue(table.indexOf(var));
    }

    /**
     * Return a new state obtained from the current one by updating each variable i
     * with the value resulting from the evaluation of the function associated with i.
     *
     * @param update a mapping associating each variable with its update.
     * @return the new state resulting from the update.
     */
    public MappingState apply(Map<Integer, ToIntFunction<MappingState>> update) {
        if (update.isEmpty()) {
            return this;
        }
        SibillaValue[] copyState = Arrays.copyOf(state,state.length);
        for (Map.Entry<Integer, ToIntFunction<MappingState>> e: update.entrySet()) {
            copyState[e.getKey()] = SibillaValue.of(e.getValue().applyAsInt(this));
        }
        return new MappingState(table, copyState);
    }


    public SibillaValue get(int idx) {
        return this.state[idx];
    }
}
