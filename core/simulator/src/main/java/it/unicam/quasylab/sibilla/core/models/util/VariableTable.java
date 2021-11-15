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

import java.util.HashMap;
import java.util.Map;

/**
 * This class is used to associate a set of variables with an index. Moreover,
 * it has the responbility to provide a string representation of states.
 */
public class VariableTable {

    private final Map<String,Integer> indexes = new HashMap<>();
    private final String[] names;
    private final int[] minValues;
    private final int[] maxValues;

    /**
     * Create an empty table with the given size.
     *
     * @param size size of the created table.
     */
    public VariableTable(int size) {
        this.names = new String[size];
        this.minValues = new int[size];
        this.maxValues = new int[size];
    }

    /**
     * Return the index associated with the given name. -1 is returned if no index
     * exists for the given name.
     *
     * @param name variable name.
     * @return the index associated with the given variable.
     */
    public int indexOf(String name) {
        return indexes.getOrDefault(name, -1);
    }


    /**
     * Record a variable in the table and return the index associated with it. If
     * the variable with the same name is already in the table, the old index is returned.
     *
     * @param idx index used to store the variable
     * @param name variable name.
     * @param min the min value the variable can assume.
     * @param max the max value the variable can assume.
     */
    public synchronized VariableTable record(int idx, String name, int min, int max) {
        if (indexes.containsKey(name)) {
            throw new IllegalArgumentException("Variable "+name+" is already recorded!");
        } else {
            indexes.put(name,idx);
            names[idx] = name;
            minValues[idx] = min;
            maxValues[idx] = max;
        }
        return this;
    }

    /**
     * Check if the given variable is registered in the table.
     *
     * @param name variable name.
     * @return true if the variable is registered in the table, false otherwise.
     */
    public boolean contains(String name) {
        return indexes.containsKey(name);
    }

    /**
     * Check if given value v is compatible with the values the variable with the given index can
     * assume. If this is not the case, the closer value in the interval is returned.
     *
     * @param idx variable index.
     * @param v tentative variable value.
     * @return closer valid value to v.
     */
    public int valueOf(int idx, int v) {
        if (v<minValues[idx]) {
            return minValues[idx];
        }
        if (v>maxValues[idx]) {
            return maxValues[idx];
        }
        return v;
    }


    /**
     * Check if given value v is compatible with the values the variable with the given name can
     * assume. If this is not the case, the closer value in the interval is returned.
     *
     * @param name variable index.
     * @param v tentative variable value.
     * @return closer valid value to v.
     */
    public double valueOf(String name, int v) {
        int idx = indexOf(name);
        if (idx < 0) {
            return Double.NaN;
        }
        return valueOf(idx, v);
    }

    public int size() {
        return names.length;
    }

    public MappingState getMappingStateOf(Map<String, Integer> values) {
        int[] state = new int[names.length];
        for(int i=0; i<state.length; i++) {
            state[i] = valueOf(i, values.getOrDefault(names[i], 0));
        }
        return new MappingState(this, state);
    }
}
