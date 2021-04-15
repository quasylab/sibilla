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

import java.io.Serializable;
import java.util.function.BiFunction;
import java.util.function.Function;

/**
 * Instances of this class can be used to create a value of a given data type <code>S</code>
 * from a given EvaluationEnvironment and a given number of arguments.
 *
 * @param <S> type of generated elements.
 */
public class ParametricValue<S> implements Serializable {

    private final String[] arguments;

    private final Function<double[],S> builder;

    /**
     * Create a new ParametricValue that depends on given arguments.
     *
     * @param arguments arguments name.
     * @param builder function used to build the value.
     */
    public ParametricValue(String[] arguments, Function<double[],S> builder) {
        this.arguments = arguments;
        this.builder = builder;
    }


    /**
     * Create a constant ParametricValue that does not depend neither on the EvaluationEnvironment nor on
     * any argument. This constructor is useful when one has to consider constant values in a context where a
     * parametric value is needed.
     *
     * @param value the value to generate.
     */
    public ParametricValue(S value) {
        this(new String[] {}, (args) -> value);
    }


    /**
     * Returns the number of expected parameters.
     *
     * @return the number of expected parameters.
     */
    public int arity() {
        return arguments.length;
    }

    /**
     * Returns the element of type <code>S</code> generated starding from parameters <code>args</code>.
     *
     * @param args actual parameters used to create a new element.
     *
     * @return the element of type <code>S</code> generated starding from parameters <code>args</code>.
     */
    public S build(double ... args) {
        if (args.length != arity()) {
            throw new IllegalArgumentException("Expected "+ arguments.length+" arguments, are "+args.length);
        }
        return builder.apply(args);
    }

}
