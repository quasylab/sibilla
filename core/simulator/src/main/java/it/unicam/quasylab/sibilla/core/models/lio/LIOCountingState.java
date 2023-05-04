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

import it.unicam.quasylab.sibilla.core.tools.ProbabilityMatrix;
import it.unicam.quasylab.sibilla.core.tools.ProbabilityVector;
import org.apache.commons.math3.random.RandomGenerator;

import java.util.Arrays;
import java.util.Objects;
import java.util.function.Predicate;
import java.util.stream.IntStream;

/**
 *
 */
public class LIOCountingState implements LIOState<LIOCountingState> {

    private final AgentsDefinition definition;
    private final int size;
    private final int[] occupancy;

    /**
     * Create a new state with the given occupancy.
     *
     * @param definition
     * @param occupancy  occupancy created state.
     */
    public LIOCountingState(AgentsDefinition definition, int[] occupancy) {
        this(definition, occupancy, IntStream.of(occupancy).sum());
    }

    private LIOCountingState(AgentsDefinition definition, int[] occupancy, int size) {
        this.definition = definition;
        this.occupancy = Arrays.copyOf(occupancy,occupancy.length);
        this.size = size;
    }

    public LIOCountingState(AgentsDefinition definition) {
        this(definition, new int[definition.numberOfAgents()], 0);
    }


    public LIOCountingState add(Agent a) {
        int[] newOccupancy = Arrays.copyOf(occupancy, occupancy.length);
        newOccupancy[a.getIndex()]++;
        int newSize = size+1;
        return new LIOCountingState(definition, newOccupancy, newSize);
    }

    @Override
    public int size() {
        return size;
    }


    @Override
    public double numberOf(Agent agent) {
        return occupancy[agent.getIndex()];
    }

    @Override
    public double numberOf(Predicate<Agent> predicate) {
        return IntStream.range(0, occupancy.length).filter(i -> predicate.test(definition.getAgent(i))).sum();
    }

    @Override
    public LIOCountingState step(RandomGenerator randomGenerator, ProbabilityMatrix<Agent> probabilityMatrix) {
        int[] occupancy = new int[this.occupancy.length];
        IntStream.range(0, occupancy.length).forEach(s ->
            IntStream.range(0, this.occupancy[s]).forEach(i -> occupancy[probabilityMatrix.sample(randomGenerator, definition.getAgent(i)).getIndex()]++)
        );
        return new LIOCountingState(definition, occupancy);
    }

    @Override
    public ProbabilityVector<LIOCountingState> next(ProbabilityMatrix<Agent> matrix) {
        ProbabilityVector<LIOCountingState> current = new ProbabilityVector<>();
        current.add(new LIOCountingState(definition), 1.0);
        for(int i = 0; i<occupancy.length; i++) {
            Agent a = definition.getAgent(i);
            for(int k=0; k<occupancy[i]; k++) {
                current = current.apply(LIOCountingState::add, matrix.getRowOf(a));
            }
        }
        return current;
    }

    @Override
    public ProbabilityVector<LIOCountingState> next() {
        return next(definition.getAgentProbabilityMatrix(this));
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        LIOCountingState that = (LIOCountingState) o;
        return size == that.size && Arrays.equals(occupancy, that.occupancy);
    }

    @Override
    public int hashCode() {
        int result = Objects.hash(size);
        result = 31 * result + Arrays.hashCode(occupancy);
        return result;
    }
}
