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
import org.apache.commons.math3.random.RandomGenerator;

import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.util.stream.IntStream;
import java.util.stream.Stream;

/**
 * Indentify a state where each agent is stored individually.
 */
public class LIOIndividualState implements LIOState, IndexedState<Agent> {

    private final AgentsDefinition definition;
    private final int[] agents;
    private final int[] multiplicity;

    /**
     * Create a new state given an agents definition and an array a agent names.
     *
     * @param definition agents definition.
     * @param agents array of agent names.
     */
    public LIOIndividualState(AgentsDefinition definition, String ... agents) {
        this(definition, Stream.of(agents).mapToInt(definition::getAgentIndex).toArray());
    }

    public LIOIndividualState(AgentsDefinition definition, int ... agents) {
        this.definition = definition;
        this.agents = agents;
        this.multiplicity = new int[definition.numberOfAgents()];
        fillMultiplicity();
    }

    /**
     * Fill the array with agent multiplicity.
     */
    private void fillMultiplicity() {
        IntStream.of(this.agents).forEach(i -> this.multiplicity[i]++);
    }

    /**
     * Create a new state given the total number of species and the array with agents state.
     *
     * @param agents agents state.
     * @param multiplicity agent state multiplicity.
     */
    private LIOIndividualState(AgentsDefinition definition, int[] agents, int[] multiplicity) {
        this.definition = definition;
        this.agents = agents;
        this.multiplicity = multiplicity;
    }

    @Override
    public int size() {
        return agents.length;
    }

    @Override
    public double fractionOf(int stateIndex) {
        return ((double) multiplicity[stateIndex])/size();
    }


    /**
     * Return the agent in position i.
     *
     * @param i agent index.
     * @return the agent in position i.
     */
    public Agent get(int i) {
        return definition.getAgent( agents[i] );
    }

    /**
     * Return the index of agent in position i.
     *
     * @param i agent index.
     * @return the index of agent in position i.
     */
    public int getIndexAt(int i) {
        return agents[i];
    }


    /**
     * Given a random generator and a probability transition matrix sample a computational step of a give state.
     *
     * @param randomGenerator a random generator
     * @param probabilityMatrix a probability transition matrix
     * @param state current state
     * @return next state
     */
    public static LIOIndividualState stepFunction(RandomGenerator randomGenerator, double[][] probabilityMatrix, LIOIndividualState state) {
        int[] agents = new int[state.agents.length];
        int[] multiplicity = new int[state.multiplicity.length];
        for(int i=0 ;i<agents.length;i++) {
//        IntStream.of(0,agents.length).forEach( i -> {
            int self = state.getIndexAt(i);
            int next = LIOState.doSample(randomGenerator,probabilityMatrix[self],self);
            agents[i] = next;
            multiplicity[next] += 1;
        }//);
        return new LIOIndividualState(state.definition,agents,multiplicity);
    }
}
