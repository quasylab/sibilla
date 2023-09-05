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

import it.unicam.quasylab.sibilla.core.models.slam.agents.AgentMessage;
import it.unicam.quasylab.sibilla.core.models.slam.agents.SlamAgent;
import it.unicam.quasylab.sibilla.core.util.values.SibillaValue;

import java.util.function.Predicate;

/**
 * Identifies a message that must be delivered at a given agent.
 */
public class DeliveredMessage {


    private final SlamAgent sender;

    private final int receiverId;

    private final AgentMessage message;

    public DeliveredMessage(SlamAgent sender, AgentMessage message, int receiverId) {
        this.sender = sender;
        this.receiverId = receiverId;
        this.message = message;
    }

    public SlamAgent getSender() {
        return sender;
    }

    public int getReceiverId() {
        return receiverId;
    }

    public AgentMessage getMessage() {
        return message;
    }

}
