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
    private final VariableMapping agentLocalState;
    //private final YodaLambda[] agentObservations;
    //private final YodaAction agentActions;
    private final YodaBehaviour agentBehaviour;

    public YodaAgent(int identifier, VariableMapping agentLocalState,  YodaBehaviour agentBehaviour) {
        this.identifier = identifier;
        this.agentLocalState = agentLocalState;
        //this.agentObservations = agentObservations;
        //this.agentActions = agentActions;
        this.agentBehaviour = agentBehaviour;
    }

    public int getIdentifier() {
        return identifier;
    }

    public VariableMapping getAgentLocalState(){
        return agentLocalState;
    }

}
