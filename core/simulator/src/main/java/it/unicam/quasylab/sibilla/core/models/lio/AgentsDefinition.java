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

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

/**
 * This class contains the definitions of a set of Interactvie Objects.
 */
public final class AgentsDefinition {


    private final Agent[] agents;

    private final AgentAction[] actions;

    private final ActionProbabilityFunction[] probabilityFunctions;

    private final Map<String,Agent> agentRegistry;

    private final Map<String,AgentAction> actionRegistry;


    /**
     * Create an empty agents definition.
     */
    public AgentsDefinition(String[] agentNames, String[] agentActions) {
        this.agents = IntStream.range(0, agentNames.length).mapToObj(i -> new Agent(agentNames[i], i)).toArray(Agent[]::new);
        this.actions = IntStream.range(0, agentActions.length).mapToObj(i -> new AgentAction(agentActions[i], i)).toArray(AgentAction[]::new);
        this.agentRegistry = Stream.of(agents).collect(Collectors.toMap(Agent::getName, a -> a));
        this.actionRegistry = Stream.of(actions).collect(Collectors.toMap(AgentAction::getName, a -> a));
        this.probabilityFunctions = new ActionProbabilityFunction[agentActions.length];
    }


    /**
     * Return the action with the given name or null if no action with that name is available.
     *
     * @param name action name.
     * @return the action with the given name.
     */
    public AgentAction getAction(String name) {
        return actionRegistry.get(name);
    }

    /**
     * Return the function associating each action with a probability value computed according to the given state.
     *
     * @param state the state used to compute action probabilities.
     * @return the function associating each action with a probability value.
     */
    public <S extends LIOCollective> ActionsProbability getActionProbability(S state) {
        double[] actionProbabilities = Stream.of(probabilityFunctions).mapToDouble(f -> f.getProbability(state)).toArray();
        return a -> actionProbabilities[a.getIndex()];
    }

    /**
     * Returns the number of defined agents.
     *
     * @return the number of defined agents.
     */
    public int numberOfAgents() {
        return agents.length;
    }

    /**
     * Compute the agents probability matrix associated with the given state.
     *
     * @param state a state.
     * @return the agents probability matrix associated with the given state.
     */
    public <S extends LIOCollective> ProbabilityMatrix<Agent> getAgentProbabilityMatrix(S state) {
        ActionsProbability actionsProbability = getActionProbability(state);
        return new ProbabilityMatrix<>(a -> a.probabilityVector(actionsProbability));
    }

    /**
     * Return the agent with the given index.
     *
     * @param i agent index.
     * @return the agent with the given index.
     */
    public Agent getAgent(int i) {
        return agents[i];
    }

    public Agent getAgent(String name) {
        return agentRegistry.get(name);
    }

    /**
     * Returns the index of agent named <code>s</code>.
     *
     * @param s agent name.
     * @return the index of agent named <code>s</code>.
     */
    public int getAgentIndex(String s) {
        Agent a = agentRegistry.get(s);
        if (a == null) {
            return -1;
        } else {
            return a.getIndex();
        }
    }

    /**
     * Returns the multiplicity array of the given list of agents.
     *
     * @param agents a list of agents.
     * @return the multiplicity array of the given list of agents.
     */
    public int[] getMultiplicity(List<Agent> agents) {
        return IntStream.range(0, numberOfAgents())
                .parallel()
                .map(i -> (int) agents.stream().mapToInt(Agent::getIndex).filter(j -> i==j).count())
                .toArray();
    }

    /**
     * Sets the probability function associated with the action with the given name.
     *
     * @param name action name.
     * @param function probability function.
     * @return the reference to the changed action.
     */
    public AgentAction setActionProbability(String name, ActionProbabilityFunction function) {
        AgentAction act = getAction(name);
        if (act == null) {
            throw new IllegalArgumentException("Action "+name+" is unknown in this definition!");
        }
        probabilityFunctions[act.getIndex()] = function;
        return act;
    }

    public LIOPopulationFraction getPopulationFractionOf(LIOState<?> state) {
        return new LIOPopulationFraction(this, Stream.of(agents).mapToDouble(state::fractionOf).toArray());
    }

    public Set<Agent> getAgents(int[] agents) {
        return IntStream.of(agents).mapToObj(this::getAgent).collect(Collectors.toSet());
    }
}
