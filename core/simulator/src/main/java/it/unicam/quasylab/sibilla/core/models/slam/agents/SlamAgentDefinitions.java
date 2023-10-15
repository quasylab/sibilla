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

package it.unicam.quasylab.sibilla.core.models.slam.agents;

import it.unicam.quasylab.sibilla.core.models.slam.*;
import it.unicam.quasylab.sibilla.core.models.slam.data.AgentStore;
import it.unicam.quasylab.sibilla.core.models.slam.data.SlamType;
import it.unicam.quasylab.sibilla.core.models.slam.data.SlamValue;
import it.unicam.quasylab.sibilla.core.util.values.SibillaValue;

import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;
import java.util.function.Function;

/**
 * An agent definition is a container for a set of agent prototypes.
 */
public class SlamAgentDefinitions {

    private int agentCounter = 0;

    private final Map<String, AgentName> agentNames = new HashMap<>();

    private final Map<AgentName, SlamAgentPrototype> agentPrototypes = new TreeMap<>();

    /**
     * Creates an empty agent definition.
     */
    public SlamAgentDefinitions() {

    }

    /**
     * Returns the agent name associated with the new created agent.
     *
     * @param name name of the new declared agent.
     * @return the new created agent name.
     * @throws IllegalArgumentException when <code>isDefined(name)==true</code>.
     */
    public AgentName addAgent(String name) {
        if (agentNames.containsKey(name)) {
            throw new IllegalArgumentException("Duplicated agent!");
        }
        AgentName agentName = new AgentName(agentCounter++, name);
        agentNames.put(name, agentName);
        return agentName;
    }

    /**
     * Returns the array containing the names of defined agents.
     * @return the array containing the names of defined agents.
     */
    public String[] getAgents() {
        return agentPrototypes.keySet().stream().map(AgentName::getAgentName).toArray(String[]::new);
    }

    /**
     * Returns the factory used to build the agent with the given name according to the given values.
     *
     * @param name the name of agent to build.
     * @param values
     * @return the factory used to build the agent with the given name according to the given values.
     */
    public AgentFactory getAgentFactory(String name, SibillaValue[] values) {
        AgentName agentName = agentNames.get(name);
        if (agentName == null) {
            return null;
        }
        SlamAgentPrototype agentPrototype = agentPrototypes.get(agentName);
        if (agentPrototype != null) {
            return agentPrototype.getAgentFactory(values);
        }
        return null;
    }

    public AgentFactory getAgentFactory(String name, String state, SibillaValue[] values) {
        AgentName agentName = agentNames.get(name);
        if (agentName == null) {
            return null;
        }
        SlamAgentPrototype agentPrototype = agentPrototypes.get(agentName);
        if (agentPrototype != null) {
            return agentPrototype.getAgentFactory(state, values);
        }
        return null;
    }

    /**
     * Adds new agent to this definition.
     *
     * @param name       agent name.
     * @param parameters array of prototype parameters.
     * @return returns the name of the agent
     */
    public AgentName addAgent(String name,
                              SlamType[] parameters) {
        AgentName agentName = addAgent(name);
        agentPrototypes.put(
            agentName,
            new SlamAgentPrototype(agentName, parameters)
        );
        return agentName;
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
    public SlamAgentPrototype getPrototype(String agentName) {
        return agentPrototypes.get(agentName);
    }

    public void setStateProvider(String agentName, Function<SibillaValue[], AgentStore> storeFunction) {
        SlamAgentPrototype prototype = agentPrototypes.get(agentName);
        if (prototype != null) {
            prototype.setStoreProvider(storeFunction);
        } else {
            throw new IllegalArgumentException();//TODO: Add message here!
        }
    }

    public void setAgentBehaviour(String agentName, SlamAgentBehaviour agentBehaviour) {
        SlamAgentPrototype prototype = agentPrototypes.get(agentName);
        if (prototype != null) {
            prototype.setAgentBehaviour(agentBehaviour);
        } else {
            throw new IllegalArgumentException();//TODO: Add message here!
        }
    }

    public void setPerceptionFunction(String agentName, PerceptionFunction perceptionFunction) {
        SlamAgentPrototype prototype = agentPrototypes.get(agentName);
        if (prototype != null) {
            prototype.setPerceptionFunction(perceptionFunction);
        } else {
            throw new IllegalArgumentException();//TODO: Add message here!
        }
    }

    public void setAgentTimePassingFunction(String agentName, AgentTimePassingFunction timePassingFunction) {
        SlamAgentPrototype prototype = agentPrototypes.get(agentName);
        if (prototype != null) {
            prototype.setTimePassingFunctionProvider(timePassingFunction);
        } else {
            throw new IllegalArgumentException();//TODO: Add message here!
        }
    }

    public AgentName getAgentName(String name) {
        return agentNames.get(name);
    }
}
