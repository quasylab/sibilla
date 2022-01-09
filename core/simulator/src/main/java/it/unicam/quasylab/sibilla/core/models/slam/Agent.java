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

import java.util.Optional;
import java.util.function.Predicate;
import java.util.function.ToDoubleFunction;

/**
 * The class <code>Agent</code> represents running agents. Each agent is characterised by:
 * <ul>
 *     <li>an identifier, an integer that univocally identifying the agent in the system;</li>
 *     <li>a memory, associating variables to names;</li>
 *     <li>a state, characterising agent behaviour.</li>
 * </ul>
 */
public final class Agent {

    private final int agentId;
    private final AgentVariable[] agentVariables;
    private final AgentMemory agentMemory;
    private AgentState state;
    private final AgentDynamicFunction dynamicFunction;
    private double schedulingTime;

    public Agent(int agentId, AgentVariable[] agentVariables, AgentMemory agentMemory, AgentState state, AgentDynamicFunction dynamicFunction) {
        this.agentId = agentId;
        this.agentVariables = agentVariables;
        this.agentMemory = agentMemory;
        this.state = state;
        this.dynamicFunction = dynamicFunction;
    }

    /**
     * Returns an array containing all the variables in the agent state.
     *
     * @return an array containing all the variables in the agent state.
     */
    public AgentVariable[] getAgentVariables() {
        return agentVariables;
    }

    /**
     * Returns a mapping associating each variable with its value in the current agent memory.
     *
     * @return a mapping associating each variable with its value in the current agent memory.
     */
    public AgentMemory getAgentMemory() {
        return agentMemory;
    }


    /**
     * Returns agent state.
     *
     * @return agent state.
     */
    public AgentState getAgentState() {
        return state;
    }

    /**
     * Returns agent identifier.
     *
     * @return agent identifier.
     */
    public int agentId() {
        return agentId;
    }

    /**
     * Evaluates the given expression with the agent memory.
     *
     * @param expr the expression to evaluate.
     *
     * @return the evaluation of the expression with agent memory.
     */
    public double eval(ToDoubleFunction<AgentMemory> expr) {
        return expr.applyAsDouble(agentMemory);
    }

    /**
     * This method is executed to notify an agent that a new message has been received. The method returns the
     * list of messages that have been sent when the receives is handled.
     *
     * @param msg received message.
     * @return list of messages that have been sent when as a consequence of the received message.
     */
    public Optional<ActivityResult> receive(RandomGenerator rg, GlobalStateExpressionEvaluator evaluator, DeliveredMessage msg) {
        return state.onReceive(this.agentMemory, msg).map(f -> this.execute(rg, evaluator, f));
    }

    private ActivityResult execute(RandomGenerator rg, GlobalStateExpressionEvaluator evaluator, AgentStepFunction function) {
        AgentStepEffect effect = function.apply(rg, evaluator, agentMemory);
        this.state = effect.getNextState();
        this.schedulingTime = this.state.getSojournTimeFunction().applyAsDouble(rg, agentMemory);
        return new ActivityResult(this, effect.getSentMessages());
    }

    /**
     * This method is invoked trigger the execution of the agent step.
     *
     * @param rg random generator used to sample random values.
     * @return the result of agent step.
     */
    public ActivityResult execute(RandomGenerator rg, GlobalStateExpressionEvaluator evaluator) {
        return execute(rg, evaluator, this.state.getStepFunction());
    }


    /**
     * Returns the time at which this agent will execute its step.
     *
     * @return the time at which this agent will execute its step.
     */
    public double timeOfNextStep() {
        return this.schedulingTime;
    }

    /**
     * This method is invoked on the agent to notify that <code>dt</code> time units are passed.
     *
     * @param rg random generator used to sample random values.
     * @param dt passed time units.
     */
    public void timeStep(RandomGenerator rg, double dt) {
        this.dynamicFunction.update(rg, dt, agentMemory);
        this.state.applyStateDynamic(rg, dt, agentMemory);
        agentMemory.recordTime(dt);
    }

    /**
     * Returns true if the agent memory of this agent satisfies the given predicate.
     *
     * @param p a predicate on agent memory.
     * @return true if the agent memory of this agent satisfies the given predicate.
     */
    public boolean test(Predicate<AgentMemory> p) {
        return p.test(agentMemory);
    }
}
