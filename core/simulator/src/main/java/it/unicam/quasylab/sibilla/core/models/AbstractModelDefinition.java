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

/**
 * This class provides basic features for handling a ModelDefinition.
 *
 * @param <T> type of states handled in the generated model.
 */
public abstract class AbstractModelDefinition<T extends State> implements ModelDefinition<T> {

    private final EvaluationEnvironment environment;

    /**
     * Creates a definition with an empty EvaluationEnvironment and a default state builder.
     */
    public AbstractModelDefinition() {
        this(new EvaluationEnvironment());
    }

    /**
     * Creates a definition with a given EvaluationEnvironment.
     */
    public AbstractModelDefinition(EvaluationEnvironment parameters) {
        this.environment = parameters;
    }

    @Override
    public String[] getModelParameters() {
        return environment.getParameters();
    }

    /**
     * Register a new parameter to the model.
     *
     * @param name parameter to register.
     * @param value parameter defaul value.
     */
    public void registerParameter(String name, double value) {
        this.environment.register(name,value);
    }

    @Override
    public void setParameter(String name, double value) {
        environment.set(name,value);
    }

    /**
     * Clear the all the data that have been computed in a ModelDefinition.
     */
    protected abstract void clearCache();

    /**
     * Reset the EvaluationEnvironment of the model to its default values.
     */
    public synchronized void reset() {
        environment.reset();
    }

    /**
     * Reset the parameter name to its default value.
     *
     * @param name the name of parameter to reset.
     */
    public synchronized void reset(String name) {
        environment.reset(name);
    }

    /**
     * Return the value associated with the given parameter.
     *
     * @param name name of parameter.
     * @return the value associated with the given parameter.
     */
    public double getValue(String name) {
        return environment.get(name);
    }

    /**
     * Return the default value associated with the given parameter.
     *
     * @param name name of parameter.
     * @return the default value associated with the given parameter.
     */
    public double getDefaultValue(String name) {
        return environment.getDefault(name);
    }

    /**
     * Return the used EvaluationEnvironment.
     * @return the used EvaluationEnvironment.
     */
    public EvaluationEnvironment getEnvironment() {
        return environment;
    }

    public abstract boolean isAState(String name);
}
