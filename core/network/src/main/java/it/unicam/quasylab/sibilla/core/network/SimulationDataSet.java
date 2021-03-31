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

package it.unicam.quasylab.sibilla.core.network;

import it.unicam.quasylab.sibilla.core.models.Model;
import it.unicam.quasylab.sibilla.core.models.State;
import it.unicam.quasylab.sibilla.core.simulator.sampling.SamplingFunction;
import org.apache.commons.math3.random.RandomGenerator;

import java.io.Serializable;

/**
 * Class that stores info about the simulation that is executed by slaves.
 *
 * @param <S> The {@link State} of the simulation model.
 * @author Stelluti Francesco Pio
 * @author Zamponi Marco
 */
public class SimulationDataSet<S extends State> implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * RandomGenerator used by the simulation.
     */
    private final RandomGenerator randomGenerator;


    private final String modelDefinitionClassName;

    /**
     * {@link Model} used in the simulation.
     */
    private final Model<S> model;

    /**
     * Initial state of the model.
     */
    private final S modelInitialState;

    /**
     * {@link SamplingFunction} used to sample the model.
     */
    private final SamplingFunction<S> modelSamplingFunction;

    /**
     * Number of times the simulation is executed.
     */
    private final int replica;

    /**
     * The deadline of the simulation.
     */
    private final double deadline;

    /**
     * Creates a SimulationDataSet object with the parameters given in input.
     *
     * @param random            RandomGenerator used by the simulation
     * @param model             {@link Model} used in the simulation
     * @param initialState      Initial state of the model
     * @param sampling_function {@link SamplingFunction} used to sample the model
     * @param replica           Number of times the simulation is executed
     * @param deadline          The deadline of the simulation
     */
    public SimulationDataSet(RandomGenerator random, String modelDefinitionClassName, Model<S> model, S initialState,
                             SamplingFunction<S> sampling_function, int replica, double deadline) {
        this.modelDefinitionClassName = modelDefinitionClassName;
        this.randomGenerator = random;
        this.model = model;
        this.modelInitialState = initialState;
        this.modelSamplingFunction = sampling_function;
        this.replica = replica;
        this.deadline = deadline;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        long temp;
        temp = Double.doubleToLongBits(deadline);
        result = prime * result + (int) (temp ^ (temp >>> 32));
        result = prime * result + ((model == null) ? 0 : model.hashCode());
        result = prime * result + ((modelInitialState == null) ? 0 : modelInitialState.hashCode());
        result = prime * result + ((modelDefinitionClassName == null) ? 0 : modelDefinitionClassName.hashCode());
        result = prime * result
                + ((modelSamplingFunction == null) ? 0 : modelSamplingFunction.hashCode());
        result = prime * result + ((randomGenerator == null) ? 0 : randomGenerator.hashCode());
        result = prime * result + replica;
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        SimulationDataSet<?> other = (SimulationDataSet<?>) obj;
        if (Double.doubleToLongBits(deadline) != Double.doubleToLongBits(other.deadline))
            return false;
        if (model == null) {
            if (other.model != null)
                return false;
        } else if (!model.equals(other.model))
            return false;
        if (modelInitialState == null) {
            if (other.modelInitialState != null)
                return false;
        } else if (!modelInitialState.equals(other.modelInitialState))
            return false;
        if (modelDefinitionClassName == null) {
            if (other.modelDefinitionClassName != null)
                return false;
        } else if (!modelDefinitionClassName.equals(other.modelDefinitionClassName))
            return false;
        if (modelSamplingFunction == null) {
            if (other.modelSamplingFunction != null)
                return false;
        } else if (!modelSamplingFunction.equals(other.modelSamplingFunction))
            return false;
        if (randomGenerator == null) {
            if (other.randomGenerator != null)
                return false;
        } else if (!randomGenerator.equals(other.randomGenerator))
            return false;
        return replica == other.replica;
    }

    /**
     * Returns the RandomGenerator used in the simulation.
     *
     * @return RandomGenerator used in the simulation
     */
    public RandomGenerator getRandomGenerator() {
        return randomGenerator;
    }


    public String getModelDefinitionClassName() {
        return modelDefinitionClassName;
    }

    /**
     * {@link Model} used in the simulation.
     *
     * @return Model used in the simulation
     */
    public Model<S> getModel() {
        return model;
    }

    /**
     * Returns the initial state of the model.
     *
     * @return initial state of the model
     */
    public S getModelInitialState() {
        return modelInitialState;
    }

    /**
     * Returns the {@link SamplingFunction} used to sample the model.
     *
     * @return SamplingFunction used to sample the model
     */
    public SamplingFunction<S> getModelSamplingFunction() {
        return modelSamplingFunction;
    }

    /**
     * Return the number of times the simulation is executed.
     *
     * @return number of times the simulation is executed
     */
    public int getReplica() {
        return replica;
    }

    /**
     * Returns the deadline of the simulation.
     *
     * @return deadline of the simulation
     */
    public double getDeadline() {
        return deadline;
    }


    public String toString() {
        return String.format(
                "randomGenerator hashcode: %d \n" + " randomGenerator class: %s \n" + " modelReferenceName: %s \n"
                        + " modelReference hashcode: %d \n" + " modelReference class: %s \n"
                        + " modelReferenceInitialState hashcode: %d \n" + " modelReferenceInitialState class: %s \n"
                        + " modelReferenceSamplingFunction hashcode: %d \n"
                        + " modelReferenceSamplingFunction class: %s \n" + " replica: %d \n deadline: %e \n",
                randomGenerator.hashCode(), randomGenerator.getClass().getName(), modelDefinitionClassName,
                model.hashCode(), model.getClass().getName(), modelInitialState.hashCode(),
                modelInitialState.getClass().getName(), modelSamplingFunction.hashCode(),
                modelSamplingFunction.getClass().getName(), replica, deadline);
    }
}
