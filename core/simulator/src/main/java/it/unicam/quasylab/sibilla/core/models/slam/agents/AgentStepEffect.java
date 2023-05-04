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

import it.unicam.quasylab.sibilla.core.models.slam.agents.AgentBehaviouralState;
import it.unicam.quasylab.sibilla.core.models.slam.agents.AgentMessage;

import java.util.List;

/**
 * This class represents the effects that are consequences of an agent step.
 */
public class AgentStepEffect {

    private final List<AgentMessage> sentMessages;
    private final AgentBehaviouralState nextState;


    public AgentStepEffect(AgentBehaviouralState nextState, List<AgentMessage> sentMessages) {
        this.sentMessages = sentMessages;
        this.nextState = nextState;
    }

    public List<AgentMessage> getSentMessages() {
        return sentMessages;
    }

    public AgentBehaviouralState getNextState() {
        return nextState;
    }


}
