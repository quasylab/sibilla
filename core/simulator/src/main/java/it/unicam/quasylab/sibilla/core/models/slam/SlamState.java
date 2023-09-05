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
import it.unicam.quasylab.sibilla.core.models.slam.agents.OutgoingMessage;
import it.unicam.quasylab.sibilla.core.models.slam.agents.SlamAgent;
import it.unicam.quasylab.sibilla.core.models.slam.data.AgentStore;
import it.unicam.quasylab.sibilla.core.util.datastructures.*;
import org.apache.commons.math3.random.RandomGenerator;

import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.ToDoubleFunction;
import java.util.stream.Stream;

/**
 * Instances of this class represents an environment where a number of agents are operating.
 */
public final class SlamState implements State, StateExpressionEvaluator {


    private final Activity.ActivityFactory activityFactory;

    private final SibillaMap<Integer, SlamAgent> agents;

    private final double now;
    private final Scheduler<Activity> scheduledActivities;

    private final SibillaMap<Integer, Activity.AgentStepActivity> agentActivities;


    public SlamState(Activity.ActivityFactory activityFactory, double now, SibillaMap<Integer, SlamAgent> agents, Scheduler<Activity> scheduledActivities, SibillaMap<Integer, Activity.AgentStepActivity> agentActivities) {
        this.activityFactory = activityFactory;
        this.now = now;
        this.scheduledActivities = scheduledActivities;
        this.agents = agents;
        this.agentActivities = agentActivities;
    }

    public static SlamState set(SlamState slamState, SibillaMap<Integer, SlamAgent> agents) {
        return new SlamState(slamState.activityFactory, slamState.now, agents, slamState.scheduledActivities, slamState.agentActivities);
    }

    public static SlamState set(SlamState slamState, Scheduler<Activity> scheduledActivities) {
        return new SlamState(slamState.activityFactory, slamState.now, slamState.agents, scheduledActivities, slamState.agentActivities);
    }

    public static SlamState set(SlamState slamState, Scheduler<Activity> scheduledActivities, SibillaMap<Integer, Activity.AgentStepActivity> agentActivities) {
        return new SlamState(slamState.activityFactory, slamState.now, slamState.agents, scheduledActivities, agentActivities);
    }


    public static SlamState updateAgentStepActivity(SlamState slamState, int agentIndex) {
        Optional<SlamAgent> oAgent = slamState.agents.get(agentIndex);
        if (oAgent.isEmpty()) return slamState;
        Optional<Activity.AgentStepActivity> optionalAgentStepActivity = slamState.agentActivities.get(agentIndex);
        if (optionalAgentStepActivity.isEmpty()) {
            return addAgentStateActivity(slamState, oAgent.get());
        } else {
            return updateAgentStepActivity(slamState, optionalAgentStepActivity.get(), oAgent.get());
        }
    }

    private static SlamState updateAgentStepActivity(SlamState slamState, Activity.AgentStepActivity agentStepActivity, SlamAgent slamAgent) {
        double time = slamAgent.timeOfNextStep();
        if (!Double.isFinite(time)) return slamState;
        Activity.AgentStepActivity activity = slamState.activityFactory.agentStepActivity(time, slamAgent.agentId());
        return SlamState.set(slamState, slamState.scheduledActivities.unscheduled(agentStepActivity.getScheduledTime(), agentStepActivity).schedule(activity, time), slamState.agentActivities.add(slamAgent.agentId(), activity));
    }

    private static SlamState addAgentStateActivity(SlamState slamState, SlamAgent slamAgent) {
        double time = slamAgent.timeOfNextStep();
        if (!Double.isFinite(time)) return slamState;
        Activity.AgentStepActivity activity = slamState.activityFactory.agentStepActivity(time, slamAgent.agentId());
        return SlamState.set(slamState, slamState.scheduledActivities.schedule(activity, time), slamState.agentActivities.add(slamAgent.agentId(), activity));
    }



    @Override
    public synchronized double getMinOf(ToDoubleFunction<AgentStore> expr) {
        return agents.streamOfValues().mapToDouble(a -> a.eval(expr)).min().orElse(Double.NaN);
    }

    public synchronized double getMinOf(SlamAgent agent, ToDoubleFunction<AgentStore> expr) {
        return agents.streamOfValues().filter(a -> !a.equals(agent)).mapToDouble(a -> a.eval(expr)).min().orElse(Double.NaN);
    }


    @Override
    public synchronized double getMinOf(ToDoubleFunction<AgentStore> expr, Predicate<SlamAgent> filter) {
        return agents.streamOfValues().filter(filter).mapToDouble(a -> a.eval(expr)).min().orElse(Double.NaN);
    }

    public synchronized double getMinOf(SlamAgent agent, ToDoubleFunction<AgentStore> expr, Predicate<SlamAgent> filter) {
        return agents.streamOfValues().filter(a -> !a.equals(agent)).filter(filter).mapToDouble(a -> a.eval(expr)).min().orElse(Double.NaN);
    }


