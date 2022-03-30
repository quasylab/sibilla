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

import java.util.ArrayList;
import java.util.Map;
import java.util.TreeMap;
import java.util.function.Function;
import java.util.stream.IntStream;

/**
 * This class contains the definitions of a set of Interactvie Objects.
 */
public final class AgentsDefinition {

    private int agentCounter = 0;
    private int actionCounter = 0;
    private final ArrayList<Agent> agentIndex;
    private final Map<String,Agent> agents;
    private final Map<String,AgentAction> actions;
    private final ArrayList<Function<LIOState,Double>> probabilityFunctions;


    /**
     * Create an empty agents definition.
     */
    public AgentsDefinition() {
        this.agents = new TreeMap<>();
        this.agentIndex = new ArrayList<>();
        this.actions = new TreeMap<>();
        this.probabilityFunctions = new ArrayList<>();
    }

    /**
     * Add a new agent.
     *
     * @param name agent name
     * @return the created agent.
     */
    public Agent addAgent(String name) {
        if (agents.containsKey(name)) {
            throw new IllegalStateException("Duplicated agent name "+name);
        }
        Agent agent = new Agent(name,agentCounter++);
        agents.put(name, agent);
        agentIndex.add(agent.getIndex(),agent);
        return agent;
    }

    /**
     * Return the agent with the given name. The method returns null if no agent with the
     * given name is defined.
     *
     * @param name an agent name.
     * @return the agent with the given name.
     */
    public Agent getAgent(String name) {
        return agents.get(name);
    }


    /**
     * Add a new action with the given name and probability function.
     *
     * @param name action name.
     * @param probabilityFunction probability function.
     * @return the new created action.
     */
    public AgentAction addAction(String name, Function<LIOState,Double> probabilityFunction) {
        if (actions.containsKey(name)) {
            throw new IllegalStateException("Dupicated action name "+name);
        }
        AgentAction action = new AgentAction(name,actionCounter++);
        actions.put(name,action);
        probabilityFunctions.add(probabilityFunction);
        return action;
    }

    /**
     * Return the action with the given name or null if no action with that name is available.
     *
     * @param name action name.
     * @return the action with the given name.
     */
    public AgentAction getAction(String name) {
        return actions.get(name);
    }

    /**
     * Return the function associating each action with a probability value computed according to the given state.
     *
     * @param state the state used to compute action probabilities.
     * @return the function associating each action with a probability value.
     */
    public ActionsProbability getActionProbability(LIOState state) {
        Double[] probs = probabilityFunctions.stream().map(f -> f.apply(state)).toArray(Double[]::new);
        return a -> probs[a.getIndex()];
    }

    /**
     * Returns the number of defined agents.
     *
     * @return the number of defined agents.
     */
    public int numberOfAgents() {
        return agentCounter;
    }

    /**
     * Compute the agents probability matrix associated with the given state.
     *
     * @param state a state.
     * @return the agents probability matrix associated with the given state.
     */
    public double[][] getAgentProbabilityMatrix(LIOState state) {
        ActionsProbability actionsProbability = getActionProbability(state);
        double[][] matrix = new double[agentCounter][agentCounter];
        IntStream.range(0,agentCounter).forEach(i ->
            getAgent(i).next(actionsProbability).forEach(p -> matrix[i][p.getValue().getIndex()] += p.getKey())
        );
        return matrix;
    }

    /**
     * Return the agent with the given index.
     *
     * @param i agent index.
     * @return the agent with the given index.
     */
    public Agent getAgent(int i) {
        return agentIndex.get(i);
    }

    /**
     * Returns the index of agent named <code>s</code>.
     *
     * @param s agent name.
     * @return the index of agent named <code>s</code>.
     */
    public int getAgentIndex(String s) {
        Agent a = getAgent(s);
        if (a == null) {
            return -1;
        } else {
            return a.getIndex();
        }
    }
}
