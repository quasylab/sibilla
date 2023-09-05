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

import it.unicam.quasylab.sibilla.core.models.slam.data.AgentStore;
import it.unicam.quasylab.sibilla.core.util.datastructures.Pair;

import java.util.List;

/**
 * This class represents the effects of an agent step.
 */
public class SlamAgentStepEffect {

    private final List<OutgoingMessage> sentMessages;
    private final AgentStore nextAgentStore;
    private final SlamAgentState nextState;


    /**
     * Creates a new step.
     *
     * @param nextState state reached after the step.
     * @param sentMessages messages sent in the step.
     * @param nextAgentStore store of the agent after the step.
     */
    public SlamAgentStepEffect(SlamAgentState nextState, List<OutgoingMessage> sentMessages, AgentStore nextAgentStore) {
        this.sentMessages = sentMessages;
        this.nextState = nextState;
        this.nextAgentStore = nextAgentStore;
    }

    public SlamAgentStepEffect(SlamAgentState state, Pair<List<OutgoingMessage>, AgentStore> commandResult) {
        this(state, commandResult.getKey(), commandResult.getValue());
    }

    /**
     * Returns the list of messages sent in this step.
     *
     * @return the list of messages sent in this step.
     */
    public List<OutgoingMessage> getSentMessages() {
        return sentMessages;
    }

    /**
     * Returns the behavioural state of the agent after the step.
     *
     * @return the behavioural state of the agent after the step.
     */
    public SlamAgentState getNextState() {
        return nextState;
    }

    /**
     * Returns the store of the agent after the step.
     *
     * @return the store of the agent after the step.
     */
    public AgentStore getNextAgentStore() {
        return nextAgentStore;
    }
}
