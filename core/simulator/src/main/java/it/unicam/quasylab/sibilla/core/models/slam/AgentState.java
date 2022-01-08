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

import org.apache.commons.math3.random.RandomGenerator;

import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.function.ToDoubleBiFunction;

/**
 * This class represents the definition of an agent state. Each state is identified by
 * a name and by an index.
 */
public final class AgentState {

    private final String stateName;

    private final int stateIndex;

    private ToDoubleBiFunction<RandomGenerator, AgentMemory> sojournTimeFunction;

    private final List<MessageHandler> messageHandlers;

    private AgentStepFunction step;
    private AgentDynamicFunction dynamicFunction;

    /**
     * Creates a new state with the given name and the given index.
     *
     * @param stateIndex index of the created state.
     * @param stateName name of the created state.
     */
    public AgentState(int stateIndex, String stateName) {
        this.stateIndex = stateIndex;
        this.messageHandlers = new LinkedList<>();
        this.stateName = stateName;
    }

    /**
     * Returns the state name.
     *
     * @return the state name.
     */
    public String getStateName() {
        return stateName;
    }

    /**
     * Returns the state index.
     *
     * @return the state index.
     */
    public int getStateIndex() {
        return stateIndex;
    }

    /**
     * Returns the function used to compute the sojourn time.
     *
     * @return the function used to compute the sojourn time.
     */
    public ToDoubleBiFunction<RandomGenerator, AgentMemory> getSojournTimeFunction() {
        return sojournTimeFunction;
    }

    /**
     * Sets the function used to compute the sojourn time.
     *
     * @param sojournTimeFunction the function used to compute the sojourn time.
     */
    public void setSoujournTimeFunction(ToDoubleBiFunction<RandomGenerator, AgentMemory> sojournTimeFunction) {
        this.sojournTimeFunction = sojournTimeFunction;
    }

    /**
     * Adds the given message handler to this state.
     *
     * @param messageHandler the message handler to add to this state.
     */
    public void addMessageHandler(MessageHandler messageHandler) {
        this.messageHandlers.add( messageHandler );
    }

    /**
     * Returns an optional with the agent step function that must be executed when a given message is
     * received in a given memory.
     *
     * @param memory memory of the agent that has received the message.
     * @param message received message.
     * @return the step function to execute when the message is received.
     */
    public Optional<AgentStepFunction> onReceive(AgentMemory memory, DeliveredMessage message) {
        for (MessageHandler mh : messageHandlers) {
            if (mh.getPredicate().test(memory, message)) {
                return Optional.of(mh.getHandlingFunction().apply(message));
            }
        }
        return Optional.empty();
    }

    /**
     * Returns the function that is executed when this state is left.
     *
     * @return the function that is executed when this state is left.
     */
    public AgentStepFunction getStepFunction() {
        return step;
    }

    /**
     * Sets the function that is executed when this state is left.
     *
     * @param step the function that is executed when this state is left.
     */
    public void setStep(AgentStepFunction step) {
        this.step = step;
    }

    /**
     * Sets the function that is used to compute the agent dynamic in this state.
     *
     * @param dynamicFunction the function that is used to compute the agent dynamic in this state.
     */
    public void setAgentDynamicFunction(AgentDynamicFunction dynamicFunction) {
        this.dynamicFunction = dynamicFunction;
    }

    /**
     * Applies the dynamic function associated with this state.
     *
     * @param rg random generator.
     * @param dt passed time units.
     * @param agentMemory agent memory.
     */
    public void applyStateDynamic(RandomGenerator rg, double dt, AgentMemory agentMemory) {
        if (this.dynamicFunction != null) {
            this.dynamicFunction.update(rg, dt, agentMemory);
        }
    }
}