    @Override
    public synchronized double getMaxOf(ToDoubleFunction<AgentStore> expr) {
        return agents.streamOfValues().mapToDouble(a -> a.eval(expr)).max().orElse(Double.NaN);
    }

    public synchronized double getMaxOf(SlamAgent agent, ToDoubleFunction<AgentStore> expr) {
        return agents.streamOfValues().filter(a -> !a.equals(agent)).mapToDouble(a -> a.eval(expr)).max().orElse(Double.NaN);
    }

    @Override
    public synchronized double getMaxOf(ToDoubleFunction<AgentStore> expr, Predicate<SlamAgent> filter) {
        return agents.streamOfValues().filter(filter).mapToDouble(a -> a.eval(expr)).max().orElse(Double.NaN);
    }

    public synchronized double getMaxOf(SlamAgent agent, ToDoubleFunction<AgentStore> expr, Predicate<SlamAgent> filter) {
        return agents.streamOfValues().filter(a -> !a.equals(agent)).filter(filter).mapToDouble(a -> a.eval(expr)).max().orElse(Double.NaN);
    }

    @Override
    public synchronized double getMeanOf(ToDoubleFunction<AgentStore> expr) {
        return agents.streamOfValues().mapToDouble(a -> a.eval(expr)).average().orElse(Double.NaN);
    }

    public synchronized double getMeanOf(SlamAgent agent, ToDoubleFunction<AgentStore> expr) {
        return agents.streamOfValues().filter(a -> !a.equals(agent)).mapToDouble(a -> a.eval(expr)).average().orElse(Double.NaN);
    }

    @Override
    public synchronized double getMeanOf(ToDoubleFunction<AgentStore> expr, Predicate<SlamAgent> filter) {
        return agents.streamOfValues().filter(filter).mapToDouble(a -> a.eval(expr)).average().orElse(Double.NaN);
    }

    public synchronized double getMeanOf(SlamAgent agent, ToDoubleFunction<AgentStore> expr, Predicate<SlamAgent> filter) {
        return agents.streamOfValues().filter(a -> !a.equals(agent)).filter(filter).mapToDouble(a -> a.eval(expr)).average().orElse(Double.NaN);
    }

    public synchronized double getSumOf(SlamAgent agent, ToDoubleFunction<AgentStore> expr, Predicate<SlamAgent> filter) {
        return agents.streamOfValues().filter(a -> !a.equals(agent)).filter(filter).mapToDouble(a -> a.eval(expr)).sum();
    }

    public synchronized double getSumOf(SlamAgent agent, ToDoubleFunction<AgentStore> expr) {
        return agents.streamOfValues().filter(a -> !a.equals(agent)).mapToDouble(a -> a.eval(expr)).sum();
    }

    public synchronized int count(SlamAgent agent, Predicate<SlamAgent> filter) {
        return (int) agents.streamOfValues().filter(a -> !a.equals(agent)).filter(filter).count();
    }


    @Override
    public synchronized boolean exists(Predicate<SlamAgent> p) {
        return agents.streamOfValues().anyMatch(p);
    }

    public synchronized boolean exists(SlamAgent agent, Predicate<SlamAgent> p) {
        return agents.streamOfValues().filter(a -> !a.equals(agent)).anyMatch(p);
    }

    @Override
    public synchronized boolean forAll(Predicate<SlamAgent> p) {
        return agents.streamOfValues().allMatch(p);
    }

    @Override
    public double getSumOf(ToDoubleFunction<AgentStore> expr, Predicate<SlamAgent> filter) {
        return agents.streamOfValues().filter(filter).mapToDouble(a -> a.eval(expr)).sum();
    }

    @Override
    public double getSumOf(ToDoubleFunction<AgentStore> expr) {
        return agents.streamOfValues().mapToDouble(a -> a.eval(expr)).sum();
    }

    @Override
    public int count(Predicate<SlamAgent> filter) {
        return (int) agents.streamOfValues().filter(filter).count();
    }

    public synchronized boolean forAll(SlamAgent agent, Predicate<SlamAgent> p) {
        return agents.streamOfValues().filter(a -> !a.equals(agent)).allMatch(p);
    }

    public Stream<SlamAgent> stream() {
        return agents.streamOfValues();
    }

    public SlamState progressTimeAt(RandomGenerator rg, double time) {
        return new SlamState(activityFactory, time, agents.apply(a -> a.progressTime(rg, time)).apply(a -> a.perceive(rg, getStateExpressionEvaluator(a))), scheduledActivities, agentActivities);
    }

