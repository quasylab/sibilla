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

import java.util.Map;
import java.util.TreeMap;

/**
 * This is a container for the set of states defining the agent behaviour.
 */
public class AgentBehaviourOld {


    private AgentBehaviouralState initialState;
    private Map<String, AgentBehaviouralState> states;

    /**
     * Creates an empty agent behaviour.
     */
    public AgentBehaviourOld() {
        this.states = new TreeMap<>();
    }

    /**
     * Returns the initial state of this behaviour.
     *
     * @return the initial state of this behaviour.
     */
    public AgentBehaviouralState initialState() {
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
        this.states.put(stateName, new AgentBehaviouralState(states.size(), stateName));
    }

    /**
     * Sets the time passing function to the state with the given name.
     *
     * @param stateName state name.
     * @param timePassingFunction time passing function.
     */
    public void setTimePassingFunction(String stateName, AgentTimePassingFunction timePassingFunction) {
        AgentBehaviouralState state = this.states.get(stateName);
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
        AgentBehaviouralState state = this.states.get(stateName);
        if (state != null) {
            state.addMessageHandler(messageHandler);
        } else {
            throw new IllegalArgumentException();//TODO: Add Message!
        }
    }

    /**
     * Sets the step function associated with the given state.
     *
     * @param stateName state name.
     * @param step the function that is executed when this state is left.
     */
    public void setStep(String stateName, AgentStepFunction step) {
        AgentBehaviouralState state = this.states.get(stateName);
        if (state != null) {
            state.setStep(step);
        } else {
            throw new IllegalArgumentException();//TODO: Add Message!
        }
    }


    /**
     * Sets the step function associated with the given state name so to jump to the
     * state named <code>nextState</code> by executing command <code>commande</code>.
     *
     * @param stateName state name
     * @param nextState name of the state reached after the step
     * @param command command executed in the step
     */
    public void setStep(String stateName, String nextState, AgentCommand command) {
        if (states.containsKey(nextState)) {
            setStep(stateName, AgentStepFunction.step(states.get(nextState), command));
        } else {
            throw new IllegalArgumentException();//TODO: Add Message!
        }
    }


    /**
     * Returns the agent step function that leads to the state named <code>nextState</code> and
     * that executes the given command.
     *
     * @param nextState state reached with the step.
     * @param command command executed in the transition.
     * @return the agent step function that leads to the state named <code>nextState</code> and
     * that executes the given command.
     */
    public AgentStepFunction getAgentStepFunction(String nextState, AgentCommand command) {
        if (states.containsKey(nextState)) {
            return AgentStepFunction.step(states.get(nextState), command);
        } else {
            throw new IllegalArgumentException();//TODO: Add Message!
        }
    }



}
