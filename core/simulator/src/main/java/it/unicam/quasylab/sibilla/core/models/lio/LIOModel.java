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

package it.unicam.quasylab.sibilla.core.models.lio;

import it.unicam.quasylab.sibilla.core.models.DiscreteModel;
import it.unicam.quasylab.sibilla.core.simulator.sampling.Measure;
import org.apache.commons.math3.random.RandomGenerator;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

/**
 * Identifies a model for a Langauge of Interactive Objects.
 *
 * @param <S> data type for model state.
 */
public class LIOModel<S extends LIOState> implements DiscreteModel<S> {

    private final AgentsDefinition definitions;
    private final LIONextStateFunction<S> stepFunction;

    /**
     * Create a model that uses individual representations of agents.
     *
     * @param definitions agent definitions.
     * @return LIO model.
     */
    public static LIOModel<LIOIndividualState> getLIOIndividualModel(AgentsDefinition definitions) {
        return new LIOModel<>(definitions, LIOIndividualState::stepFunction);
    }

    public static LIOModel<LIOCountingState> getLIOCountingModel(AgentsDefinition definitions) {
        return new LIOModel<>(definitions, LIOCountingState::stepFunction);
    }

    /**
     * Create a model with the given definitions and step functions.
     * @param definitions agent definitions.
     * @param stepFunction step funcitons.
     */
    public LIOModel(AgentsDefinition definitions, LIONextStateFunction<S> stepFunction) {
        this.definitions = definitions;
        this.stepFunction = stepFunction;
    }

    @Override
    public int stateByteArraySize() {
        return 0;
    }

    @Override
    public byte[] serializeState(S state) throws IOException {
        return new byte[0];
    }

    @Override
    public void serializeState(ByteArrayOutputStream toSerializeInto, S state) throws IOException {

    }

    @Override
    public S deserializeState(byte[] bytes) throws IOException {
        return null;
    }

    @Override
    public S deserializeState(ByteArrayInputStream toDeserializeFrom) throws IOException {
        return null;
    }

    @Override
    public String[] measures() {
        return new String[0];
    }

    @Override
    public double measure(String m, S state) {
        return 0;
    }

    @Override
    public Measure<S> getMeasure(String m) {
        return null;
    }

    @Override
    public S sampleNextState(RandomGenerator r, double time, S state) {
        return stepFunction.step(r, definitions.getAgentProbabilityMatrix(state),state);
    }
}