    private StateExpressionEvaluator getStateExpressionEvaluator(SlamAgent agent) {
        return new StateExpressionEvaluator() {
            @Override
            public double getMinOf(ToDoubleFunction<AgentStore> expr) {
                return SlamState.this.getMinOf(agent, expr);
            }

            @Override
            public double getMinOf(ToDoubleFunction<AgentStore> expr, Predicate<SlamAgent> filter) {
                return SlamState.this.getMinOf(agent, expr, filter);
            }

            @Override
            public double getMaxOf(ToDoubleFunction<AgentStore> expr) {
                return SlamState.this.getMaxOf(agent, expr);
            }

            @Override
            public double getMaxOf(ToDoubleFunction<AgentStore> expr, Predicate<SlamAgent> filter) {
                return SlamState.this.getMaxOf(agent, expr, filter);
            }

            @Override
            public double getMeanOf(ToDoubleFunction<AgentStore> expr) {
                return SlamState.this.getMeanOf(agent, expr);
            }

            @Override
            public double getMeanOf(ToDoubleFunction<AgentStore> expr, Predicate<SlamAgent> filter) {
                return SlamState.this.getMeanOf(agent, expr, filter);
            }

            @Override
            public boolean exists(Predicate<SlamAgent> p) {
                return SlamState.this.exists(agent, p);
            }

            @Override
            public boolean forAll(Predicate<SlamAgent> p) {
                return SlamState.this.forAll(agent, p);
            }

            @Override
            public double getSumOf(ToDoubleFunction<AgentStore> expr, Predicate<SlamAgent> filter) {
                return SlamState.this.getSumOf(agent, expr, filter);
            }

            @Override
            public double getSumOf(ToDoubleFunction<AgentStore> expr) {
                return SlamState.this.getSumOf(agent, expr);
            }

            @Override
            public int count(Predicate<SlamAgent> filter) {
                return SlamState.this.count(agent, filter);
            }
        };
    }

    public SlamState deliverMessage(RandomGenerator rg, DeliveredMessage message) {
        Function<SlamAgent, Optional<Pair<List<OutgoingMessage>, SlamAgent>>> deliverFunction = a -> a.receive(rg, message);
        int receiverId = message.getReceiverId();
        Optional<Pair<List<OutgoingMessage>, SibillaMap<Integer, SlamAgent>>> result = agents.apply(receiverId, deliverFunction);
        if (result.isEmpty()) {
            return this;
        }
        Pair<List<OutgoingMessage>, SibillaMap<Integer, SlamAgent>> pair = result.get();
        SlamState state =  SlamState.updateAgentStepActivity(SlamState.set(this, pair.getValue()), receiverId);
        Optional<SlamAgent> receiver = pair.getValue().get(message.getReceiverId());
        return receiver.map(slamAgent -> state.send(rg, slamAgent, pair.getKey())).orElse(state);
    }

    private SlamState send(RandomGenerator rg, SlamAgent sender, List<OutgoingMessage> messages) {
        if (messages.isEmpty()) {
            return this;
        }
        Scheduler<Activity> queue = this.scheduledActivities;
        for (OutgoingMessage message: messages) {
            for(int i=0; i<agents.size(); i++) {
                DeliveredMessage dm = new DeliveredMessage(sender, message.getMessage(), i);
                double time = message.getDeliveryTime().applyAsDouble(rg, sender.getAgentMemory());
                queue = queue.schedule(activityFactory.messageDeliveryActivity(time, dm), time);
            }
        }
        return SlamState.set(this, queue);
    }


    public SlamState executeAgentStep(RandomGenerator rg, int agent) {
        Function<SlamAgent, Optional<Pair<List<OutgoingMessage>, SlamAgent>>> stepFunction = a -> a.execute(rg);
        Optional<Pair<List<OutgoingMessage>, SibillaMap<Integer, SlamAgent>>> result = agents.apply(agent, stepFunction);
        if (result.isEmpty()) {
            return this;
        }
        Pair<List<OutgoingMessage>, SibillaMap<Integer, SlamAgent>> pair = result.get();
        SlamState state =  SlamState.updateAgentStepActivity(SlamState.set(this, pair.getValue()), agent);
        Optional<SlamAgent> receiver = state.agents.get(agent);
        return receiver.map(slamAgent -> state.send(rg, slamAgent, pair.getKey())).orElse(state);

    }



    public boolean isTerminal() {
        return scheduledActivities.isEmpty();
    }


    public Optional<SlamState> next(RandomGenerator rg) {
        Optional<Pair<ScheduledElements<Activity>, Scheduler<Activity>>> scheduledElementsSibillaSchedulerInterafacePair = this.scheduledActivities.scheduleNext();
        return scheduledElementsSibillaSchedulerInterafacePair.map(p -> SlamState.set(this, p.getValue()).schedule(rg, p.getKey()));
    }

    private SlamState schedule(RandomGenerator rg, ScheduledElements<Activity> activities) {
        SlamState state = this.progressTimeAt(rg, activities.getTime());
        for (Activity activity: activities.getScheduledElements()) {
            state = activity.execute(rg, state);
        }
        return state;
    }

    public double now() {
        return now;
    }
}
