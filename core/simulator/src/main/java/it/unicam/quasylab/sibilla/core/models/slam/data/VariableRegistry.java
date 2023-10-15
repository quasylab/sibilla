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

package it.unicam.quasylab.sibilla.core.models.slam.data;

import it.unicam.quasylab.sibilla.core.util.values.SibillaValue;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * This class is used to record variables.
 */
public class VariableRegistry {

    private final Map<String, AgentVariable> dictionary;

    /**
     * Creates an empty registry.
     */
    public VariableRegistry() {
        this.dictionary = new HashMap<>();
    }

    /**
     * Returns an {@link Optional} containing the variable with the given name in the registry if it exists
     * in this registry, otherwise returns an empty {@link Optional}.
     *
     * @param name a variable name.
     * @return an {@link Optional} containing the variable with the given name in the registry if it exists
     * in this registry, otherwise returns an empty {@link Optional}.
     */
    public Optional<AgentVariable> getVariable(String name) {
        return Optional.ofNullable(dictionary.get(name));
    }

    /**
     * Returns the variable with the given name stored in this registry. If no variable does exist with this name,
     * a new one is created.
     *
     * @param name a variable name.
     * @return the variable with the given name stored in this registry.
     */
    public AgentVariable record(String name) {
        if (this.dictionary.containsKey(name)) {
            return this.dictionary.get(name);
        } else {
            AgentVariable var = new AgentVariable(name, this.dictionary.size());
            this.dictionary.put(name, var);
            return var;
        }
    }

    public AgentStore getStore(String[] parameters, double[] args) {
        if (parameters.length != args.length) {
            throw new IllegalArgumentException(String.format("Inconsistent number of parameters: expected %d are %d!", parameters.length, args.length));
        }
        AgentStore store = new AgentStore();
        for (int i = 0; i < parameters.length; i++) {
            Optional<AgentVariable> optionalAgentVariable = getVariable(parameters[i]);
            if (optionalAgentVariable.isPresent()) {
                store = store.set(optionalAgentVariable.get(), SibillaValue.of(args[i]));
            } else {
                throw new IllegalArgumentException(String.format("Unknown variable %s", parameters[i]));
            }
        }
        return store;
    }
}
