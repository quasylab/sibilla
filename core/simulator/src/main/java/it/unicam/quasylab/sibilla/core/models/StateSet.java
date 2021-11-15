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

import java.util.Map;
import java.util.TreeMap;

public class StateSet<T extends State> {

    private final Map<String,ParametricValue<T>> index;
    private ParametricValue<T> defaultState;

    public StateSet() {
        this.index = new TreeMap<>();
        this.defaultState = null;
    }

    public StateSet(ParametricValue<T> defaultState) {
        this();
        this.defaultState = defaultState;
    }

    public StateSet(ParametricValue<T> defaultState, Map<String,ParametricValue<T>> states) {
        this(defaultState);
        this.index.putAll(states);
    }

    public String[] states() {
        return index.keySet().toArray(new String[0]);
    }

    public int arity() {
        return defaultState.arity();
    }

    public int arity(String name) {
        ParametricValue<T> state =  index.get(name);
        if (state != null) {
            return state.arity();
        }
        return -1;
    }

    public synchronized T get(double[] args) {
        if (defaultState != null) {
            return defaultState.build(args);
        }
        return null;
    }

    public synchronized T state(String name, double ... arguments) {
        ParametricValue<T> builder = index.get(name);
        if (builder != null) {
            return builder.build(arguments);
        }
        return null;
    }

    public void setDefaultState(ParametricValue<T> defaultState) {
        this.defaultState = defaultState;
    }

    public void set(String name, ParametricValue<T> stateBuilder) {
        this.index.put(name, stateBuilder);
    }

    public static <T extends State> StateSet<T> newStateSet(T state) {
        return new StateSet<>(new ParametricValue<>(state));
    }

    public boolean isDefined(String name) {
        return index.containsKey(name);
    }

    public String getInfo(String name) {
        if (index.containsKey(name)) {
            return String.format("%s%s",name,index.get(name).getInfo());
        }
        return String.format("State %s is unknown",name);
    }


}
