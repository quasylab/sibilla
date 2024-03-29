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

import it.unicam.quasylab.sibilla.core.tools.ProbabilityEntries;
import it.unicam.quasylab.sibilla.core.tools.ProbabilityMatrix;
import it.unicam.quasylab.sibilla.core.tools.ProbabilityVector;
import org.apache.commons.math3.random.RandomGenerator;

import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

/**
 * Indentify a state where each agent is stored individually.
 */
public class LIOIndividualState implements LIOState {

    private final LIOAgentDefinitions definition;
    private final ArrayList<LIOAgent> agents;
    private final int[] multiplicity;

    private final int hashValue;

    /**
     * Creates a new state containing no agent.
     *
     * @param definition agents definition.
     */
    public LIOIndividualState(LIOAgentDefinitions definition) {
        this(definition, new ArrayList<>());
    }

    /**
     * Create a new state given an agents definition and the names of agents in the state.
     *
     * @param definition agents definition.
     * @param agents array of agent names.
     */
    public LIOIndividualState(LIOAgentDefinitions definition, String ... agents) {
        this(definition, Stream.of(agents).map(LIOAgentName::new).toArray(LIOAgentName[]::new));
    }




    /**
     * Create a new state given an agents definition and the names of agents in the state.
     *
     * @param definition agents definition.
     * @param agents array of agent names.
     */
    public LIOIndividualState(LIOAgentDefinitions definition, LIOAgentName... agents) {
        this(definition, Stream.of(agents).map(definition::getAgent).collect(Collectors.toCollection(ArrayList::new)));
    }




    /**
     * Create a new state given an agents definition and the indexes of agents in the state.
     *
     * @param definition agents definition.
     * @param agents array of agent indexes.
     */
    public LIOIndividualState(LIOAgentDefinitions definition, int ... agents) {
        this(definition, IntStream.of(agents).mapToObj(definition::getAgent).collect(Collectors.toCollection(ArrayList::new)));
    }


    /**
     * Create a new state given an agents definition and the indexes of agents in the state.
     *
     * @param definition agents definition.
     * @param agents a list of agents.
     */
    public LIOIndividualState(LIOAgentDefinitions definition, ArrayList<LIOAgent> agents) {
        this(definition, agents, definition.getMultiplicity(agents));
    }


    /**
     * Create a new state given the total number of species and the array with agents state.
     *
     * @param agents agents state.
     * @param multiplicity agent state multiplicity.
     */
    private LIOIndividualState(LIOAgentDefinitions definition, ArrayList<LIOAgent> agents, int[] multiplicity) {
        this(definition, agents, multiplicity, agents.hashCode());
    }

    private LIOIndividualState(LIOAgentDefinitions definition, ArrayList<LIOAgent> agents, int[] multiplicity, int hashCode) {
        this.definition = definition;
        this.agents = agents;
        this.multiplicity = multiplicity;
        this.hashValue = hashCode;

    }

    /**
     * Creates a new state obtained by this one by adding a new state.
     *
     * @param a the agent to add.
     * @return a new state obtained by this one by adding a new state.
     */
    public LIOIndividualState add(LIOAgent a) {
        ArrayList<LIOAgent> newList = new ArrayList<>(agents);
        int[] newMultiplicity = Arrays.copyOf(this.multiplicity, this.multiplicity.length);
        newMultiplicity[a.getIndex()]++;
        newList.add(a);
        return new LIOIndividualState(definition, newList, newMultiplicity);
    }


    @Override
    public double fractionOf(LIOAgent a) {
        return numberOf(a)/numberOfAgents();
    }

    @Override
    public double fractionOf(Predicate<LIOAgent> predicate) {
        return numberOf(predicate)/numberOf(predicate);
    }

    @Override
    public Set<LIOAgent> getAgents() {
        return null;
    }

    public double numberOf(LIOAgent a) {
        return multiplicity[a.getIndex()];
    }


    public double numberOf(Predicate<LIOAgent> predicate) {
        return IntStream.range(0,multiplicity.length).filter(i -> predicate.test(definition.getAgent(i))).mapToDouble(i -> multiplicity[i]).sum();
    }


    /**
     * Return the agent in position i.
     *
     * @param i agent index.
     * @return the agent in position i.
     */
    public LIOAgent get(int i) {
        return agents.get(i);
    }

    @Override
    public int numberOfAgents() {
        return agents.size();
    }

    /**
     * Return the index of agent in position i.
     *
     * @param i agent index.
     * @return the index of agent in position i.
     */
    public int getIndexAt(int i) {
        return get(i).getIndex();
    }


    /**
     * Given a random generator and a probability transition matrix sample a computational step of a give state.
     *
     * @param randomGenerator   a random generator
     * @param probabilityMatrix a probability transition matrix
     * @return next state
     */
    @Override
    public LIOIndividualState step(RandomGenerator randomGenerator, ProbabilityMatrix<LIOAgent> probabilityMatrix) {
        ArrayList<LIOAgent> nextAgents = new ArrayList<>();
        int[] multiplicity = new int[this.multiplicity.length];
        for (LIOAgent a: this.agents) {
            LIOAgent nA = probabilityMatrix.sample(randomGenerator, a);
            multiplicity[nA.getIndex()]++;
            nextAgents.add(nA);
        }
        return new LIOIndividualState(this.definition, nextAgents, multiplicity);
    }

    @Override
    public ProbabilityVector<LIOIndividualState> next(ProbabilityMatrix<LIOAgent> matrix) {
        //ProbabilityVector<LIOIndividualState> current = new ProbabilityVector<>();
        List<ProbabilityEntries<LIOIndividualState>> current = List.of(new ProbabilityEntries<>(new LIOIndividualState(definition), 1.0));
        for (LIOAgent a: agents) {
            List<ProbabilityEntries<LIOIndividualState>> next = new LinkedList<>();
            ProbabilityVector<LIOAgent> v = matrix.getRowOf(a);
            current.forEach(e -> v.iterate((s, p) -> next.add(e.apply(LIOIndividualState::add, s, p))));
            current = next;
        }
        return ProbabilityVector.getProbabilityVector(current);
    }

    @Override
    public ProbabilityVector<LIOIndividualState> next() {
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
        LIOIndividualState that = (LIOIndividualState) o;
        return (this.hashValue == that.hashValue)&&(definition == that.definition) && agents.equals(that.agents);
    }

    @Override
    public int hashCode() {
        return hashValue;
    }

    @Override
    public String toString() {
        return agents.toString();
    }
}
