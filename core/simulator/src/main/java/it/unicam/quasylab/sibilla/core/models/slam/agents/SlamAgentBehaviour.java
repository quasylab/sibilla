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

import it.unicam.quasylab.sibilla.core.models.slam.AgentTimePassingFunction;
import it.unicam.quasylab.sibilla.core.models.slam.MessageHandler;
import it.unicam.quasylab.sibilla.core.models.slam.data.AgentStore;
import org.apache.commons.math3.random.RandomGenerator;

import java.util.Map;
import java.util.TreeMap;
import java.util.function.ToDoubleBiFunction;

/**
 * This is a container for the set of states defining the agent behaviour.
 */
public class SlamAgentBehaviour {


    private SlamAgentState initialState;
    private final Map<String, SlamAgentState> states;

    /**
     * Creates an empty agent behaviour.
     */
    public SlamAgentBehaviour() {
        this.states = new TreeMap<>();
    }

    /**
     * Returns the initial state of this behaviour.
     *
     * @return the initial state of this behaviour.
     */
    public SlamAgentState initialState() {
        return null;
    }

    /**
     * Adds a new state with the given name to this behaviour.
     *
     * @param stateName state name.
     */
    public void addState(String stateName) {
        if (this.states.containsKey(stateName)) {
            throw new IllegalArgumentException();//TODO: Add Message Here!
        }
        SlamAgentState newState = new SlamAgentState(states.size(), stateName);
        this.states.put(stateName, newState);
        if (initialState == null) {
            initialState = newState;
        }
    }

    public void setInitialState(String stateName) {
        SlamAgentState state = this.states.get(stateName);
        if (state == null) {
            throw new IllegalArgumentException();//TODO: Add Message Here!
        }
        this.initialState = state;
    }

    /**
     * Sets the time passing function to the state with the given name.
     *
     * @param stateName state name.
     * @param timePassingFunction time passing function.
     */
    public void setTimePassingFunction(String stateName, AgentTimePassingFunction timePassingFunction) {
        SlamAgentState state = this.states.get(stateName);
        if (state != null) {
            state.setTimePassingFunction(timePassingFunction);
        } else {
            throw new IllegalArgumentException();//TODO: Add Message!
        }
    }

    /**
     * Adds the given message handler to the given state.
     *
     * @param stateName state name
     * @param messageHandler the message handler to add to this state.
     */
    public void addMessageHandler(String stateName, MessageHandler messageHandler) {
        SlamAgentState state = this.states.get(stateName);
        if (state != null) {
            state.addMessageHandler(messageHandler);
        } else {
            throw new IllegalArgumentException();//TODO: Add Message!
        }
    }

    /**
     * Sets the behaviour of the state associated with the passage of time.
     *
     * @param stateName           state name.
     * @param sojournTimeFunction the function used to compute the sojourn time in the given state.
     * @param step                the step performed when the given time is passed.
     */
    public void setTimeDependentStep(String stateName, ToDoubleBiFunction<RandomGenerator, AgentStore> sojournTimeFunction, SlamAgentStep step) {
        SlamAgentState state = this.states.get(stateName);
        if (state != null) {
            state.setTimeDependentStep(sojournTimeFunction, step);
        } else {
            throw new IllegalArgumentException();//TODO: Add Message!
        }
    }

    /**
     * Returns the state with the given name.
     *
     * @param stateName the name of the state to return
     * @return the state with the given name.
     */
    public SlamAgentState getAgentState(String stateName) {
        return this.states.get(stateName);
    }

    /**
     * Returns true if a state with the given name is defined.
     *
     * @param stateName the name of the state for which the presence is tested
     * @return true if a state with the given name is defined.
     */
    public boolean hasState(String stateName) {
        return this.states.containsKey(stateName);
    }
}
