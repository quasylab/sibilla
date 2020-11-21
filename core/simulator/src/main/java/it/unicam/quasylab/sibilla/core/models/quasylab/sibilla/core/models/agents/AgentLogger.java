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

package it.unicam.quasylab.sibilla.core.models.quasylab.sibilla.core.models.agents;

import org.apache.commons.math3.random.RandomGenerator;

public class AgentLogger implements AgentBehaviour {

    private final AgentBehaviour observedAgent;
    private final AgentLog agentLog;

    public AgentLogger(AgentBehaviour observedAgent) {
        this(observedAgent,new AgentLog());
    }

    public AgentLogger(AgentBehaviour observedAgent, AgentLog agentLog) {
        this.observedAgent = observedAgent;
        this.agentLog = agentLog;
    }


    @Override
    public AgentAction step(RandomGenerator rg, double now, VariableMapping currentState, VariableMapping observations) {
        AgentAction action = observedAgent.step(rg,now,currentState,observations);
        agentLog.add(currentState,observations,action);
        return action;
    }

    public AgentLog getLog() {
        return agentLog;
    }
}
