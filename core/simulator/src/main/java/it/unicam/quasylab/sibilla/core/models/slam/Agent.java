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
import java.util.Map;
import java.util.function.ToDoubleFunction;

/**
 * The class <code>Agent</code> represents running agents. Each agent is characterised by:
 * <ul>
 *     <li>an identifier, an integer that univocally identifying the agent in the system;</li>
 *     <li>a memory, associating variables to names;</li>
 *     <li>a state, characterising agent behaviour.</li>
 * </ul>
 */
public interface Agent {

    /**
     * Returns a mapping associating each variable with its value in the current agent memory.
     *
     * @return a mapping associating each variable with its value in the current agent memory.
     */
    Map<String,Double> getAgentMemory();


    /**
     * Returns agent state.
     *
     * @return agent state.
     */
    AgentState getAgentState();

    /**
     * Returns agent identifier.
     *
     * @return agent identifier.
     */
    int agentId();

    /**
     * Evaluates the given expression with the agent memory.
     *
     * @param expr the expression to evaluate.
     *
     * @return the evaluation of the expression with agent memory.
     */
    double eval(ToDoubleFunction<AgentMemory> expr);

    /**
     * This method is executed to notify an agent that a new message has been received. The method returns the
     * list of messages that have been sent when the receives is handled.
     *
     * @param msg received message.
     * @return list of messages that have been sent when as a consequence of the received message.
     */
    List<PendingMessage> receive(AgentMessage msg);


    /**
     * This method is invoked trigger the execution of the agent step.
     *
     * @param currentTime current time.
     * @return list of messages that have been sent when the agent performs its step.
     */
    List<PendingMessage> execute(double currentTime);


    /**
     * Returns the time at which this agent will execute its step.
     *
     * @return the time at which this agent will execute its step.
     */
    double timeOfNextStep();
}
