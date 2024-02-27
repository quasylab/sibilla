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

package it.unicam.quasylab.sibilla.core.util.values;

import org.apache.commons.math3.random.RandomGenerator;

/**
 * This functional interface is used to represent a random function that, given a value of type <code>T</code>
 * returns a value of type <code>R</code>.
 *
 * @param <T> type of function parameter
 */
@FunctionalInterface
public interface SibillaRandomFunction<T, R> {


    /**
     * Returns the sampled value by using the given random generator and the given value.
     *
     * @param rg the random generator used to sample values
     * @param t the function parameter
     * @return the sampled value by using the given random generator and the given value.
     */
    R eval(RandomGenerator rg, T t);

}
