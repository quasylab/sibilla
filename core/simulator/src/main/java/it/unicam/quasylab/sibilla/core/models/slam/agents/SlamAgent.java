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
import it.unicam.quasylab.sibilla.core.util.datastructures.Pair;
import org.apache.commons.math3.random.RandomGenerator;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.function.ToDoubleFunction;

/**
 * The class <code>LIOAgent</code> represents running agents. Each agent is characterised by:
 * <ul>
 *     <li>an identifier, an integer that univocally identifying the agent in the system;</li>
 *     <li>a memory, associating variables to names;</li>
 *     <li>a state, characterising agent behaviour.</li>
 * </ul>
 */
public final class SlamAgent {

    private final int agentId;

    private final SlamAgentPrototype agentPrototype;

    private final AgentStore agentMemory;
    private final SlamAgentState state;
    private final double timeOfNextStep;

    public SlamAgent(SlamAgentPrototype agentPrototype, int agentId, SlamAgentState state, AgentStore agentMemory, double timeOfNextStep) {
        this.agentId = agentId;
        this.agentPrototype = agentPrototype;
        this.agentMemory = agentMemory;
        this.state = state;
        this.timeOfNextStep = timeOfNextStep;
    }


    public SlamAgent(SlamAgent agent, SlamAgentState nextAgentState, AgentStore nextAgentStore, double agentSchedulingTime) {
        this(agent.agentPrototype, agent.agentId, nextAgentState, nextAgentStore, agentSchedulingTime);
    }

    public SlamAgent(RandomGenerator rg, SlamAgent agent, SlamAgentState nextAgentState, AgentStore nextAgentStore) {
        this(agent.agentPrototype, agent.agentId, nextAgentState, nextAgentStore, nextAgentState.sampleTimeOfNextStep(rg, nextAgentStore));
    }

    public SlamAgent(SlamAgent agent, AgentStore agentStore) {
        this(agent.agentPrototype, agent.agentId, agent.state, agentStore, agent.timeOfNextStep);
    }

    public SlamAgent(SlamAgentPrototype agentPrototype, int agentId, SlamAgentState agentBehaviouralState, AgentStore agentStore) {
        this(agentPrototype, agentId, agentBehaviouralState, agentStore, Double.NaN);
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
    public SlamAgentState getAgentState() {
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
     * @param message received message.
     * @return list of messages that have been sent when as a consequence of the received message.
     */
    public Optional<Pair<List<OutgoingMessage>, SlamAgent>> receive(RandomGenerator rg, DeliveredMessage message) {
        return state.onReceive(rg, agentMemory, message).map(e -> this.apply(rg, e));
    }

    private Pair<List<OutgoingMessage>, SlamAgent> apply(RandomGenerator rg, SlamAgentStepEffect effect) {
        SlamAgent nextAgent = new SlamAgent(rg, this, effect.getNextState(), effect.getNextAgentStore());
        return Pair.of(effect.getSentMessages(), nextAgent);
    }

    /**
     * This method is invoked trigger the execution of the agent step.
     *
     * @param rg random generator used to sample random values.
     * @return the result of agent step.
     */
    public Optional<Pair<List<OutgoingMessage>, SlamAgent>> execute(RandomGenerator rg) {
            return this.state.step(rg, this.agentMemory).map(e -> apply(rg, e));
    }


    /**
     * Returns the time at which this agent will execute its step.
     *
     * @return the time at which this agent will execute its step.
     */
    public double timeOfNextStep() {
        return this.timeOfNextStep;
    }

    /**
     * This method is invoked on the agent to notify the passage of time to this agent.
     *
     * @param rg random generator used to sample random values.
     * @param time progress agent time to the given value.
     */
    public SlamAgent progressTime(RandomGenerator rg, double time) {
        if (time > timeOfNextStep) throw new SlamInternalRuntimeException("Time step missed for agent"+this);
        return new SlamAgent(this,
                this.state.applyStateDynamic(rg, time-agentMemory.now(), this.agentPrototype.getTimePassingFunctionProvider().update(rg, time-agentMemory.now(), agentMemory)).
                recordTime(time));
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
        SlamAgent agent = (SlamAgent) o;
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
    public SlamAgentPrototype getAgentPrototype() {
        return agentPrototype;
    }

    public SlamAgent perceive(RandomGenerator rg, StateExpressionEvaluator stateExpressionEvaluator) {
        PerceptionFunction perceptionFunction = agentPrototype.getPerceptionFunction();
        if (perceptionFunction == null) return this;
        return new SlamAgent(this, perceptionFunction.perceive(rg, stateExpressionEvaluator, agentMemory));
    }

    public AgentName getAgentName() {
        return agentPrototype.getAgentName();
    }
}
