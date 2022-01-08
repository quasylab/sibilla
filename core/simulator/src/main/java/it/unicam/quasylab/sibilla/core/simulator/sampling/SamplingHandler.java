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

package it.unicam.quasylab.sibilla.core.simulator.sampling;

/**
 * A sampling handler is an object that has the responsibility to collect
 * data from a sequence of samplings from a simulation generating elements of type
 * <code>S</code>. Each simulation step is recorded by calling method
 * {@link SamplingHandler#sample(double, S)}. While the fact that simulation is completed
 * is recorded with the method {@link SamplingHandler#end(double)}.
 *
 * @param <S> the type of object generated in the handled simulation run.
 */
public interface SamplingHandler<S> {

    /**
     * Records that the handled simulation started.
     */
    void start();

    /**
     * Records a simulation step performed ending at time <code>time</code> and
     * leading to state <code>state</code>.
     *
     * @param time simulation time.
     * @param state reached state.
     */
    void sample(double time, S state);

    /**
     * Records that simulation terminated at time <code>time</code>.
     */
    void end(double time);


}
