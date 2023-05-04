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

package it.unicam.quasylab.sibilla.core.models;

/**
 * Identifies state whose elements are indexed.
 *
 * @param <S> data type of indexed elements.
 */
public interface IndexedState<S> extends State {

    /**
     * Returns the element at position i.
     *
     * @param i index.
     * @return the element at position i.
     */
    S get(int i);

    /**
     * Returns the number of elements.
     *
     * @return the number of elements.
     */
    int size();


}
