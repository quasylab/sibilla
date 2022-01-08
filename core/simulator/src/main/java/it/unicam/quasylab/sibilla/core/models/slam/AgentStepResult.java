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

import java.util.List;

/**
 * Instances of this class represent the result of an agent step. This consists of a list of messages
 * and the agent that has performed the step.
 */
public class AgentStepResult {

    private final List<AgentMessage> messages;
    private final Agent sender;

    /**
     * Creates a new instance of agent results.
     *
     * @param sender agent performing the step.
     * @param messages list of sent messages.
     */
    public AgentStepResult(Agent sender, List<AgentMessage> messages) {
        this.sender = sender;
        this.messages = messages;
    }

    /**
     * Returns the list of sent messages.
     *
     * @return the list of sent messages.
     */
    public List<AgentMessage> getMessages() {
        return messages;
    }

    /**
     * Returns the agent performing the step.
     *
     * @return the agent performing the step.
     */
    public Agent getAgent() {
        return sender;
    }


}
