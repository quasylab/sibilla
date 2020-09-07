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

package quasylab.sibilla.core.models.pm;

import quasylab.sibilla.core.models.State;

import java.util.function.Function;

public class SimpleStateBuilder<S extends State> implements StateBuilder<S> {

    private final int arity;
    private final Function<double[], S> builder;

    public SimpleStateBuilder(Function<double[], S> builder) {
        this(0,builder);
    }

    public SimpleStateBuilder(int arity, Function<double[], S> builder) {
        this.arity = arity;
        this.builder = builder;
    }

    @Override
    public int arity() {
        return arity;
    }

    @Override
    public S build(double... args) {
        return builder.apply(args);
    }
}
