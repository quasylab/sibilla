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

import it.unicam.quasylab.sibilla.core.models.AbstractModel;
import it.unicam.quasylab.sibilla.core.models.DiscreteModel;
import it.unicam.quasylab.sibilla.core.simulator.sampling.Measure;
import org.apache.commons.math3.random.RandomGenerator;

import java.io.IOException;
import java.util.Map;
import java.util.function.Predicate;

/**
 * Identifies a model for a Langauge of Interactive Objects.
 *
 */
public class LIOModel<S extends LIOState<S>> extends AbstractModel<S> implements DiscreteModel<S> {

    private final AgentsDefinition definitions;

    /**
     * Creates a new model with the given definitions.
     *
     * @param definitions agent definitions.
     */
    public LIOModel(AgentsDefinition definitions) {
        this(definitions, Map.of(), Map.of());
    }



    /**
     * Create a model with the given definitions and step functions.
     *
     * @param definitions agent definitions.
     */
    public LIOModel(AgentsDefinition definitions, Map<String, Measure<? super S>> measures, Map<String, Predicate<? super S>> predicates) {
        super(measures, predicates);
        this.definitions = definitions;
    }

    @Override
    public int stateByteArraySize() {
        return 0;
    }

    @Override
    public byte[] byteOf(S state) throws IOException {
        return new byte[0];
    }

    @Override
    public S fromByte(byte[] bytes) throws IOException {
        return null;
    }

    @Override
    public S sampleNextState(RandomGenerator r, double time, S state) {
        return state.step(r, definitions.getAgentProbabilityMatrix(state));
    }
}
