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

import it.unicam.quasylab.sibilla.core.models.IndexedState;
import it.unicam.quasylab.sibilla.core.tools.ProbabilityMatrix;
import it.unicam.quasylab.sibilla.core.tools.ProbabilityVector;
import org.apache.commons.math3.random.RandomGenerator;

import java.util.Arrays;
import java.util.Objects;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.IntStream;

/**
 *
 */
public class LIOCountingState implements LIOState {

    private final LIOAgentDefinitions definition;
    private final int size;
    private final int[] occupancy;

    /**
     * Create a new state with the given occupancy.
     *
     * @param definition
     * @param occupancy  occupancy created state.
     */
    public LIOCountingState(LIOAgentDefinitions definition, int[] occupancy) {
        this(definition, occupancy, IntStream.of(occupancy).sum());
    }

    private LIOCountingState(LIOAgentDefinitions definition, int[] occupancy, int size) {
        this.definition = definition;
        this.occupancy = Arrays.copyOf(occupancy,occupancy.length);
        this.size = size;
    }

    public LIOCountingState(LIOAgentDefinitions definition) {
        this(definition, new int[definition.numberOfAgents()], 0);
    }


    public LIOCountingState add(LIOAgent a) {
        int[] newOccupancy = Arrays.copyOf(occupancy, occupancy.length);
        newOccupancy[a.getIndex()]++;
        int newSize = size+1;
        return new LIOCountingState(definition, newOccupancy, newSize);
    }

    public double size() {
        return size;
    }

    @Override
    public double fractionOf(LIOAgent a) {
        return numberOf(a)/size();
    }

    @Override
    public double fractionOf(Predicate<LIOAgent> predicate) {
        return numberOf(predicate)/size();
    }

    @Override
    public Set<LIOAgent> getAgents() {
        return definition.getAgents(IntStream.range(0, occupancy.length).filter(i -> occupancy[i]>0).toArray());
    }


    public double numberOf(LIOAgent agent) {
        return occupancy[agent.getIndex()];
    }

    public double numberOf(Predicate<LIOAgent> predicate) {
        return IntStream.range(0, occupancy.length).filter(i -> predicate.test(definition.getAgent(i))).sum();
    }

    @Override
    public LIOCountingState step(RandomGenerator randomGenerator, ProbabilityMatrix<LIOAgent> probabilityMatrix) {
        int[] occupancy = new int[this.occupancy.length];
        IntStream.range(0, occupancy.length).forEach(s ->
            IntStream.range(0, this.occupancy[s]).forEach(i -> {
                LIOAgent nextAgent = probabilityMatrix.sample(randomGenerator, definition.getAgent(s));
                occupancy[nextAgent.getIndex()]++;
            })
        );
        return new LIOCountingState(definition, occupancy);
    }

    @Override
    public ProbabilityVector<LIOCountingState> next(ProbabilityMatrix<LIOAgent> matrix) {
        ProbabilityVector<LIOCountingState> current = new ProbabilityVector<>();
        current.add(new LIOCountingState(definition), 1.0);
        for(int i = 0; i<occupancy.length; i++) {
            LIOAgent a = definition.getAgent(i);
            for(int k=0; k<occupancy[i]; k++) {
                current = current.apply(LIOCountingState::add, matrix.getRowOf(a));
            }
        }
        return current;
    }

    @Override
    public ProbabilityVector<LIOCountingState> next() {
            return next(getAgentsDefinition().getAgentProbabilityMatrix(this));
    }


    @Override
    public LIOAgentDefinitions getAgentsDefinition() {
        return definition;
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

    /**
     * Returns a copy of this state where the given agent has been removed.
     * @param a the agent to remove.
     * @return a copy of this state where the given agent has been removed.
     */
    public LIOCountingState remove(LIOAgent a) {
        if (this.occupancy[a.getIndex()]==0) {
            return this;
        } else {
            int[] newOccupancy = Arrays.copyOf(this.occupancy, this.occupancy.length);
            return new LIOCountingState(definition, occupancy, size);
        }
    }

    public void add(LIOAgent key, Integer value) {
    }

}
