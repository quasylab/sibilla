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
import it.unicam.quasylab.sibilla.core.models.DiscreteTimeModel;
import it.unicam.quasylab.sibilla.core.models.TimeStep;
import it.unicam.quasylab.sibilla.core.simulator.DefaultSimulationCursor;
import it.unicam.quasylab.sibilla.core.simulator.DiscreteTimeSimulationStepFunction;
import it.unicam.quasylab.sibilla.core.simulator.SimulatorCursor;
import it.unicam.quasylab.sibilla.core.simulator.sampling.Measure;
import org.apache.commons.math3.random.RandomGenerator;

import java.io.IOException;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Predicate;

/**
 * Identifies a model for a Langauge of Interactive Objects.
 *
 */
public class LIOModel extends AbstractModel<LIOState> implements DiscreteTimeModel<LIOState> {

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
    public LIOModel(AgentsDefinition definitions, Map<String, Measure<? super LIOState>> measures, Map<String, Predicate<? super LIOState>> predicates) {
        super(measures, predicates);
        this.definitions = definitions;
    }

    @Override
    public int stateByteArraySize() {
        return 0;
    }

    @Override
    public byte[] byteOf(LIOState state) throws IOException {
        return new byte[0];
    }

    @Override
    public LIOState fromByte(byte[] bytes) throws IOException {
        return null;
    }

    @Override
    public LIOState sampleNextState(RandomGenerator r, double time, LIOState state) {
        return state.step(r, definitions.getAgentProbabilityMatrix(state));
    }

    public SimulatorCursor<LIOIndividualState> createIndividualSimulationCursor(RandomGenerator randomGenerator, Function<RandomGenerator, LIOIndividualState> indivistateBuilder) {
        return new DefaultSimulationCursor<>(randomGenerator, this.nextIndividuals(), indivistateBuilder);
    }

    public DiscreteTimeSimulationStepFunction<LIOIndividualState> nextIndividuals() {
        return (rg, state) -> state.step(rg, definitions.getAgentProbabilityMatrix(state));
    }

    public DiscreteTimeSimulationStepFunction<LIOMixedState> nextMixed() {
        return (rg, state) -> state.step(rg, definitions.getAgentProbabilityMatrix(state));
    }

    public DiscreteTimeSimulationStepFunction<LIOCountingState> nextCounting() {
        return (rg, state) -> state.step(rg, definitions.getAgentProbabilityMatrix(state));
    }

    public DiscreteTimeSimulationStepFunction<LIOMeanFieldState> nextMeanField() {
        return (rg, state) -> state.step(rg, definitions.getAgentProbabilityMatrix(state));
    }

}
