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

import org.apache.commons.math3.random.RandomGenerator;

/**
 * The class <code>YodaAgent</code> represents the agents available in the simulation
 * Each one has the following components:
 * <ul>
 *     <li>a local state containing the information known to the agent</li>
 *     <li>a set of observations done by the agent</li>
 *     <li>a set of actions, that the agent can execute</li>
 *     <li>a behaviour that</li>
 * </ul>
 *
 */
public final class YodaAgent {

    private final int identifier;
    private YodaVariableMapping agentLocalState;
    private final YodaBehaviour agentBehaviour;

    public YodaAgent(int identifier, YodaVariableMappingWrapper agentLocalState, YodaBehaviour agentBehaviour) {
        this.identifier = identifier;
        this.agentLocalState = agentLocalState;
        this.agentBehaviour = agentBehaviour;
    }

    /**
     * This method returns the identifier of the agent
     *
     * @return the identifier of the agent
     */
    public int getIdentifier() {
        return identifier;
    }

    /**
     * This method returns the agent local state
     *
     * @return the agent local state
     */
    public YodaVariableMapping getAgentLocalState(){
        return agentLocalState;
    }

    /**
     * This method returns the agent behaviour
     *
     * @return the agent behaviour
     */
    public YodaBehaviour getAgentBehaviour() {
        return agentBehaviour;
    }

    /**
     * This method returns the updated state of this agent
     *
     * @param rg a random generator
     * @param action the action that the agent should perform
     * @return the updated state of this agent
     */
    public YodaVariableMapping update(RandomGenerator rg, YodaAction action){
        YodaVariableMapping newState = action.performAction(rg, agentLocalState);
        return agentLocalState = newState;
    }
}
