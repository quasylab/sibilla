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

package it.unicam.quasylab.sibilla.core.util.values;

import java.util.Map;

/**
 * This value represents a record that associates SibillaValues to Strings.
 *
 */
public class SibillaRecord implements SibillaValue {

    private final Map<String, SibillaValue> values;

    /**
     * Creates a new instance with the given binding.
     *
     * @param values mapping fields name to values to use in the created record.
     */
    public SibillaRecord(Map<String, SibillaValue> values) {
        this.values = values;
    }

    /**
     * Returns the value associated with the given field name.
     *
     * @param name field name.
     * @return
     */
    public SibillaValue get(String name) {
        return values.getOrDefault(name, SibillaValue.ERROR_VALUE);
    }

    @Override
    public double doubleOf() {
        return Double.NaN;
    }

    @Override
    public boolean booleanOf() {
        return false;
    }

    @Override
    public int intOf() {
        return -1;
    }

}
