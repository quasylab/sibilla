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

package it.unicam.quasylab.sibilla.core.models.slam;

import java.util.Map;
import java.util.TreeMap;
import java.util.function.Function;

/**
 * An agent definition is a container for a set of agent prototypes.
 */
public class AgentDefinition {

    private final Map<String, AgentPrototype> agentPrototypes;

    /**
     * Creates an empty agent definition.
     */
    public AgentDefinition() {
        this.agentPrototypes = new TreeMap<>();
    }

    /**
     * Returns the array containing the names of defined agents.
     * @return the array containing the names of defined agents.
     */
    public String[] getAgents() {
        return agentPrototypes.keySet().toArray(new String[0]);
    }

    /**
     * Returns the factory used to build the agent with the given name according to the given values.
     *
     * @param name the name of agent to build.
     * @param values
     * @return the factory used to build the agent with the given name according to the given values.
     */
    public AgentFactory getAgentFactory(String name, SlamValue[] values) {
        AgentPrototype agentPrototype = agentPrototypes.get(name);
        if (agentPrototype != null) {
            return agentPrototype.getAgentFactory(values);
        }
        return null;
    }

    /**
     * Adds new agent to this definition.
     *
     * @param name                  agent name.
     * @param parameters            array of prototype parameters.
     */
    public void addAgent(String name,
            SlamType[] parameters) {
        agentPrototypes.put(
            name,
            new AgentPrototype(name, agentPrototypes.size(), parameters)
        );
    }

    /**
     * Returns true if this definition contains an agent with the given name.
     *
     * @param agentName agent name.
     * @return true if this definition contains an agent with the given name.
     */
    public boolean contains(String agentName) {
        return agentPrototypes.containsKey(agentName);
    }

    /**
     * Returns the prototype associated with the given name.
     *
     * @param agentName agent name.
     * @return the prototype associated with the given name.
     */
    public AgentPrototype getPrototype(String agentName) {
        return agentPrototypes.get(agentName);
    }

    public void setStateProvider(String agentName, Function<SlamValue[],AgentStore> storeFunction) {
        AgentPrototype prototype = agentPrototypes.get(agentName);
        if (prototype != null) {
            prototype.setStoreProvider(storeFunction);
        } else {
            throw new IllegalArgumentException();//TODO: Add message here!
        }
    }

    public void setAgentBehaviour(String agentName, AgentBehaviour agentBehaviour) {
        AgentPrototype prototype = agentPrototypes.get(agentName);
        if (prototype != null) {
            prototype.setAgentBehaviour(agentBehaviour);
        } else {
            throw new IllegalArgumentException();//TODO: Add message here!
        }
    }

    public void setPerceptionFunction(String agentName, PerceptionFunction perceptionFunction) {
        AgentPrototype prototype = agentPrototypes.get(agentName);
        if (prototype != null) {
            prototype.setPerceptionFunction(perceptionFunction);
        } else {
            throw new IllegalArgumentException();//TODO: Add message here!
        }
    }

    public void setAgentTimePassingFunction(String agentName, AgentTimePassingFunction timePassingFunction) {
        AgentPrototype prototype = agentPrototypes.get(agentName);
        if (prototype != null) {
            prototype.setTimePassingFunctionProvider(timePassingFunction);
        } else {
            throw new IllegalArgumentException();//TODO: Add message here!
        }
    }
}
