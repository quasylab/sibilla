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

import it.unicam.quasylab.sibilla.core.models.State;
import it.unicam.quasylab.sibilla.core.models.slam.agents.Agent;
import it.unicam.quasylab.sibilla.core.models.slam.agents.AgentFactory;
import it.unicam.quasylab.sibilla.core.models.slam.agents.AgentMessage;
import it.unicam.quasylab.sibilla.core.models.slam.data.AgentStore;
import org.apache.commons.math3.random.RandomGenerator;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Optional;
import java.util.PriorityQueue;
import java.util.function.Predicate;
import java.util.function.ToDoubleFunction;
import java.util.stream.Stream;

/**
 * Instances of this class represents an environment where a number of agents are operating.
 */
public final class SlamState implements State, StateExpressionEvaluator {

    private int agentCounter = 0;
    private final HashMap<Integer, Agent> agents;
    private double now;
    private final HashMap<Integer,Activity> agentsActivities;
    private final PriorityQueue<Activity> scheduledActivities;

    public SlamState(AgentFactory... agents) {
        this(0.0, agents);
    }

    public SlamState(double now, AgentFactory ... agents) {
        this.agents = new HashMap<>();
        this.now = now;
        this.scheduledActivities = new PriorityQueue<>();
        this.agentsActivities = new HashMap<>();
        Arrays.stream(agents).forEach(this::addAgent);
    }

    public void addAgent(AgentFactory agentFactory) {
        Agent newAgent= agentFactory.getAgent(agentCounter++);
        agents.put(newAgent.agentId(), newAgent);
    }

    @Override
    public synchronized double getMinOf(ToDoubleFunction<AgentStore> expr) {
        return agents.values().stream().mapToDouble(a -> a.eval(expr)).min().orElse(Double.NaN);
    }

    @Override
    public synchronized double getMinOf(ToDoubleFunction<AgentStore> expr, Predicate<Agent> filter) {
        return agents.values().stream().filter(filter).mapToDouble(a -> a.eval(expr)).min().orElse(Double.NaN);
    }

    @Override
    public synchronized double getMaxOf(ToDoubleFunction<AgentStore> expr) {
        return agents.values().stream().mapToDouble(a -> a.eval(expr)).max().orElse(Double.NaN);
    }

    @Override
    public synchronized double getMaxOf(ToDoubleFunction<AgentStore> expr, Predicate<Agent> filter) {
        return agents.values().stream().filter(filter).mapToDouble(a -> a.eval(expr)).max().orElse(Double.NaN);
    }

    @Override
    public synchronized double getMeanOf(ToDoubleFunction<AgentStore> expr) {
        return agents.values().stream().mapToDouble(a -> a.eval(expr)).average().orElse(Double.NaN);
    }

    @Override
    public synchronized double getMeanOf(ToDoubleFunction<AgentStore> expr, Predicate<Agent> filter) {
        return agents.values().stream().filter(filter).mapToDouble(a -> a.eval(expr)).average().orElse(Double.NaN);
    }

    @Override
    public synchronized boolean exists(Predicate<Agent> p) {
        return agents.values().stream().anyMatch(p);
    }

    @Override
    public synchronized boolean forAll(Predicate<Agent> p) {
        return agents.values().stream().allMatch(p);
    }

    public Stream<Agent> stream() {
        return agents.values().stream();
    }

    public void timeStep(RandomGenerator rg, double time) {
        double dt = time - now;
        if (dt < 0) {
            throw new IllegalStateException();//TODO: Add Message!
        }
        if (dt > 0) {
            agents.values().forEach(a -> a.timeStep(rg, dt));
            agents.values().forEach(a -> a.perceive(rg, this));
            now = time;
        }
    }

    public void executeAgentStep(RandomGenerator rg, Agent agent) {
        agentsActivities.remove(agent.agentId());
        applyActivityResult(rg, agent.execute(rg, this));
    }

    private void applyActivityResult(RandomGenerator rg, ActivityResult result) {
        Agent agent = result.getAgent();
        if (!Double.isNaN(result.getAgent().timeOfNextStep())) {
            assert agent.timeOfNextStep() > now;
            scheduleAgentStep(agent);
        }
        result.getSentMessages().forEach(m -> this.sendMessage(rg, agent, m));
    }

    private void scheduleAgentStep(Agent agent) {
        Activity activity = new Activity.AgentStepActivity(agent);
        recordActivity(activity);
        agentsActivities.put(agent.agentId(), activity);
    }

    public void sendMessage(RandomGenerator rg, Agent sender, AgentMessage agentMessage) {
        this.agents.values()
                .stream()
                .filter(a -> a != sender)
                .map(a -> agentMessage.apply(rg, a))
                .filter(Optional::isPresent)
                .map(Optional::get)
                .forEach(this::scheduleMessage);
    }

    public void scheduleMessage(DeliveredMessage m) {
        recordActivity(new Activity.MessageDeliveryActivity(m));
    }

    public void recordActivity(Activity activity) {
        this.scheduledActivities.add(activity);
    }

    public void deliverMessage(RandomGenerator rg, DeliveredMessage message) {
        Agent target = message.getTarget();
        Optional<ActivityResult> optionalResult = target.receive(rg, this, message);
        if (optionalResult.isPresent()) {
            ActivityResult result = optionalResult.get();
            Activity activity = agentsActivities.remove(result.getAgent().agentId());
            if (activity != null) {
                scheduledActivities.remove(activity);
            }
            applyActivityResult(rg, result);
        }
    }

    public Activity nextScheduledActivity() {
        return scheduledActivities.poll();
    }

    public double now() {
        return now;
    }

    public boolean isTerminal() {
        return scheduledActivities.isEmpty();
    }
}
