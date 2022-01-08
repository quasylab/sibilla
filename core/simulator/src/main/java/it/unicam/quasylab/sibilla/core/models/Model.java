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

import it.unicam.quasylab.sibilla.core.simulator.sampling.*;
import org.apache.commons.math3.random.RandomGenerator;

import java.io.IOException;
import java.io.Serializable;
import java.util.*;
import java.util.function.Predicate;

/**
 * Represents a <i>Stochastic Process</i>.
 *
 * @param <S> data type for the state of the process.
 */
public interface Model<S extends State> extends Serializable {

    /**
     * Returns the simulator cursor assocated with the given initial state.
     *
     * @param initialState starting state in the created cursor.
     */
    SimulatorCursor<S> createSimulationCursor(RandomGenerator r, S initialState);

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
     */
    byte[] byteOf(S state) throws IOException;

    /**
     * Build a state from the given array of states.
     *
     * @param bytes bute arrau
     * @return a state drepresented by the given array of states.
     * @throws IOException
     */
    S fromByte(byte[] bytes) throws IOException;

    /**
     * Each model is associated with a set of measures. This method returns the
     * array of measure names identified by strings.
     *
     * @return the array of measure names.
     */
    String[] measures();

    /**
     * Compute the measure <code>m</code> on the state <code>state</code>.
     *
     * @param m     name of the measure to compute.
     * @param state state to measure.
     * @return the value of measure <code>m</code> on state <code>state</code>.
     */
    double measure(String m, S state);

    /**
     * Returns the measure with name <code>m</code>.
     * 
     * @param m measure name.
     * @return the measure with name <code>m</code>.
     */
    Measure<S> getMeasure(String m);


    /**
     * Returns the predicate associated with the given name.
     *
     * @param name predicate name.
     * @return the predicate associated with the given name.
     */
    Predicate<S> getPredicate(String name);

    /**
     * Returns the array containing the names of predicates defined in this model.
     *
     * @return the array containing the names of predicates defined in this model.
     */
    String[] predicates();

    /**
     * Returns the samplings that can be used to collect simulation data of the
     * given measures.
     *
     * @param samplings number of samplings to collect.
     * @param dt        time gap among two samplings.
     * @param measures  neames of measures to collect.
     * @return the samplings that can be used to collect simulation data of the
     *         given measures.
     */
    default SamplingFunction<S> selectSamplingFunction(int samplings, double dt, String... measures) {
        return selectSamplingFunction(false, samplings, dt, measures);
    }

    /**
     * Returns the samplings that can be used to collect simulation data of the
     * given measures.
     *
     * @param summary true if summary statistics statistics is used.
     * @param samplings number of samplings to collect.
     * @param dt        time gap among two samplings.
     * @param measures  neames of measures to collect.
     * @return the samplings that can be used to collect simulation data of the
     *         given measures.
     */
    default SamplingFunction<S> selectSamplingFunction(boolean summary, int samplings, double dt, String... measures) {
        if (measures.length == 0)
            return new SamplingCollection<>();
        if (measures.length == 1) {
            Measure<S> m = getMeasure(measures[0]);
            if (m != null)
                return getSamplingStatistic(summary, samplings, dt, m);
        } else {
            SamplingCollection<S> collection = new SamplingCollection<>();
            Arrays.stream(measures).map(this::getMeasure).filter(Objects::nonNull).sequential()
                    .forEach(m -> collection.add(getSamplingStatistic(summary, samplings, dt, m)));
            return collection;
        }
        return new SamplingCollection<>();
    }

    default SamplingFunction<S> getSamplingStatistic(boolean summary, int samplings, double dt, Measure<S> m) {
        if (summary) {
            return new SummaryStatisticSampling<>(samplings, dt, m);
        } else {
            return new DescriptiveStatisticSampling<>(samplings, dt, m);
        }
    }

    default SamplingFunction<S> getSamplingFunction(int samplings, double dt) {
        return selectSamplingFunction(true, samplings, dt, measures());
    }

    default SamplingFunction<S> getSamplingFunction(boolean summary, int samplings, double dt) {
        return selectSamplingFunction(summary, samplings, dt, measures());
    }

    default SamplingFunction<S> selectSamplingFunction(double deadline, double dt) {
        return selectSamplingFunction(deadline,dt, measures());
    }

    default SamplingFunction<S> selectSamplingFunction(double deadline, double dt, String ... measures) {
        return selectSamplingFunction(true, deadline, dt, measures);
    }

    default SamplingFunction<S> selectSamplingFunction(boolean summary, double deadline, double dt, String ... measures) {
        return selectSamplingFunction(summary, (int) (deadline/dt),dt,measures);
    }

    default Map<String, Double> measuresOf(S state) {
        TreeMap<String, Double> toReturn = new TreeMap<>();
        for (String name : measures()) {
            toReturn.put(name, measure(name, state));
        }
        return toReturn;
    }
}
