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

import it.unicam.quasylab.sibilla.core.models.pm.StateBuilder;

import java.util.TreeMap;

public abstract class AbstractModelDefinition<T extends State> implements ModelDefinition<T> {

    private TreeMap<String,Double> parameters;
    private TreeMap<String, StateBuilder<T>> states;
    private StateBuilder<T> defaultStateBuilder;

    public AbstractModelDefinition() {
        this.parameters = new TreeMap<>();
        this.states = new TreeMap<>();
    }

    @Override
    public String[] getModelParameters() {
        return parameters.keySet().toArray(new String[0]);
    }

    @Override
    public void setParameter(String name, double value) {
        reset();
        parameters.put(name,value);
    }

    protected abstract void reset();

    public double getParameter(String name) {
        return parameters.getOrDefault(name,0.0);
    }

    public void setDefaultStateBuilder(StateBuilder<T> defaultStateBuilder) {
        this.defaultStateBuilder = defaultStateBuilder;
    }

    public void addStateBuilder(String name, StateBuilder<T> builder) {
        this.states.put(name,builder);
    }

    @Override
    public int stateArity() {
        if (defaultStateBuilder != null) {
            return defaultStateBuilder.arity();
        }
        return -1;
    }

    @Override
    public int stateArity(String name) {
        StateBuilder<T> builder = states.get(name);
        if (builder != null) {
            return builder.arity();
        }
        return -1;
    }

    @Override
    public String[] states() {
        return states.keySet().toArray(new String[0]);
    }

    @Override
    public T state(String name, double... parameters) {
        StateBuilder<T> builder = states.get(name);
        if (builder != null) {
            return builder.build(parameters);
        }
        return null;
    }

    @Override
    public T state(double... parameters) {
        if (defaultStateBuilder != null) {
            return defaultStateBuilder.build(parameters);
        }
        return null;
    }
}
