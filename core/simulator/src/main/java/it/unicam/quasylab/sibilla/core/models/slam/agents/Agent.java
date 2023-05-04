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

import it.unicam.quasylab.sibilla.core.models.slam.*;
import it.unicam.quasylab.sibilla.core.models.slam.data.AgentStore;
import org.apache.commons.math3.random.RandomGenerator;

import java.util.Objects;
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

    private final AgentPrototype agentPrototype;

    private final AgentStore agentMemory;
    private AgentBehaviouralState state;
    private final AgentTimePassingFunction timePassingFunction;
    private final PerceptionFunction perceptionFunction;
    private double schedulingTime;

    public Agent(AgentPrototype agentPrototype, int agentId,  AgentStore agentMemory, AgentBehaviouralState state, AgentTimePassingFunction timePassingFunction, PerceptionFunction perceptionFunction) {
        this.agentId = agentId;
        this.agentPrototype = agentPrototype;
        this.agentMemory = agentMemory;
        this.state = state;
        this.timePassingFunction = timePassingFunction;
        this.perceptionFunction = perceptionFunction;
    }

    /**
     * Returns a mapping associating each variable with its value in the current agent memory.
     *
     * @return a mapping associating each variable with its value in the current agent memory.
     */
    public AgentStore getAgentMemory() {
        return agentMemory;
    }


    /**
     * Returns agent state.
     *
     * @return agent state.
     */
    public AgentBehaviouralState getAgentState() {
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
    public double eval(ToDoubleFunction<AgentStore> expr) {
        return expr.applyAsDouble(agentMemory);
    }

    /**
     * This method is executed to notify an agent that a new message has been received. The method returns the
     * list of messages that have been sent when the receives is handled.
     *
     * @param msg received message.
     * @return list of messages that have been sent when as a consequence of the received message.
     */
    public Optional<ActivityResult> receive(RandomGenerator rg, StateExpressionEvaluator evaluator, DeliveredMessage msg) {
        return state.onReceive(this.agentMemory, msg).map(f -> this.execute(rg, evaluator, f));
    }

    private ActivityResult execute(RandomGenerator rg, StateExpressionEvaluator evaluator, AgentStepFunction function) {
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
    public ActivityResult execute(RandomGenerator rg, StateExpressionEvaluator evaluator) {
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
        this.timePassingFunction.update(rg, dt, agentMemory);
        this.state.applyStateDynamic(rg, dt, agentMemory);
        agentMemory.recordTime(dt);
    }

    /**
     * Returns true if the agent memory of this agent satisfies the given predicate.
     *
     * @param p a predicate on agent memory.
     * @return true if the agent memory of this agent satisfies the given predicate.
     */
    public boolean test(Predicate<AgentStore> p) {
        return p.test(agentMemory);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Agent agent = (Agent) o;
        return agentId == agent.agentId;
    }

    @Override
    public int hashCode() {
        return Objects.hash(agentId);
    }

    /**
     * Returns the prototype describing the behaviour of this agent.
     *
     * @return the prototype describing the behaviour of this agent.
     */
    public AgentPrototype getAgentPrototype() {
        return agentPrototype;
    }

    public void perceive(RandomGenerator rg, SlamState slamState) {
        this.perceptionFunction.perceive(rg,slamState,agentMemory);
    }
}
