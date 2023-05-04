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

import it.unicam.quasylab.sibilla.core.models.slam.agents.AgentStepFunction;
import it.unicam.quasylab.sibilla.core.models.slam.data.AgentStore;

import java.util.function.BiPredicate;
import java.util.function.Function;

/**
 * Describes how a given message received by an agent can be handled.
 */
public class MessageHandler {

    private final BiPredicate<AgentStore,DeliveredMessage> predicate;
    private final Function<DeliveredMessage, AgentStepFunction> handlingFunction;

    /**
     * Creates a new handler that handles messages satisfying the given predicate and that when received triggers
     * the execution of the given handling function. The predicate is evaluated according to the current agent memory
     * and the content of the received message. The handling function generates the {@link AgentStepFunction}
     * from the received message.
     *
     * @param predicate predicate used to check if the message must be handled or not.
     * @param handlingFunction a function used to generate the step function that is consequent to the received message.
     */
    public MessageHandler(BiPredicate<AgentStore,DeliveredMessage> predicate, Function<DeliveredMessage, AgentStepFunction> handlingFunction) {
        this.predicate = predicate;
        this.handlingFunction = handlingFunction;
    }

    /**
     * Returns the predicate used to select received messages.
     *
     * @return the predicate used to select received messages.
     */
    public BiPredicate<AgentStore,DeliveredMessage> getPredicate() {
        return predicate;
    }

    /**
     * Returns the function used to generate the {@link AgentStepFunction} triggered by the received message.
     *
     * @return the function used to generate the {@link AgentStepFunction} triggered by the received message.
     */
    public Function<DeliveredMessage, AgentStepFunction> getHandlingFunction() {
        return handlingFunction;
    }

}
