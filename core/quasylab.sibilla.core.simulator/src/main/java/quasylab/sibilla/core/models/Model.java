/*
 * Sibilla:  a Java framework designed to support analysis of Collective
 * Adaptive Systems.
 *
 * Copyright (C) 2020.
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
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied.
 *
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 *
 */

package quasylab.sibilla.core.models;

import org.apache.commons.math3.random.RandomGenerator;
import quasylab.sibilla.core.models.pm.PopulationState;
import quasylab.sibilla.core.simulator.sampling.Measure;
import quasylab.sibilla.core.simulator.sampling.SamplingCollection;
import quasylab.sibilla.core.simulator.sampling.SamplingFunction;
import quasylab.sibilla.core.simulator.sampling.StatisticSampling;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

/**
 * Represents a <i>Stochastic Process</i>.
 *
 * @param <S> data type for the state of the process.
 */
public interface Model<S extends State> {

    /**
     * Samples possible next state when the process is in a given state at
     * a given time. A random generator is passed to sample random values when
     * needed.
     *
     * @param r     random generator used to sample needed random values.
     * @param time  current time.
     * @param state current state.
     * @return process time step.
     */
    TimeStep<S> next(RandomGenerator r, double time, S state);

    /**
     * Returns the list of actions that are enabled when the process a a given
     * time is in a given state.
     *
     * @param r     random generator used to sample needed random values.
     * @param time  current time.
     * @param state current state.
     * @return list of enabled actions.
     */
    List<Action<S>> actions(RandomGenerator r, double time, S state);

    ModelDefinition<S> getModelDefinition();

    /**
     * Returns the number of bytes needed to store model states.
     *
     * @return the number of bytes needed to store model states.
     */
    int stateByteArraySize();

    /**
     * Returns the array of bytes representing the given state.
     *
     * @param state the state to serialise.
     * @return the array of bytes representing the given state.
     * @throws IOException this exception is thrown if the
     */
    byte[] serializeState(S state) throws IOException;

    S deserializeState(byte[] bytes) throws IOException;

    S deserializeState(ByteArrayInputStream toDeserializeFrom) throws IOException;

    /**
     * Each model is associated with a set of measures. This method returns the array of measure
     * names identified by strings.
     *
     * @return the array of measure names.
     */
    String[] measures();

    /**
     * Compute the measure <code>m</code> on the state <code>state</code>.
     *
     * @param m name of the measure to compute.
     * @param state state to measure.
     * @return the value of measure <code>m</code> on state <code>state</code>.
     */
    double measure(String m, S state);

    /**
     * Returns the measure with name <code>m</code>.
     * @param m measure name.
     * @return the measure with name <code>m</code>.
     */
    Measure<S> getMeasure(String m);


    /**
     * Returns the samplings that can be used to collect simulation data of the given measures.
     *
     * @param samplings number of samplings to collect.
     * @param dt time gap among two samplings.
     * @param measures neames of measures to collect.
     * @return the samplings that can be used to collect simulation data of the given measures.
     */
    default SamplingFunction<S> selectSamplingFunction(int samplings, double dt, String ... measures) {
        if (measures.length == 0)  return null;
        if (measures.length == 1) {
            Measure<S> m = getMeasure(measures[0]);
            if (m != null) return new StatisticSampling<>(samplings,dt,m);
        } else {
            SamplingCollection<S> collection = new SamplingCollection<>();
            Arrays.stream(measures).map(this::getMeasure).filter(Objects::nonNull)
                    .sequential().forEach(m -> collection.add(new StatisticSampling<S>(samplings,dt,m)));
            return collection;
        }
        return null;
    }

    default SamplingFunction<S> getSamplingFunction(int samplings, double dt) {
        return selectSamplingFunction(samplings,dt,measures());
    }

}
