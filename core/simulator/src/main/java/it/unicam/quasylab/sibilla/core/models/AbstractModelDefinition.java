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

import it.unicam.quasylab.sibilla.core.models.pm.PopulationState;
import it.unicam.quasylab.sibilla.core.util.values.SibillaValue;
import org.apache.commons.math3.random.RandomGenerator;

import java.util.Map;
import java.util.TreeMap;
import java.util.function.Function;

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
    public void registerParameter(String name, SibillaValue value) {
        this.environment.register(name, value);
    }

    @Override
    public void setParameter(String name, SibillaValue value) {
        environment.set(name,value);
        clearCache();
    }

    /**
     * Clear the all the data that have been computed in a ModelDefinition.
     */
    protected abstract void clearCache();

    @Override
    public synchronized void reset() {
        environment.reset();
    }

    @Override
    public synchronized void reset(String name) {
        environment.reset(name);
    }

    @Override
    public SibillaValue getParameterValue(String name) {
        return environment.get(name);
    }

    /**
     * Return the default value associated with the given parameter.
     *
     * @param name name of parameter.
     * @return the default value associated with the given parameter.
     */
    public SibillaValue getDefaultValue(String name) {
        return environment.getDefault(name);
    }

    @Override
    public EvaluationEnvironment getEnvironment() {
        return environment;
    }


}
