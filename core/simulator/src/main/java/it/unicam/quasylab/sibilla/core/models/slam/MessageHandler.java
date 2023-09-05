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

import it.unicam.quasylab.sibilla.core.models.slam.agents.SlamAgent;
import it.unicam.quasylab.sibilla.core.models.slam.agents.SlamAgentDeterministicStep;
import it.unicam.quasylab.sibilla.core.models.slam.agents.SlamAgentStep;
import it.unicam.quasylab.sibilla.core.models.slam.agents.SlamAgentStepEffect;
import it.unicam.quasylab.sibilla.core.models.slam.data.AgentStore;
import it.unicam.quasylab.sibilla.core.util.values.SibillaValue;
import org.apache.commons.math3.random.RandomGenerator;

import java.util.Optional;
import java.util.function.BiFunction;
import java.util.function.BiPredicate;
import java.util.function.Predicate;

/**
 * Describes how a given message received by an agent can be handled.
 */
public class MessageHandler {

    private final MessageTag handledTag;

    private final BiFunction<AgentStore, SibillaValue[], AgentStore> handlerBindings;

    private final BiPredicate<AgentStore, SlamAgent> predicateOnSender;

    private final BiPredicate<RandomGenerator, AgentStore> predicateOnContent;
    private final SlamAgentStep triggeredStep;

    /**
     * Creates a new handler that handles messages satisfying the given predicate and that when received triggers
     * the execution of the given handling function. The predicate is evaluated according to the current agent memory
     * and the content of the received message. The handling function generates the {@link SlamAgentDeterministicStep}
     * from the received message.
     *
     * @param handledTag
     * @param handlerBindings
     * @param predicateOnSender
     * @param predicateOnContent
     * @param triggeredStep      a function used to generate the step function that is consequent to the received message.
     */
    public MessageHandler(MessageTag handledTag, BiFunction<AgentStore, SibillaValue[], AgentStore> handlerBindings, BiPredicate<AgentStore, SlamAgent> predicateOnSender, BiPredicate<RandomGenerator, AgentStore> predicateOnContent, SlamAgentStep triggeredStep) {
        this.handledTag = handledTag;
        this.handlerBindings = handlerBindings;
        this.predicateOnSender = predicateOnSender;
        this.predicateOnContent = predicateOnContent;
        this.triggeredStep = triggeredStep;
    }


    public Optional<SlamAgentStepEffect> doReceive(RandomGenerator rg, AgentStore receiverStore, DeliveredMessage message) {
        if (this.handledTag.equals(message.getMessage().getTag())) {
            AgentStore handlerStore = handlerBindings.apply(receiverStore, message.getMessage().getContent());
            if (this.predicateOnSender.test(handlerStore, message.getSender())&&this.predicateOnContent.test(rg, handlerStore)) {
                return triggeredStep.apply(rg, handlerStore);
            }
        }
        return Optional.empty();
    }


}
