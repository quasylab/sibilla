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

import it.unicam.quasylab.sibilla.core.util.values.SibillaValue;
import org.apache.commons.math3.random.RandomGenerator;

import java.util.function.Function;

/**
 * This interface implements a factory that can be used to build a model according
 * to some parameters.
 *
 * @param <S>
 */
public interface ModelDefinition<S extends State> {

    /**
     * Reset all the model parameters to their default values.
     */
    void reset();

    /**
     * Resets the parameter having the given name to its default value.
     *
     * @param name the name of parameter to reset.
     */
    void reset(String name);

    /**
     * Return the value associated with the given parameter.
     *
     * @param name name of parameter whose value is returned.
     * @return the value associated with the given parameter.
     */
    SibillaValue getParameterValue(String name);

    /**
     * Returns the {@link EvaluationEnvironment} containing all the parameters in this model.
     *
     * @return the {@link EvaluationEnvironment} containing all the parameters in this model.
     */
    EvaluationEnvironment getEnvironment();

    /**
     * Returns the number of parameters needed to build default initial state.
     *
     * @return the number of parameters needed to build default initial state.
     */
    int defaultConfigurationArity();

    /**
     * Returns the number of parameters needed to build initial configuration having the given name.
     *
     * @param name the name of the configuration whose number of parameters is returned.
     * @return the number of parameters needed to build initial configuration having the given name.
     */
    int configurationArity(String name);

    /**
     * Returns the array containing the names of all the parameters in this model.
     *
     * @return the array containing the names of all the parameters in this model.
     */
    default String[] getModelParameters() {
        return getEnvironment().getParameters();
    }

    /**
     * Sets the value of parameter <code>name</code>. An {@link IllegalArgumentException} is
     * thrown if the parameter is unknowns.
     *
     * @param name  name of parameter to set.
     * @param value value of parameter.
     */
    default void setParameter(String name, SibillaValue value) {
        throw new IllegalArgumentException(String.format("Species %s is unknown!", name));
    }

    /**
     * Returns the array of possible initial states defined in the model.
     *
     * @return the array of possible initial states defined in the model.
     */
    String[] configurations();

    /**
     * Returns the configuration associated with the given name by using the given arguments.
     *
     * @param args arguments to use in state creation.
     * @return the default state associated the given arguments.
     */
    Function<RandomGenerator, S> getConfiguration(String name, double... args);

    /**
     * Create the default state (that is the first one in the array) with
     * the given arguments.
     *
     * @param args arguments to use in state creation.
     * @return the default state associated the given arguments.
     */
    Function<RandomGenerator, S> getDefaultConfiguration(double... args);


    /**
     * Returns a string describing the state having the given name.
     *
     * @param name the name of the state whose info are provided
     * @return a string describing the state having the given name.
     */
    String getStateInfo(String name);

    /**
     * Creates a new {@link Model}.
     *
     * @return a model built from a given set of parameters.
     */
    Model<S> createModel();


    /**
     * Returns true if the given name is associated with an initial configuration.
     *
     * @param name the name is tested the association with an initial configuration.
     * @return true if the given name is associated with an initial configuration.
     */
    boolean isAnInitialConfiguration(String name);

}
