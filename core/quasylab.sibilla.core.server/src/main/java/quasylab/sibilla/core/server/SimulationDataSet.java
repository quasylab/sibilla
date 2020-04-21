/*
 * Sibilla:  a Java framework designed to support analysis of Collective
 * Adaptive Systems.
 *
 *  Copyright (C) 2020.
 *
 *  See the NOTICE file distributed with this work for additional information
 *  regarding copyright ownership.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *            http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 *  or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 *
 */

package quasylab.sibilla.core.server;

import org.apache.commons.math3.random.RandomGenerator;
import quasylab.sibilla.core.simulator.Model;
import quasylab.sibilla.core.simulator.pm.State;
import quasylab.sibilla.core.simulator.sampling.SamplingFunction;

import java.io.Serializable;

public class SimulationDataSet<S extends State> implements Serializable {

    private static final long serialVersionUID = 1L;
    private final RandomGenerator randomGenerator;
    private final String modelName;
    private final Model<S> model;
    private final S modelInitialState;
    private final SamplingFunction<S> modelSamplingFunction;
    private final int replica;
    private final double deadline;

    public SimulationDataSet(RandomGenerator random, String modelName, Model<S> model, S initialState,
                             SamplingFunction<S> sampling_function, int replica, double deadline) {
        this.modelName = modelName;
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
        result = prime * result + ((modelName == null) ? 0 : modelName.hashCode());
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
        if (modelName == null) {
            if (other.modelName != null)
                return false;
        } else if (!modelName.equals(other.modelName))
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

    public RandomGenerator getRandomGenerator() {
        return randomGenerator;
    }


    public String getModelName() {
        return modelName;
    }


    public Model<S> getModel() {
        return model;
    }


    public S getModelInitialState() {
        return modelInitialState;
    }


    public SamplingFunction<S> getModelSamplingFunction() {
        return modelSamplingFunction;
    }


    public int getReplica() {
        return replica;
    }


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
                randomGenerator.hashCode(), randomGenerator.getClass().getName(), modelName,
                model.hashCode(), model.getClass().getName(), modelInitialState.hashCode(),
                modelInitialState.getClass().getName(), modelSamplingFunction.hashCode(),
                modelSamplingFunction.getClass().getName(), replica, deadline);
    }
}
