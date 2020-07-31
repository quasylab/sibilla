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
import quasylab.sibilla.core.past.State;
import quasylab.sibilla.core.simulator.sampling.Sample;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.List;

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

    int stateByteArraySize();

    byte[] serializeState(S state) throws IOException;

    S deserializeState(byte[] bytes) throws IOException;

    S deserializeState(ByteArrayInputStream toDeserializeFrom) throws IOException;
}
