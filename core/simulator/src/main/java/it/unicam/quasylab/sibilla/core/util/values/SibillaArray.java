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

import java.util.Optional;

public class SibillaArray<S extends SibillaValue> implements SibillaValue {

    private final S[] elements;

    private final SibillaArrayType<S, SibillaType<S>> thisType;

    public SibillaArray(S[] elements) {
        this.elements = elements;
        this.thisType = new SibillaArrayType<>();
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
        return 0;
    }

    @Override
    public SibillaType<?> getType() {
        return thisType;
    }


    /**
     * Returns the length of this array.
     *
     * @return the length of this array.
     */
    public int length() {
        return elements.length;
    }


    /**
     * Returns the element in position i if it exists.
     *
     * @param i an index.
     * @return the element in position i if it exists.
     */
    public Optional<S> get(int i) {
        if ((i>=0)&&(i<elements.length)) {
            return Optional.of(elements[i]);
        } else {
            return Optional.empty();
        }
    }
}
