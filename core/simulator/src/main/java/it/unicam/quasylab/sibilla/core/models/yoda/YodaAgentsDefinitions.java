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

package it.unicam.quasylab.sibilla.core.models.yoda;

import it.unicam.quasylab.sibilla.core.util.values.SibillaRandomBiFunction;

import java.util.HashMap;
import java.util.Map;

/**
 * This class is used to record the definitions of YODA agents in a system.
 */
public class YodaAgentsDefinitions {

    private final Map<YodaElementName, YodaAgentPrototype> agentDefinitions;

    private final Map<YodaElementName, YodaVariableMapping> elementDefinitions;

    /**
     * Crestes a new instance.
     */
    public YodaAgentsDefinitions() {
        agentDefinitions = new HashMap<>();
        elementDefinitions = new HashMap<>();
    }

    /**
     * Adds a new prototype to this set of definitions.
     *
     * @param prototype the agent prototype to add.
     */
    public void add(YodaAgentPrototype prototype) {
        this.agentDefinitions.put(prototype.getAgentName(), prototype);
    }


    public void addElement(YodaElementName name, YodaVariableMapping environmentalAttributes) {
        this.elementDefinitions.put(name, environmentalAttributes);
    }

    /**
     * Returns an agent with the given id and name instantiated by using the given parameters (that
     * are stored in a variable mapping).
     *
     * @param agentId the id to use in the new created agent.
     * @param agentName the name of the new created agent.
     * @param initialAssignment a mapping containing the parameters used to instantiate the agents
     * @return an agent with the given id and name.
     */
    public YodaAgent getAgent(int agentId, YodaElementName agentName, YodaVariableMapping initialAssignment) {
        if (agentDefinitions.containsKey(agentName)) {
            return agentDefinitions.get(agentName).getAgent(initialAssignment, agentId);
        }
        throw new IllegalStateException("No definition does exist for agent "+agentName+"!");
    }

    public YodaSceneElement getElement(int agentId, YodaElementName elementName, YodaVariableMapping initialAssignment) {
        return new YodaSceneElement(elementName, agentId, this.elementDefinitions.get(elementName).setAll(initialAssignment));
    }


    public void add(YodaElementName name, YodaVariableMapping initialAgentAttributes, YodaVariableMapping initialEnvironmentalAttributes, YodaVariableMapping initialAgentObservations) {
        add(new YodaAgentPrototype(name, initialAgentAttributes, initialEnvironmentalAttributes, initialAgentObservations));
    }

    public void setBehaviour(YodaElementName agentName, YodaBehaviour behaviour) {
        if (agentDefinitions.containsKey(agentName)) {
            agentDefinitions.get(agentName).setAgentBehaviour(behaviour);
        } else {
            throw new IllegalStateException("No definition does exist for agent "+agentName+"!");
        }
    }

    public void setSensing(YodaElementName agentName, SibillaRandomBiFunction<YodaSystemState, YodaAgent, YodaVariableMapping> sensingFunction) {
        if (agentDefinitions.containsKey(agentName)) {
            agentDefinitions.get(agentName).setAgentSensing(sensingFunction);
        } else {
            throw new IllegalStateException("No definition does exist for agent "+agentName+"!");
        }
    }

    public void setDynamics(YodaElementName agentName, SibillaRandomBiFunction<YodaVariableMapping, YodaVariableMapping, YodaVariableMapping> updateFunction) {
        if (agentDefinitions.containsKey(agentName)) {
            agentDefinitions.get(agentName).setEnvironmentalAttributeUpdateFunction(updateFunction);
        } else {
            throw new IllegalStateException("No definition does exist for agent "+agentName+"!");
        }
    }

    public boolean isAgent(YodaElementName name) {
        return agentDefinitions.containsKey(name);
    }

    public boolean isSceneElement(YodaElementName name) {
        return elementDefinitions.containsKey(name);
    }
}
