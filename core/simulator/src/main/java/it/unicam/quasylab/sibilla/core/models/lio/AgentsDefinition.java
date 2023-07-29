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
import it.unicam.quasylab.sibilla.core.util.values.SibillaValue;

import java.util.*;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * This class contains the definitions of a set of Interactive Objects.
 */
public final class AgentsDefinition {


    private final ArrayList<Agent> agents;

    private final ArrayList<AgentAction> actions;

    private final ArrayList<ActionProbabilityFunction> probabilityFunctions;

    private final Map<AgentName,Agent> agentRegistry;

    private final Map<String,AgentAction> actionRegistry;

    private final Map<String, Integer> agentArity;


    /**
     * Create an empty definition.
     */
    public AgentsDefinition() {
        this.agents = new ArrayList<>();
        this.actions = new ArrayList<>();
        this.probabilityFunctions = new ArrayList<>();
        this.agentRegistry = new HashMap<>();
        this.actionRegistry = new HashMap<>();
        this.agentArity = new HashMap<>();
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
        double[] actionProbabilities = probabilityFunctions.stream().mapToDouble(f -> f.getProbability(state)).toArray();
        return a -> actionProbabilities[a.getIndex()];
    }

    /**
     * Returns the number of defined agents.
     *
     * @return the number of defined agents.
     */
    public int numberOfAgents() {
        return agents.size();
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
        return agents.get(i);
    }

    /**
     * Creates, if not exists, an agent with the given name and indexes and returns its reference.
     *
     * @param name name of the agent to create
     * @param indexes indexes of the agent to create
     * @return a reference to the created agent.
     */
    public Agent addAgent(String name, SibillaValue ... indexes) {
        return addAgent(new AgentName(name, indexes));
    }

    /**
     * Creates a new agent with the given name. This method returns true if the agent has been successfully created
     * and false if another agent with the same name already exists.
     *
     * @param agentName name of the agent to create
     * @return true if the agent has been successfully created and false if another agent with the same name
     * already exists.
     */
    public Agent addAgent(AgentName agentName) {
        if (this.agentRegistry.containsKey(agentName)) {
            return this.agentRegistry.get(agentName);
        } else {
            if ((agentArity.containsKey(agentName.getName()))&&(agentArity.get(agentName.getName()).intValue()!=agentName.numberOfIndexes())) {
                throw new IllegalArgumentException(
                        String.format("Inconsistent number of indexes for agent %s! Expected %d are %d!",
                                agentName.getName(),
                                agentArity.get(agentName.getName()),
                                agentName.numberOfIndexes()
                        )
                );
            }
            Agent newAgent = new Agent(agentName, this.agents.size());
            agents.add(newAgent);
            agentRegistry.put(agentName, newAgent);
            return newAgent;
        }
    }

    /**
     * Returns the number indexes used in agents with the given name, -1 is returned if no agent with the given name
     * is present.
     *
     * @param name agent name
     * @return the number indexes used in agents with the given name, -1 is returned if no agent with the given name
     * is present.
     */
    public int getAgentArity(String name) {
        return agentArity.getOrDefault(name, -1);
    }

    /**
     * Returns the agent with the given name.
     *
     * @param name the agent name.
     * @return the agent with the given name.
     */
    public Agent getAgent(AgentName name) {
        return agentRegistry.get(name);
    }

    /**
     * Returns the index of agent with the given name.
     *
     * @param name agent name.
     * @return the index of agent with the given name.
     */
    public int getAgentIndex(AgentName name) {
        Agent a = agentRegistry.get(name);
        if (a == null) {
            return -1;
        } else {
            return a.getIndex();
        }
    }

    /**
     * Returns the index of agent with the given name and indexes.
     *
     * @param name agent name.
     * @param indexes agent indexes
     * @return the index of agent with the given name and indexes.
     */
    public int getAgentIndex(String name, SibillaValue ... indexes) {
        return getAgentIndex(new AgentName(name, indexes));
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
     * Adds a new action with the given name and returns the instance of the new created instance. If an action with
     * the same name already exists, the old instance is returned.
     *
     * @param name name of the new created action
     * @return the instance of the new created instance. If an action with the same name already exists,
     * the old instance is returned.
     */
    public AgentAction addAction(String name) {
        return addAction(name, s -> 0.0);
    }

    /**
     * Adds a new action with the given name and sets the probability function. Returns the instance of the new created
     * instance or, if an action with the same name already exists, the old instance is returned.
     *
     * @param name name of the new created action
     * @param function probability function of the action
     * @return the instance of the new created instance or, if an action with the same name already exists,
     * the old instance is returned.
     */
    public AgentAction addAction(String name, ActionProbabilityFunction function) {
        if (actionRegistry.containsKey(name)) {
            return actionRegistry.get(name);
        } else {
            AgentAction newAction = new AgentAction(name, this.actions.size());
            this.actionRegistry.put(name, newAction);
            this.actions.add(newAction);
            this.probabilityFunctions.add(function);
            return newAction;
        }
    }

    /**
     * Sets the probability function associated with the action with the given name.
     *
     * @param name action name.
     * @param function probability function.
     * @return the reference to the changed action.
     */
    public boolean setActionProbability(String name, ActionProbabilityFunction function) {
        AgentAction action = getAction(name);
        if (action == null) {
            return false;
        } else {
            setActionProbability(action, function);
            return true;
        }
    }

    /**
     * Sets the probability function of the given action.
     * @param action agent action.
     * @param function probability function
     */
    public void setActionProbability(AgentAction action, ActionProbabilityFunction function) {
        probabilityFunctions.set(action.getIndex(), function);
    }

    public LIOPopulationFraction getPopulationFractionOf(LIOState<?> state) {
        return new LIOPopulationFraction(this, agents.stream().mapToDouble(state::fractionOf).toArray());
    }

    public Set<Agent> getAgents(int[] agents) {
        return IntStream.of(agents).mapToObj(this::getAgent).collect(Collectors.toSet());
    }

    /**
     * Returns the agent with the given name and indexes.
     *
     * @param name agent name
     * @param indexes agent index values
     * @return the agent with the given name and indexes
     */
    public Agent getAgent(String name, SibillaValue ... indexes) {
        return getAgent(new AgentName(name, indexes));
    }


    public void addAgentStep(String name, Predicate<SibillaValue[]> guard, AgentAction action, Function<SibillaValue[],Agent> step) {
        this.agentRegistry.entrySet().stream().filter(e -> e.getKey().getName().equals(name)).forEach(e -> {
            e.getValue().addAction(action, step.apply(e.getKey().getIndexes()));
        });
    }

    public Set<Agent> getAgents(Predicate<AgentName> predicate) {
        return this.agentRegistry.entrySet().stream().filter(e -> predicate.test(e.getKey())).map(Map.Entry::getValue).collect(Collectors.toSet());
    }
}
