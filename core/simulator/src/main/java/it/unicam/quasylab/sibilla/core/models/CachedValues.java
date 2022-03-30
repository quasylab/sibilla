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

import it.unicam.quasylab.sibilla.core.models.EvaluationEnvironment;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

public class CachedValues {

    private EvaluationEnvironment environment;
    private final Map<String,Function<Function<String,Double>,Double>> definitions;
    private Map<String,Double> values;

    public CachedValues(Map<String, Function<Function<String,Double>,Double>> definitions) {
        this.definitions = definitions;
    }

    public CachedValues(Map<String, Function<Function<String,Double>,Double>> definitions, Map<String, Double> values) {
        this(definitions);
        this.values = values;
    }

    public CachedValues() {
        this(new HashMap<>());
    }

    public void setEnvironment(EvaluationEnvironment environment) {
        this.environment = environment;
    }

    public synchronized void reset() {
        this.values = null;
    }

    public synchronized void compute() {
        if (this.values != null) return ;
        this.values = new HashMap<>();
        definitions.forEach((key, value) -> {
            this.values.put(key,value.apply(this::resolve));
        });
    }

    public double resolve(String s) {
        if (environment == null) {
            return this.values.getOrDefault(s, Double.NaN);
        } else {
            return this.values.getOrDefault(s,environment.get(s));
        }
    }

    public synchronized double get(String s) {
        compute();
        return this.values.getOrDefault(s, Double.NaN);
    }

    public synchronized void register(String name, Function<Function<String,Double>,Double> def) {
        this.definitions.put(name, def);
    }

    public boolean isDefined(String name) {
        return this.definitions.containsKey(name);
    }
}
